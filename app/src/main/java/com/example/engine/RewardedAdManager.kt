package com.example.engine

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdManager(
    private val activity: Activity
) {
    companion object {
        private const val TAG = "RewardedAd"
        private const val RETRY_DELAY_MS = 10_000L
        private const val MAX_RETRY_DELAY_MS = 60_000L
    }

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var retryDelayMs = RETRY_DELAY_MS

    private val handler = Handler(Looper.getMainLooper())

    private var isShowingAd = false

    var isReady by mutableStateOf(false)
        private set

    private var retryRunnable: Runnable? = null

    /** Các callback đang chờ lần tải quảng cáo hiện tại kết thúc (thành công hoặc thất bại). */
    private val pendingLoadCallbacks = mutableListOf<() -> Unit>()

    private fun notifyLoadFinished() {
        if (pendingLoadCallbacks.isEmpty()) return
        val callbacks = pendingLoadCallbacks.toList()
        pendingLoadCallbacks.clear()
        callbacks.forEach { callback ->
            runCatching { callback() }
                .onFailure { Log.w(TAG, "onFinished callback failed", it) }
        }
    }

    /**
     * Tải quảng cáo thưởng. [onFinished] được gọi đúng một lần khi việc tải kết thúc:
     * tải xong, tải lỗi, hoặc không cần tải (đã có sẵn / bị tắt / Activity không hợp lệ).
     * Splash dùng callback này để biết lúc nào có thể chuyển sang Home.
     */
    fun load(onFinished: () -> Unit = {}) {
        if (!AdsConfig.reviveEnabled) {
            Log.d(TAG, "LOAD SKIPPED: rewarded ads disabled by Remote Config")
            onFinished()
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "LOAD SKIPPED: Activity is not valid")
            onFinished()
            return
        }

        if (rewardedAd != null || isShowingAd) {
            Log.d(
                TAG,
                "LOAD SKIPPED: isReady=${rewardedAd != null}, isShowingAd=$isShowingAd"
            )
            onFinished()
            return
        }

        if (isLoading) {
            Log.d(TAG, "LOAD SKIPPED: already loading, waiting for result")
            pendingLoadCallbacks.add(onFinished)
            return
        }

        pendingLoadCallbacks.add(onFinished)
        cancelRetry()
        isLoading = true
        isReady = false

        val adUnitId = AdIds.rewarded()
        Log.d(TAG, "LOAD START: adUnitId=$adUnitId")

        RewardedAd.load(
            activity,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(ad: RewardedAd) {
                    isLoading = false
                    rewardedAd = ad
                    isReady = true
                    retryDelayMs = RETRY_DELAY_MS

                    Log.d(TAG, "LOAD SUCCESS")
                    notifyLoadFinished()

                    ad.fullScreenContentCallback =
                        object : FullScreenContentCallback() {

                            override fun onAdShowedFullScreenContent() {
                                Log.d(TAG, "AD SHOWED")
                                isShowingAd = true
                            }

                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "AD DISMISSED")
                                isShowingAd = false
                                rewardedAd = null
                                isReady = false
                                load()
                            }

                            override fun onAdFailedToShowFullScreenContent(
                                adError: AdError
                            ) {
                                Log.e(
                                    TAG,
                                    "SHOW FAILED: code=${adError.code}, " +
                                            "message=${adError.message}, " +
                                            "domain=${adError.domain}"
                                )

                                isShowingAd = false
                                rewardedAd = null
                                isReady = false

                                showToast(
                                    "Không thể phát quảng cáo. Vui lòng thử lại."
                                )
                                scheduleRetry()
                            }

                            override fun onAdImpression() {
                                Log.d(TAG, "AD IMPRESSION")
                            }

                            override fun onAdClicked() {
                                Log.d(TAG, "AD CLICKED")
                            }
                        }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    rewardedAd = null
                    isReady = false

                    Log.e(
                        TAG,
                        "LOAD FAILED: code=${error.code}, " +
                                "message=${error.message}, " +
                                "domain=${error.domain}, " +
                                "responseInfo=${error.responseInfo}"
                    )

                    notifyLoadFinished()
                    scheduleRetry()
                }
            }
        )
    }

    fun show(onRewardEarned: () -> Unit) {
        if (!AdsConfig.reviveEnabled) {
            Log.w(TAG, "SHOW BLOCKED: rewarded ads disabled")
            showToast("Quảng cáo hồi sinh hiện đang tắt.")
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "SHOW BLOCKED: Activity is not valid")
            return
        }

        if (isShowingAd) {
            Log.w(TAG, "SHOW BLOCKED: An ad is already showing")
            return
        }

        val ad = rewardedAd

        if (ad == null) {
            Log.w(
                TAG,
                "SHOW UNAVAILABLE: isLoading=$isLoading, isReady=$isReady"
            )
            showToast("Quảng cáo đang tải, thử lại sau ít giây.")
            load()
            return
        }

        rewardedAd = null
        isReady = false
        isShowingAd = true

        var earnedReward = false
        var rewardDelivered = false

        Log.d(TAG, "SHOW START")

        ad.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "AD SHOWED")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(
                        TAG,
                        "AD DISMISSED: earnedReward=$earnedReward"
                    )

                    isShowingAd = false

                    if (earnedReward && !rewardDelivered) {
                        rewardDelivered = true
                        Log.d(TAG, "REWARD DELIVERED")
                        onRewardEarned()
                    } else if (!earnedReward) {
                        Log.w(TAG, "REWARD NOT EARNED")
                        showToast("Hãy xem hết quảng cáo để hồi sinh.")
                    }

                    load()
                }

                override fun onAdFailedToShowFullScreenContent(
                    adError: AdError
                ) {
                    isShowingAd = false
                    rewardedAd = null
                    isReady = false

                    Log.e(
                        TAG,
                        "SHOW FAILED: code=${adError.code}, " +
                                "message=${adError.message}, " +
                                "domain=${adError.domain}"
                    )

                    showToast(
                        "Không thể phát quảng cáo. Vui lòng thử lại."
                    )
                    scheduleRetry()
                }

                override fun onAdImpression() {
                    Log.d(TAG, "AD IMPRESSION")
                }

                override fun onAdClicked() {
                    Log.d(TAG, "AD CLICKED")
                }
            }

        try {
            ad.show(activity) {
                earnedReward = true
                Log.d(TAG, "REWARD EARNED")
            }
        } catch (e: Exception) {
            isShowingAd = false
            rewardedAd = null
            isReady = false

            Log.e(TAG, "SHOW EXCEPTION", e)
            showToast("Có lỗi khi mở quảng cáo. Vui lòng thử lại.")
            scheduleRetry()
        }
    }

    private fun scheduleRetry() {
        if (!AdsConfig.reviveEnabled) {
            Log.d(TAG, "RETRY SKIPPED: ads disabled")
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "RETRY SKIPPED: Activity is not valid")
            return
        }

        if (isShowingAd || isLoading || rewardedAd != null) {
            Log.d(TAG, "RETRY SKIPPED: ad is loading, ready, or showing")
            return
        }

        cancelRetry()

        val delay = retryDelayMs
        retryDelayMs = (retryDelayMs * 2).coerceAtMost(MAX_RETRY_DELAY_MS)

        Log.d(TAG, "RETRY SCHEDULED: in ${delay}ms")

        val runnable = Runnable {
            retryRunnable = null
            Log.d(TAG, "RETRY EXECUTED")
            load()
        }

        retryRunnable = runnable
        handler.postDelayed(runnable, delay)
    }

    private fun cancelRetry() {
        retryRunnable?.let(handler::removeCallbacks)
        retryRunnable = null
    }

    private fun showToast(message: String) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(
                activity,
                message,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            handler.post {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    Toast.makeText(
                        activity,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}