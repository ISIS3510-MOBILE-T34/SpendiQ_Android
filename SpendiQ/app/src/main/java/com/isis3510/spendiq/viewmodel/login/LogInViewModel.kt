package com.isis3510.spendiq.viewmodel.login

import com.isis3510.spendiq.LANDING_SCREEN
import com.isis3510.spendiq.model.service.AccountService
import com.isis3510.spendiq.LOG_IN_SCREEN
import com.isis3510.spendiq.viewmodel.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(
    private val accountService: AccountService
) : AppViewModel() {
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    fun updateEmail(newEmail:String){
        email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        password.value = newPassword
    }

    fun onLogInClick(openAndPopUp: (String, String) -> Unit) {
        launchCatching {
            accountService.signIn(email.value, password.value)
            openAndPopUp(LANDING_SCREEN, LOG_IN_SCREEN)
        }
    }

}