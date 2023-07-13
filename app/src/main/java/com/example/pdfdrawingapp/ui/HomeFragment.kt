package com.example.pdfdrawingapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pdfdrawingapp.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null

    private var mUri: Uri? = null

    private val openDocumentLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result == null) return@registerForActivityResult
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val resultData = result.data
        if (resultData != null) {
            mUri = result.data!!.data
            val pdfName = resultData.data?.path
            Log.d("PDFDrawing App", "pdfName = $pdfName")
            if (mUri != null) {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToPdfViewFragment(
                        mUri.toString()
                    )
                )
            }
            invalidateOptionsMenu(requireActivity())
        }
    }

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

//        init()
        binding.openBtn.setOnClickListener {
            openDocument()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openDocument() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        openDocumentLauncher.launch(intent)
    }
}