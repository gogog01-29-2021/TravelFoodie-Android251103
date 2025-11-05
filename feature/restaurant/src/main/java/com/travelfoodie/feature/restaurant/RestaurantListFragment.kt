package com.travelfoodie.feature.restaurant

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelfoodie.core.data.local.entity.RestaurantEntity
import com.travelfoodie.core.ui.SharedTripViewModel
import com.travelfoodie.feature.restaurant.databinding.FragmentRestaurantListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RestaurantListFragment : Fragment() {

    private var _binding: FragmentRestaurantListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RestaurantViewModel by viewModels()
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    private lateinit var adapter: RestaurantAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRestaurantListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeRestaurants()
        observeSelectedTrip()
    }

    /**
     * 🔗 CONNECTED: Observes SharedTripViewModel for trip selection
     * When TripListFragment creates/selects a trip, this automatically loads restaurants
     */
    private fun observeSelectedTrip() {
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.selectedTripId.collect { tripId ->
                if (tripId != null) {
                    // Auto-load restaurants for the selected trip
                    android.util.Log.d("RestaurantListFragment", "Loading restaurants for tripId: $tripId")
                    viewModel.loadRestaurants(tripId)
                } else {
                    android.util.Log.d("RestaurantListFragment", "No trip selected")
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = RestaurantAdapter(
            onRestaurantClick = { restaurant ->
                // Google Maps will open automatically in the adapter
            },
            onShareClick = { restaurant -> shareRestaurant(restaurant) }
        )

        binding.recyclerViewRestaurants.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RestaurantListFragment.adapter
        }
    }

    private fun shareRestaurant(restaurant: RestaurantEntity) {
        val shareText = """
            🍽️ ${restaurant.name}

            📍 카테고리: ${restaurant.category}
            ⭐ 평점: ${restaurant.rating}
            ${restaurant.distance?.let { "📏 거리: %.1f km".format(it) } ?: ""}

            TravelFoodie에서 공유됨
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "${restaurant.name} - 맛집 추천")
        }

        startActivity(Intent.createChooser(shareIntent, "맛집 공유하기"))
    }

    private fun observeRestaurants() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.restaurants.collect { restaurants ->
                adapter.submitList(restaurants)
                binding.textViewEmpty.visibility = if (restaurants.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
