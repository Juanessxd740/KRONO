package com.krono.feature.tasks.ui

import android.content.Context
import com.krono.feature.tasks.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration

/**
 * Utilidades para formatear fechas y duraciones de tareas en la interfaz gráfica.
 */
object TaskDateFormatter {

    /**
     * Formatea la fecha y hora límite de entrega en una cadena amigable en español.
     * Si la fecha coincide con [today], formatea como "Hoy, HH:mm hrs".
     */
    fun formatDueDate(context: Context, dueDate: LocalDateTime?, today: LocalDate): String {
        if (dueDate == null) {
            return context.getString(R.string.no_due_date)
        }
        val timeString = String.format("%02d:%02d", dueDate.hour, dueDate.minute)
        return if (dueDate.date == today) {
            context.getString(R.string.due_today_format, timeString)
        } else {
            val dateString = String.format("%02d/%02d", dueDate.dayOfMonth, dueDate.monthNumber)
            context.getString(R.string.due_time_format, dateString, timeString)
        }
    }

    /**
     * Formatea una duración estimada a formato de texto legible (ej. "~30 min est.").
     */
    fun formatEstimatedDuration(context: Context, duration: Duration?): String? {
        if (duration == null) return null
        val totalMinutes = duration.inWholeMinutes
        val hours = totalMinutes / 60
        val remainingMinutes = totalMinutes % 60

        val durationString = when {
            hours > 0 && remainingMinutes > 0 ->
                context.getString(R.string.duration_hours_minutes, hours, remainingMinutes)
            hours > 0 ->
                context.getString(R.string.duration_hours, hours)
            else ->
                context.getString(R.string.duration_minutes, totalMinutes)
        }
        return context.getString(R.string.estimated_duration_format, durationString)
    }
}
