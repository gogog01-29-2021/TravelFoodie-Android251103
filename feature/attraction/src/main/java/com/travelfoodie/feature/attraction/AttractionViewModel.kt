package com.travelfoodie.feature.attraction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelfoodie.core.data.local.entity.PoiEntity
import com.travelfoodie.core.data.repository.PoiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttractionViewModel @Inject constructor(
    private val poiRepository: PoiRepository
) : ViewModel() {

    private val _attractions = MutableStateFlow<List<PoiEntity>>(emptyList())
    val attractions: StateFlow<List<PoiEntity>> = _attractions.asStateFlow()

    fun loadAttractions(regionId: String) {
        viewModelScope.launch {
            android.util.Log.d("AttractionViewModel", "Loading attractions for regionId: $regionId")
            poiRepository.getPoisByRegion(regionId).collect { pois ->
                android.util.Log.d("AttractionViewModel", "Received ${pois.size} attractions")
                pois.forEach {
                    android.util.Log.d("AttractionViewModel", "  - ${it.name} (regionId: ${it.regionId})")
                }
                _attractions.value = pois
            }
        }
    }
}
