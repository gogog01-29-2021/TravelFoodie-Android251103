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
import com.travelfoodie.core.ui.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
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
    fun createTripWithAutoGeneration(
        trip: TripEntity,
        regionName: String,
        members: String,
        lat: Double = 37.5665, // Default to Seoul if not provided
        lng: Double = 126.9780
    ) {
        android.util.Log.d("TripViewModel", "createTripWithAutoGeneration START - tripId: ${trip.tripId}, coords: ($lat, $lng)")
        viewModelScope.launch {
            try {
                android.util.Log.d("TripViewModel", "Setting state: SavingTrip")
                _creationState.value = TripCreationState.SavingTrip

                // 1. Save trip to database
                android.util.Log.d("TripViewModel", "Inserting trip into database")
                tripRepository.insertTrip(trip)
                android.util.Log.d("TripViewModel", "Trip inserted successfully")

                // 2. Create Region entity with actual coordinates from Google Places
                val regionId = UUID.randomUUID().toString()
                val region = com.travelfoodie.core.data.local.entity.RegionEntity(
                    regionId = regionId,
                    tripId = trip.tripId,
                    name = regionName,
                    lat = lat, // Use actual coordinates from Google Places Autocomplete
                    lng = lng,
                    order = 0
                )
                android.util.Log.d("TripViewModel", "Creating region: $regionId for trip: ${trip.tripId} at ($lat, $lng)")
                tripRepository.insertRegion(region)
                android.util.Log.d("TripViewModel", "Region created successfully")

                // 3. Auto-generate 5 AI attractions for the region with user preferences
                android.util.Log.d("TripViewModel", "Setting state: GeneratingAttractions")
                _creationState.value = TripCreationState.GeneratingAttractions
                android.util.Log.d("TripViewModel", "Generating attractions for regionId: $regionId, regionName: $regionName, theme: ${trip.theme}, members: ${trip.members}")
                val attractions = poiRepository.generateMockAttractions(
                    regionId = regionId,  // Use actual regionId
                    regionName = regionName,
                    theme = trip.theme,
                    members = trip.members,
                    startDate = trip.startDate,
                    endDate = trip.endDate,
                    lat = lat,
                    lng = lng
                )
                android.util.Log.d("TripViewModel", "Generated ${attractions.size} attractions for regionId: $regionId")

                // 4. Auto-generate 10 restaurants for the region using actual coordinates and preferences
                android.util.Log.d("TripViewModel", "Setting state: GeneratingRestaurants")
                _creationState.value = TripCreationState.GeneratingRestaurants
                android.util.Log.d("TripViewModel", "Generating restaurants for regionId: $regionId at ($lat, $lng), members: ${trip.members}")
                val restaurants = restaurantRepository.createMockRestaurants(
                    regionId = regionId,  // Use actual regionId
                    regionName = regionName,
                    theme = trip.theme,
                    members = trip.members,
                    lat = lat, // Use actual coordinates from Google Places
                    lng = lng
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
                    regionId = regionId,
                    attractionsCount = attractions.size,
                    restaurantsCount = restaurants.size
                )
                android.util.Log.d("TripViewModel", "SUCCESS - regionId: $regionId, attractionsCount: ${attractions.size}, restaurantsCount: ${restaurants.size}")

                // 6. Show immediate notification about trip creation
                android.util.Log.d("TripViewModel", "Showing trip creation notification")
                NotificationHelper.showTripCreatedNotification(
                    context = context,
                    tripTitle = trip.title,
                    attractionCount = attractions.size,
                    restaurantCount = restaurants.size
                )
                android.util.Log.d("TripViewModel", "Trip creation notification displayed")

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

    suspend fun getRegionsForTrip(tripId: String): List<com.travelfoodie.core.data.local.entity.RegionEntity> {
        return tripRepository.getRegionsByTrip(tripId).first()
    }

    /**
     * Regenerate attractions and restaurants for an existing trip
     */
    fun regenerateAttractionsAndRestaurants(trip: TripEntity) {
        android.util.Log.d("TripViewModel", "regenerateAttractionsAndRestaurants - tripId: ${trip.tripId}, region: ${trip.regionName}")
        viewModelScope.launch {
            try {
                // Get existing region for this trip (should exist)
                val existingRegion = tripRepository.getRegionsByTrip(trip.tripId).first().firstOrNull()

                val regionId = if (existingRegion != null) {
                    android.util.Log.d("TripViewModel", "Using existing regionId: ${existingRegion.regionId}")
                    existingRegion.regionId
                } else {
                    // Create new region if doesn't exist (shouldn't happen, but safe fallback)
                    val newRegionId = UUID.randomUUID().toString()
                    val newRegion = com.travelfoodie.core.data.local.entity.RegionEntity(
                        regionId = newRegionId,
                        tripId = trip.tripId,
                        name = trip.regionName,
                        lat = 37.5665,
                        lng = 126.9780,
                        order = 0
                    )
                    android.util.Log.d("TripViewModel", "Creating new regionId: $newRegionId")
                    tripRepository.insertRegion(newRegion)
                    newRegionId
                }

                // Delete old data first
                android.util.Log.d("TripViewModel", "Deleting old attractions...")
                poiRepository.deletePoiByRegionId(regionId)

                android.util.Log.d("TripViewModel", "Deleting old restaurants...")
                restaurantRepository.deleteRestaurantsByRegionId(regionId)

                // Regenerate new data from APIs
                android.util.Log.d("TripViewModel", "Regenerating attractions from ChatGPT + Google Places...")
                val attractions = poiRepository.generateMockAttractions(
                    regionId = regionId,
                    regionName = trip.regionName
                )
                android.util.Log.d("TripViewModel", "Regenerated ${attractions.size} attractions")

                android.util.Log.d("TripViewModel", "Regenerating restaurants...")
                val restaurants = restaurantRepository.createMockRestaurants(
                    regionId = regionId,
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
        val regionId: String,
        val attractionsCount: Int,
        val restaurantsCount: Int
    ) : TripCreationState()
    data class Error(val message: String) : TripCreationState()
}
