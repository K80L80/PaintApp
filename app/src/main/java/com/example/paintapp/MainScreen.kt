package com.example.paintapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.paintapp.databinding.ActivityMainScreenBinding

class MainScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}