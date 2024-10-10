package com.example.paintapp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
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

    //Test that after the splash animation you arrive on the main screen
    @RunWith(AndroidJUnit4::class)
    class SplashScreenFragmentTest {

        @get:Rule
        val composeTestRule = createAndroidComposeRule<MainActivity>() // Any host activity

        @Test
        fun testSplashScreenAnimationTriggersNavigation() {
            // Initialize the NavController
            val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

            // Launch the SplashFragment in the container
            val scenario = launchFragmentInContainer<SplashScreenFragment>()

            scenario.onFragment { fragment ->
                // Attach the NavController to the fragment
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        // Set up the NavController with the navigation graph
                        navController.setGraph(R.navigation.nav_graph)
                        Navigation.setViewNavController(fragment.requireView(), navController)
                    }
                }
            }

            // Wait for idle state to ensure the fragment and composables are set up
            composeTestRule.waitForIdle()

            // Simulate the passage of time to let the animation complete (2 seconds: 1 sec fade-in + 1 sec hold)
            composeTestRule.mainClock.advanceTimeBy(2000)

            // Assert that the NavController navigated to the expected destination after animation completes
            assert(navController.currentDestination?.id == R.id.mainScreen)
        }
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
    fun navigationToMainMenu() {
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
        scenario.onFragment { fragment ->
            // Call the function that should be triggered when the animation ends
            fragment.navigateToMainMenu()  // Manually trigger the navigation
        }

        // Introduce a delay to wait for the navigation to complete
        scenario.onFragment {
            assert(navController.currentDestination?.id == R.id.mainScreen)
        }
    }


    //While on main menu does clicking 'new drawing' button take you to the drawing screen?
    @Test
    fun mainMenuToDrawFragment() {

        val mainScreen = launchFragmentInContainer<MainScreen>()
        // Step 3: Set up the mock NavController when the view is created

        mainScreen.onFragment { fragment ->
            fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                val navController = TestNavHostController(fragment.requireContext())
                navController.setGraph(R.navigation.nav_graph)

                // Set the current destination to MainScreen manually
                navController.setCurrentDestination(R.id.mainScreen)

                // Attach the NavController to the fragment's view
                Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }


        // Step 4: Simulate the button click that should trigger navigation to DrawFragment
        onView(withId(R.id.button2)).perform(click())

        // Use Espresso idle synchronization to ensure UI is idle before checking nav state
        Espresso.onIdle()

        // Introduce a delay to wait for the navigation to complete
        mainScreen.onFragment { fragment ->
            val navController = Navigation.findNavController(fragment.requireView())
            assert(navController.currentDestination?.id == R.id.drawFragment)
        }
    }
}
