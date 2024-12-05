package com.isis3510.spendiq.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.isis3510.spendiq.utils.SharedPreferencesManager

class OnboardingViewModel(app: Application) : AndroidViewModel(Application()) {
    private val sharedPreferencesManager = SharedPreferencesManager(app)

    // MutableState para controlar el estado del onboarding
    var isOnboardingShown = mutableStateOf(sharedPreferencesManager.isOnboardingShown())
        private set

    fun setOnboardingShown(isShown: Boolean) {
        sharedPreferencesManager.setOnboardingShown(isShown)
        isOnboardingShown.value = isShown // Actualizar el estado
    }
}