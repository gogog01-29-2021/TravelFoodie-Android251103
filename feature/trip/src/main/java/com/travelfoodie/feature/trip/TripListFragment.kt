package com.travelfoodie.feature.trip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelfoodie.feature.trip.databinding.FragmentTripListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TripListFragment : Fragment() {

    private var _binding: FragmentTripListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: TripViewModel by viewModels()
    private lateinit var adapter: TripAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeTrips()
        
        binding.fabAddTrip.setOnClickListener {
            // Navigate to add trip screen
            showAddTripDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = TripAdapter(
            onTripClick = { trip ->
                // Navigate to trip detail
            },
            onTripLongClick = { trip ->
                // Show edit/delete menu
                showTripOptionsDialog(trip)
            }
        )
        
        binding.recyclerViewTrips.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TripListFragment.adapter
        }
    }

    private fun observeTrips() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trips.collect { trips ->
                adapter.submitList(trips)
                binding.textViewEmpty.visibility = if (trips.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showAddTripDialog() {
        // TODO: Implement add trip dialog
    }

    private fun showTripOptionsDialog(trip: com.travelfoodie.core.data.local.entity.TripEntity) {
        // TODO: Implement options dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
