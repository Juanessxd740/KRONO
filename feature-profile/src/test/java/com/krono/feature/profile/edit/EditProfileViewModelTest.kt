package com.krono.feature.profile.edit

import app.cash.turbine.test
import com.krono.core.common.KronoResult
import com.krono.feature.profile.MainDispatcherRule
import com.krono.feature.profile.domain.UpdateProfileUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Doble de prueba en memoria para [UpdateProfileUseCase] que permite simular latencia
 * suspendida con compuertas ([gate]), registrar llamadas y simular fallos de persistencia.
 */
private class TestUpdateProfileUseCase : UpdateProfileUseCase() {
    var failure: Throwable? = null
    var gate: CompletableDeferred<Unit>? = null
    var callCount: Int = 0
        private set
    var lastSavedName: String? = null
        private set
    var lastSavedRole: String? = null
        private set

    override suspend operator fun invoke(name: String, role: String): KronoResult<Unit> {
        callCount++
        lastSavedName = name
        lastSavedRole = role
        gate?.await()
        failure?.let { return KronoResult.Error(it) }
        return super.invoke(name, role)
    }
}

/**
 * Pruebas unitarias para [EditProfileViewModel] (F-19 CA-2: Edición de Perfil).
 *
 * Valida de forma exhaustiva:
 * - El estado inicial por defecto del formulario.
 * - La actualización de nombre y rol junto con la limpieza de errores previos.
 * - La validación obligatoria ante nombres en blanco sin invocar el caso de uso.
 * - La protección estricta contra doble envío concurrente mientras [EditProfileUiState.isSaving] es true.
 * - El flujo de guardado exitoso con emisión de [EditProfileEvent.ProfileSaved].
 * - El manejo y exposición de errores si la persistencia falla.
 * - La cancelación del formulario con emisión de [EditProfileEvent.NavigateBack].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val updateProfileUseCase = TestUpdateProfileUseCase()

    private fun createViewModel(): EditProfileViewModel =
        EditProfileViewModel(updateProfile = updateProfileUseCase)

    @Test
    fun elEstadoInicialReflejaLosValoresPorDefecto() {
        val vm = createViewModel()
        val state = vm.uiState.value

        assertEquals("Alexa Gómez", state.name)
        assertEquals("alexa.gomez@correo.com", state.email)
        assertEquals("Estudiante y Trabajadora", state.role)
        assertEquals(4, state.availableRoles.size)
        assertEquals("AG", state.userInitials)
        assertFalse(state.isSaving)
        assertFalse(state.isSuccess)
        assertNull(state.nameError)
        assertNull(state.errorMessage)
        assertTrue(state.canSave)
    }

    @Test
    fun actualizarNombreYRolActualizaElEstadoYCalculaNuevasIniciales() {
        val vm = createViewModel()

        vm.onNameChange("Carlos Sánchez")
        vm.onRoleChange("Profesional Técnico")

        val state = vm.uiState.value
        assertEquals("Carlos Sánchez", state.name)
        assertEquals("CS", state.userInitials)
        assertEquals("Profesional Técnico", state.role)
        assertNull(state.nameError)
        assertNull(state.errorMessage)
    }

    @Test
    fun guardarConNombreVacioGeneraErrorDeValidacionYNoInvocaPersistencia() {
        val vm = createViewModel()

        vm.onNameChange("   ")
        vm.onSave()

        val state = vm.uiState.value
        assertNotNull(state.nameError)
        assertEquals("El nombre completo es obligatorio", state.nameError)
        assertFalse(state.isSaving)
        assertEquals(0, updateProfileUseCase.callCount)
    }

    @Test
    fun escribirNombreValidoLimpiaElErrorDeNombrePrevio() {
        val vm = createViewModel()

        vm.onNameChange("")
        vm.onSave()
        assertNotNull(vm.uiState.value.nameError)

        vm.onNameChange("Juan Sora")
        assertNull(vm.uiState.value.nameError)
    }

    @Test
    fun guardarBloqueaDobleEnvioMientrasSeEncuentraGuardando() = runTest {
        val gate = CompletableDeferred<Unit>()
        updateProfileUseCase.gate = gate
        val vm = createViewModel()

        vm.events.test {
            vm.onNameChange("Esteban Sora")
            vm.onSave()

            // Estado en suspensión de guardado
            assertTrue(vm.uiState.value.isSaving)

            // Segundo intento inmediato (simulando doble clic)
            vm.onSave()

            // Liberamos la operación
            gate.complete(Unit)
            advanceUntilIdle()

            assertEquals(EditProfileEvent.ProfileSaved, awaitItem())
            expectNoEvents()
        }

        assertEquals("No debe invocar el caso de uso nuevamente mientras isSaving está activo", 1, updateProfileUseCase.callCount)
        assertFalse(vm.uiState.value.isSaving)
        assertTrue(vm.uiState.value.isSuccess)
    }

    @Test
    fun guardadoExitosoEmiteEventoProfileSaved() = runTest {
        val vm = createViewModel()

        vm.events.test {
            vm.onNameChange("Laura Rivas")
            vm.onRoleChange("Investigadora & Operadora")
            vm.onSave()
            advanceUntilIdle()

            val state = vm.uiState.value
            assertFalse(state.isSaving)
            assertTrue(state.isSuccess)
            assertEquals("Laura Rivas", updateProfileUseCase.lastSavedName)
            assertEquals("Investigadora & Operadora", updateProfileUseCase.lastSavedRole)

            assertEquals(EditProfileEvent.ProfileSaved, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun fallaEnPersistenciaDesactivaIsSavingYExponeMensajeDeError() = runTest {
        updateProfileUseCase.failure = RuntimeException("Error en Firestore de prueba")
        val vm = createViewModel()

        vm.events.test {
            vm.onSave()
            advanceUntilIdle()

            val state = vm.uiState.value
            assertFalse(state.isSaving)
            assertFalse(state.isSuccess)
            assertEquals("Error en Firestore de prueba", state.errorMessage)

            expectNoEvents()
        }
    }

    @Test
    fun cancelarEmiteEventoNavigateBackSinModificarDatos() = runTest {
        val vm = createViewModel()

        vm.events.test {
            vm.onCancel()
            advanceUntilIdle()

            assertEquals(EditProfileEvent.NavigateBack, awaitItem())
            assertEquals(0, updateProfileUseCase.callCount)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
