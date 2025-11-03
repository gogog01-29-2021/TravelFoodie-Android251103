package com.travelfoodie.feature.trip

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelfoodie.core.data.local.entity.TripEntity
import com.travelfoodie.core.data.repository.PoiRepository
import com.travelfoodie.core.data.repository.RestaurantRepository
import com.travelfoodie.core.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tripRepository: TripRepository,
    private val poiRepository: PoiRepository,
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    private val _trips = MutableStateFlow<List<TripEntity>>(emptyList())
    val trips: StateFlow<List<TripEntity>> = _trips.asStateFlow()

    private val _creationState = MutableStateFlow<TripCreationState>(TripCreationState.Idle)
    val creationState: StateFlow<TripCreationState> = _creationState.asStateFlow()

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

    /**
     * Complete trip creation flow with auto-generation
     *
     * Flow: Save Trip → Generate Attractions → Generate Restaurants → Schedule Notifications
     */
    fun createTripWithAutoGeneration(trip: TripEntity, regionName: String, members: String) {
        android.util.Log.d("TripViewModel", "createTripWithAutoGeneration START - tripId: ${trip.tripId}")
        viewModelScope.launch {
            try {
                android.util.Log.d("TripViewModel", "Setting state: SavingTrip")
                _creationState.value = TripCreationState.SavingTrip

                // 1. Save trip to database
                android.util.Log.d("TripViewModel", "Inserting trip into database")
                tripRepository.insertTrip(trip)
                android.util.Log.d("TripViewModel", "Trip inserted successfully")

                // 2. Auto-generate 5 AI attractions for the region
                android.util.Log.d("TripViewModel", "Setting state: GeneratingAttractions")
                _creationState.value = TripCreationState.GeneratingAttractions
                android.util.Log.d("TripViewModel", "Generating attractions for tripId: ${trip.tripId}, regionName: $regionName")
                val attractions = poiRepository.generateMockAttractions(
                    regionId = trip.tripId,  // Use tripId as regionId for simplicity
                    regionName = regionName
                )
                android.util.Log.d("TripViewModel", "Generated ${attractions.size} attractions for tripId: ${trip.tripId}")

                // 3. Auto-generate 10 restaurants for the region
                android.util.Log.d("TripViewModel", "Setting state: GeneratingRestaurants")
                _creationState.value = TripCreationState.GeneratingRestaurants
                android.util.Log.d("TripViewModel", "Generating restaurants")
                val restaurants = restaurantRepository.createMockRestaurants(
                    regionId = trip.tripId,  // Use tripId as regionId for simplicity
                    regionName = regionName,
                    lat = 37.5665, // Default Seoul coordinates, should be from region
                    lng = 126.9780
                )
                android.util.Log.d("TripViewModel", "Generated ${restaurants.size} restaurants")

                // 4. Schedule push notifications (D-7, D-3, D-0)
                android.util.Log.d("TripViewModel", "Setting state: SchedulingNotifications")
                _creationState.value = TripCreationState.SchedulingNotifications
                android.util.Log.d("TripViewModel", "Scheduling notifications")
                scheduleNotifications(trip)
                android.util.Log.d("TripViewModel", "Notifications scheduled")

                // 5. Success!
                android.util.Log.d("TripViewModel", "Setting state: Success")
                _creationState.value = TripCreationState.Success(
                    attractionsCount = attractions.size,
                    restaurantsCount = restaurants.size
                )
                android.util.Log.d("TripViewModel", "SUCCESS - attractionsCount: ${attractions.size}, restaurantsCount: ${restaurants.size}")

            } catch (e: Exception) {
                android.util.Log.e("TripViewModel", "ERROR in createTripWithAutoGeneration: ${e.message}", e)
                _creationState.value = TripCreationState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Schedule push notifications for D-7, D-3, D-0
     */
    private fun scheduleNotifications(trip: TripEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nickname = "여행자" // TODO: Get from user profile

        // Calculate notification times
        val startDate = Calendar.getInstance().apply {
            timeInMillis = trip.startDate
        }

        // Schedule D-7 notification
        scheduleNotification(
            alarmManager,
            trip,
            nickname,
            "D-7",
            startDate.timeInMillis - (7 * 24 * 60 * 60 * 1000),
            1000 + trip.hashCode()
        )

        // Schedule D-3 notification
        scheduleNotification(
            alarmManager,
            trip,
            nickname,
            "D-3",
            startDate.timeInMillis - (3 * 24 * 60 * 60 * 1000),
            2000 + trip.hashCode()
        )

        // Schedule D-0 notification (day of trip)
        scheduleNotification(
            alarmManager,
            trip,
            nickname,
            "D-0",
            startDate.timeInMillis,
            3000 + trip.hashCode()
        )
    }

    private fun scheduleNotification(
        alarmManager: AlarmManager,
        trip: TripEntity,
        nickname: String,
        notifType: String,
        triggerTime: Long,
        requestCode: Int
    ) {
        try {
            val intent = Intent("com.travelfoodie.TRIP_NOTIFICATION").apply {
                putExtra("trip_title", trip.title)
                putExtra("notif_type", notifType)
                putExtra("nickname", nickname)
                setPackage(context.packageName)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Only schedule if the time is in the future
            if (triggerTime > System.currentTimeMillis()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    fun resetCreationState() {
        _creationState.value = TripCreationState.Idle
    }

    /**
     * Regenerate attractions and restaurants for an existing trip
     */
    fun regenerateAttractionsAndRestaurants(trip: TripEntity) {
        android.util.Log.d("TripViewModel", "regenerateAttractionsAndRestaurants - tripId: ${trip.tripId}, region: ${trip.regionName}")
        viewModelScope.launch {
            try {
                // Delete old data first
                android.util.Log.d("TripViewModel", "Deleting old attractions...")
                poiRepository.deletePoiByRegionId(trip.tripId)

                android.util.Log.d("TripViewModel", "Deleting old restaurants...")
                restaurantRepository.deleteRestaurantsByRegionId(trip.tripId)

                // Regenerate new data from APIs
                android.util.Log.d("TripViewModel", "Regenerating attractions from ChatGPT + Google Places...")
                val attractions = poiRepository.generateMockAttractions(
                    regionId = trip.tripId,
                    regionName = trip.regionName
                )
                android.util.Log.d("TripViewModel", "Regenerated ${attractions.size} attractions")

                android.util.Log.d("TripViewModel", "Regenerating restaurants...")
                val restaurants = restaurantRepository.createMockRestaurants(
                    regionId = trip.tripId,
                    regionName = trip.regionName,
                    lat = 37.5665,
                    lng = 126.9780
                )
                android.util.Log.d("TripViewModel", "Regenerated ${restaurants.size} restaurants")

                android.util.Log.d("TripViewModel", "Regeneration complete!")
            } catch (e: Exception) {
                android.util.Log.e("TripViewModel", "Error regenerating: ${e.message}", e)
            }
        }
    }
}

/**
 * Tracks the state of trip creation and auto-generation flow
 */
sealed class TripCreationState {
    object Idle : TripCreationState()
    object SavingTrip : TripCreationState()
    object GeneratingAttractions : TripCreationState()
    object GeneratingRestaurants : TripCreationState()
    object SchedulingNotifications : TripCreationState()
    data class Success(
        val attractionsCount: Int,
        val restaurantsCount: Int
    ) : TripCreationState()
    data class Error(val message: String) : TripCreationState()
}
