package com.travelfoodie.feature.attraction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelfoodie.feature.attraction.databinding.FragmentAttractionListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AttractionListFragment : Fragment() {

    private var _binding: FragmentAttractionListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AttractionViewModel by viewModels()
    private lateinit var adapter: AttractionAdapter

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
        
        setupRecyclerView()
        observeAttractions()
    }

    private fun setupRecyclerView() {
        adapter = AttractionAdapter()
        binding.recyclerViewAttractions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AttractionListFragment.adapter
        }
    }

    private fun observeAttractions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.attractions.collect { attractions ->
                adapter.submitList(attractions)
                binding.textViewEmpty.visibility = if (attractions.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
