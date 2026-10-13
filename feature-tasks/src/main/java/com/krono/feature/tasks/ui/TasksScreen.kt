package com.krono.feature.tasks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import com.krono.core.domain.task.Task
import com.krono.core.ui.theme.DeepMidnight
import com.krono.core.ui.theme.KronoGlass
import com.krono.core.ui.theme.KronoSizes
import com.krono.core.ui.theme.KronoSpacing
import com.krono.core.ui.theme.OnSurface
import com.krono.core.ui.theme.OnSurfaceVariant
import com.krono.core.ui.theme.PrimaryFixedDim
import com.krono.core.ui.theme.PrimaryMagenta
import com.krono.core.ui.theme.SurfaceContainerHigh
import com.krono.feature.tasks.R
import com.krono.feature.tasks.TaskCategoryFilter
import com.krono.feature.tasks.TasksUiState
import com.krono.feature.tasks.TasksViewModel

/**
 * Punto de entrada con estado para la pantalla de tareas (F-03).
 * Conecta [TasksViewModel] con la interfaz reactiva.
 */
@Composable
fun TasksRoute(
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier,
    onTaskClick: ((String) -> Unit)? = null,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { errorMsg ->
            snackbarHostState.showSnackbar(message = errorMsg)
            viewModel.clearError()
        }
    }

    TasksScreen(
        state = state,
        onFilterSelected = viewModel::onSelectCategory,
        onToggleCompletion = viewModel::onToggleTaskCompletion,
        onDeleteTask = viewModel::onDeleteTask,
        onAddTask = onAddTask,
        modifier = modifier,
        onTaskClick = onTaskClick,
        snackbarHostState = snackbarHostState,
    )
}

/**
 * Pantalla principal de la lista de tareas (F-03 CA-1 a CA-4).
 * Diseñada bajo el sistema visual Lumina Glass con fondo Deep Midnight y acentos Magenta/Cyan.
 */
@Composable
fun TasksScreen(
    state: TasksUiState,
    onFilterSelected: (TaskCategoryFilter) -> Unit,
    onToggleCompletion: (Task) -> Unit,
    onDeleteTask: (String) -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier,
    onTaskClick: ((String) -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val addTaskLabel = stringResource(R.string.action_add_task)

    Scaffold(
        modifier = modifier,
        containerColor = DeepMidnight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTask,
                containerColor = PrimaryMagenta,
                contentColor = OnSurface,
                shape = CircleShape,
                modifier = Modifier.semantics {
                    contentDescription = addTaskLabel
                },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.size(KronoSizes.iconMedium),
                    tint = OnSurface,
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = PrimaryFixedDim,
                        strokeWidth = KronoSizes.progressStroke,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = KronoSpacing.lg,
                        end = KronoSpacing.lg,
                        top = KronoSpacing.md,
                        bottom = KronoSizes.controlHeight + KronoSpacing.xxl,
                    ),
                    verticalArrangement = Arrangement.spacedBy(KronoSpacing.md),
                ) {
                    item {
                        TasksHeader()
                    }

                    item {
                        TasksFilterRow(
                            selectedCategory = state.selectedCategory,
                            categoryCounts = state.categoryCounts,
                            onSelectCategory = onFilterSelected,
                        )
                    }

                    item {
                        DailyFocusCard(
                            percentage = state.dailyFocusPercentage,
                            pendingCount = state.dailyPendingCount,
                        )
                    }

                    if (state.isEmpty) {
                        item {
                            TasksEmptyState(
                                onAddTask = onAddTask,
                                isFiltered = state.selectedCategory != TaskCategoryFilter.TODAS,
                            )
                        }
                    } else {
                        if (state.todayTasks.isNotEmpty()) {
                            item {
                                TaskSectionHeader(
                                    title = stringResource(R.string.section_today),
                                    count = state.todayTasks.size,
                                )
                            }
                            items(state.todayTasks, key = { it.id }) { task ->
                                TaskCard(
                                    task = task,
                                    today = state.today,
                                    onToggleCompletion = onToggleCompletion,
                                    onDelete = onDeleteTask,
                                    onClick = onTaskClick?.let { { it(task.id) } },
                                )
                            }
                        }

                        if (state.thisWeekTasks.isNotEmpty()) {
                            item {
                                TaskSectionHeader(
                                    title = stringResource(R.string.section_this_week),
                                    count = state.thisWeekTasks.size,
                                )
                            }
                            items(state.thisWeekTasks, key = { it.id }) { task ->
                                TaskCard(
                                    task = task,
                                    today = state.today,
                                    onToggleCompletion = onToggleCompletion,
                                    onDelete = onDeleteTask,
                                    onClick = onTaskClick?.let { { it(task.id) } },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Cabecera de sección para agrupar tareas ("Para Hoy" y "Esta Semana") con conteo en Lumina Glass (CA-1).
 */
@Composable
fun TaskSectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    val countText = if (count == 1) {
        stringResource(R.string.tasks_count_single)
    } else {
        stringResource(R.string.tasks_count_multiple, count)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = KronoSpacing.sm, bottom = KronoSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = OnSurface,
        )

        val badgeShape = RoundedCornerShape(KronoSizes.minTouchTarget)
        Box(
            modifier = Modifier
                .clip(badgeShape)
                .background(SurfaceContainerHigh)
                .border(
                    width = KronoGlass.borderWidth,
                    color = KronoGlass.borderColor,
                    shape = badgeShape,
                )
                .padding(horizontal = KronoSpacing.sm, vertical = KronoSpacing.xs / 2),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = countText,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
            )
        }
    }
}
