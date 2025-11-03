package com.travelfoodie.feature.trip

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.travelfoodie.core.ui.SharedTripViewModel
import com.travelfoodie.core.data.local.entity.TripEntity
import com.travelfoodie.feature.trip.databinding.DialogAddTripBinding
import com.travelfoodie.feature.trip.databinding.FragmentTripListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@AndroidEntryPoint
class TripListFragment : Fragment() {

    private var _binding: FragmentTripListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TripViewModel by viewModels()
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
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
                // Select trip and load its attractions/restaurants via SharedViewModel
                val regionName = "서울" // TODO: Get actual region from trip's regions
                sharedViewModel.selectTrip(trip.tripId, regionName)

                Toast.makeText(
                    requireContext(),
                    "\"${trip.title}\" 여행을 선택했습니다",
                    Toast.LENGTH_SHORT
                ).show()
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
        val dialogBinding = DialogAddTripBinding.inflate(layoutInflater)
        var startDateMillis: Long = 0
        var endDateMillis: Long = 0
        var createdTripId: String? = null
        var createdRegionName: String? = null
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
            val title = dialogBinding.editTripTitle.text.toString().trim()
            val region = dialogBinding.editRegion.text.toString().trim()
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
                region.isEmpty() -> {
                    Toast.makeText(requireContext(), "여행지를 입력하세요", Toast.LENGTH_SHORT).show()
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
                theme = theme
            )

            // Store for navigation after success
            createdTripId = tripId
            createdRegionName = region

            // 🔥 THIS IS THE KEY - Trigger the complete auto-generation flow
            viewModel.createTripWithAutoGeneration(trip, region, members)

            dialog.dismiss()
            showCreationProgress(createdTripId, createdRegionName)
        }

        dialog.show()
    }

    /**
     * Shows progress of the auto-generation flow and navigates to attractions on success
     */
    private fun showCreationProgress(tripId: String?, regionName: String?) {
        var progressDialog: AlertDialog? = null

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.creationState.collect { state ->
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
                        Toast.makeText(
                            requireContext(),
                            "✅ 여행 생성 완료!\n명소 ${state.attractionsCount}개, 맛집 ${state.restaurantsCount}개 생성됨",
                            Toast.LENGTH_LONG
                        ).show()

                        // 🔗 STEP 1 COMPLETE: Set selected trip in shared ViewModel
                        if (tripId != null && regionName != null) {
                            sharedViewModel.selectTrip(tripId, regionName)
                        }

                        // 🔗 STEP 2: Navigate to attractions tab using bottom nav
                        // User will see the generated attractions when they switch tabs

                        viewModel.resetCreationState()
                    }
                    is TripCreationState.Error -> {
                        progressDialog?.dismiss()
                        Toast.makeText(requireContext(), "오류: ${state.message}", Toast.LENGTH_SHORT).show()
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
        // TODO: Implement options dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
