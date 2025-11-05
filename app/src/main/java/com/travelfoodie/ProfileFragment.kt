package com.travelfoodie

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.travelfoodie.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var auth: FirebaseAuth

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

            btnSignout.setOnClickListener {
                signOut()
            }

            btnGoToLogin.setOnClickListener {
                navigateToLogin()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
