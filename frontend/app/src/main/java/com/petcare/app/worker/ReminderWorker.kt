package com.petcare.app.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.petcare.app.R
import kotlin.random.Random

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val appointmentId = inputData.getInt(KEY_APPOINTMENT_ID, -1)
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val date = inputData.getString(KEY_DATE).orEmpty()
        val time = inputData.getString(KEY_TIME).orEmpty()
        val offsetLabel = inputData.getString(KEY_OFFSET_LABEL).orEmpty() // "7j" / "24h" / "30min"

        if (appointmentId <= 0 || title.isBlank()) return Result.failure()

        // Android 13+ : permission notif
        if (!canPostNotifications(applicationContext)) {
            return Result.success() // on ne fail pas, on ignore juste
        }

        createChannelIfNeeded(applicationContext)

        val text = "Rappel $offsetLabel • $date à $time"
        val notifId = uniqueNotifId(appointmentId, offsetLabel)

        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // ✅ change si tu as une icône notif dédiée
            .setContentTitle("PetCare • ${title}")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(notifId, notif)

        return Result.success()
    }

    private fun createChannelIfNeeded(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = nm.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Rappels Agenda",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications de rappel des rendez-vous (7j/24h/30min)"
        }

        nm.createNotificationChannel(channel)
    }

    private fun canPostNotifications(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun uniqueNotifId(appointmentId: Int, offsetLabel: String): Int {
        // stable : même RDV + même offset -> même notifId
        val base = appointmentId * 1000
        val suffix = when (offsetLabel) {
            "7j" -> 7
            "24h" -> 24
            "30min" -> 30
            else -> Random.nextInt(100, 999)
        }
        return base + suffix
    }

    companion object {
        const val CHANNEL_ID = "petcare_agenda_reminders"

        const val KEY_APPOINTMENT_ID = "appointment_id"
        const val KEY_TITLE = "title"
        const val KEY_DATE = "date"
        const val KEY_TIME = "time"
        const val KEY_OFFSET_MIN = "offset_min"
        const val KEY_OFFSET_LABEL = "offset_label"
    }
}
