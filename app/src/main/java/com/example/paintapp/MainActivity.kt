package com.example.paintapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material.Button
//import androidx.compose.material.Surface
//import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Button
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                var isVisible by remember { mutableStateOf(true) }

                // Use NavController to manage navigation
                val navController = rememberNavController()

                // Animation visibility: toggles visibility after 2 seconds
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(1000)),
                    exit = fadeOut(animationSpec = tween(1000))
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        NavHost(navController = navController, startDestination = "mainScreen") {
                            // Main screen (MainScreen)
                            composable("mainScreen") {
                                MainScreen(navController)
                            }
                            // Drawing screen (DrawFragment corresponding Composable)
                            composable("drawScreen") {
                                DrawScreen()
                            }
                        }
                    }
                }

                // Delay for 2 seconds and toggle visibility
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    isVisible = !isVisible
                }
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        // Button triggers navigation and adds animation
        Button(onClick = {
            navController.navigate("drawScreen")
        }) {
            Text("Start Drawing")
        }
    }
}

@Composable
fun DrawScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Text("This is the Draw Screen")
    }
}
