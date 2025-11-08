package com.travelfoodie.feature.board

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.travelfoodie.core.ui.SharedTripViewModel
import com.travelfoodie.feature.board.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupMessageInput()
        observeMessages()
        observeSelectedTrip()
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter()
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChatFragment.adapter
        }
    }

    private fun setupMessageInput() {
        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.sendMessage(message)
                    binding.editTextMessage.text?.clear()
                }
            } else {
                Snackbar.make(binding.root, "메시지를 입력하세요", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                adapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    binding.recyclerViewChat.smoothScrollToPosition(messages.size - 1)
                }
                binding.textViewEmpty.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun observeSelectedTrip() {
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.selectedTripId.collect { tripId ->
                if (tripId != null) {
                    viewModel.loadMessages(tripId)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
