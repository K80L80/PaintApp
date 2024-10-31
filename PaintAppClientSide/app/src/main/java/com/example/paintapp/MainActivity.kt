/**The main activity to handle startup of the application.
 * Date: 09/08/2024
 *
 */
package com.example.paintapp

import android.os.Bundle
import androidx.compose.ui.unit.dp
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
import androidx.compose.ui.platform.ComposeView
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
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


/*
TODO: Database class: Keep track of path in SQLLite + save file (bitmap) on device (RECOMMENDED)
TODO: have user enter a filename
TODO: store the file name use in database (SQLLite)
TODO: actual drawing stored on device (Dir folder -special folder for the application
*/

//Removed support Manager and do jetpack navigation
class MainActivity : AppCompatActivity() {

    private lateinit var drawRepository: DrawRepository
    private lateinit var navController: NavController
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Log out is performed each time the application is started
        Firebase.auth.signOut()

        FirebaseApp.initializeApp(this)
        val drawDao = DrawDatabase.getDatabase(this).drawDao()
        drawRepository = (application as DrawApp).drawRepository

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            var showAuthScreen by remember { mutableStateOf(true) }

            Surface(color = MaterialTheme.colors.background) {
                if (showAuthScreen) {
                    FirebaseAuthScreen(onLoginSuccess = { uId, email ->
                        Log.e("uIDMain", "$uId")
                        drawRepository.setUserData(uId, email)
                        viewModel.onLoginComplete()
                        showAuthScreen = false
                        // After the login is successful, ComposeView is hidden and FragmentContainerView is displayed
                        showAuthScreen = false
                        runOnUiThread {
                            composeView.isVisible = false
                            findViewById<FragmentContainerView>(R.id.fragmentContainerView).isVisible =
                                true
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun FirebaseAuthScreen(onLoginSuccess: (String, String) -> Unit) {
    var user by remember { mutableStateOf(Firebase.auth.currentUser) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Center the content
    ) {
        if (user == null) {
            AuthScreen(
                onLoginSuccess = { uId, email ->
                    Log.d("FirebaseAuthScreen", "Login successful, UID: $uId")
                    onLoginSuccess(uId, email)
                    user = Firebase.auth.currentUser
                }
            )
        } else {
            Log.d("FirebaseAuthScreen", "User already logged in, UID: ${user?.uid}")
            LaunchedEffect(user) {
                onLoginSuccess(user!!.uid, user!!.email ?: "")
            }
        }
    }
}

@Composable
fun AuthScreen(onLoginSuccess: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Center the content
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Not logged in")
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Row (
                horizontalArrangement = Arrangement.spacedBy(30.dp) // Set the spacing between buttons
            ){
                Button(onClick = {
                    Firebase.auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser = Firebase.auth.currentUser
                                val uId = firebaseUser?.uid ?: ""
                                val userEmail = firebaseUser?.email ?: ""
                                onLoginSuccess(uId, userEmail)
                            } else {
                                email = "Login failed, try again"
                            }
                        }
                }) {
                    Text("Log In")
                }
                Button(onClick = {
                    Firebase.auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser = Firebase.auth.currentUser
                                val uId = firebaseUser?.uid ?: ""
                                val userEmail = firebaseUser?.email ?: ""
                                onLoginSuccess(uId, userEmail)
                            } else {
                                email = "Create user failed, try again"
                                Log.e("Create user error", "${task.exception}")
                            }
                        }
                }) {
                    Text("Sign Up")
                }
            }
        }
    }
}

//@Composable
//fun WelcomeScreen(userEmail: String?, onSignOut: () -> Unit) {
//    var dataString by remember { mutableStateOf("") }
//    val db = Firebase.firestore
//    val collection = db.collection("demoCollection")
//
//    LaunchedEffect(Unit) {
//        collection.get()
//            .addOnSuccessListener { result ->
//                val doc = result.firstOrNull()
//                dataString = "${doc?.id} => ${doc?.data}"
//            }
//            .addOnFailureListener { exception ->
//                Log.w("Error", "Error getting documents.", exception)
//            }
//    }
//
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center // Center the content
//    ) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally,  modifier = Modifier.fillMaxSize()) {
//            Text("Welcome $userEmail")
//            Text("Data string: $dataString")
//            Button(onClick = onSignOut) {
//                Text("Sign Out")
//            }
//        }
//    }
//}