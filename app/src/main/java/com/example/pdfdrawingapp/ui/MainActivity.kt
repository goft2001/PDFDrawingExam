package com.example.pdfdrawingapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.example.pdfdrawingapp.R
import com.example.pdfdrawingapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        checkPermission(this)
    }
   private fun checkPermission(activity: MainActivity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var permissions: Array<String>? = null
            permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf<String>(
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.CAMERA,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                arrayOf<String>(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
            val denied_permissions: MutableList<String> = ArrayList()
            for (perm in permissions) {
                if (ActivityCompat.checkSelfPermission(this, perm)
                    != PackageManager.PERMISSION_GRANTED
                ) denied_permissions.add(perm)
            }
            if (denied_permissions.size > 0) {
                val deniedPerms = denied_permissions.toTypedArray()
                ActivityCompat.requestPermissions(
                    activity, deniedPerms,
                    REQUEST_PERMISSIONS
                )
                return false
            }
        }
        return true
    }

    companion object {
        var REQUEST_PERMISSIONS = 1
    }


}
