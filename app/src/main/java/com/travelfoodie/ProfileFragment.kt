package com.travelfoodie

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.travelfoodie.databinding.FragmentProfileBinding
import com.travelfoodie.ocr.ReceiptOcrHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var auth: FirebaseAuth

    private var textToSpeech: TextToSpeech? = null
    private lateinit var ocrHelper: ReceiptOcrHelper
    private var selectedImageUri: Uri? = null
    private var lastOcrResult: String? = null

    // STT launcher
    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) ?: ""
            Toast.makeText(requireContext(), "인식된 텍스트: $spokenText", Toast.LENGTH_LONG).show()

            // Optionally speak it back using TTS
            textToSpeech?.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // Image picker for OCR
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            performOcr(it)
        }
    }

    // Camera permission for OCR
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pickImageLauncher.launch("image/*")
        } else {
            Toast.makeText(requireContext(), "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    // Audio permission for STT
    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startSpeechRecognition()
        } else {
            Toast.makeText(requireContext(), "마이크 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize TTS
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.KOREAN
            }
        }

        // Initialize OCR Helper
        ocrHelper = ReceiptOcrHelper(requireContext())

        val sharedPrefs = requireContext().getSharedPreferences("TravelFoodie", Context.MODE_PRIVATE)
        val loginProvider = sharedPrefs.getString("login_provider", null)

        // Check login status
        val isLoggedIn = auth.currentUser != null || loginProvider != null

        if (isLoggedIn) {
            showProfileContent()
            loadUserInfo(loginProvider)
        } else {
            showGuestMode()
        }

        setupClickListeners()
    }

    private fun showProfileContent() {
        binding.apply {
            scrollView.visibility = View.VISIBLE
            guestModeOverlay.visibility = View.GONE
        }
    }

    private fun showGuestMode() {
        binding.apply {
            scrollView.visibility = View.GONE
            guestModeOverlay.visibility = View.VISIBLE
        }
    }

    private fun loadUserInfo(provider: String?) {
        when (provider) {
            "google" -> loadGoogleUserInfo()
            "kakao" -> loadKakaoUserInfo()
            "naver" -> loadNaverUserInfo()
            else -> {
                // Check Firebase user
                auth.currentUser?.let { user ->
                    binding.apply {
                        textUserName.text = user.displayName ?: "사용자"
                        textUserEmail.text = user.email ?: ""
                        textLoginProvider.text = "Google 계정"
                    }
                }
            }
        }
    }

    private fun loadGoogleUserInfo() {
        auth.currentUser?.let { user ->
            binding.apply {
                textUserName.text = user.displayName ?: "사용자"
                textUserEmail.text = user.email ?: ""
                textLoginProvider.text = "Google 계정"
            }
        }
    }

    private fun loadKakaoUserInfo() {
        val sharedPrefs = requireContext().getSharedPreferences("TravelFoodie", Context.MODE_PRIVATE)
        binding.apply {
            textUserName.text = sharedPrefs.getString("user_name", "사용자")
            textUserEmail.text = sharedPrefs.getString("user_email", "")
            textLoginProvider.text = "Kakao 계정"
        }
    }

    private fun loadNaverUserInfo() {
        val sharedPrefs = requireContext().getSharedPreferences("TravelFoodie", Context.MODE_PRIVATE)
        binding.apply {
            textUserName.text = sharedPrefs.getString("user_name", "사용자")
            textUserEmail.text = sharedPrefs.getString("user_email", "")
            textLoginProvider.text = "Naver 계정"
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnEditProfile.setOnClickListener {
                Toast.makeText(requireContext(), "프로필 편집 기능은 준비 중입니다", Toast.LENGTH_SHORT).show()
            }

            btnNotifications.setOnClickListener {
                Toast.makeText(requireContext(), "알림 설정 기능은 준비 중입니다", Toast.LENGTH_SHORT).show()
            }

            btnPrivacy.setOnClickListener {
                Toast.makeText(requireContext(), "개인정보처리방침", Toast.LENGTH_SHORT).show()
            }

            btnTerms.setOnClickListener {
                Toast.makeText(requireContext(), "이용약관", Toast.LENGTH_SHORT).show()
            }

            // Features - logged in mode
            btnStt.setOnClickListener { handleSttClick() }
            btnTts.setOnClickListener { handleTtsClick() }
            btnOcr.setOnClickListener { handleOcrClick() }

            // Features - guest mode
            btnGuestStt.setOnClickListener { handleSttClick() }
            btnGuestTts.setOnClickListener { handleTtsClick() }
            btnGuestOcr.setOnClickListener { handleOcrClick() }

            btnSignout.setOnClickListener {
                signOut()
            }

            btnGoToLogin.setOnClickListener {
                navigateToLogin()
            }

            btnTravelCompanion.setOnClickListener {
                navigateToTravelCompanion()
            }

            btnAddWidget.setOnClickListener {
                addWidgetToHomeScreen()
            }
        }
    }

    private fun signOut() {
        val sharedPrefs = requireContext().getSharedPreferences("TravelFoodie", Context.MODE_PRIVATE)
        val loginProvider = sharedPrefs.getString("login_provider", null)

        when (loginProvider) {
            "google" -> {
                // Sign out from Google
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
                googleSignInClient.signOut().addOnCompleteListener {
                    auth.signOut()
                    clearUserData()
                    showGuestMode()
                    Toast.makeText(requireContext(), "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
                }
            }
            "kakao" -> {
                // Sign out from Kakao
                UserApiClient.instance.logout { error ->
                    if (error != null) {
                        Toast.makeText(requireContext(), "로그아웃 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        clearUserData()
                        showGuestMode()
                        Toast.makeText(requireContext(), "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            "naver" -> {
                // Sign out from Naver
                NaverIdLoginSDK.logout()
                clearUserData()
                showGuestMode()
                Toast.makeText(requireContext(), "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Firebase only
                auth.signOut()
                clearUserData()
                showGuestMode()
                Toast.makeText(requireContext(), "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearUserData() {
        requireContext().getSharedPreferences("TravelFoodie", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    private fun navigateToLogin() {
        try {
            findNavController().navigate(R.id.action_profile_to_login)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "로그인 화면으로 이동할 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToTravelCompanion() {
        try {
            findNavController().navigate(R.id.action_profile_to_travel_companion)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "여행 동반자 화면으로 이동할 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addWidgetToHomeScreen() {
        try {
            // Launch the widget picker to allow user to add the widget to home screen
            val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(requireContext())
            val myProvider = android.content.ComponentName(requireContext(), com.travelfoodie.feature.widget.TripWidgetProvider::class.java)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // For Android O and above, request to pin the widget
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    appWidgetManager.requestPinAppWidget(myProvider, null, null)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "위젯 고정 기능이 지원되지 않습니다\n홈 화면에서 위젯을 수동으로 추가해주세요",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                // For older versions, show instruction
                Toast.makeText(
                    requireContext(),
                    "홈 화면에서 길게 눌러 위젯을 추가해주세요",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "위젯 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            android.util.Log.e("ProfileFragment", "Error adding widget", e)
        }
    }

    // STT Handler
    private fun handleSttClick() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startSpeechRecognition()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "음성을 입력하세요")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "음성인식을 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    // TTS Handler
    private fun handleTtsClick() {
        val testText = "안녕하세요! TravelFoodie 음성출력 기능입니다. 여행과 맛집 정보를 음성으로 들어보세요."
        textToSpeech?.speak(testText, TextToSpeech.QUEUE_FLUSH, null, null)
        Toast.makeText(requireContext(), "음성출력 테스트 중...", Toast.LENGTH_SHORT).show()
    }

    // OCR Handler
    private fun handleOcrClick() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            pickImageLauncher.launch("image/*")
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun performOcr(imageUri: Uri) {
        Toast.makeText(requireContext(), "영수증을 스캔하는 중...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val result = ocrHelper.extractTextFromImage(imageUri)
                lastOcrResult = result

                // Show result and offer to share
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("영수증 스캔 완료")
                    .setMessage("인식된 텍스트:\n\n$result")
                    .setPositiveButton("카카오톡 공유") { _, _ ->
                        shareViaKakao(result)
                    }
                    .setNegativeButton("닫기", null)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "OCR 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // KakaoTalk Sharing
    private fun shareViaKakao(receiptText: String) {
        val defaultFeed = FeedTemplate(
            content = Content(
                title = "TravelFoodie 영수증",
                description = receiptText.take(100) + if (receiptText.length > 100) "..." else "",
                imageUrl = "https://via.placeholder.com/300x200.png?text=Receipt",
                link = Link(
                    webUrl = "https://travelfoodie.com",
                    mobileWebUrl = "https://travelfoodie.com"
                )
            )
        )

        // Check if KakaoTalk is available
        if (ShareClient.instance.isKakaoTalkSharingAvailable(requireContext())) {
            ShareClient.instance.shareDefault(requireContext(), defaultFeed) { sharingResult, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "카카오톡 공유 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                } else if (sharingResult != null) {
                    Toast.makeText(requireContext(), "카카오톡으로 공유되었습니다", Toast.LENGTH_SHORT).show()

                    // Open KakaoTalk
                    startActivity(sharingResult.intent)
                }
            }
        } else {
            // Use web sharer as fallback
            val sharerUrl = WebSharerClient.instance.makeDefaultUrl(defaultFeed)
            try {
                startActivity(Intent(Intent.ACTION_VIEW, sharerUrl))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "카카오톡 공유를 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        _binding = null
    }
}
