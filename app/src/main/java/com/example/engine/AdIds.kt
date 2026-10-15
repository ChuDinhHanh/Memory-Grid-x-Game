package com.example.engine

import com.example.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig



object AdIds {
    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    private const val REAL_BANNER = "ca-app-pub-3940256099942544/6300978111"
//    private const val REAL_BANNER = "ca-app-pub-8862068446033526/3330935913"

    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    private const val REAL_REWARDED = "ca-app-pub-3940256099942544/5224354917"
//    private const val REAL_REWARDED = "ca-app-pub-8862068446033526/7298845945"

    fun banner(): String {
        if (BuildConfig.DEBUG) return TEST_BANNER
        return Firebase.remoteConfig.getString("ad_unit_id_banner").ifBlank { REAL_BANNER }
    }

    fun rewarded(): String {
        if (BuildConfig.DEBUG) return TEST_REWARDED
        return Firebase.remoteConfig.getString("ad_unit_id_rewarded").ifBlank { REAL_REWARDED }
    }
}