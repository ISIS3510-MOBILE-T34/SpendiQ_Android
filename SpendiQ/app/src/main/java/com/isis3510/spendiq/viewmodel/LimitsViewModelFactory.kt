package com.isis3510.spendiq.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LimitsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LimitsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LimitsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
