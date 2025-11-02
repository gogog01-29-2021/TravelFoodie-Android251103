package com.travelfoodie.feature.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelfoodie.core.data.local.entity.TripEntity
import com.travelfoodie.core.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _trips = MutableStateFlow<List<TripEntity>>(emptyList())
    val trips: StateFlow<List<TripEntity>> = _trips.asStateFlow()

    init {
        loadTrips()
    }

    private fun loadTrips() {
        // TODO: Replace with actual Firebase Auth user ID when Firebase is configured
        val userId = "dev_user_001"
        viewModelScope.launch {
            tripRepository.getTripsByUser(userId).collect { tripList ->
                _trips.value = tripList
            }
        }
    }

    fun createTrip(trip: TripEntity) {
        viewModelScope.launch {
            tripRepository.insertTrip(trip)
        }
    }

    fun updateTrip(trip: TripEntity) {
        viewModelScope.launch {
            tripRepository.updateTrip(trip)
        }
    }

    fun deleteTrip(trip: TripEntity) {
        viewModelScope.launch {
            tripRepository.deleteTrip(trip)
        }
    }
}
