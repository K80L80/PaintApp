package com.example.paintapp

import android.widget.Button
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

//By using this technique, the NavController is available before onViewCreated() is called, allowing the fragment to use NavigationUI methods without crashing.
@RunWith(AndroidJUnit4::class)
class TitleScreenTest {
    private lateinit var navController: TestNavHostController
    private lateinit var repository: DrawRepository
    private lateinit var drawViewModel: DrawViewModel

    @Before
    fun setUp() {
        val appContext =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as DrawApp

        repository = appContext.drawRepository
        drawViewModel = DrawViewModel(repository)
        navController = TestNavHostController(appContext)
    }

    //While on main menu does clicking 'new drawing' button take you to the drawing screen?
    @Test
    fun testNav() {
        val scenario = launchFragmentInContainer {
            MainScreen().also { fragment ->

                // In addition to returning a new instance of our Fragment,
                // get a callback whenever the fragment’s view is created
                // or destroyed so that we can set the NavController
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        // The fragment’s view has just been created
                        navController.setGraph(R.navigation.nav_graph)
                        Navigation.setViewNavController(fragment.requireView(), navController)
                    }
                }
            }
        }
        // Simulate the button click that should trigger navigation to DrawFragment
        scenario.onFragment { fragment ->
            // Find button2 and perform click
            val button2 = fragment.requireView().findViewById<Button>(R.id.button2)
            button2.performClick()

            // Assert that the correct navigation action has been triggered
            assert(navController.currentDestination?.id == R.id.drawFragment)
        }
    }
    //While on Draw screen does clicking back button go to main menu (drawing gallary)
    @Test
    fun testBackNavigationFromDrawFragment() {
        // Launch the DrawFragment in the container
        val scenario = launchFragmentInContainer {
            DrawFragment().also { fragment ->

                // Observe the view lifecycle to attach the NavController
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        // Set the navigation graph for the test
                        navController.setGraph(R.navigation.nav_graph)
                        Navigation.setViewNavController(fragment.requireView(), navController)

                        // Optionally navigate to DrawFragment for this test
                        navController.navigate(R.id.action_mainScreen_to_drawFragment)
                    }
                }
            }
        }

        // Simulate pressing the back button
        scenario.onFragment { fragment ->
            navController.popBackStack()  // Simulates the back button press

            // Assert that after pressing back, we are back on the MainScreen
            assert(navController.currentDestination?.id == R.id.mainScreen)
        }
    }

    @Test
    fun testSplashFragmentNavigatesToMainMenu() {
        // Initialize the NavController
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        // Launch the SplashFragment in the container
        val scenario = launchFragmentInContainer {
            SplashScreenFragment().also { fragment ->

                // Observe the view lifecycle to attach the NavController
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        // Set the navigation graph for the test
                        navController.setGraph(R.navigation.nav_graph)
                        Navigation.setViewNavController(fragment.requireView(), navController)
                    }
                }
            }
        }

        // Simulate the passage of time or animation completion
        // You can use idling resources or directly simulate the end of animation
        // For example, simulate calling the method that gets triggered when the animation ends
        scenario.onFragment { fragment ->
            // Call the function that should be triggered when animation ends
            runTest {
                advanceTimeBy(8000)
            }

            // Assert that after animation ends, we are on the MainMenu
            assert(navController.currentDestination?.id == R.id.mainScreen)
        }
    }
}