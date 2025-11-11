package com.dentim.karaoke.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.dentim.karaoke.R
import com.dentim.karaoke.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity hosting navigation and common UI components
 * Entry point for the karaoke application
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        
        setupNavigation()
        setupActionBar()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }
    
    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}