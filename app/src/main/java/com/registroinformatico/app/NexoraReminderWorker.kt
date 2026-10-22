package com.registroinformatico.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONArray
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** Recordatorios de seguimiento: lunes-sábado, 08:30–16:30, en ciclos de dos horas. */
class NexoraReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val now = Calendar.getInstance()
        val day = now.get(Calendar.DAY_OF_WEEK)
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val inWorkDays = day != Calendar.SUNDAY
        val minutesOfDay = hour * 60 + minute
        val withinWorkHours = minutesOfDay in (8 * 60 + 30)..(16 * 60 + 30)
        if (!inWorkDays || !withinWorkHours) return Result.success()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return Result.success()
        }

        val reports = runCatching {
            val raw = applicationContext.getSharedPreferences("registro_informatico", Context.MODE_PRIVATE)
                .getString("reports", "[]") ?: "[]"
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getJSONObject(it).optString("status", "Pendiente") }
        }.getOrDefault(emptyList())
        val pending = reports.count { it.equals("Pendiente", true) || it.equals("Pendientes", true) }
        val inProgress = reports.count { it.equals("En proceso", true) || it.equals("En progreso", true) }
        val active = pending + inProgress
        val body = if (active == 0) {
            "No hay reportes pendientes ni en proceso. Todo está al día."
        } else {
            "Tienes $pending reportes pendientes y $inProgress en proceso. Revisa su seguimiento."
        }
        val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 4103, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Nexora · Seguimiento de reportes")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        runCatching { NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification) }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "nexora_report_reminders"
        private const val CHANNEL_ID = "nexora_report_reminders"
        private const val NOTIFICATION_ID = 4104

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel(
                    CHANNEL_ID, "Recordatorios de Nexora", NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Avisos periódicos para revisar reportes informáticos." }
                manager.createNotificationChannel(channel)
            }
        }

        /** Primera ejecución: próxima franja permitida; las siguientes se repiten cada dos horas. */
        fun delayUntilNextSlot(nowMillis: Long = System.currentTimeMillis()): Long {
            val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val slots = listOf(8 * 60 + 30, 10 * 60 + 30, 12 * 60 + 30, 14 * 60 + 30, 16 * 60 + 30)
            for (dayOffset in 0..7) {
                val candidateDay = (now.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, dayOffset)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (candidateDay.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) continue
                for (slot in slots) {
                    val candidate = (candidateDay.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, slot / 60)
                        set(Calendar.MINUTE, slot % 60)
                    }
                    if (candidate.timeInMillis > nowMillis) {
                        return (candidate.timeInMillis - nowMillis).coerceAtLeast(0L)
                    }
                }
            }
            return TimeUnit.HOURS.toMillis(24)
        }
    }
}
