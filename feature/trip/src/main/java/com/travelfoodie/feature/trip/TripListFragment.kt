package com.travelfoodie.feature.trip

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.travelfoodie.core.ui.SharedTripViewModel
import com.travelfoodie.core.data.local.entity.TripEntity
import com.travelfoodie.core.data.remote.GooglePlacesApi
import com.travelfoodie.core.data.BuildConfig
import com.travelfoodie.feature.trip.databinding.DialogAddTripBinding
import com.travelfoodie.feature.trip.databinding.FragmentTripListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class TripListFragment : Fragment() {

    private var _binding: FragmentTripListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TripViewModel by viewModels()
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    private lateinit var adapter: TripAdapter

    @Inject
    lateinit var googlePlacesApi: GooglePlacesApi

    // Store selected place info
    private var selectedPlaceName: String? = null
    private var selectedPlaceLat: Double = 37.5665 // Default to Seoul
    private var selectedPlaceLng: Double = 126.9780

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

        android.util.Log.d("TripListFragment", "onViewCreated - SharedViewModel instance: ${sharedViewModel.hashCode()}")

        setupRecyclerView()
        setupCalendar()
        observeTrips()

        binding.fabAddTrip.setOnClickListener {
            // Navigate to add trip screen
            showAddTripDialog()
        }

        // Close details card button
        binding.btnCloseDetails.setOnClickListener {
            binding.cardTripDetails.visibility = View.GONE
        }
    }

    private fun setupCalendar() {
        // Handle date selection on calendar
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.timeInMillis

            // Find trip that includes this date
            val trip = viewModel.trips.value.find { trip ->
                selectedDate >= trip.startDate && selectedDate <= trip.endDate
            }

            if (trip != null) {
                showTripDetails(trip)
            } else {
                binding.cardTripDetails.visibility = View.GONE
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "이 날짜에 예정된 여행이 없습니다",
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showTripDetails(trip: TripEntity) {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        val duration = TimeUnit.MILLISECONDS.toDays(trip.endDate - trip.startDate) + 1

        binding.apply {
            cardTripDetails.visibility = View.VISIBLE
            textTripTitle.text = trip.title
            textTripDates.text = "${dateFormat.format(Date(trip.startDate))} - ${dateFormat.format(Date(trip.endDate))} (${duration}일)"
            textTripRegion.text = trip.regionName
            textTripTheme.text = trip.theme

            // Get members info if available (stored in members field)
            // For now, we'll skip this as we don't have members stored in TripEntity

            btnViewDetails.setOnClickListener {
                // Select this trip - user can then switch to Attractions/Restaurants tab
                viewLifecycleOwner.lifecycleScope.launch {
                    val regions = viewModel.getRegionsForTrip(trip.tripId)
                    val region = regions.firstOrNull()

                    if (region != null) {
                        sharedViewModel.selectTrip(region.regionId, region.name)

                        // Hide the details card
                        binding.cardTripDetails.visibility = View.GONE

                        com.google.android.material.snackbar.Snackbar.make(
                            binding.root,
                            "\"${trip.title}\" 선택됨 - 명소/맛집 탭에서 확인하세요",
                            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = TripAdapter(
            onTripClick = { trip ->
                // Get the regionId for this trip, then select it
                viewLifecycleOwner.lifecycleScope.launch {
                    val regions = viewModel.getRegionsForTrip(trip.tripId)
                    val region = regions.firstOrNull()

                    if (region != null) {
                        android.util.Log.d("TripListFragment", "Selecting trip: ${trip.tripId}, regionId: ${region.regionId}, regionName: ${region.name}")
                        sharedViewModel.selectTrip(region.regionId, region.name)

                        // Show Snackbar with action to view attractions
                        com.google.android.material.snackbar.Snackbar.make(
                            binding.root,
                            "\"${trip.title}\" 선택됨 - 명소/맛집 탭에서 확인하세요",
                            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        android.util.Log.e("TripListFragment", "No region found for trip: ${trip.tripId}")
                        com.google.android.material.snackbar.Snackbar.make(
                            binding.root,
                            "여행 정보를 찾을 수 없습니다",
                            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                        ).setBackgroundTint(
                            resources.getColor(com.google.android.material.R.color.design_default_color_error, null)
                        ).show()
                    }
                }
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

        // Add swipe-to-delete functionality
        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(
            object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
                0,
                androidx.recyclerview.widget.ItemTouchHelper.LEFT or androidx.recyclerview.widget.ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: androidx.recyclerview.widget.RecyclerView,
                    viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                    target: androidx.recyclerview.widget.RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val trip = adapter.currentList[position]

                    // Show confirmation dialog
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("여행 삭제")
                        .setMessage("\"${trip.title}\" 여행을 삭제하시겠습니까?")
                        .setPositiveButton("삭제") { _, _ ->
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewModel.deleteTrip(trip.tripId)
                                com.google.android.material.snackbar.Snackbar.make(
                                    binding.root,
                                    "여행이 삭제되었습니다",
                                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .setNegativeButton("취소") { _, _ ->
                            // Restore item
                            adapter.notifyItemChanged(position)
                        }
                        .setOnCancelListener {
                            // Restore item if dialog is dismissed
                            adapter.notifyItemChanged(position)
                        }
                        .show()
                }
            }
        )
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewTrips)
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
        android.util.Log.d("TripListFragment", "showAddTripDialog called")
        val dialogBinding = DialogAddTripBinding.inflate(layoutInflater)
        var startDateMillis: Long = 0
        var endDateMillis: Long = 0
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Reset selected place for this new dialog
        selectedPlaceName = null
        selectedPlaceLat = 37.5665 // Default to Seoul
        selectedPlaceLng = 126.9780

        // Setup Start Date Picker
        dialogBinding.editStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    startDateMillis = calendar.timeInMillis
                    dialogBinding.editStartDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Setup End Date Picker
        dialogBinding.editEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    endDateMillis = calendar.timeInMillis
                    dialogBinding.editEndDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Setup Google Places Autocomplete for Region
        val regionAutoComplete = dialogBinding.editRegion as AutoCompleteTextView
        val placesAdapter = PlacesAutocompleteAdapter(
            requireContext(),
            googlePlacesApi,
            com.travelfoodie.core.data.BuildConfig.GOOGLE_PLACES_API_KEY
        )
        regionAutoComplete.setAdapter(placesAdapter)
        regionAutoComplete.threshold = 3 // Start suggestions after 3 characters

        android.util.Log.d("TripListFragment", "Google Places Autocomplete setup complete")

        // Handle place selection from dropdown
        regionAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            val selectedPlace = placesAdapter.getPlaceAtPosition(position)
            if (selectedPlace != null) {
                selectedPlaceName = selectedPlace.description
                selectedPlaceLat = selectedPlace.lat ?: 37.5665
                selectedPlaceLng = selectedPlace.lng ?: 126.9780

                android.util.Log.d("TripListFragment", "✅ Place selected: $selectedPlaceName at ($selectedPlaceLat, $selectedPlaceLng)")
            }
        }

        // Create Dialog
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // Cancel Button
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Save Button - THE KEY PART THAT TRIGGERS THE ENTIRE FLOW
        dialogBinding.btnSave.setOnClickListener {
            android.util.Log.d("TripListFragment", "Save button clicked")
            val title = dialogBinding.editTripTitle.text.toString().trim()
            // Use selected place name from autocomplete, or fallback to text input
            val region = selectedPlaceName ?: dialogBinding.editRegion.text.toString().trim()
            val members = dialogBinding.editMembers.text.toString().trim()
            android.util.Log.d("TripListFragment", "Input - title: $title, region: $region, members: $members, coords: ($selectedPlaceLat, $selectedPlaceLng)")

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
                region.isEmpty() -> {
                    Toast.makeText(requireContext(), "여행지를 선택하세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                selectedPlaceName == null && dialogBinding.editRegion.text.toString().trim().isNotEmpty() -> {
                    Toast.makeText(requireContext(), "드롭다운에서 여행지를 선택해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                startDateMillis > endDateMillis -> {
                    Toast.makeText(requireContext(), "출발일이 도착일보다 늦을 수 없습니다", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Create trip entity (simplified - regions and members stored separately in DB)
            val tripId = UUID.randomUUID().toString()
            val trip = TripEntity(
                tripId = tripId,
                userId = "dev_user_001", // TODO: Get from auth
                title = title,
                startDate = startDateMillis,
                endDate = endDateMillis,
                theme = theme,
                regionName = region // Save region name for API regeneration
            )

            android.util.Log.d("TripListFragment", "Calling createTripWithAutoGeneration - tripId: $tripId, region: $region, coords: ($selectedPlaceLat, $selectedPlaceLng)")
            // 🔥 THIS IS THE KEY - Trigger the complete auto-generation flow with actual coordinates
            viewModel.createTripWithAutoGeneration(trip, region, members, selectedPlaceLat, selectedPlaceLng)

            // Reset selected place for next dialog
            selectedPlaceName = null
            selectedPlaceLat = 37.5665
            selectedPlaceLng = 126.9780

            dialog.dismiss()
            android.util.Log.d("TripListFragment", "Showing creation progress")
            showCreationProgress(region)
        }

        dialog.show()
    }

    /**
     * Shows progress of the auto-generation flow and navigates to attractions on success
     */
    private fun showCreationProgress(regionName: String) {
        android.util.Log.d("TripListFragment", "showCreationProgress - observing creation state")
        var progressDialog: AlertDialog? = null

        viewLifecycleOwner.lifecycleScope.launch {
            android.util.Log.d("TripListFragment", "Coroutine launched to observe creationState")
            viewModel.creationState.collect { state ->
                android.util.Log.d("TripListFragment", "CreationState changed: ${state::class.simpleName}")
                when (state) {
                    is TripCreationState.SavingTrip -> {
                        progressDialog = MaterialAlertDialogBuilder(requireContext())
                            .setTitle("여행 생성 중...")
                            .setMessage("여행을 저장하고 있습니다")
                            .setCancelable(false)
                            .show()
                    }
                    is TripCreationState.GeneratingAttractions -> {
                        progressDialog?.setMessage("AI가 명소를 추천하고 있습니다... (5개)")
                    }
                    is TripCreationState.GeneratingRestaurants -> {
                        progressDialog?.setMessage("맛집 리스트를 생성하고 있습니다... (10개)")
                    }
                    is TripCreationState.SchedulingNotifications -> {
                        progressDialog?.setMessage("알림을 설정하고 있습니다...")
                    }
                    is TripCreationState.Success -> {
                        progressDialog?.dismiss()

                        // 🔗 Set selected trip using regionId from the Success state
                        android.util.Log.d("TripListFragment", "Setting selected trip - regionId: ${state.regionId}, regionName: $regionName")
                        sharedViewModel.selectTrip(state.regionId, regionName)

                        // Show success Snackbar with action button
                        com.google.android.material.snackbar.Snackbar.make(
                            binding.root,
                            "✅ 여행 생성 완료! 명소 ${state.attractionsCount}개, 맛집 ${state.restaurantsCount}개",
                            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                        ).setAction("확인하기") {
                            // User can tap to manually navigate, but it's optional
                        }.setBackgroundTint(
                            resources.getColor(com.google.android.material.R.color.design_default_color_secondary, null)
                        ).setActionTextColor(
                            resources.getColor(android.R.color.white, null)
                        ).show()

                        viewModel.resetCreationState()
                    }
                    is TripCreationState.Error -> {
                        progressDialog?.dismiss()

                        // Show error Snackbar
                        com.google.android.material.snackbar.Snackbar.make(
                            binding.root,
                            "❌ 오류: ${state.message}",
                            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                        ).setBackgroundTint(
                            resources.getColor(com.google.android.material.R.color.design_default_color_error, null)
                        ).show()

                        viewModel.resetCreationState()
                    }
                    is TripCreationState.Idle -> {
                        // Do nothing
                    }
                }
            }
        }
    }

    private fun showTripOptionsDialog(trip: com.travelfoodie.core.data.local.entity.TripEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(trip.title)
            .setItems(arrayOf("선택하기", "명소/맛집 재생성", "삭제")) { _, which ->
                when (which) {
                    0 -> {
                        // Select trip - need to get regionId first
                        viewLifecycleOwner.lifecycleScope.launch {
                            val regions = viewModel.getRegionsForTrip(trip.tripId)
                            val region = regions.firstOrNull()

                            if (region != null) {
                                sharedViewModel.selectTrip(region.regionId, region.name)
                                com.google.android.material.snackbar.Snackbar.make(
                                    binding.root,
                                    "\"${trip.title}\" 선택됨 - 명소/맛집 탭에서 확인하세요",
                                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                                ).show()
                            } else {
                                com.google.android.material.snackbar.Snackbar.make(
                                    binding.root,
                                    "여행 정보를 찾을 수 없습니다",
                                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                                ).setBackgroundTint(
                                    resources.getColor(com.google.android.material.R.color.design_default_color_error, null)
                                ).show()
                            }
                        }
                    }
                    1 -> {
                        // Regenerate attractions and restaurants
                        if (trip.regionName.isEmpty()) {
                            com.google.android.material.snackbar.Snackbar.make(
                                binding.root,
                                "지역 정보가 없어 재생성할 수 없습니다",
                                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                            ).setBackgroundTint(
                                resources.getColor(com.google.android.material.R.color.design_default_color_error, null)
                            ).show()
                        } else {
                            viewModel.regenerateAttractionsAndRestaurants(trip)
                            com.google.android.material.snackbar.Snackbar.make(
                                binding.root,
                                "🔄 명소와 맛집을 재생성하고 있습니다...",
                                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                    2 -> {
                        // Delete trip
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("여행 삭제")
                            .setMessage("\"${trip.title}\" 여행을 삭제하시겠습니까?")
                            .setPositiveButton("삭제") { _, _ ->
                                viewModel.deleteTrip(trip)
                                com.google.android.material.snackbar.Snackbar.make(
                                    binding.root,
                                    "\"${trip.title}\" 여행이 삭제되었습니다",
                                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                                ).show()
                            }
                            .setNegativeButton("취소", null)
                            .show()
                    }
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
