package com.krono.feature.profile.data

import com.krono.core.common.KronoResult
import com.krono.feature.profile.domain.NotificationPrefs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para [FirestoreProfileDataSource].
 * Valida tolerancia a fallos y ejecución segura en entornos locales sin Firebase inicializado.
 */
class FirestoreProfileDataSourceTest {

    @Test
    fun saveUserProfile_conFirebaseNulo_noLanzaExcepcionYRetornaExito() = runTest {
        val dataSource = FirestoreProfileDataSource(
            firestoreProvider = { null },
            authProvider = { null },
        )

        val result = dataSource.saveUserProfile(
            userId = "user-123",
            name = "Carlos",
            role = "Estudiante",
            avatarUri = null,
        )

        assertTrue(result is KronoResult.Success)
    }

    @Test
    fun saveNotificationPrefs_conFirebaseNulo_retornaExito() = runTest {
        val dataSource = FirestoreProfileDataSource(
            firestoreProvider = { null },
            authProvider = { null },
        )

        val result = dataSource.saveNotificationPrefs(
            userId = "user-123",
            prefs = NotificationPrefs(),
        )

        assertTrue(result is KronoResult.Success)
    }

    @Test
    fun saveSettingsToggles_conFirebaseNulo_retornaExito() = runTest {
        val dataSource = FirestoreProfileDataSource(
            firestoreProvider = { null },
            authProvider = { null },
        )

        val result = dataSource.saveSettingsToggles(
            userId = "user-123",
            dynamicBreaks = true,
            silenceNotifications = true,
            priorityAlerts = false,
        )

        assertTrue(result is KronoResult.Success)
    }

    @Test
    fun resetUserData_conFirebaseNulo_retornaExito() = runTest {
        val dataSource = FirestoreProfileDataSource(
            firestoreProvider = { null },
            authProvider = { null },
        )

        val result = dataSource.resetUserData("user-123")
        assertTrue(result is KronoResult.Success)
    }

    @Test
    fun deleteAccount_sinSesionActiva_retornaExitoOErrorControladoSinCrash() = runTest {
        val dataSource = FirestoreProfileDataSource(
            firestoreProvider = { null },
            authProvider = { null },
        )

        val result = dataSource.deleteAccount("password123")
        assertTrue(result is KronoResult.Success)
    }
}
