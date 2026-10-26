package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.R
import com.example.Utility.UsageStatsHelper

class SocialMediaCheckWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val context = applicationContext

        // Nếu chưa cấp quyền thì bỏ qua
        if (!UsageStatsHelper.hasUsageStatsPermission(context)) {
            return Result.success()
        }

        val totalMinutes = UsageStatsHelper.getSocialMediaUsageMinutes(context)

        // Nếu lướt quá 60 phút thì bắn thông báo "cà khịa"
        if (totalMinutes >= 60) {
            showDoomscrollingNotification(context, totalMinutes)
        }

        return Result.success()
    }

    private fun showDoomscrollingNotification(context: Context, minutes: Long) {
        val channelId = "doomscrolling_reminder_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Nhắc nhở cai lướt MXH", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Cảnh báo khi lướt Facebook/TikTok quá nhiều"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.success_txt) // Thay bằng icon thông báo phù hợp trong dự án của bạn
            .setContentTitle("🛑 Nô lệ thuật toán ơi!")
            .setContentText("Bạn đã tốn ${minutes} phút lướt MXH hôm nay rồi đấy. Vào Memory Grid rèn não ngay nào!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Bạn đã tốn ${minutes} phút lướt MXH hôm nay rồi đấy. Mạng xã hội không làm bạn thông minh hơn đâu, vào 'cân não' vài ván cùng Memory Grid xem nào!")
            ).setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build()

        notificationManager.notify(2026, notification)
    }
}