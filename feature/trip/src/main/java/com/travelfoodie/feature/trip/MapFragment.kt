package com.travelfoodie.feature.trip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.travelfoodie.core.data.remote.GooglePlacesApi
import com.travelfoodie.core.ui.SharedTripViewModel
import com.travelfoodie.feature.trip.databinding.FragmentMapBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedTripViewModel by activityViewModels()

    @Inject
    lateinit var googlePlacesApi: GooglePlacesApi

    private var googleMap: GoogleMap? = null
    private var selectedMarker: Marker? = null
    private var selectedLocationName: String? = null
    private var selectedLatLng: LatLng? = null

    private lateinit var placesAdapter: PlacesAutocompleteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Setup search autocomplete
        setupSearchAutocomplete()

        // Handle "Add Trip Here" button
        binding.btnAddTripHere.setOnClickListener {
            if (selectedLocationName != null && selectedLatLng != null) {
                showAddTripDialog(selectedLocationName!!, selectedLatLng!!)
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Default location (Seoul)
        val defaultLocation = LatLng(37.5665, 126.9780)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))

        // Enable location controls
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        googleMap?.uiSettings?.isMyLocationButtonEnabled = true

        // Handle map clicks
        googleMap?.setOnMapClickListener { latLng ->
            onLocationSelected(latLng, "선택한 위치")
        }
    }

    private fun setupSearchAutocomplete() {
        placesAdapter = PlacesAutocompleteAdapter(
            requireContext(),
            googlePlacesApi,
            com.travelfoodie.core.data.BuildConfig.GOOGLE_PLACES_API_KEY
        )
        binding.searchLocation.setAdapter(placesAdapter)
        binding.searchLocation.threshold = 3

        // Handle place selection from search
        binding.searchLocation.setOnItemClickListener { parent, _, position, _ ->
            val selectedPlace = placesAdapter.getPlaceAtPosition(position)
            if (selectedPlace != null) {
                val latLng = LatLng(
                    selectedPlace.lat ?: 37.5665,
                    selectedPlace.lng ?: 126.9780
                )
                onLocationSelected(latLng, selectedPlace.description)

                // Move camera to selected location
                googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, 12f)
                )

                // Clear search text
                binding.searchLocation.setText("")
            }
        }
    }

    private fun onLocationSelected(latLng: LatLng, locationName: String) {
        // Remove previous marker
        selectedMarker?.remove()

        // Add new marker
        selectedMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(locationName)
        )

        // Store selected location
        selectedLocationName = locationName
        selectedLatLng = latLng

        // Show selected location card
        binding.cardSelectedLocation.visibility = View.VISIBLE
        binding.textSelectedLocation.text = locationName
        binding.textSelectedCoordinates.text = "${String.format("%.4f", latLng.latitude)}, ${String.format("%.4f", latLng.longitude)}"

        android.util.Log.d("MapFragment", "Location selected: $locationName at ($latLng)")
    }

    private fun showAddTripDialog(locationName: String, latLng: LatLng) {
        val dialogBinding = com.travelfoodie.feature.trip.databinding.DialogAddTripBinding.inflate(layoutInflater)
        var startDateMillis: Long = 0
        var endDateMillis: Long = 0
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        // Pre-fill the region with selected location
        dialogBinding.editRegion.setText(locationName)
        dialogBinding.editRegion.isEnabled = false // Disable editing

        // Setup Start Date Picker
        dialogBinding.editStartDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    startDateMillis = calendar.timeInMillis
                    dialogBinding.editStartDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Setup End Date Picker
        dialogBinding.editEndDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    endDateMillis = calendar.timeInMillis
                    dialogBinding.editEndDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Create Dialog
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // Cancel Button
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Save Button
        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.editTripTitle.text.toString().trim()
            val members = dialogBinding.editMembers.text.toString().trim()

            // Get selected theme
            val theme = when (dialogBinding.chipGroupTheme.checkedChipId) {
                R.id.chip_active -> "액티브"
                R.id.chip_culture -> "문화"
                R.id.chip_relaxation -> "휴식"
                R.id.chip_shopping -> "쇼핑"
                R.id.chip_food -> "맛집 투어"
                else -> "액티브"
            }

            // Validation
            when {
                title.isEmpty() -> {
                    Toast.makeText(requireContext(), "여행 제목을 입력하세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                startDateMillis == 0L -> {
                    Toast.makeText(requireContext(), "출발일을 선택하세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                endDateMillis == 0L -> {
                    Toast.makeText(requireContext(), "도착일을 선택하세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                startDateMillis > endDateMillis -> {
                    Toast.makeText(requireContext(), "출발일이 도착일보다 늦을 수 없습니다", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Create trip with map-selected coordinates
            val tripId = java.util.UUID.randomUUID().toString()
            val trip = com.travelfoodie.core.data.local.entity.TripEntity(
                tripId = tripId,
                userId = "dev_user_001",
                title = title,
                startDate = startDateMillis,
                endDate = endDateMillis,
                theme = theme,
                regionName = locationName
            )

            // Create trip using TripViewModel
            viewLifecycleOwner.lifecycleScope.launch {
                // Get TripViewModel from parent activity
                val tripViewModel = (requireActivity() as? androidx.appcompat.app.AppCompatActivity)
                    ?.let { androidx.lifecycle.ViewModelProvider(it)[TripViewModel::class.java] }

                if (tripViewModel != null) {
                    android.util.Log.d("MapFragment", "Creating trip from map: $locationName at (${latLng.latitude}, ${latLng.longitude})")
                    tripViewModel.createTripWithAutoGeneration(
                        trip,
                        locationName,
                        members,
                        latLng.latitude,
                        latLng.longitude
                    )

                    dialog.dismiss()

                    Toast.makeText(
                        requireContext(),
                        "여행을 생성하고 있습니다...\n여행 탭에서 확인하세요!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(requireContext(), "오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
