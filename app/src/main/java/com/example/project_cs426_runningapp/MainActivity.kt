package com.example.project_cs426_runningapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.project_cs426_runningapp.databinding.ActivityOnboardingBinding
import com.example.project_cs426_runningapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var auth: FirebaseAuth
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigateToRunningFragmentIfNeeded(intent)

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        setContentView(binding.root)
        supportActionBar?.hide()
        val navView = binding.navView
        val musicFragmentItem = navView.menu.findItem(R.id.musicFragment)
        val navController = findNavController(R.id.nav_host_fragment_activity_home)
        navView.setupWithNavController(navController)


        musicFragmentItem.iconTintList = ContextCompat.getColorStateList(this, R.color.bottom_tab_music_icon_color)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if(destination.id == R.id.onboardingFragment
                || destination.id == R.id.runningFragment
                || destination.id == R.id.logInFragment
                || destination.id == R.id.registerFragment
                || destination.id == R.id.settingFragment
                || destination.id == R.id.editProfileFragment
                || destination.id == R.id.forgotPasswordFragment) {
                navView.visibility = View.GONE
            } else {
                navView.visibility = View.VISIBLE
            }

            if(destination.id == R.id.musicFragment){
                navView.setBackgroundResource(R.color.trans)
            }
            else navView.setBackgroundResource(R.drawable.rounded_bottom_nav_view_background)
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null) {
            findNavController(R.id.nav_host_fragment_activity_home).navigate(R.id.action_onboardingFragment_to_homeFragment)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToRunningFragmentIfNeeded(intent)
    }

    private fun navigateToRunningFragmentIfNeeded(intent: Intent?) {
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            val navController = findNavController(R.id.nav_host_fragment_activity_home)
            navController.navigate(R.id.action_global_runningFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        auth.currentUser?.let {
            auth.signOut()
        }
    }
}