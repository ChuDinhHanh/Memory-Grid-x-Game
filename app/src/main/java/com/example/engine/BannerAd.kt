package com.example.engine

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    if (!AdsConfig.bannerEnabled) return
    val width = LocalConfiguration.current.screenWidthDp
    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width))
            adUnitId = AdIds.banner()
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) { onDispose { adView.destroy() } }

    AndroidView(modifier = modifier.fillMaxWidth(), factory = { adView })
}