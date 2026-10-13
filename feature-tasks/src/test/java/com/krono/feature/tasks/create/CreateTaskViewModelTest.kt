package com.krono.feature.tasks.create

import app.cash.turbine.test
import com.krono.core.domain.task.FakeTaskRepository
import com.krono.core.domain.task.Task
import com.krono.core.domain.task.TaskCategory
import com.krono.core.domain.task.TaskPriority
import com.krono.core.domain.task.UpsertTaskUseCase
import com.krono.feature.tasks.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

/**
 * Pruebas unitarias para [CreateTaskViewModel] (F-04 CA-1 a CA-4).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CreateTaskViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fixedNow = LocalDateTime(2026, 10, 14, 12, 0)

    private lateinit var fakeRepository: FakeTaskRepository
    private lateinit var viewModel: CreateTaskViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeTaskRepository()
        viewModel = CreateTaskViewModel(
            upsertTaskUseCase = UpsertTaskUseCase(fakeRepository),
        ).apply {
            currentDateTimeProvider = { fixedNow }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun alGuardarTareaValida_persisteYEmiteNavegarAtras() = runTest {
        viewModel.onTitleChanged("Ensayo de macroeconomía")
        viewModel.onCategorySelected(TaskCategory.ESTUDIO)
        viewModel.onDateSelected(LocalDate(2026, 10, 15))
        viewModel.onTimeSelected(LocalTime(18, 0))
        viewModel.onPrioritySelected(TaskPriority.ALTA)
        viewModel.onDurationSelected(45.minutes)

        viewModel.events.test {
            viewModel.saveTask()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is CreateTaskEvent.NavigateBack)

            val tasks = fakeRepository.observeTasks().first()
            assertEquals(1, tasks.size)
            val task = tasks.first()
            assertEquals("Ensayo de macroeconomía", task.title)
            assertEquals(TaskCategory.ESTUDIO, task.category)
            assertEquals(TaskPriority.ALTA, task.priority)
            assertEquals(LocalDateTime(2026, 10, 15, 18, 0), task.dueDate)
            assertEquals(45.minutes, task.estimatedDuration)
            assertFalse(task.isCompleted)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun alGuardarSinTitulo_muestraErrorVisualYNoPersiste() = runTest {
        viewModel.onTitleChanged("   ")
        viewModel.onDateSelected(LocalDate(2026, 10, 15))

        viewModel.saveTask()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(R.string.error_title_required, state.titleErrorRes)
        assertTrue(state.isTitleError)
        assertTrue(fakeRepository.observeTasks().first().isEmpty())
    }

    @Test
    fun alGuardarSinFecha_muestraErrorVisualYNoPersiste() = runTest {
        viewModel.onTitleChanged("Tarea sin fecha")
        // dueDate es null por defecto

        viewModel.saveTask()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(R.string.error_date_required, state.dateErrorRes)
        assertTrue(state.isDateError)
        assertTrue(fakeRepository.observeTasks().first().isEmpty())
    }

    @Test
    fun alGuardarConFechaHoraEnElPasado_muestraErrorYNoPersiste() = runTest {
        // fixedNow es 2026-10-14 12:00
        viewModel.onTitleChanged("Tarea atrasada")
        viewModel.onDateSelected(LocalDate(2026, 10, 14))
        viewModel.onTimeSelected(LocalTime(10, 0)) // 2 horas antes de fixedNow

        viewModel.saveTask()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(R.string.error_due_date_in_past, state.dateTimeErrorRes)
        assertTrue(state.isTimeError)
        assertTrue(fakeRepository.observeTasks().first().isEmpty())
    }

    @Test
    fun alGuardarSinEstimacion_persisteConDuracionNula() = runTest {
        viewModel.onTitleChanged("Tarea sin tiempo estimado")
        viewModel.onDateSelected(LocalDate(2026, 10, 15))
        viewModel.onDurationSelected(null)

        viewModel.events.test {
            viewModel.saveTask()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is CreateTaskEvent.NavigateBack)

            val tasks = fakeRepository.observeTasks().first()
            assertEquals(1, tasks.size)
            assertNull(tasks.first().estimatedDuration)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun alEscribirTitulo_limpiaErrorPrevio() = runTest {
        viewModel.onTitleChanged("")
        viewModel.saveTask()
        assertEquals(R.string.error_title_required, viewModel.uiState.value.titleErrorRes)

        viewModel.onTitleChanged("Nuevo título")
        assertNull(viewModel.uiState.value.titleErrorRes)
        assertFalse(viewModel.uiState.value.isTitleError)
    }
}
