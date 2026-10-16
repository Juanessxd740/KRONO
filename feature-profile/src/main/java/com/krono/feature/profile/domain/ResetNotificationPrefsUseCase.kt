package com.krono.feature.profile.domain

import com.krono.core.common.KronoResult
import javax.inject.Inject

/**
 * Caso de uso desacoplado dentro de `:feature-profile` para restablecer las
 * preferencias de notificaciones y alarmas a sus valores predeterminados (F-15 CA-2).
 *
 * Restaura `deadlineReminderMinutes = 30`, avisos activos y sonido de concentración habilitado,
 * garantizando el cumplimiento estricto de CA-2.
 */
open class ResetNotificationPrefsUseCase @Inject constructor() {

    /**
     * Restablece las preferencias a los valores predeterminados del sistema.
     *
     * @return [KronoResult.Success] conteniendo una nueva instancia de [NotificationPrefs]
     * con sus valores base de fábrica.
     */
    open suspend operator fun invoke(): KronoResult<NotificationPrefs> {
        return KronoResult.Success(NotificationPrefs())
    }
}
