package com.isis3510.spendiq.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.spendiq.model.data.Offer
import com.isis3510.spendiq.model.repository.OffersRepository
import com.isis3510.spendiq.services.LocationBasedOfferService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * OffersViewModel class
 *
 * This ViewModel handles the data and logic related to offers in the application.
 * It interacts with the OffersRepository to fetch offers and utilizes a service to manage
 * location-based offers.
 *
 * Key Features:
 * - Fetching Offers: Retrieves a list of offers from the repository and updates the UI state.
 * - Location-Based Monitoring: Starts monitoring for location-based offers using the LocationBasedOfferService.
 * - Offer Selection: Allows fetching an individual offer by its ID and maintains its state.
 *
 * State Management:
 * - `_offers`: MutableStateFlow holding a list of offers.
 * - `offers`: Public immutable StateFlow for observing the list of offers.
 * - `_selectedOffer`: MutableStateFlow holding the currently selected offer.
 * - `selectedOffer`: Public immutable StateFlow for observing the selected offer.
 * - `_uiState`: MutableStateFlow representing the current UI state (Idle, Loading, Success, Error).
 * - `uiState`: Public immutable StateFlow for observing the UI state.
 *
 * Functions:
 * - `fetchOffers()`: Fetches the offers from the repository and updates the UI state accordingly.
 * - `getOfferById(offerId: String)`: Fetches an individual offer by its ID and updates the selected offer state.
 *
 * Lifecycle Management:
 * - Stops location monitoring when the ViewModel is cleared.
 */
class OffersViewModel(application: Application) : AndroidViewModel(application) {
    // Instance of OffersRepository to interact with the data layer
    private val repository = OffersRepository()

    // Instance of LocationBasedOfferService for monitoring location-based offers
    private val locationBasedOfferService = LocationBasedOfferService(application)

    // MutableStateFlow to hold the list of offers
    private val _offers = MutableStateFlow<List<Offer>>(emptyList())

    // Public immutable StateFlow for the list of offers
    val offers: StateFlow<List<Offer>> = _offers

    // MutableStateFlow to hold the currently selected offer
    private val _selectedOffer = MutableStateFlow<Offer?>(null)

    // Public immutable StateFlow for the selected offer
    val selectedOffer: StateFlow<Offer?> = _selectedOffer

    // MutableStateFlow representing the current UI state
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)

    // Public immutable StateFlow for the UI state
    val uiState: StateFlow<UiState> = _uiState

    // Fetches offers from the repository
    fun fetchOffers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getOffers().collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        val offersList = result.getOrNull() ?: emptyList()
                        _offers.value = offersList
                        // Start monitoring for nearby offers
                        locationBasedOfferService.startMonitoring(offersList)
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

    // Fetches an offer by its ID
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

    // Stops location monitoring when the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        locationBasedOfferService.stopMonitoring()
    }

    // Sealed class representing the UI state
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}
