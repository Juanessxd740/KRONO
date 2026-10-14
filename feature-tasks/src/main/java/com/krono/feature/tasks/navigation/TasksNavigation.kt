package com.krono.feature.tasks.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.krono.feature.tasks.create.ui.CreateTaskRoute
import com.krono.feature.tasks.edit.EditTaskViewModel.Companion.TASK_ID_NAV_ARG
import com.krono.feature.tasks.edit.ui.EditTaskRoute
import com.krono.feature.tasks.ui.TasksRoute

/**
 * Ruta canónica de la pantalla de tareas en el grafo de navegación.
 */
const val TASKS_ROUTE = "tareas"

/**
 * Ruta canónica de la pantalla de creación de tarea (F-04).
 */
const val CREATE_TASK_ROUTE = "tareas/nueva"

/**
 * Ruta base para la pantalla de edición de tarea (F-05).
 */
const val EDIT_TASK_ROUTE_BASE = "tareas/editar"

/**
 * Ruta canónica con argumento de la pantalla de edición de tarea (F-05).
 */
const val EDIT_TASK_ROUTE = "$EDIT_TASK_ROUTE_BASE/{$TASK_ID_NAV_ARG}"

/**
 * Navega hacia la pantalla de tareas.
 */
fun NavController.navigateToTasks(navOptions: NavOptions? = null) {
    navigate(TASKS_ROUTE, navOptions)
}

/**
 * Navega hacia la pantalla de creación de tarea (F-04).
 */
fun NavController.navigateToCreateTask(navOptions: NavOptions? = null) {
    navigate(CREATE_TASK_ROUTE, navOptions)
}

/**
 * Navega hacia la pantalla de edición de tarea recibiendo su [taskId] (F-05 CA-1).
 */
fun NavController.navigateToEditTask(taskId: String, navOptions: NavOptions? = null) {
    navigate("$EDIT_TASK_ROUTE_BASE/$taskId", navOptions)
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

/**
 * Registra el destino del formulario de creación de tarea (F-04) en el grafo de navegación.
 *
 * @param onBack Callback invocado para regresar tras guardar o cancelar.
 */
fun NavGraphBuilder.createTaskScreen(
    onBack: () -> Unit,
) {
    composable(route = CREATE_TASK_ROUTE) {
        CreateTaskRoute(
            onBack = onBack,
        )
    }
}

/**
 * Registra el destino del formulario de edición de tarea (F-05) en el grafo de navegación.
 *
 * @param onBack Callback invocado para regresar tras guardar cambios o cancelar.
 */
fun NavGraphBuilder.editTaskScreen(
    onBack: () -> Unit,
) {
    composable(
        route = EDIT_TASK_ROUTE,
        arguments = listOf(
            navArgument(TASK_ID_NAV_ARG) { type = NavType.StringType },
        ),
    ) {
        EditTaskRoute(
            onBack = onBack,
        )
    }
}

