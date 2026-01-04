package com.petcare.app.worker

import android.content.Context
import androidx.work.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    // minutes avant le RDV
    private val DEFAULT_OFFSETS_MIN = listOf(
        7 * 24 * 60, // 7 jours
        24 * 60,     // 24h
        30           // 30 min
    )

    fun scheduleAll(
        context: Context,
        appointmentId: Int,
        title: String,
        date: String, // yyyy-MM-dd
        time: String, // HH:mm
        offsetsMinutes: List<Int> = DEFAULT_OFFSETS_MIN
    ) {
        val eventMillis = toEpochMillis(date, time)

        offsetsMinutes.distinct().forEach { offsetMin ->
            val triggerMillis = eventMillis - offsetMin * 60_000L
            val delay = triggerMillis - System.currentTimeMillis()
            if (delay <= 0) return@forEach

            val data = workDataOf(
                ReminderWorker.KEY_APPOINTMENT_ID to appointmentId,
                ReminderWorker.KEY_TITLE to title,
                ReminderWorker.KEY_DATE to date,
                ReminderWorker.KEY_TIME to time,
                ReminderWorker.KEY_OFFSET_MIN to offsetMin,
                ReminderWorker.KEY_OFFSET_LABEL to labelForOffset(offsetMin) // ✅ 7j/24h/30min
            )

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(tagFor(appointmentId))
                .addTag(tagFor(appointmentId, offsetMin))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueNameFor(appointmentId, offsetMin),
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    fun cancelAll(context: Context, appointmentId: Int) {
        DEFAULT_OFFSETS_MIN.forEach { offsetMin ->
            WorkManager.getInstance(context).cancelUniqueWork(uniqueNameFor(appointmentId, offsetMin))
        }
        WorkManager.getInstance(context).cancelAllWorkByTag(tagFor(appointmentId))
    }

    private fun uniqueNameFor(appointmentId: Int, offsetMin: Int) =
        "appointment_reminder_${appointmentId}_$offsetMin"

    private fun tagFor(appointmentId: Int) =
        "appointment_reminder_$appointmentId"

    private fun tagFor(appointmentId: Int, offsetMin: Int) =
        "appointment_reminder_${appointmentId}_$offsetMin"

    private fun labelForOffset(offsetMin: Int): String = when (offsetMin) {
        7 * 24 * 60 -> "7j"
        24 * 60 -> "24h"
        30 -> "30min"
        else -> "${offsetMin}min"
    }

    private fun toEpochMillis(date: String, time: String): Long {
        val d = LocalDate.parse(date.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
        val t = LocalTime.parse(time.trim(), DateTimeFormatter.ofPattern("HH:mm"))
        val zdt = ZonedDateTime.of(d, t, ZoneId.systemDefault())
        return zdt.toInstant().toEpochMilli()
    }
}
