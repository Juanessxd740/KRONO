package com.krono.feature.tasks

import app.cash.turbine.test
import com.krono.core.domain.task.DeleteTaskUseCase
import com.krono.core.domain.task.FakeTaskRepository
import com.krono.core.domain.task.ObserveTasksUseCase
import com.krono.core.domain.task.Task
import com.krono.core.domain.task.TaskCategory
import com.krono.core.domain.task.TaskPriority
import com.krono.core.domain.task.UpsertTaskUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

/**
 * Pruebas unitarias para [TasksViewModel] (F-03 CA-1 a CA-4).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fixedToday = LocalDate(2026, 10, 15)

    private lateinit var fakeRepository: FakeTaskRepository
    private lateinit var viewModel: TasksViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeTaskRepository()
        viewModel = TasksViewModel(
            observeTasksUseCase = ObserveTasksUseCase(fakeRepository),
            upsertTaskUseCase = UpsertTaskUseCase(fakeRepository),
            deleteTaskUseCase = DeleteTaskUseCase(fakeRepository),
        ).apply {
            currentDateProvider = { fixedToday }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun alCargarTareas_agrupaEnHoyYEstaSemana_conConteos(
    ) = runTest {
        val taskHoy1 = Task(
            id = "1",
            title = "Informe de Cálculo",
            category = TaskCategory.ESTUDIO,
            priority = TaskPriority.ALTA,
            dueDate = LocalDateTime(2026, 10, 15, 18, 0),
        )
        val taskHoy2 = Task(
            id = "2",
            title = "Reunión laboral",
            category = TaskCategory.TRABAJO,
            dueDate = LocalDateTime(2026, 10, 15, 19, 0),
        )
        val taskSemana = Task(
            id = "3",
            title = "Lectura personal",
            category = TaskCategory.PERSONAL,
            dueDate = LocalDateTime(2026, 10, 18, 10, 0),
            estimatedDuration = 30.minutes,
        )

        fakeRepository.givenTasks(taskHoy1, taskHoy2, taskSemana)

        viewModel.uiState.test {
            // Estado inicial cargando
            awaitItem()

            val state = awaitItem()
            assertFalse(state.isLoading)
            assertFalse(state.isEmpty)
            assertEquals(2, state.todayTasks.size)
            assertEquals(1, state.thisWeekTasks.size)
            assertEquals(3, state.categoryCounts[TaskCategoryFilter.TODAS])
            assertEquals(1, state.categoryCounts[TaskCategoryFilter.ESTUDIO])
            assertEquals(1, state.categoryCounts[TaskCategoryFilter.TRABAJO])
            assertEquals(1, state.categoryCounts[TaskCategoryFilter.PERSONAL])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun alFiltrarPorCategoria_soloMuestraTareasDeEsaCategoria() = runTest {
        val tEstudio = Task(
            id = "1",
            title = "Estudio",
            category = TaskCategory.ESTUDIO,
            dueDate = LocalDateTime(2026, 10, 15, 10, 0),
        )
        val tTrabajo = Task(
            id = "2",
            title = "Trabajo",
            category = TaskCategory.TRABAJO,
            dueDate = LocalDateTime(2026, 10, 15, 12, 0),
        )
        fakeRepository.givenTasks(tEstudio, tTrabajo)

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial load con TODAS

            viewModel.onSelectCategory(TaskCategoryFilter.ESTUDIO)

            val filteredState = awaitItem()
            assertEquals(TaskCategoryFilter.ESTUDIO, filteredState.selectedCategory)
            assertEquals(1, filteredState.todayTasks.size)
            assertEquals("Estudio", filteredState.todayTasks[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun alConmutarCompletitud_actualizaTareaYRecalculaEnfoqueDiario() = runTest {
        val t1 = Task(
            id = "1",
            title = "Tarea 1",
            dueDate = LocalDateTime(2026, 10, 15, 10, 0),
            isCompleted = false,
        )
        val t2 = Task(
            id = "2",
            title = "Tarea 2",
            dueDate = LocalDateTime(2026, 10, 15, 14, 0),
            isCompleted = false,
        )
        fakeRepository.givenTasks(t1, t2)

        viewModel.uiState.test {
            awaitItem() // Loading
            val initial = awaitItem()
            assertEquals(0, initial.dailyFocusPercentage)
            assertEquals(2, initial.dailyPendingCount)

            viewModel.onToggleTaskCompletion(t1)
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(50, updated.dailyFocusPercentage)
            assertEquals(1, updated.dailyPendingCount)

            viewModel.onToggleTaskCompletion(t2)
            advanceUntilIdle()

            val allCompleted = awaitItem()
            assertEquals(100, allCompleted.dailyFocusPercentage)
            assertEquals(0, allCompleted.dailyPendingCount)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun cuandoNoHayTareas_emiteEstadoVacio() = runTest {
        viewModel.uiState.test {
            awaitItem() // Loading
            val emptyState = awaitItem()
            assertTrue(emptyState.isEmpty)
            assertEquals(0, emptyState.dailyFocusPercentage)
            assertEquals(0, emptyState.todayTasks.size)
            assertEquals(0, emptyState.thisWeekTasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun alEliminarTarea_seRemueveDeLaLista() = runTest {
        val task = Task(id = "del", title = "Por borrar")
        fakeRepository.givenTasks(task)

        viewModel.uiState.test {
            awaitItem() // Loading
            val withTask = awaitItem()
            assertEquals(1, withTask.thisWeekTasks.size)

            viewModel.onDeleteTask("del")
            advanceUntilIdle()

            val empty = awaitItem()
            assertTrue(empty.isEmpty)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
