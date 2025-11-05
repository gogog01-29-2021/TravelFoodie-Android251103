package com.travelfoodie

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import com.travelfoodie.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                // Firebase Auth with ID token
                firebaseAuthWithGoogle(idToken)
            } else {
                // Fallback: No Firebase Auth, just save basic info
                Log.w(TAG, "No ID token available. Google Sign-In not properly configured in Firebase.")
                hideLoading()
                Toast.makeText(
                    requireContext(),
                    "Firebase에서 Google 로그인을 활성화해주세요.\n\nFirebase Console > Authentication > Sign-in method에서 Google을 활성화하고 google-services.json을 업데이트하세요.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign in failed", e)
            hideLoading()
            Toast.makeText(requireContext(), "Google 로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google sign in", e)
            hideLoading()
            Toast.makeText(requireContext(), "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if user is already logged in
        if (auth.currentUser != null) {
            navigateToProfile()
            return
        }

        setupGoogleSignIn()
        setupClickListeners()
    }

    private fun setupGoogleSignIn() {
        try {
            // Try to get the web client ID from resources
            val webClientId = try {
                getString(R.string.default_web_client_id)
            } catch (e: Exception) {
                Log.e(TAG, "default_web_client_id not found. Google Sign-In not properly configured.", e)
                null
            }

            val gso = if (webClientId != null && webClientId.isNotBlank()) {
                // Proper setup with Firebase Auth
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build()
            } else {
                // Fallback: Setup without ID token
                Log.w(TAG, "Setting up Google Sign-In without Firebase Auth (ID token not available)")
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
            }

            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
            Log.d(TAG, "Google Sign-In client initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup Google Sign-In", e)
            // Last resort fallback
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnGoogleSignin.setOnClickListener {
                signInWithGoogle()
            }

            btnKakaoSignin.setOnClickListener {
                signInWithKakao()
            }

            btnNaverSignin.setOnClickListener {
                signInWithNaver()
            }

            btnSkip.setOnClickListener {
                navigateToProfile()
            }
        }
    }

    private fun signInWithGoogle() {
        try {
            Log.d(TAG, "Starting Google Sign-In")
            showLoading()
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching Google Sign-In", e)
            hideLoading()
            Toast.makeText(
                requireContext(),
                "Google 로그인 시작 실패: ${e.message}\n\n앱을 다시 시작해주세요.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                hideLoading()
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    // Save Google user info to SharedPreferences
                    val user = auth.currentUser
                    saveGoogleUserInfo(user?.uid, user?.displayName, user?.email)
                    Toast.makeText(requireContext(), "Google 로그인 성공", Toast.LENGTH_SHORT).show()
                    navigateToProfile()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(requireContext(), "인증 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithKakao() {
        showLoading()

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "Kakao login failed", error)
                hideLoading()
                Toast.makeText(requireContext(), "Kakao 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                Log.d(TAG, "Kakao login success: ${token.accessToken}")
                // Get Kakao user info
                UserApiClient.instance.me { user, userError ->
                    hideLoading()
                    if (userError != null) {
                        Log.e(TAG, "Failed to get Kakao user info", userError)
                        Toast.makeText(requireContext(), "사용자 정보 가져오기 실패", Toast.LENGTH_SHORT).show()
                    } else if (user != null) {
                        Log.d(TAG, "Kakao user info: ${user.kakaoAccount?.email}")
                        Toast.makeText(requireContext(), "Kakao 로그인 성공", Toast.LENGTH_SHORT).show()
                        // Store Kakao user info in SharedPreferences or local DB
                        saveKakaoUserInfo(user.id.toString(), user.kakaoAccount?.profile?.nickname, user.kakaoAccount?.email)
                        navigateToProfile()
                    }
                }
            }
        }

        // Check if KakaoTalk is available
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireContext())) {
            UserApiClient.instance.loginWithKakaoTalk(requireContext(), callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(requireContext(), callback = callback)
        }
    }

    private fun signInWithNaver() {
        showLoading()

        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                Log.d(TAG, "Naver login success")
                // Get Naver user profile
                NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
                    override fun onSuccess(result: NidProfileResponse) {
                        hideLoading()
                        val profile = result.profile
                        Log.d(TAG, "Naver user info: ${profile?.email}")
                        Toast.makeText(requireContext(), "Naver 로그인 성공", Toast.LENGTH_SHORT).show()
                        // Store Naver user info
                        saveNaverUserInfo(profile?.id, profile?.name, profile?.email)
                        navigateToProfile()
                    }

                    override fun onFailure(httpStatus: Int, message: String) {
                        hideLoading()
                        Log.e(TAG, "Failed to get Naver profile: $message")
                        Toast.makeText(requireContext(), "사용자 정보 가져오기 실패", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(errorCode: Int, message: String) {
                        hideLoading()
                        Log.e(TAG, "Error getting Naver profile: $message")
                        Toast.makeText(requireContext(), "오류 발생: $message", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onFailure(httpStatus: Int, message: String) {
                hideLoading()
                Log.e(TAG, "Naver login failed: $message")
                Toast.makeText(requireContext(), "Naver 로그인 실패: $message", Toast.LENGTH_SHORT).show()
            }

            override fun onError(errorCode: Int, message: String) {
                hideLoading()
                Log.e(TAG, "Naver login error: $message")
                Toast.makeText(requireContext(), "오류 발생: $message", Toast.LENGTH_SHORT).show()
            }
        }

        NaverIdLoginSDK.authenticate(requireContext(), oauthLoginCallback)
    }

    private fun saveKakaoUserInfo(userId: String, name: String?, email: String?) {
        requireContext().getSharedPreferences("TravelFoodie", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("login_provider", "kakao")
            .putString("user_id", userId)
            .putString("user_name", name)
            .putString("user_email", email)
            .apply()
    }

    private fun saveGoogleUserInfo(userId: String?, name: String?, email: String?) {
        requireContext().getSharedPreferences("TravelFoodie", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("login_provider", "google")
            .putString("user_id", userId)
            .putString("user_name", name)
            .putString("user_email", email)
            .apply()
    }

    private fun saveNaverUserInfo(userId: String?, name: String?, email: String?) {
        requireContext().getSharedPreferences("TravelFoodie", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("login_provider", "naver")
            .putString("user_id", userId)
            .putString("user_name", name)
            .putString("user_email", email)
            .apply()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnGoogleSignin.isEnabled = false
        binding.btnKakaoSignin.isEnabled = false
        binding.btnNaverSignin.isEnabled = false
        binding.btnSkip.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnGoogleSignin.isEnabled = true
        binding.btnKakaoSignin.isEnabled = true
        binding.btnNaverSignin.isEnabled = true
        binding.btnSkip.isEnabled = true
    }

    private fun navigateToProfile() {
        // Navigate back or to profile (user is already on profile tab, just refresh)
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "LoginFragment"
    }
}
