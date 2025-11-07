package com.travelfoodie.feature.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelfoodie.core.data.local.entity.RestaurantEntity
import com.travelfoodie.core.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    private val _restaurants = MutableStateFlow<List<RestaurantEntity>>(emptyList())
    val restaurants: StateFlow<List<RestaurantEntity>> = _restaurants.asStateFlow()

    fun loadRestaurants(regionId: String) {
        viewModelScope.launch {
            restaurantRepository.getRestaurantsByRegion(regionId).collect { restaurantList ->
                _restaurants.value = restaurantList
            }
        }
    }

    fun getRandomRestaurants(count: Int = 3): List<RestaurantEntity> {
        return _restaurants.value.shuffled().take(count)
    }
}
