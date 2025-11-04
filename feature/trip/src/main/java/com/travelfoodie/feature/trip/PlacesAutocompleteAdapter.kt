package com.travelfoodie.feature.trip

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.travelfoodie.core.data.remote.GooglePlacesApi
import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Custom adapter for AutoCompleteTextView that provides place suggestions from Google Places API
 */
class PlacesAutocompleteAdapter(
    context: Context,
    private val placesApi: GooglePlacesApi,
    private val apiKey: String
) : ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line), Filterable {

    private val predictions = mutableListOf<PlacePrediction>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    data class PlacePrediction(
        val placeId: String,
        val description: String,
        val lat: Double?,
        val lng: Double?
    )

    override fun getCount(): Int = predictions.size

    override fun getItem(position: Int): String = predictions[position].description

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()

                if (constraint.isNullOrEmpty() || constraint.length < 3) {
                    results.values = emptyList<PlacePrediction>()
                    results.count = 0
                    return results
                }

                Log.d("PlacesAdapter", "Searching for: $constraint")

                // Use CountDownLatch to wait for async operation
                val latch = CountDownLatch(1)
                val fetchedPredictions = mutableListOf<PlacePrediction>()

                scope.launch {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            placesApi.searchPlaces(
                                query = constraint.toString(),
                                apiKey = apiKey,
                                language = "ko"
                            )
                        }

                        Log.d("PlacesAdapter", "Google Places response status: ${response.status}")

                        if (response.status == "OK") {
                            response.results.take(5).forEach { place ->
                                fetchedPredictions.add(
                                    PlacePrediction(
                                        placeId = place.placeId,
                                        description = place.name,
                                        lat = place.geometry?.location?.lat,
                                        lng = place.geometry?.location?.lng
                                    )
                                )
                            }
                            Log.d("PlacesAdapter", "Found ${fetchedPredictions.size} places")
                        } else {
                            Log.e("PlacesAdapter", "Google Places API error: ${response.status}")
                        }
                    } catch (e: Exception) {
                        Log.e("PlacesAdapter", "Failed to fetch predictions: ${e.message}", e)
                    } finally {
                        latch.countDown()
                    }
                }

                // Wait for API response (max 5 seconds)
                latch.await(5, TimeUnit.SECONDS)

                results.values = fetchedPredictions
                results.count = fetchedPredictions.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                predictions.clear()

                if (results != null && results.count > 0) {
                    @Suppress("UNCHECKED_CAST")
                    val resultList = results.values as? List<PlacePrediction> ?: emptyList()
                    predictions.addAll(resultList)
                    Log.d("PlacesAdapter", "Publishing ${predictions.size} results")
                } else {
                    Log.d("PlacesAdapter", "No results to publish")
                }

                notifyDataSetChanged()
            }
        }
    }

    fun getPlaceAtPosition(position: Int): PlacePrediction? {
        return predictions.getOrNull(position)
    }

    override fun clear() {
        predictions.clear()
        notifyDataSetChanged()
    }
}
