package com.sandeepprabhakula.eventmanagementapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.sandeepprabhakula.eventmanagementapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = findNavController(R.id.loginNavHost)
        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_, destination, _ ->
            when(destination.id){
                R.id.loginFragment->{
                    binding.bottomNav.visibility = View.GONE
                }
                else->{
                    binding.bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }
}