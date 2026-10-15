package com.example.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

object AdsConfig {
    var bannerEnabled by mutableStateOf(true)
        private set
    var reviveEnabled by mutableStateOf(true)
        private set

    fun fetch() {
        val rc = Firebase.remoteConfig
        rc.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
            }
        )
        rc.setDefaultsAsync(
            mapOf(
                "ads_banner_enabled" to true,
                "ads_rewarded_enabled" to true,
                "ad_unit_id_banner" to "ca-app-pub-8862068446033526/7764799432",
                "ad_unit_id_rewarded" to "ca-app-pub-8862068446033526/7298845945"
            )
        )
        readValues()
        rc.fetchAndActivate().addOnCompleteListener { readValues() }
    }

    private fun readValues() {
        val rc = Firebase.remoteConfig
        bannerEnabled = rc.getBoolean("ads_banner_enabled")
        reviveEnabled = rc.getBoolean("ads_rewarded_enabled")
    }
}