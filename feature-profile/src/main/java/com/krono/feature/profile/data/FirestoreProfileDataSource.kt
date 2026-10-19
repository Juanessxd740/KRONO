package com.krono.feature.profile.data

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.krono.core.common.KronoResult
import com.krono.feature.profile.domain.NotificationPrefs
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de [ProfileRemoteDataSource] respaldada por Cloud Firestore y Firebase Auth (D-1, D-3, F-21).
 * Opera bajo el documento raíz `users/{userId}` según la Sección 6 de `SPEC.md`.
 * Maneja de forma tolerante a fallos la ausencia de Firebase durante pruebas JVM locales.
 */
@Singleton
class FirestoreProfileDataSource(
    private val firestoreProvider: () -> FirebaseFirestore? = {
        try { FirebaseFirestore.getInstance() } catch (_: Throwable) { null }
    },
    private val authProvider: () -> FirebaseAuth? = {
        try { FirebaseAuth.getInstance() } catch (_: Throwable) { null }
    },
) : ProfileRemoteDataSource {

    @Inject
    constructor() : this(
        firestoreProvider = {
            try { FirebaseFirestore.getInstance() } catch (_: Throwable) { null }
        },
        authProvider = {
            try { FirebaseAuth.getInstance() } catch (_: Throwable) { null }
        },
    )

    override suspend fun saveUserProfile(
        userId: String,
        name: String,
        role: String,
        avatarUri: String?,
    ): KronoResult<Unit> = runCatchingSafe {
        val targetUid = userId.ifBlank { authProvider()?.currentUser?.uid.orEmpty() }
        if (targetUid.isNotBlank()) {
            val firestore = firestoreProvider()
            if (firestore != null) {
                val data = mutableMapOf<String, Any?>(
                    FIELD_NAME to name,
                    FIELD_ROLE to role,
                    FIELD_AVATAR_URI to avatarUri,
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp(),
                )
                firestore.collection(COLLECTION_USERS)
                    .document(targetUid)
                    .set(data, SetOptions.merge())
                    .await()
            }
        }

        // Actualizar displayName en Firebase Authentication
        val auth = authProvider()
        val user = auth?.currentUser
        if (user != null && name.isNotBlank()) {
            val updateRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(updateRequest).await()
        }
    }

    override suspend fun saveNotificationPrefs(
        userId: String,
        prefs: NotificationPrefs,
    ): KronoResult<Unit> = runCatchingSafe {
        val targetUid = userId.ifBlank { authProvider()?.currentUser?.uid.orEmpty() }
        if (targetUid.isNotBlank()) {
            val firestore = firestoreProvider()
            if (firestore != null) {
                val data = mapOf<String, Any?>(
                    FIELD_DEADLINE_MINUTES to prefs.deadlineReminderMinutes,
                    FIELD_FOCUS_SOUND to prefs.focusStartSound,
                    FIELD_SOUND_TONE to prefs.soundTone,
                    FIELD_DEADLINE_ENABLED to prefs.deadlineRemindersEnabled,
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp(),
                )
                firestore.collection(COLLECTION_USERS)
                    .document(targetUid)
                    .set(data, SetOptions.merge())
                    .await()
            }
        }
    }

    override suspend fun saveSettingsToggles(
        userId: String,
        dynamicBreaks: Boolean,
        silenceNotifications: Boolean,
        priorityAlerts: Boolean,
    ): KronoResult<Unit> = runCatchingSafe {
        val targetUid = userId.ifBlank { authProvider()?.currentUser?.uid.orEmpty() }
        if (targetUid.isNotBlank()) {
            val firestore = firestoreProvider()
            if (firestore != null) {
                val data = mapOf<String, Any?>(
                    FIELD_DYNAMIC_BREAKS to dynamicBreaks,
                    FIELD_SILENCE_NOTIFS to silenceNotifications,
                    FIELD_PRIORITY_ALERTS to priorityAlerts,
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp(),
                )
                firestore.collection(COLLECTION_USERS)
                    .document(targetUid)
                    .set(data, SetOptions.merge())
                    .await()
            }
        }
    }

    override suspend fun resetUserData(userId: String): KronoResult<Unit> = runCatchingSafe {
        val targetUid = userId.ifBlank { authProvider()?.currentUser?.uid.orEmpty() }
        if (targetUid.isNotBlank()) {
            val firestore = firestoreProvider()
            if (firestore != null) {
                val defaults = mapOf<String, Any?>(
                    FIELD_NAME to DEFAULT_NAME,
                    FIELD_ROLE to DEFAULT_ROLE,
                    FIELD_AVATAR_URI to null,
                    FIELD_DEADLINE_MINUTES to NotificationPrefs.DEFAULT_REMINDER_MINUTES,
                    FIELD_FOCUS_SOUND to true,
                    FIELD_SOUND_TONE to NotificationPrefs.DEFAULT_SOUND_TONE,
                    FIELD_DEADLINE_ENABLED to true,
                    FIELD_DYNAMIC_BREAKS to true,
                    FIELD_SILENCE_NOTIFS to true,
                    FIELD_PRIORITY_ALERTS to true,
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp(),
                )
                firestore.collection(COLLECTION_USERS)
                    .document(targetUid)
                    .set(defaults, SetOptions.merge())
                    .await()
            }
        }
    }

    override suspend fun deleteAccount(password: String): KronoResult<Unit> = runCatchingSafe {
        val auth = authProvider() ?: return@runCatchingSafe
        val user = auth.currentUser ?: throw IllegalStateException("No hay sesión activa para eliminar")
        val email = user.email ?: throw IllegalStateException("El usuario no tiene correo asociado")

        // 1. Reautenticación sensible exigida por Firebase
        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential).await()

        // 2. Borrar documento del perfil en Firestore
        val firestore = firestoreProvider()
        firestore?.collection(COLLECTION_USERS)?.document(user.uid)?.delete()?.await()

        // 3. Eliminar cuenta de Firebase Authentication
        user.delete().await()
    }

    private suspend fun runCatchingSafe(block: suspend () -> Unit): KronoResult<Unit> {
        return try {
            block()
            KronoResult.Success(Unit)
        } catch (t: Throwable) {
            KronoResult.Error(t)
        }
    }

    companion object {
        private const val COLLECTION_USERS = "users"

        private const val FIELD_NAME = "name"
        private const val FIELD_ROLE = "role"
        private const val FIELD_AVATAR_URI = "avatarUri"
        private const val FIELD_DEADLINE_MINUTES = "deadlineReminderMinutes"
        private const val FIELD_FOCUS_SOUND = "focusStartSound"
        private const val FIELD_SOUND_TONE = "soundTone"
        private const val FIELD_DEADLINE_ENABLED = "deadlineRemindersEnabled"
        private const val FIELD_DYNAMIC_BREAKS = "dynamicBreaksEnabled"
        private const val FIELD_SILENCE_NOTIFS = "silenceInternalNotificationsEnabled"
        private const val FIELD_PRIORITY_ALERTS = "priorityAlertsEnabled"
        private const val FIELD_UPDATED_AT = "updatedAt"

        private const val DEFAULT_NAME = "Usuario"
        private const val DEFAULT_ROLE = "Estudiante y trabajador"
    }
}
