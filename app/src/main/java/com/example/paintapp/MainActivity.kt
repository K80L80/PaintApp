/**The main activity to handle startup of the application.
 * Date: 09/08/2024
 *
 */
package com.example.paintapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.fragment.app.Fragment
import com.example.paintapp.databinding.ActivityMainBinding

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.delay

//TODO: Remove support Manager and do jetpack navigation
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val binding = ActivityMainBinding.inflate(layoutInflater)
        setContent {
            ShowSplashScreenAnimation{

            }
        }
    }
//        // Check if fragmentContainerView contains a fragment already due to rotation
//        if (savedInstanceState == null) {
//            // Add MainScreen fragment initially if none exists
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, MainScreen(), "mainScreenTag")
//                .commitNow()
//        }
//
//        // Set up button function inside MainScreen to replace with PaintScreen
//        val mainScreen = supportFragmentManager.findFragmentByTag("mainScreenTag") as? MainScreen
//        mainScreen?.setButtonFunction {
//            val drawFragment = DrawFragment()
//            val transaction = supportFragmentManager.beginTransaction()
//            transaction.replace(R.id.fragment_container, drawFragment, "drawFragmentTag")
//            transaction.addToBackStack(null)
//            transaction.commit()
//        }
//
//        setContentView(binding.root)
//    }
}

//This method handles the timing and animation logic
@Composable
fun ShowSplashScreenAnimation(onAnimationComplete: () -> Unit) {

    //is needed to make the UI react to changes.
    var isVisible by remember { mutableStateOf(true) }

    //Displays the spash screen for 3 seconds total; 1 sec fade in, 1 sec hold, 1 sec fade out
    //Acts as timer, starts immediately when main activity starts
    LaunchedEffect(Unit) {
        delay(1000)
        isVisible = false
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 1000)),  // Fade in over 1.5 seconds
        exit = fadeOut(animationSpec = tween(durationMillis = 1000))   // Fade out over 1.5 seconds
    ) {
        SplashScreenComposable()
    }
}

//Splash screen is composable now, instead of a view (ie Activity)
//SplashScreenComposable strictly handles the UI presentation
@Composable
fun SplashScreenComposable() {
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
////TODO:
//private fun navigateToMainScreen() {
//    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//    val navController = navHostFragment.navController
//
//    // Use the action defined in your navigation graph to navigate to the main screen
//    navController.navigate(R.id.action_splashScreen_to_mainScreenFragment)
//}
//

