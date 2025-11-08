package com.travelfoodie.feature.attraction

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelfoodie.core.data.local.entity.PoiEntity
import com.travelfoodie.core.ui.SharedTripViewModel
import com.travelfoodie.feature.attraction.databinding.FragmentAttractionListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class AttractionListFragment : Fragment() {

    private var _binding: FragmentAttractionListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttractionViewModel by viewModels()
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    private lateinit var adapter: AttractionAdapter

    // Text-to-Speech
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttractionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        android.util.Log.d("AttractionListFragment", "onViewCreated - SharedViewModel instance: ${sharedViewModel.hashCode()}")

        initializeTextToSpeech()
        setupRecyclerView()
        observeAttractions()
        observeSelectedTrip()
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.KOREAN)
                isTtsInitialized = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED

                if (isTtsInitialized) {
                    com.google.android.material.snackbar.Snackbar.make(
                        binding.root,
                        "명소 설명을 클릭하면 읽어드립니다",
                        com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun speakAttraction(poi: PoiEntity) {
        if (!isTtsInitialized) {
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                "음성 읽기 기능을 사용할 수 없습니다",
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        val textToSpeak = "${poi.name}. ${poi.description ?: "설명이 없습니다."}"
        textToSpeech?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)

        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "🔊 음성으로 읽는 중...",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    /**
     * 🔗 CONNECTED: Observes SharedTripViewModel for trip selection
     * When TripListFragment creates/selects a trip, this automatically loads attractions
     */
    private fun observeSelectedTrip() {
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.selectedTripId.collect { tripId ->
                if (tripId != null) {
                    // Auto-load attractions for the selected trip
                    android.util.Log.d("AttractionListFragment", "Loading attractions for tripId: $tripId")
                    viewModel.loadAttractions(tripId)
                } else {
                    android.util.Log.d("AttractionListFragment", "No trip selected")
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AttractionAdapter(
            onShareClick = { poi -> shareAttraction(poi) },
            onSpeakClick = { poi -> speakAttraction(poi) }
        )
        binding.recyclerViewAttractions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AttractionListFragment.adapter
        }
    }

    private fun shareAttraction(poi: PoiEntity) {
        val shareText = """
            🎯 ${poi.name}

            📍 카테고리: ${poi.category}
            ⭐ 평점: ${poi.rating}
            ${poi.description?.let { "\n📝 $it" } ?: ""}

            TravelFoodie에서 공유됨
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "${poi.name} - 명소 추천")
        }

        startActivity(Intent.createChooser(shareIntent, "명소 공유하기"))
    }

    private fun observeAttractions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.attractions.collect { attractions ->
                android.util.Log.d("AttractionListFragment", "Displaying ${attractions.size} attractions")
                adapter.submitList(attractions)
                binding.textViewEmpty.visibility = if (attractions.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _binding = null
    }
}
