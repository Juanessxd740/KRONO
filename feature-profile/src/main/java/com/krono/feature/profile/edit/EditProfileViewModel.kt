package com.krono.feature.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krono.core.common.KronoResult
import com.krono.feature.profile.domain.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que gobierna la pantalla de Edición de Perfil (F-19 CA-2).
 *
 * Administra el estado inmutable [EditProfileUiState], valida los campos requeridos
 * antes de invocar la persistencia, previene el doble envío mientras la operación
 * está activa ([EditProfileUiState.isSaving]) y emite eventos de un solo uso [EditProfileEvent].
 *
 * Cumple estrictamente la regla de arquitectura de KRONO: no importa `androidx.compose.*`.
 *
 * @property updateProfile Caso de uso para validar y persistir los cambios de perfil.
 */
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val updateProfile: UpdateProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())

    /** Flujo de estado visual inmutable observable por la UI. */
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<EditProfileEvent>(Channel.BUFFERED)

    /** Canal de eventos de un solo uso para navegación desacoplada. */
    val events: Flow<EditProfileEvent> = _events.receiveAsFlow()

    /**
     * Actualiza el valor del nombre y limpia los errores activos si el nuevo valor no está en blanco.
     *
     * @param newName Nuevo texto del nombre completo ingresado.
     */
    fun onNameChange(newName: String) {
        _uiState.update { current ->
            current.copy(
                name = newName,
                nameError = if (newName.isNotBlank()) null else current.nameError,
                errorMessage = null,
            )
        }
    }

    /**
     * Actualiza el rol académico y profesional seleccionado.
     *
     * @param newRole Nuevo rol seleccionado de la lista disponible.
     */
    fun onRoleChange(newRole: String) {
        _uiState.update { current ->
            current.copy(
                role = newRole,
                errorMessage = null,
            )
        }
    }

    /**
     * Cancela la edición y emite el evento para regresar a la pantalla anterior sin guardar.
     */
    fun onCancel() {
        viewModelScope.launch {
            _events.send(EditProfileEvent.NavigateBack)
        }
    }

    /**
     * Valida los campos obligatorios y persiste los cambios del perfil del usuario (F-19 CA-2).
     *
     * Reglas:
     * 1. Si ya se está guardando ([EditProfileUiState.isSaving]), descarta la llamada para evitar doble envío.
     * 2. Si el nombre se encuentra en blanco, actualiza [EditProfileUiState.nameError] y detiene el flujo.
     * 3. Durante la ejecución activa `isSaving = true`.
     * 4. Al finalizar exitosamente, emite [EditProfileEvent.ProfileSaved].
     * 5. En caso de error, desactiva `isSaving` y expone el mensaje de fallo.
     */
    fun onSave() {
        val currentState = _uiState.value

        // Protección contra pulsaciones repetidas o concurrentes
        if (currentState.isSaving) return

        // Validación de campo obligatorio
        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "El nombre completo es obligatorio") }
            return
        }

        _uiState.update {
            it.copy(
                isSaving = true,
                nameError = null,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            when (val result = updateProfile(currentState.name, currentState.role)) {
                is KronoResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSuccess = true,
                        )
                    }
                    _events.send(EditProfileEvent.ProfileSaved)
                }
                is KronoResult.Error -> {
                    val message = result.throwable.message ?: "Ocurrió un error al guardar los cambios"
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = message,
                        )
                    }
                }
                is KronoResult.Loading -> {
                    _uiState.update { it.copy(isSaving = true) }
                }
            }
        }
    }
}
