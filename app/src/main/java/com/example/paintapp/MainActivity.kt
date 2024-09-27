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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.Button
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                var isWelcomeVisible by remember { mutableStateOf(true) }
                val navController = rememberNavController()

                // AnimatedVisibility for WelcomeScreen
                AnimatedVisibility(
                    visible = isWelcomeVisible,
                    enter = fadeIn(animationSpec = tween(1000)), // Fade in over 1 second
                    exit = fadeOut(animationSpec = tween(1000))  // Fade out over 1 second
                ) {
                    WelcomeScreen()  // Your animated WelcomeScreen
                }

                // Navigate to MainScreen after the animation
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000) // Delay for 2 seconds
                    isWelcomeVisible = false
                    navController.navigate("mainScreen")
                }

                // Navigation Setup
                NavHost(navController = navController, startDestination = "mainScreen") {
                    // MainScreen Navigation
                    composable("mainScreen") {
                        MainScreen {
                            navController.navigate("drawScreen")
                        }
                    }
                    // DrawScreen Navigation
                    composable("drawScreen") {
                        DrawScreen(
                            onChangeColorClick = { /* Handle color change */ },
                            onChangeSizeClick = { /* Handle size change */ },
                            onChangeShapeClick = { /* Handle shape change */ },
                            onClearClick = { /* Handle clear */ },
                            onSaveClick = { /* Handle save */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "👏Welcome to the Paint App!\nLet's start drawing ✌️!",
            fontSize = 25.sp,
            color = Color.Blue,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MainScreen(onStartDrawingClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Optional padding for the whole Box
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter), // Align the column with text and button to the bottom center
            horizontalAlignment = Alignment.CenterHorizontally, // Center text and button horizontally
            verticalArrangement = Arrangement.Center
        ) {
            // Text placed above the button
            Text(
                text = "Welcome to Main Screen",
                fontSize = 25.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            // Button
            Button(
                onClick = onStartDrawingClick,
                modifier = Modifier
                    .padding(top = 16.dp) // Padding between the text and the button
            ) {
                Text(text = "Start Drawing")
            }
        }
    }
}



@Composable
fun BrushSizeSlider(brushSize: Float, onSizeChange: (Float) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Brush Size: ${brushSize.toInt()}", fontSize = 18.sp)

        Slider(
            value = brushSize,
            onValueChange = { onSizeChange(it) },
            valueRange = 1f..50f,
            steps = 49 // 50 steps minus one
        )
    }
}


@Composable
fun DrawScreen(
    onChangeColorClick: () -> Unit,
    onChangeSizeClick: () -> Unit,
    onChangeShapeClick: () -> Unit,
    onClearClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    var brushSize by remember { mutableStateOf(10f) }
    var brushColor by remember { mutableStateOf(Color.Blue) }
    var path = remember { Path() }
    var isCleared by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Custom Draw View
        CustomDrawView(
            brushSize = brushSize,
            brushColor = brushColor,
//            path = path,
            isCleared = isCleared
        )

        // Top-level Column to place buttons at the top and other controls below
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top // Arrange items from the top
        ) {
            // Place buttons at the top
            TopControls(onClearClick = onClearClick, onSaveClick = onSaveClick)

            Spacer(modifier = Modifier.height(16.dp)) // Add some space between controls

            // Brush size slider below buttons
            BrushSizeSlider(brushSize = brushSize, onSizeChange = { brushSize = it })

            // Controls to change color, size, shape at the bottom
            Spacer(modifier = Modifier.weight(1f)) // Push the color and shape controls to the bottom
            DrawControls(
                onChangeColorClick = {
                    brushColor = listOf(Color.Blue, Color.Red, Color.Green).random()
                    onChangeColorClick()
                },
                onChangeSizeClick = {
                    brushSize = if (brushSize < 50) brushSize + 5 else 10f
                    onChangeSizeClick()
                },
                onChangeShapeClick = {
                    onChangeShapeClick()
                }
            )
        }
    }
}

@Composable
fun CustomDrawView(
    brushSize: Float,
    brushColor: Color,
    isCleared: Boolean
) {
    val path = remember { Path() }
    var clearedState by remember { mutableStateOf(false) }  // Track whether we've cleared the canvas

    val drawModifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .pointerInput(Unit) {
            detectDragGestures { change, _ ->
                change.consume()
                val touchX = change.position.x
                val touchY = change.position.y
                if (isCleared && !clearedState) {
                    path.reset()
                    clearedState = true // Mark that we've cleared
                } else {
                    path.lineTo(touchX, touchY)
                    clearedState = false // Reset the clear state
                }
            }
        }

    Canvas(modifier = drawModifier) {
        if (!isCleared) {
            drawPath(path, brushColor, style = Stroke(width = brushSize * 5))
        } else {
            drawRect(
                color = Color.White,
                size = size
            )
        }
    }
}

@Composable
fun DrawControls(
    onChangeColorClick: () -> Unit,
    onChangeSizeClick: () -> Unit,
    onChangeShapeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(1.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onChangeColorClick) {
                Text("Change Color")
            }
            Button(onClick = onChangeSizeClick) {
                Text("Change Size")
            }
            Button(onClick = onChangeShapeClick) {
                Text("Change Shape")
            }
        }
    }
}

@Composable
fun TopControls(
    onClearClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = onClearClick, modifier = Modifier.weight(1f)) {
            Text("Clear")
        }
        Button(onClick = onSaveClick, modifier = Modifier.weight(1f)) {
            Text("Save")
        }
    }
}
