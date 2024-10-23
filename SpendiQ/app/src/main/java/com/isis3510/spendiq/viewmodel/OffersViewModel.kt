package com.isis3510.spendiq.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.model.data.Offer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OffersViewModel : ViewModel() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    fun fetchOffers() {
        viewModelScope.launch {
            try {
                Log.d("OffersViewModel", "Fetching offers...")
                val snapshot = firestore.collection("offers").get().await()
                val offerList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val offer = doc.toObject(Offer::class.java)
                        Log.d("OffersViewModel", "Successfully deserialized offer: $offer")
                        offer
                    } catch (e: Exception) {
                        Log.e("OffersViewModel", "Error deserializing document: ${doc.data} - ${e.message}")
                        null
                    }
                }
                _offers.value = offerList
                Log.d("OffersViewModel", "Fetched offers: $offerList")
            } catch (e: Exception) {
                Log.e("OffersViewModel", "Error fetching offers: ${e.message}")
                _offers.value = emptyList()
            }
        }
    }


}
