package com.example.paintapp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

//By using this technique, the NavController is available before onViewCreated() is called, allowing the fragment to use NavigationUI methods without crashing.
@RunWith(AndroidJUnit4::class)
class NavigationTests {
    private lateinit var navController: TestNavHostController
    private lateinit var repository: DrawRepository
    private lateinit var drawViewModel: DrawViewModel
    private var scenario: FragmentScenario<SplashScreenFragment>? = null

    @Before
    fun setUp() {
        val appContext =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as DrawApp

        repository = appContext.drawRepository
        drawViewModel = DrawViewModel(repository)
        navController = TestNavHostController(appContext)
    }

    @After
    fun destroyFragment() {
            scenario?.moveToState(Lifecycle.State.DESTROYED)
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>() // Any host activity

    @Test
    fun testSplashScreenAnimationTriggersNavigation() {
        // Initialize the NavController
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        // Launch the SplashFragment in the container
        val scenario = launchFragmentInContainer<SplashScreenFragment>()

        scenario.onFragment { fragment ->
            fragment.viewLifecycleOwnerLiveData.observeForever {
                if (fragment.view != null) {
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


    @Test
    fun assertNavControllerIsAttached() {
        // Initialize the TestNavHostController
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        // Launch the fragment in the container
        val scenario = launchFragmentInContainer {
            SplashScreenFragment().also { fragment ->
                // Observe the view lifecycle to attach the NavController before the fragment is resumed
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (fragment.view != null) {
                        // The fragment’s view has just been created
                        navController.setGraph(R.navigation.nav_graph) // Use your actual navigation graph
                        Navigation.setViewNavController(fragment.requireView(), navController)
                        assert(Navigation.findNavController(fragment.requireView()) == navController)
                    }
                }
            }
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
                fragment.viewLifecycleOwnerLiveData.observeForever {
                    // The fragment’s view has just been created
                    if (fragment.view != null) {
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

    //While on main menu does clicking 'new drawing' button take you to the drawing screen?
    @Test
    fun mainMenuToDrawFragment() {
        val mainScreen = launchFragmentInContainer<MainScreen>()
        mainScreen.onFragment { fragment ->
            fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                if (fragment.view != null) {
                    val navController = TestNavHostController(fragment.requireContext())
                    navController.setGraph(R.navigation.nav_graph)

                    // Set the current destination to MainScreen manually
                    navController.setCurrentDestination(R.id.mainScreen)

                    // Attach the NavController to the fragment's view
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }
        }
        //Simulate the button click that should trigger navigation to DrawFragment
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
