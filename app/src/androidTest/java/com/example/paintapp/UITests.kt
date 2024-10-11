package com.example.paintapp

import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.filters.MediumTest
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)

@MediumTest
class TestAnimation {

    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var drawApp: DrawApp
    private lateinit var drawViewModel: DrawViewModel


    @Test
    fun testAnimationPost() {
        composeTestRule.setContent {
            ShowSplashScreenAnimation {}
            composeTestRule.mainClock.advanceTimeBy(4000)
        }
        composeTestRule.onNodeWithText("Welcome to the Paint App!").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("Let's start drawing").assertIsNotDisplayed()

    }

    @Test
    fun testNoButtonSplash() {
        composeTestRule.setContent {
            ShowSplashScreenAnimation {}
        }
        composeTestRule.onNodeWithText("New Drawing").isNotDisplayed()
    }
}