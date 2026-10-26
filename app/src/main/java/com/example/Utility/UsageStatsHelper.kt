package com.example.Utility

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import java.util.Calendar

object UsageStatsHelper {

    // Kiểm tra xem user đã cấp quyền "Usage Access" chưa
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // Mở màn hình cài đặt hệ thống để user cấp quyền
    fun requestUsageStatsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        context.startActivity(intent)
    }

    // Tính tổng số phút lướt các mạng xã hội trong ngày tính từ 00:00
    fun getSocialMediaUsageMinutes(context: Context): Long {
        // Sửa thành truyền chuỗi "usagestats" trực tiếp để tránh lỗi Unresolved reference
        val usageStatsManager = context.getSystemService("usagestats") as UsageStatsManager

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        val socialPackages = listOf(
            "com.facebook.katana",      // Facebook
            "com.zhiliaoapp.musically",  // TikTok
            "com.instagram.android",    // Instagram
            "com.google.android.youtube"// YouTube
        )

        var totalTimeMillis = 0L
        for (stat in stats) {
            if (socialPackages.contains(stat.packageName)) {
                totalTimeMillis += stat.totalTimeInForeground
            }
        }

        return totalTimeMillis / (1000 * 60) // Đổi từ milliseconds sang phút
    }
}