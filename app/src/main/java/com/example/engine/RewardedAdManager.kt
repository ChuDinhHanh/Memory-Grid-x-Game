package com.example.engine

import android.app.Activity
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Owns one preloaded rewarded ad. The ID is Google's official Android demo
 * rewarded unit, so this can safely be used during development only.
 */
class RewardedAdManager(private val activity: Activity) {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    fun initializeAndLoad() {
        MobileAds.initialize(activity) { load() }
    }

    fun show(onRewardEarned: () -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            Toast.makeText(activity, "Quảng cáo đang tải, thử lại sau ít giây.", Toast.LENGTH_SHORT).show()
            load()
            return
        }

        rewardedAd = null
        var earnedReward = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                if (earnedReward) onRewardEarned()
                else Toast.makeText(activity, "Hãy xem hết quảng cáo để nhận lượt chơi.", Toast.LENGTH_SHORT).show()
                load()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Toast.makeText(activity, "Không thể phát quảng cáo. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                load()
            }
        }
        ad.show(activity) { earnedReward = true }
    }

    private fun load() {
        if (isLoading || rewardedAd != null) return
        isLoading = true
        RewardedAd.load(
            activity,
            TEST_REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    isLoading = false
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    isLoading = false
                    rewardedAd = null
                }
            }
        )
    }

    private companion object {
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }
}
