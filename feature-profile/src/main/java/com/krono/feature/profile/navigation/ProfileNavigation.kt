package com.krono.feature.profile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.krono.feature.profile.edit.EditProfileRoute
import com.krono.feature.profile.settings.SettingsEvent
import com.krono.feature.profile.settings.SettingsScreen
import com.krono.feature.profile.settings.SettingsViewModel

/**
 * Ruta canónica de la pantalla de Ajustes y Perfil (F-19, F-20, F-25 CA-4).
 */
const val SETTINGS_ROUTE = "profile/settings"

/**
 * Ruta canónica de la pantalla de Edición de Perfil (F-19 CA-2).
 */
const val EDIT_PROFILE_ROUTE = "profile/edit"

/**
 * Contrato de navegación de `feature-profile`.
 *
 * Agrega el destino de Ajustes al grafo de navegación de la aplicación sin acoplar
 * el módulo con destinos de otros features ni con el módulo `:app`.
 *
 * @param onBackClick Navegación hacia atrás (ej. retorno a Dashboard).
 * @param onEditProfileClick Navegación para editar el perfil del usuario.
 * @param onFocusTemplatesClick Navegación al configurador de plantillas de concentración.
 * @param onSecurityZoneClick Navegación a la zona de seguridad y purga de datos.
 * @param onLoggedOut Notificación de cierre de sesión exitoso (para redirigir a Auth).
 * @param onThemeSelectorClick Acción para abrir el selector de tema visual.
 */
fun NavGraphBuilder.settingsScreen(
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit = {},
    onFocusTemplatesClick: () -> Unit = {},
    onSecurityZoneClick: () -> Unit = {},
    onLoggedOut: () -> Unit,
    onThemeSelectorClick: () -> Unit = {},
) {
    composable(SETTINGS_ROUTE) {
        SettingsRoute(
            onBackClick = onBackClick,
            onEditProfileClick = onEditProfileClick,
            onFocusTemplatesClick = onFocusTemplatesClick,
            onSecurityZoneClick = onSecurityZoneClick,
            onLoggedOut = onLoggedOut,
            onThemeSelectorClick = onThemeSelectorClick,
        )
    }
}

/**
 * Punto de entrada con estado del destino de Ajustes.
 *
 * Conecta [SettingsViewModel] con la pantalla stateless [SettingsScreen] y
 * escucha los eventos de un solo uso (como [SettingsEvent.LoggedOut]).
 */
@Composable
internal fun SettingsRoute(
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onFocusTemplatesClick: () -> Unit,
    onSecurityZoneClick: () -> Unit,
    onLoggedOut: () -> Unit,
    onThemeSelectorClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsEvent.LoggedOut -> onLoggedOut()
            }
        }
    }

    SettingsScreen(
        state = state,
        onBackClick = onBackClick,
        onEditProfileClick = onEditProfileClick,
        onDynamicBreaksToggle = viewModel::onDynamicBreaksToggle,
        onSilenceNotificationsToggle = viewModel::onSilenceNotificationsToggle,
        onFocusTemplatesClick = onFocusTemplatesClick,
        onPriorityAlertsToggle = viewModel::onPriorityAlertsToggle,
        onThemeSelectorClick = onThemeSelectorClick,
        onSecurityZoneClick = onSecurityZoneClick,
        onRequestLogout = viewModel::onRequestLogout,
        onDismissLogoutDialog = viewModel::onDismissLogoutDialog,
        onConfirmLogout = viewModel::onConfirmLogout,
    )
}

/**
 * Agrega el destino de Edición de Perfil (F-19 CA-2) al grafo de navegación.
 *
 * @param onBackClick Acción al presionar atrás o cancelar la edición.
 * @param onProfileSaved Acción al completar y guardar los cambios exitosamente (por defecto regresa atrás).
 */
fun NavGraphBuilder.editProfileScreen(
    onBackClick: () -> Unit,
    onProfileSaved: () -> Unit = onBackClick,
) {
    composable(EDIT_PROFILE_ROUTE) {
        EditProfileRoute(
            onBackClick = onBackClick,
            onProfileSaved = onProfileSaved,
        )
    }
}

