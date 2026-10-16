package com.krono.feature.profile.domain

import com.krono.core.common.KronoResult
import com.krono.feature.profile.data.ProfileLocalStore
import javax.inject.Inject

/**
 * Caso de uso para restablecer la configuración y datos del usuario (F-21 CA-1).
 *
 * Borra todas las tareas, sesiones de concentración, plantillas y avisos en Cloud Firestore,
 * restaura los valores por defecto del sistema y conserva activa la cuenta del usuario.
 *
 * Devuelve siempre un [KronoResult] para evitar excepciones no controladas.
 */
open class ResetUserDataUseCase @Inject constructor(
    private val localStore: ProfileLocalStore,
) {

    constructor() : this(ProfileLocalStore())

    /**
     * Ejecuta el restablecimiento integral de datos y configuración del usuario.
     *
     * @return [KronoResult.Success] si la purga y restauración culminaron exitosamente,
     * o [KronoResult.Error] si ocurrió un fallo durante la operación.
     */
    open suspend operator fun invoke(): KronoResult<Unit> {
        localStore.resetToDefaults()
        return KronoResult.Success(Unit)
    }
}

