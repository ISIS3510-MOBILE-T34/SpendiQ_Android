package com.isis3510.spendiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.spendiq.model.data.Offer
import com.isis3510.spendiq.model.repository.OffersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OffersViewModel : ViewModel() {
    private val repository = OffersRepository()

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    private val _selectedOffer = MutableStateFlow<Offer?>(null)
    val selectedOffer: StateFlow<Offer?> = _selectedOffer

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun fetchOffers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getOffers().collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        _offers.value = result.getOrNull() ?: emptyList()
                        UiState.Success
                    }
                    result.isFailure -> {
                        UiState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch offers")
                    }
                    else -> {
                        UiState.Error("Unknown error occurred")
                    }
                }
            }
        }
    }

    fun getOfferById(offerId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getOfferById(offerId).collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        _selectedOffer.value = result.getOrNull()
                        UiState.Success
                    }
                    result.isFailure -> {
                        UiState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch offer")
                    }
                    else -> {
                        UiState.Error("Unknown error occurred")
                    }
                }
            }
        }
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}
