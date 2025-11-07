package com.travelfoodie.core.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Shared ViewModel to communicate selected trip/region across fragments
 *
 * IMPORTANT: selectedTripId actually stores the REGION ID (not trip ID)
 * This is because AttractionViewModel and RestaurantViewModel need regionId to query data.
 *
 * Flow: TripListFragment → [Select Trip] → SharedTripViewModel → Attraction/Restaurant Fragments
 */
@HiltViewModel
class SharedTripViewModel @Inject constructor() : ViewModel() {

    // IMPORTANT: This stores regionId, not tripId! (Historical naming issue)
    private val _selectedTripId = MutableStateFlow<String?>(null)
    val selectedTripId: StateFlow<String?> = _selectedTripId.asStateFlow()

    private val _selectedRegionName = MutableStateFlow<String?>(null)
    val selectedRegionName: StateFlow<String?> = _selectedRegionName.asStateFlow()

    /**
     * Call this when user creates or selects a trip
     * @param regionId The ID of the region (NOT the trip ID!)
     * @param regionName The name of the region
     */
    fun selectTrip(regionId: String, regionName: String) {
        android.util.Log.d("SharedTripViewModel", "selectTrip called - regionId: $regionId, regionName: $regionName")
        _selectedTripId.value = regionId  // Note: stores regionId despite variable name
        _selectedRegionName.value = regionName
        android.util.Log.d("SharedTripViewModel", "State updated - selectedRegionId: ${_selectedTripId.value}, selectedRegionName: ${_selectedRegionName.value}")
    }

    fun clearSelection() {
        _selectedTripId.value = null
        _selectedRegionName.value = null
    }
}
