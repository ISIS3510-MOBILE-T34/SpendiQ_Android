package com.isis3510.spendiq.viewmodel.start

import com.isis3510.spendiq.LOG_IN_SCREEN
import com.isis3510.spendiq.SIGN_UP_SCREEN
import com.isis3510.spendiq.START_SCREEN

import com.isis3510.spendiq.viewmodel.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(): AppViewModel() {

    fun onLogInClick(openAndPopUp: (String, String) -> Unit) {
        launchCatching {
            openAndPopUp(LOG_IN_SCREEN, START_SCREEN)
        }
    }

    fun onSignUpClick(openAndPopUp: (String, String) -> Unit) {
        launchCatching {
            openAndPopUp(SIGN_UP_SCREEN, START_SCREEN)
        }
    }
}