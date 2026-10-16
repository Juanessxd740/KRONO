package com.krono.feature.profile.domain

import com.krono.core.common.KronoResult
import javax.inject.Inject

/**
 * Caso de uso desacoplado dentro de `:feature-profile` para validar y persistir
 * las preferencias de notificaciones y alarmas (F-15 CA-2).
 *
 * Cumple con el aislamiento de arquitectura (D-8) y maneja errores retornando [KronoResult],
 * evitando que excepciones no controladas crucen hacia la capa de presentación.
 */
open class SaveNotificationPrefsUseCase @Inject constructor() {

    /**
     * Valida y guarda las preferencias de notificación provistas.
     *
     * @param prefs Objeto [NotificationPrefs] con las configuraciones a persistir.
     * @return [KronoResult.Success] si se guardaron correctamente, o [KronoResult.Error]
     * con [IllegalArgumentException] si la configuración no cumple con las restricciones mínimas.
     */
    open suspend operator fun invoke(prefs: NotificationPrefs): KronoResult<Unit> {
        if (prefs.deadlineReminderMinutes <= 0) {
            return KronoResult.Error(IllegalArgumentException("Los minutos de anticipación deben ser mayores a 0"))
        }
        return KronoResult.Success(Unit)
    }
}
