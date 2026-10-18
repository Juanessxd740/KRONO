package com.krono.feature.tasks.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.krono.core.ui.brand.KronoSymbol
import com.krono.core.ui.theme.KronoSizes
import com.krono.core.ui.theme.KronoSpacing
import com.krono.core.ui.theme.OnSurface
import com.krono.core.ui.theme.OnSurfaceVariant
import com.krono.feature.tasks.R

/**
 * Cabecera superior de la pantalla de tareas con identidad visual de KRONO.
 */
@Composable
fun TasksHeader(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = KronoSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KronoSymbol(height = KronoSizes.brandSymbol, contentDescription = null)
        Spacer(modifier = Modifier.width(KronoSpacing.md))
        Column {
            Text(
                text = stringResource(R.string.tasks_title),
                style = MaterialTheme.typography.titleLarge,
                color = OnSurface,
            )
            Text(
                text = stringResource(R.string.tasks_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
            )
        }
    }
}
