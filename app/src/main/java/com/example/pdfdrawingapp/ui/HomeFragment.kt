package com.example.pdfdrawingapp.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.pdfdrawingapp.adapter.PDFAdapter
import com.example.pdfdrawingapp.databinding.FragmentHomeBinding
import java.io.File
import java.util.ArrayList

class HomeFragment : Fragment() {
    var dir: File? = null

    private var _binding: FragmentHomeBinding? = null
    var pdfAdapter: PDFAdapter? = null
    var booleanPermission = false

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        init()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun init() {
        dir = File(Environment.getExternalStorageDirectory().absolutePath)
        permissionCheck()
        binding.pdfListView.onItemClickListener = AdapterView.OnItemClickListener { _, view, position, _ ->
            view.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPdfViewFragment(position = position))
        }
    }

    private fun getFile(dir: File?): ArrayList<File> {
        val listFile = dir!!.listFiles()
        if (listFile != null && listFile.isNotEmpty()) {
            for (i in listFile.indices) {
                if (listFile[i].isDirectory) {
                    getFile(listFile[i])
                } else {
                    var booleanpdf = false
                    if (listFile[i].name.endsWith(".pdf")) {
                        for (j in fileList.indices) {
                            if (fileList[j].name == listFile[i].name) {
                                booleanpdf = true
                            } else {
                            }
                        }
                        if (booleanpdf) {
                            booleanpdf = false
                        } else {
                            fileList.add(listFile[i])
                        }
                    }
                }
            }
        }
        return fileList
    }
    private fun permissionCheck() {
        val context: Context = requireContext()

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_PERMISSIONS
                )
            }
        } else {
            booleanPermission = true
            getFile(dir)
            pdfAdapter = PDFAdapter(context, fileList)
            binding.pdfListView.adapter = pdfAdapter
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            val context: Context = requireContext()
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                booleanPermission = true
                getFile(dir)
                pdfAdapter = PDFAdapter(context, fileList)
                binding.pdfListView.adapter = pdfAdapter
            } else {
                Toast.makeText(context, "Please allow the permission", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    companion object {
        var fileList = ArrayList<File>()
        var REQUEST_PERMISSIONS = 1
    }
}