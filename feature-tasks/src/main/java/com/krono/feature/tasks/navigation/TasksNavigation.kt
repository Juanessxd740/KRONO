package com.krono.feature.tasks.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.krono.feature.tasks.ui.TasksRoute

/**
 * Ruta canónica de la pantalla de tareas en el grafo de navegación.
 */
const val TASKS_ROUTE = "tareas"

/**
 * Navega hacia la pantalla de tareas.
 */
fun NavController.navigateToTasks(navOptions: NavOptions? = null) {
    navigate(TASKS_ROUTE, navOptions)
}

/**
 * Registra el destino de la lista de tareas (F-03) en el grafo de navegación.
 *
 * @param onAddTask Callback invocado al pulsar el botón de crear nueva tarea (F-04).
 * @param onTaskClick Callback opcional al pulsar sobre una tarjeta de tarea.
 */
fun NavGraphBuilder.tasksScreen(
    onAddTask: () -> Unit,
    onTaskClick: ((String) -> Unit)? = null,
) {
    composable(route = TASKS_ROUTE) {
        TasksRoute(
            onAddTask = onAddTask,
            onTaskClick = onTaskClick,
        )
    }
}
