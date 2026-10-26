package com.example.engine

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.engine.ReviewHelper.onGameFinished
import com.example.engine.ReviewHelper.openPlayStore
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.launch

/**
 * Chức năng đánh giá ứng dụng.
 *
 * 1. [openPlayStore]       : nút "Đánh giá" do người dùng chủ động bấm -> mở thẳng trang app trên Play Store.
 * 2. [onGameFinished]      : tự động hỏi đánh giá bằng popup của Google (In-App Review) vào lúc hợp lý.
 *
 * Lưu ý: Google giới hạn tần suất popup In-App Review (quota) và không báo lại kết quả
 * người dùng có đánh giá hay không, nên KHÔNG dùng popup này cho nút bấm thủ công.
 */
object ReviewHelper {
    private const val TAG = "ReviewHelper"
    private const val PREFS = "review_prefs"
    private const val KEY_GAMES_PLAYED = "games_played"
    private const val KEY_LAST_PROMPT_TIME = "last_prompt_time"
    private const val KEY_PROMPT_COUNT = "prompt_count"

    // Hỏi lần đầu sau 5 ván, các lần sau cách nhau tối thiểu 30 ngày, tối đa 3 lần.
    private const val FIRST_PROMPT_AFTER_GAMES = 5
    private const val MIN_DAYS_BETWEEN_PROMPTS = 30
    private const val MAX_PROMPTS = 3

    /** Mở trang ứng dụng trên Google Play để người dùng tự đánh giá. */
    fun openPlayStore(context: Context) {
        val pkg = context.packageName
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$pkg")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            // Máy không có Play Store app -> mở bằng trình duyệt
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e2: Exception) {
                Log.e(TAG, "Cannot open Play Store", e2)
            }
        }
    }

    /** Gọi mỗi khi người chơi kết thúc / thoát một ván. Tự quyết định có hiện popup đánh giá hay không. */
    fun onGameFinished(context: Context) {
        val activity = context.findActivity() as? ComponentActivity ?: return
        val prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0) + 1
        prefs.edit().putInt(KEY_GAMES_PLAYED, gamesPlayed).apply()

        val promptCount = prefs.getInt(KEY_PROMPT_COUNT, 0)
        val lastPrompt = prefs.getLong(KEY_LAST_PROMPT_TIME, 0L)
        val daysSinceLast = (System.currentTimeMillis() - lastPrompt) / (24L * 60 * 60 * 1000)

        val shouldPrompt =
            gamesPlayed >= FIRST_PROMPT_AFTER_GAMES && promptCount < MAX_PROMPTS && (lastPrompt == 0L || daysSinceLast >= MIN_DAYS_BETWEEN_PROMPTS)

        if (!shouldPrompt) return

        launchInAppReview(activity) {
            prefs.edit().putLong(KEY_LAST_PROMPT_TIME, System.currentTimeMillis())
                .putInt(KEY_PROMPT_COUNT, promptCount + 1).apply()
        }
    }

    private fun launchInAppReview(activity: ComponentActivity, onDone: () -> Unit) {
        val manager = ReviewManagerFactory.create(activity)
        activity.lifecycleScope.launch {
            try {
                val reviewInfo = manager.requestReview()
                manager.launchReview(activity, reviewInfo)
                onDone()
            } catch (e: Exception) {
                // Lỗi hoặc hết quota -> bỏ qua, lần sau thử lại
                Log.w(TAG, "In-App Review failed: ${e.message}")
            }
        }
    }

    private fun Context.findActivity(): Activity? {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
}
