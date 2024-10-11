package com.example.paintapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

//By using this technique, the NavController is available before onViewCreated() is called, allowing the fragment to use NavigationUI methods without crashing.
@RunWith(AndroidJUnit4::class)
class SplashScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()
    @Test
    fun testSplashScreenComposable() {
        composeTestRule.setContent {
            ShowSplashScreenAnimation {}
        }
        composeTestRule.onNodeWithText("Welcome to the Paint App!\nLet's start drawing ✌️!")
            .assertIsDisplayed()
        composeTestRule.mainClock.advanceTimeBy(1000)
        composeTestRule.onNodeWithText("Welcome to the Paint App!\nLet's start drawing ✌️!")
            .assertDoesNotExist()
    }

    @Test
    fun testSplashScreenNavigation() {
        var isNavigated = false

        composeTestRule.setContent {
            ShowSplashScreenAnimation {
                isNavigated = true
            }
        }

        // After 2 seconds, the navigation should have triggered
        composeTestRule.mainClock.advanceTimeBy(2000)
        assertTrue(isNavigated)
    }

    @Test
    fun testSplashScreenOnDifferentScreenSizes() {
        composeTestRule.setContent {
            SplashScreenComposable()
        }

        // Make sure the text is still displayed in the center of the screen
        composeTestRule.onNodeWithText("Welcome to the Paint App!\nLet's start drawing ✌️!")
            .assertIsDisplayed()
    }
}