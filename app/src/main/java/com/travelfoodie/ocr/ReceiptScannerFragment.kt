package com.travelfoodie.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.travelfoodie.databinding.FragmentReceiptScannerBinding
import kotlinx.coroutines.launch

class ReceiptScannerFragment : Fragment() {

    private var _binding: FragmentReceiptScannerBinding? = null
    private val binding get() = _binding!!

    private lateinit var ocrHelper: ReceiptOcrHelper
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imageViewReceipt.setImageURI(it)
            binding.buttonScan.isEnabled = true
        }
    }

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pickImage.launch("image/*")
        } else {
            Snackbar.make(binding.root, "카메라 권한이 필요합니다", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ocrHelper = ReceiptOcrHelper(requireContext())

        binding.buttonSelectImage.setOnClickListener {
            selectImage()
        }

        binding.buttonScan.setOnClickListener {
            scanReceipt()
        }
    }

    private fun selectImage() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                pickImage.launch("image/*")
            }
            else -> {
                cameraPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun scanReceipt() {
        val imageUri = selectedImageUri ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.buttonScan.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val receiptData = ocrHelper.scanReceipt(imageUri)

                binding.textViewMerchantName.text = "가맹점: ${receiptData.merchantName}"
                binding.textViewTotal.text = "금액: ${String.format("%,.0f", receiptData.total)}원"
                binding.textViewOcrResult.text = "인식된 텍스트:\n${receiptData.fullOcrText}"

                binding.resultContainer.visibility = View.VISIBLE

                Snackbar.make(binding.root, "영수증 스캔 완료!", Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Snackbar.make(
                    binding.root,
                    "영수증 스캔 실패: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.buttonScan.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ocrHelper.close()
        _binding = null
    }
}
