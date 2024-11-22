package com.isis3510.spendiq.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.spendiq.model.data.Offer
import com.isis3510.spendiq.model.local.database.DatabaseProvider
import com.isis3510.spendiq.model.local.database.OfferDao
import com.isis3510.spendiq.model.repository.OffersRepository
import com.isis3510.spendiq.services.LocationBasedOfferService
import com.isis3510.spendiq.utils.toDatabaseModel
import com.isis3510.spendiq.utils.toDomainModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OffersViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OffersRepository()
    private val locationBasedOfferService = LocationBasedOfferService(application)
    private val offerDao: OfferDao = DatabaseProvider.getDatabase(application).offerDao()

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    private val _selectedOffer = MutableStateFlow<Offer?>(null)
    val selectedOffer: StateFlow<Offer?> = _selectedOffer

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun fetchOffers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.getOffers().collect { result ->
                    val remoteOffers = result.getOrNull()

                    if (remoteOffers.isNullOrEmpty()) {
                        // Remote offers are empty, fallback to local database
                        loadOffersFromLocalDatabase()
                    } else {
                        // Remote offers are available, update the state and save to local database
                        _offers.value = remoteOffers
                        saveOffersToLocalDatabase(remoteOffers)
                        _uiState.value = UiState.Success
                    }
                }
            } catch (e: Exception) {
                // Exception occurred, fallback to local database
                loadOffersFromLocalDatabase()
            }
        }
    }

    suspend fun fetchOffersFinal() {
        try {
            repository.getOffers().collect { result ->
                val remoteOffers = result.getOrNull()

                if (!remoteOffers.isNullOrEmpty()) {
                    saveOffersToLocalDatabase(remoteOffers)
                }
            }
        } catch (e: Exception) {

        }
    }

    private suspend fun loadOffersFromLocalDatabase() {
        try {
            val localOffers = offerDao.getAllOffers().map { it.toDomainModel() }
            _offers.value = localOffers
            _uiState.value = if (localOffers.isNotEmpty()) UiState.Success else UiState.Error("No offers available locally")
        } catch (e: Exception) {
            _uiState.value = UiState.Error("Error loading local offers: ${e.message}")
        }
    }

    fun getOfferById(offerId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val offer = offerDao.getOfferById(offerId)?.toDomainModel()
                _selectedOffer.value = offer
                _uiState.value = if (offer != null) UiState.Success else UiState.Error("Offer not found")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error fetching offer: ${e.message}")
            }
        }
    }

    private fun saveOffersToLocalDatabase(offers: List<Offer>) {
        viewModelScope.launch {
            try {
                val databaseOffers = offers.map { it.toDatabaseModel() }
                offerDao.clearAllOffers()
                offerDao.insertOffers(databaseOffers)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationBasedOfferService.stopMonitoring()
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}
