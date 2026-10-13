package com.krono.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krono.core.common.KronoResult
import com.krono.core.domain.task.DeleteTaskUseCase
import com.krono.core.domain.task.ObserveTasksUseCase
import com.krono.core.domain.task.Task
import com.krono.core.domain.task.UpsertTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * ViewModel que gestiona la lógica de presentación y el estado reactivo de la lista de tareas (F-03).
 *
 * Regla dura de arquitectura: este ViewModel nunca importa `androidx.compose.*` y expone
 * su estado exclusivamente mediante [StateFlow].
 */
@HiltViewModel
class TasksViewModel @Inject constructor(
    private val observeTasksUseCase: ObserveTasksUseCase,
    private val upsertTaskUseCase: UpsertTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
) : ViewModel() {

    /** Proveedor de la fecha actual; modificable en pruebas unitarias para aislar el tiempo. */
    internal var currentDateProvider: () -> LocalDate = {
        Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    private val selectedCategory = MutableStateFlow(TaskCategoryFilter.TODAS)
    private val errorMessage = MutableStateFlow<String?>(null)

    /** Estado reactivo observable por la interfaz de usuario. */
    val uiState: StateFlow<TasksUiState> = combine(
        observeTasksUseCase(),
        selectedCategory,
        errorMessage,
    ) { allTasks, categoryFilter, error ->
        val today = currentDateProvider()

        // 1. Conteo de tareas por filtro para los chips de la cabecera
        val counts = TaskCategoryFilter.entries.associateWith { filter ->
            val domainCat = filter.toDomainCategory()
            if (domainCat == null) allTasks.size else allTasks.count { it.category == domainCat }
        }

        // 2. Enfoque Diario se calcula con las tareas de hoy sin filtrar por chip (CA-3)
        val allTodayTasks = allTasks.filter { TaskTimeClassifier.isToday(it, today) }
        val focusPercentage = DailyFocusCalculator.calculatePercentage(allTodayTasks)
        val pendingCount = DailyFocusCalculator.countPending(allTodayTasks)

        // 3. Filtrar tareas según la categoría activa (CA-2)
        val domainCat = categoryFilter.toDomainCategory()
        val filteredTasks = if (domainCat == null) {
            allTasks
        } else {
            allTasks.filter { it.category == domainCat }
        }

        // 4. Agrupar en "Para Hoy" y "Esta Semana" (CA-1)
        val todayGroup = filteredTasks.filter { TaskTimeClassifier.isToday(it, today) }
        val thisWeekGroup = filteredTasks.filter { TaskTimeClassifier.isThisWeek(it, today) }

        TasksUiState(
            isLoading = false,
            today = today,
            selectedCategory = categoryFilter,
            categoryCounts = counts,
            dailyFocusPercentage = focusPercentage,
            dailyPendingCount = pendingCount,
            todayTasks = todayGroup,
            thisWeekTasks = thisWeekGroup,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = TasksUiState(isLoading = true),
    )

    /**
     * Cambia la categoría seleccionada para filtrar la lista de tareas (CA-2).
     */
    fun onSelectCategory(filter: TaskCategoryFilter) {
        selectedCategory.value = filter
    }

    /**
     * Conmuta el estado de completitud de una tarea y persiste el cambio (CA-3).
     */
    fun onToggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            when (val result = upsertTaskUseCase(updatedTask)) {
                is KronoResult.Error -> {
                    errorMessage.value = result.throwable.message
                }
                is KronoResult.Success -> {
                    errorMessage.value = null
                }
                is KronoResult.Loading -> Unit
            }
        }
    }

    /**
     * Elimina una tarea y sus subtareas asociadas.
     */
    fun onDeleteTask(taskId: String) {
        viewModelScope.launch {
            when (val result = deleteTaskUseCase(taskId)) {
                is KronoResult.Error -> {
                    errorMessage.value = result.throwable.message
                }
                is KronoResult.Success -> {
                    errorMessage.value = null
                }
                is KronoResult.Loading -> Unit
            }
        }
    }

    /**
     * Limpia el mensaje de error activo.
     */
    fun clearError() {
        errorMessage.value = null
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
