package com.krono.feature.profile.domain

import com.krono.core.common.KronoResult
import com.krono.feature.profile.data.ProfileLocalStore
import javax.inject.Inject

/**
 * Caso de uso desacoplado dentro de `:feature-profile` para actualizar y validar
 * la información del perfil del usuario (F-19 CA-2).
 *
 * Cumple con el criterio de aceptación CA-2: campos obligatorios vacíos devuelven
 * error y cambios válidos persisten en el estado local.
 */
open class UpdateProfileUseCase @Inject constructor(
    private val localStore: ProfileLocalStore,
) {

    constructor() : this(ProfileLocalStore())

    /**
     * Valida y actualiza los datos del perfil de usuario.
     *
     * @param name Nombre completo del usuario (obligatorio).
     * @param role Rol académico / profesional (obligatorio).
     * @return [KronoResult.Success] si los cambios son válidos, o [KronoResult.Error]
     * con [IllegalArgumentException] si algún campo obligatorio se encuentra vacío.
     */
    open suspend operator fun invoke(name: String, role: String): KronoResult<Unit> {
        if (name.isBlank()) {
            return KronoResult.Error(IllegalArgumentException("El nombre no puede estar vacío"))
        }
        if (role.isBlank()) {
            return KronoResult.Error(IllegalArgumentException("El rol no puede estar vacío"))
        }
        localStore.saveProfile(name, role)
        return KronoResult.Success(Unit)
    }
}

