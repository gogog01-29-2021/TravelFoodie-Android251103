package com.travelfoodie.core.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Shared ViewModel to communicate selected trip across fragments
 *
 * Flow: TripListFragment → [Select Trip] → SharedTripViewModel → Attraction/Restaurant Fragments
 */
@HiltViewModel
class SharedTripViewModel @Inject constructor() : ViewModel() {

    private val _selectedTripId = MutableStateFlow<String?>(null)
    val selectedTripId: StateFlow<String?> = _selectedTripId.asStateFlow()

    private val _selectedRegionName = MutableStateFlow<String?>(null)
    val selectedRegionName: StateFlow<String?> = _selectedRegionName.asStateFlow()

    /**
     * Call this when user creates or selects a trip
     */
    fun selectTrip(tripId: String, regionName: String) {
        android.util.Log.d("SharedTripViewModel", "selectTrip called - tripId: $tripId, regionName: $regionName")
        _selectedTripId.value = tripId
        _selectedRegionName.value = regionName
        android.util.Log.d("SharedTripViewModel", "State updated - selectedTripId: ${_selectedTripId.value}, selectedRegionName: ${_selectedRegionName.value}")
    }

    fun clearSelection() {
        _selectedTripId.value = null
        _selectedRegionName.value = null
    }
}
