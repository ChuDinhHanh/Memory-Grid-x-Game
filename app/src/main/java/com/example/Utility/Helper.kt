package com.example.Utility


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

/**
 * Hàm mở đường dẫn Privacy Policy trên trình duyệt
 */
fun openPrivacyPolicy(context: Context) {
    val url = "https://qr-scan-gilt.vercel.app/#privacy"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Hàm chia sẻ Game (Share Intent)
 */
fun shareGame(context: Context) {
    val shareMessage = """
        🎮 Check out this awesome Memory Game! 
        Download and challenge your brain now: 
        https://play.google.com/store/apps/details?id=${context.packageName}
    """.trimIndent()

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, shareMessage)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share game via")
    try {
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to share", Toast.LENGTH_SHORT).show()
    }
}

