package com.example.paintapp

import android.widget.Button
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlin.time.measureTime

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
                //testDispatcher.advanceTimeBy(3000L)
                //Assert that after animation ends, we are on the MainMenu
                assert(navController.currentDestination?.id == R.id.mainScreen)
            }
        }
    }

    @Test
    fun exampleTest() = runTest {
        val elapsed = TimeSource.Monotonic.measureTime {
            val deferred = async {
                delay(1.seconds) // will be skipped
                withContext(Dispatchers.Default) {
                    delay(5.seconds) // Dispatchers.Default doesn't know about TestCoroutineScheduler
                }
            }
            deferred.await()
        }
        println(elapsed) // about five seconds
    }

    @Test
    fun testEagerlyEnteringChildCoroutines() = runTest(UnconfinedTestDispatcher()) {
        var entered = false
        val deferred = CompletableDeferred<Unit>()
        var completed = false
        launch {
            entered = true
            deferred.await()
            completed = true
        }
        assertTrue(entered) // `entered = true` already executed.
        assertFalse(completed) // however, the child coroutine then suspended, so it is enqueued.
        deferred.complete(Unit) // resume the coroutine.
        assertTrue(completed) // now the child coroutine is immediately completed.
    }

    @Test
    fun assertNavControllerIsAttached() {
        // Initialize the TestNavHostController
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        // Launch the fragment in the container
        val scenario = launchFragmentInContainer {
            SplashScreenFragment().also { fragment ->

                // Observe the view lifecycle to attach the NavController before the fragment is resumed
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        // The fragment’s view has just been created
                        navController.setGraph(R.navigation.nav_graph) // Use your actual navigation graph
                        Navigation.setViewNavController(fragment.requireView(), navController)
                    }
                }
            }
        }

        // Now you can perform navigation tests
        scenario.onFragment { fragment ->
            // Assert that the NavController is properly attached
            assert(Navigation.findNavController(fragment.requireView()) == navController)
        }
    }


    @Test
    fun assertAppStartAtSplashScreen() {
        // Initialize the TestNavHostController
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        // Launch the fragment in the container
        val scenario = launchFragmentInContainer {
            SplashScreenFragment().also { fragment ->

                // Observe the view lifecycle to attach the NavController before the fragment is resumed
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        // The fragment’s view has just been created
                        navController.setGraph(R.navigation.nav_graph) // Use your actual navigation graph
                        Navigation.setViewNavController(fragment.requireView(), navController)
                    }
                }
            }
        }

        // Now you can perform navigation tests
        scenario.onFragment {
            assert(navController.currentDestination?.id == R.id.splashScreenFragment) // Replace with your actual SplashFragment ID
        }
    }

    //Test that navigation
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testing() {
        // Initialize the NavController
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

//        val testDispatcher = StandardTestDispatcher()

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
        scenario.onFragment { fragment ->
            // Call the function that should be triggered when the animation ends
            fragment.navigateToMainMenu()  // Manually trigger the navigation
        }

        // Introduce a delay to wait for the navigation to complete
        scenario.onFragment {
            assert(navController.currentDestination?.id == R.id.mainScreen)
        }
//
//        testDispatcher.scheduler.advanceTimeBy(10000L)
//        // Log the current destination for debugging
//        println("Current Destination: ${navController.currentDestination?.id}")
//        //assert(navController.currentDestination?.id == R.id.mainScreen)
    }
}
