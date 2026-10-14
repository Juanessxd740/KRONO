package com.krono.feature.tasks.detail.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.krono.core.ui.icons.KronoIcons
import com.krono.core.ui.theme.KronoSizes
import com.krono.core.ui.theme.OnSurface
import com.krono.core.ui.theme.OnSurfaceVariant
import com.krono.feature.tasks.R

/**
 * Barra superior de navegación para la pantalla de detalle de tarea (F-06).
 *
 * Sigue el diseño de `/design/detalle_de_tarea_y_subtareas/` con botón de volver y acceso a edición.
 *
 * @param onBack Callback para volver a la pantalla anterior.
 * @param onEdit Callback para abrir la edición de la tarea (F-05).
 * @param modifier Modificador de diseño.
 */
@Composable
fun TaskDetailTopBar(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(KronoSizes.minTouchTarget),
        ) {
            Icon(
                painter = painterResource(KronoIcons.ArrowBack),
                contentDescription = stringResource(R.string.action_back),
                tint = OnSurface,
                modifier = Modifier.size(KronoSizes.iconMedium),
            )
        }

        Text(
            text = stringResource(R.string.task_detail_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnSurface,
        )

        IconButton(
            onClick = onEdit,
            modifier = Modifier.size(KronoSizes.minTouchTarget),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_edit_note),
                contentDescription = stringResource(R.string.cd_edit_task),
                tint = OnSurfaceVariant,
                modifier = Modifier.size(KronoSizes.iconMedium),
            )
        }
    }
}
