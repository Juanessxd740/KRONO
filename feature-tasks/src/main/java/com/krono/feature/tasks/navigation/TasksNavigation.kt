package com.krono.feature.tasks.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.krono.feature.tasks.create.ui.CreateTaskRoute
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
