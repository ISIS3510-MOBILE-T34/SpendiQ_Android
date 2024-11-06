// viewmodel/FinancialStatisticsViewModel.kt
package com.isis3510.spendiq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.spendiq.model.FinancialData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FinancialStatisticsViewModel : ViewModel() {
    // Estado que contiene la lista de datos financieros
    private val _financialData = MutableStateFlow<List<FinancialData>>(emptyList())
    val financialData: StateFlow<List<FinancialData>> = _financialData

    // Estado para el período seleccionado
    private val _selectedPeriod = MutableStateFlow("Daily")
    val selectedPeriod: StateFlow<String> = _selectedPeriod

    init {
        loadFinancialData()
    }

    fun setSelectedPeriod(period: String) {
        _selectedPeriod.value = period
        loadFinancialData() // Recargar datos al cambiar el período
    }

    private fun loadFinancialData() {
        viewModelScope.launch {
            // Aquí puedes reemplazar los datos estáticos con datos reales desde una base de datos o API
            val data = fetchFinancialDataFromSource(_selectedPeriod.value)
            _financialData.value = data
        }
    }

    private suspend fun fetchFinancialDataFromSource(period: String): List<FinancialData> {
        // Simulación de datos; reemplaza esto con lógica real según el período
        return if (period == "Daily") {
            listOf(
                FinancialData("Lun", 500f, 300f),
                FinancialData("Mar", 700f, 400f),
                FinancialData("Mié", 600f, 350f),
                FinancialData("Jue", 800f, 500f),
                FinancialData("Vie", 750f, 450f),
                FinancialData("Sáb", 900f, 600f),
                FinancialData("Dom", 650f, 400f)
            )
        } else { // Weekly
            listOf(
                FinancialData("Semana 1", 3500f, 2100f),
                FinancialData("Semana 2", 4200f, 2800f),
                FinancialData("Semana 3", 3900f, 2600f),
                FinancialData("Semana 4", 4700f, 3100f)
            )
        }
    }

    // Métodos adicionales para actualizar, agregar o eliminar datos pueden ir aquí
}
