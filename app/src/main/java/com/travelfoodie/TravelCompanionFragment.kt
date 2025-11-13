package com.travelfoodie

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.travelfoodie.core.data.local.AppDatabase
import com.travelfoodie.core.data.local.entity.FriendEntity
import com.travelfoodie.databinding.FragmentTravelCompanionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class TravelCompanionFragment : Fragment() {

    private var _binding: FragmentTravelCompanionBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var auth: FirebaseAuth

    private lateinit var database: AppDatabase
    private lateinit var friendAdapter: FriendAdapter
    private var currentUserId: String? = null

    // Contacts permission launcher
    private val contactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openContactPicker()
        } else {
            Toast.makeText(requireContext(), "연락처 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    // Contact picker launcher
    private val contactPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { contactUri ->
        contactUri?.let { uri ->
            lifecycleScope.launch {
                val contact = getContactDetails(uri.toString())
                contact?.let { savePhoneContact(it) }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTravelCompanionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getInstance(requireContext())

        // Get current user ID
        val sharedPrefs = requireContext().getSharedPreferences("TravelFoodie", Context.MODE_PRIVATE)
        currentUserId = auth.currentUser?.uid ?: sharedPrefs.getString("user_id", null)

        if (currentUserId == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        setupRecyclerView()
        setupClickListeners()
        observeFriends()
    }

    private fun setupRecyclerView() {
        friendAdapter = FriendAdapter(
            onDeleteClick = { friend ->
                deleteFriend(friend)
            }
        )

        binding.recyclerFriends.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendAdapter
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAddFromContacts.setOnClickListener {
            checkContactsPermissionAndPick()
        }

        binding.btnAddFromKakao.setOnClickListener {
            addFromKakaoFriends()
        }
    }

    private fun observeFriends() {
        currentUserId?.let { userId ->
            lifecycleScope.launch {
                database.friendDao().getFriendsByUser(userId).collect { friends ->
                    if (friends.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.recyclerFriends.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.recyclerFriends.visibility = View.VISIBLE
                        friendAdapter.submitList(friends)
                    }
                }
            }
        }
    }

    private fun checkContactsPermissionAndPick() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                openContactPicker()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                Toast.makeText(
                    requireContext(),
                    "연락처 접근 권한이 필요합니다",
                    Toast.LENGTH_LONG
                ).show()
                contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
            else -> {
                contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun openContactPicker() {
        try {
            contactPickerLauncher.launch(null)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "연락처를 열 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun getContactDetails(contactUri: String): ContactInfo? = withContext(Dispatchers.IO) {
        val contentResolver: ContentResolver = requireContext().contentResolver
        var cursor: Cursor? = null

        try {
            cursor = contentResolver.query(
                android.net.Uri.parse(contactUri),
                null,
                null,
                null,
                null
            )

            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)

                val name = if (nameIndex >= 0) cursor.getString(nameIndex) else "Unknown"
                val contactId = if (idIndex >= 0) cursor.getString(idIndex) else return@withContext null

                // Get phone number
                val phoneCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(contactId),
                    null
                )

                var phoneNumber: String? = null
                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    val phoneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    phoneNumber = if (phoneIndex >= 0) phoneCursor.getString(phoneIndex) else null
                }
                phoneCursor?.close()

                if (phoneNumber != null) {
                    ContactInfo(name, phoneNumber)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("TravelCompanionFragment", "Error reading contact", e)
            null
        } finally {
            cursor?.close()
        }
    }

    private fun savePhoneContact(contact: ContactInfo) {
        currentUserId?.let { userId ->
            lifecycleScope.launch {
                try {
                    // Check if contact already exists
                    val existing = database.friendDao().getFriendByContact(userId, contact.phoneNumber)
                    if (existing != null) {
                        Toast.makeText(requireContext(), "이미 추가된 연락처입니다", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val friend = FriendEntity(
                        userId = userId,
                        friendUserId = null,
                        name = contact.name,
                        contactType = "phone",
                        contactValue = contact.phoneNumber
                    )

                    database.friendDao().insertFriend(friend)
                    Toast.makeText(requireContext(), "${contact.name} 추가됨", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "친구 추가 실패", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("TravelCompanionFragment", "Error saving friend", e)
                }
            }
        }
    }

    private fun addFromKakaoFriends() {
        // Note: Kakao Friends API requires additional permissions and setup
        // For now, show a message that this feature is coming soon
        Toast.makeText(
            requireContext(),
            "카카오 친구 추가 기능은 준비 중입니다",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun deleteFriend(friend: FriendEntity) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("동반자 삭제")
            .setMessage("${friend.name}을(를) 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                lifecycleScope.launch {
                    try {
                        database.friendDao().deleteFriend(friend)
                        Toast.makeText(requireContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "삭제 실패", Toast.LENGTH_SHORT).show()
                        android.util.Log.e("TravelCompanionFragment", "Error deleting friend", e)
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class ContactInfo(
        val name: String,
        val phoneNumber: String
    )
}

// RecyclerView Adapter
class FriendAdapter(
    private val onDeleteClick: (FriendEntity) -> Unit
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    private var friends = listOf<FriendEntity>()

    fun submitList(newFriends: List<FriendEntity>) {
        friends = newFriends
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(friends[position])
    }

    override fun getItemCount() = friends.size

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val friendName: android.widget.TextView = itemView.findViewById(R.id.text_friend_name)
        private val friendContact: android.widget.TextView = itemView.findViewById(R.id.text_friend_contact)
        private val contactType: android.widget.TextView = itemView.findViewById(R.id.text_contact_type)
        private val deleteButton: android.widget.ImageButton = itemView.findViewById(R.id.btn_delete)

        fun bind(friend: FriendEntity) {
            friendName.text = friend.name
            friendContact.text = friend.contactValue

            contactType.text = when (friend.contactType) {
                "phone" -> "전화번호"
                "kakao" -> "카카오톡"
                "email" -> "이메일"
                else -> friend.contactType
            }

            deleteButton.setOnClickListener {
                onDeleteClick(friend)
            }
        }
    }
}
