package com.krono.feature.profile.data

import android.content.Context
import android.content.SharedPreferences
import com.krono.feature.profile.domain.NotificationPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Almacén local persistente para las preferencias de perfil, ajustes del sistema
 * y notificaciones (F-19 CA-2, F-20 CA-2, F-15 CA-2, F-21 CA-1).
 *
 * Persiste los datos en [SharedPreferences] cuando [context] está disponible (en ejecución
 * real de Android) y mantiene una copia reactiva observable en memoria con [StateFlow],
 * soportando ejecución en pruebas unitarias puras JVM sin necesidad de Context ni mocks.
 */
@Singleton
class ProfileLocalStore internal constructor(
    private val prefs: SharedPreferences?,
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        } catch (_: Exception) {
            null
        },
    )

    constructor() : this(null as SharedPreferences?)


    // Datos de perfil (F-19)
    private val _userName = MutableStateFlow(loadString(KEY_USER_NAME, DEFAULT_NAME))
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userRole = MutableStateFlow(loadString(KEY_USER_ROLE, DEFAULT_ROLE))
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _userEmail = MutableStateFlow(loadString(KEY_USER_EMAIL, DEFAULT_EMAIL))
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    // Interruptores de ajustes (F-20 CA-2)
    private val _dynamicBreaksEnabled = MutableStateFlow(loadBoolean(KEY_DYNAMIC_BREAKS, true))
    val dynamicBreaksEnabled: StateFlow<Boolean> = _dynamicBreaksEnabled.asStateFlow()

    private val _silenceInternalNotificationsEnabled = MutableStateFlow(loadBoolean(KEY_SILENCE_NOTIFS, true))
    val silenceInternalNotificationsEnabled: StateFlow<Boolean> = _silenceInternalNotificationsEnabled.asStateFlow()

    private val _priorityAlertsEnabled = MutableStateFlow(loadBoolean(KEY_PRIORITY_ALERTS, true))
    val priorityAlertsEnabled: StateFlow<Boolean> = _priorityAlertsEnabled.asStateFlow()

    // Preferencias de notificación (F-15 CA-2)
    private val _deadlineReminderMinutes = MutableStateFlow(loadInt(KEY_DEADLINE_MINUTES, NotificationPrefs.DEFAULT_REMINDER_MINUTES))
    val deadlineReminderMinutes: StateFlow<Int> = _deadlineReminderMinutes.asStateFlow()

    private val _deadlineRemindersEnabled = MutableStateFlow(loadBoolean(KEY_DEADLINE_ENABLED, true))
    val deadlineRemindersEnabled: StateFlow<Boolean> = _deadlineRemindersEnabled.asStateFlow()

    private val _focusStartSound = MutableStateFlow(loadBoolean(KEY_FOCUS_SOUND, true))
    val focusStartSound: StateFlow<Boolean> = _focusStartSound.asStateFlow()

    private val _soundTone = MutableStateFlow(loadString(KEY_SOUND_TONE, NotificationPrefs.DEFAULT_SOUND_TONE))
    val soundTone: StateFlow<String> = _soundTone.asStateFlow()

    /**
     * Guarda el nombre y rol del perfil del usuario (F-19 CA-2).
     */
    fun saveProfile(name: String, role: String) {
        _userName.value = name
        _userRole.value = role
        saveString(KEY_USER_NAME, name)
        saveString(KEY_USER_ROLE, role)
    }

    /**
     * Persiste el interruptor de sugerencia dinámica de descansos (F-20 CA-2).
     */
    fun setDynamicBreaksEnabled(enabled: Boolean) {
        _dynamicBreaksEnabled.value = enabled
        saveBoolean(KEY_DYNAMIC_BREAKS, enabled)
    }

    /**
     * Persiste el interruptor de silenciar notificaciones internas (F-20 CA-2).
     */
    fun setSilenceInternalNotificationsEnabled(enabled: Boolean) {
        _silenceInternalNotificationsEnabled.value = enabled
        saveBoolean(KEY_SILENCE_NOTIFS, enabled)
    }

    /**
     * Persiste el interruptor de alertas prioritarias P1 (F-20 CA-2).
     */
    fun setPriorityAlertsEnabled(enabled: Boolean) {
        _priorityAlertsEnabled.value = enabled
        saveBoolean(KEY_PRIORITY_ALERTS, enabled)
    }

    /**
     * Guarda y persiste las preferencias de notificación y alarmas (F-15 CA-2).
     */
    fun saveNotificationPrefs(prefs: NotificationPrefs) {
        _deadlineReminderMinutes.value = prefs.deadlineReminderMinutes
        _deadlineRemindersEnabled.value = prefs.deadlineRemindersEnabled
        _focusStartSound.value = prefs.focusStartSound
        _soundTone.value = prefs.soundTone

        saveInt(KEY_DEADLINE_MINUTES, prefs.deadlineReminderMinutes)
        saveBoolean(KEY_DEADLINE_ENABLED, prefs.deadlineRemindersEnabled)
        saveBoolean(KEY_FOCUS_SOUND, prefs.focusStartSound)
        saveString(KEY_SOUND_TONE, prefs.soundTone)
    }

    /**
     * Obtiene el modelo actual de preferencias de notificación.
     */
    fun getNotificationPrefs(): NotificationPrefs {
        return NotificationPrefs(
            deadlineReminderMinutes = _deadlineReminderMinutes.value,
            deadlineRemindersEnabled = _deadlineRemindersEnabled.value,
            focusStartSound = _focusStartSound.value,
            soundTone = _soundTone.value,
        )
    }

    /**
     * Restablece todos los datos de perfil, ajustes y notificaciones a valores de fábrica (F-21 CA-1).
     */
    fun resetToDefaults() {
        _userName.value = DEFAULT_NAME
        _userRole.value = DEFAULT_ROLE
        _userEmail.value = DEFAULT_EMAIL
        _dynamicBreaksEnabled.value = true
        _silenceInternalNotificationsEnabled.value = true
        _priorityAlertsEnabled.value = true
        _deadlineReminderMinutes.value = NotificationPrefs.DEFAULT_REMINDER_MINUTES
        _deadlineRemindersEnabled.value = true
        _focusStartSound.value = true
        _soundTone.value = NotificationPrefs.DEFAULT_SOUND_TONE

        prefs?.edit()?.clear()?.apply()
    }

    private fun loadString(key: String, default: String): String =
        prefs?.getString(key, default) ?: default

    private fun loadBoolean(key: String, default: Boolean): Boolean =
        prefs?.getBoolean(key, default) ?: default

    private fun loadInt(key: String, default: Int): Int =
        prefs?.getInt(key, default) ?: default

    private fun saveString(key: String, value: String) {
        prefs?.edit()?.putString(key, value)?.apply()
    }

    private fun saveBoolean(key: String, value: Boolean) {
        prefs?.edit()?.putBoolean(key, value)?.apply()
    }

    private fun saveInt(key: String, value: Int) {
        prefs?.edit()?.putInt(key, value)?.apply()
    }

    companion object {
        private const val PREFS_NAME = "krono_profile_local_store"
        const val DEFAULT_NAME = "Alexa Gómez"
        const val DEFAULT_ROLE = "Estudiante y Trabajadora"
        const val DEFAULT_EMAIL = "alexa.gomez@correo.com"

        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_DYNAMIC_BREAKS = "key_dynamic_breaks"
        private const val KEY_SILENCE_NOTIFS = "key_silence_notifs"
        private const val KEY_PRIORITY_ALERTS = "key_priority_alerts"
        private const val KEY_DEADLINE_MINUTES = "key_deadline_minutes"
        private const val KEY_DEADLINE_ENABLED = "key_deadline_enabled"
        private const val KEY_FOCUS_SOUND = "key_focus_sound"
        private const val KEY_SOUND_TONE = "key_sound_tone"
    }
}
