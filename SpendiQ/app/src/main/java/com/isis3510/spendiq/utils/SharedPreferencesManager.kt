package com.isis3510.spendiq.utils

import android.content.Context

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    fun isOnboardingShown(): Boolean {
        return sharedPreferences.getBoolean("isOnboardingShown", false)
    }

    fun setOnboardingShown(isShown: Boolean) {
        sharedPreferences.edit().putBoolean("isOnboardingShown", isShown).apply()
    }
}