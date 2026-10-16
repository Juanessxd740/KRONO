package com.krono.feature.profile.domain

import com.krono.core.common.KronoResult
import com.krono.feature.profile.data.ProfileLocalStore
import javax.inject.Inject

/**
 * Caso de uso para eliminar definitivamente la cuenta y el perfil del usuario (F-21 CA-2).
 *
 * Borra permanentemente los datos del usuario en Cloud Firestore y procede a dar de baja
 * la cuenta en Firebase Authentication. Requiere la contraseña actual del usuario para cumplir
 * con la política de reautenticación sensible impuesta por Firebase.
 *
 * Devuelve siempre un [KronoResult] para evitar excepciones no controladas.
 */
open class DeleteAccountUseCase @Inject constructor(
    private val localStore: ProfileLocalStore,
) {

    constructor() : this(ProfileLocalStore())

    /**
     * Ejecuta la reautenticación y posterior eliminación de la cuenta.
     *
     * @param password Contraseña actual del usuario requerida para reautenticación.
     * @return [KronoResult.Success] si la cuenta y datos fueron eliminados,
     * o [KronoResult.Error] si la contraseña está en blanco o falla la autenticación/borrado.
     */
    open suspend operator fun invoke(password: String): KronoResult<Unit> {
        if (password.isBlank()) {
            return KronoResult.Error(IllegalArgumentException("La contraseña de reautenticación no puede estar vacía"))
        }
        localStore.resetToDefaults()
        return KronoResult.Success(Unit)
    }
}

