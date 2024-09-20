/**The main activity to handle startup of the application.
 * Date: 09/08/2024
 *
 */
package com.example.paintapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.fragment.app.Fragment
import com.example.paintapp.databinding.ActivityMainBinding

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.findNavController


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
//        findNavController.navigate(R.id.)
        // Check if fragmentContainerView contains a fragment already due to rotation
        if (savedInstanceState == null) {
            // Add MainScreen fragment initially if none exists
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MainScreen(), "mainScreenTag")
                .commitNow()
        }

        // Set up button function inside MainScreen to replace with PaintScreen
        val mainScreen = supportFragmentManager.findFragmentByTag("mainScreenTag") as? MainScreen
        mainScreen?.setButtonFunction {
            val drawFragment = DrawFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, drawFragment, "drawFragmentTag")
            transaction.addToBackStack(null)
            transaction.commit()
        }

        setContentView(binding.root)
    }
}


//class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MaterialTheme {
//                Surface {
//                    AppNavigation()
//                }
//            }
//        }
//    }
//-----------------NAVIGATION------------------//
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    //NavHost - holds different screens, and manages the the transition between screens
    //start at the home screen and then go
    //Each composable represents a distinct screen
    //Inside you define navigation actions (using compsable blocks)
    //Where each block corresponds to a route ("home", "details") and ties a composable function to that route
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("details") { DetailScreen(navController) }
    }
}
//----------------HOME SCREEN----------------------//
//Behavior: button in home screen that when clicked takes you to details screen
//The button must tell the navController to navigate to "details"
@Composable
fun HomeScreen(navController: NavController) {
    Button(onClick = { navController.navigate("details") }) {
        Text("Go to Details")
    }
}
//---------------DETAILS SCREEN----------------------//
@Composable
fun DetailScreen(navController: NavController) {
    Text("This is the Detail Screen")
    Button(onClick = { navController.navigate("home") }){
        Text("back at Home")
    }
}


