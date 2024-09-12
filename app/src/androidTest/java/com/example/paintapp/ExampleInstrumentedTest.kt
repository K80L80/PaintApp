package com.example.paintapp

import android.graphics.Color
import android.util.Log
import androidx.core.graphics.red
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.fragment.app.activityViewModels

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule


import androidx.fragment.app.activityViewModels
import androidx.fragment.app.findFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.withFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.test.espresso.matcher.ViewMatchers.isActivated
import org.junit.Before


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    val vm = DrawViewModel()
    val lifeCycleOwner = TestLifecycleOwner()
    private lateinit var drawViewModel: DrawViewModel

    @Test
    fun startsCorrectly() {
        assertEquals(vm.getColor(), Color.BLACK)
        assertEquals(vm.getSize(), 5f)
        assertEquals(vm.getShape(), "free")
    }
    @Test
    fun basicTesting(){
        val before = vm.getColor()
        vm.setColor(Color.BLUE)
        assertNotEquals(before, vm.getColor())
        val new = vm.getColor()
        assertEquals(new, vm.getColor())
        vm.setColor(Color.GREEN)
        val new2 = vm.getColor()
        assertEquals(new2, vm.getColor())
        vm.setSize(10f)
        assertEquals(10f, vm.getSize())
        vm.setSize(5f)
        assertEquals(5f, vm.getSize())
    }

        @Test
        fun testColorChangeRed() {
        val scenario = launchFragmentInContainer<DrawFragment>()
            // Click on color button
            onView(withId(R.id.buttonChangeColor)).perform(click())

            // Select the "Red" color from the AlertDialog
            onView(withText("Red")).perform(click())

        scenario.onFragment { fragment ->
        val selectedColor = fragment.getPaintColor()
        assertEquals(Color.RED, selectedColor)


    }
    }

    @Test
    fun testColorChangeBlue() {
        val scenario = launchFragmentInContainer<DrawFragment>()

        // Click on color button
        onView(withId(R.id.buttonChangeColor)).perform(click())

        // Select the "Red" color from the AlertDialog
        onView(withText("Blue")).perform(click())

        scenario.onFragment { fragment ->
            val selectedColor = fragment.getPaintColor()
            assertEquals(Color.BLUE, selectedColor)
        }
    }

    @Test
    fun testColorSizeChange() {
        val scenario = launchFragmentInContainer<DrawFragment>()

        // Click on color button
        onView(withId(R.id.buttonChangeSize)).perform(click())

        // Select the "Red" color from the AlertDialog
        onView(withText("Small")).perform(click())

        scenario.onFragment { fragment ->
            val selectedSize = fragment.getPaintSize()
            assertEquals(5f, selectedSize)
        }
    }

    @Test
    fun testShapeChange() {
        val scenario = launchFragmentInContainer<DrawFragment>()

        // Click on color button
        onView(withId(R.id.buttonChangeShape)).perform(click())

        // Select the "Red" color from the AlertDialog
        onView(withText("Circle")).perform(click())

        scenario.onFragment { fragment ->
            val selectVal = fragment.getPaintShape()
            assertEquals("circle", selectVal)
        }
    }

    @Test
    fun testColorShapeChange() {
        val scenario = launchFragmentInContainer<DrawFragment>()

        // Click on color button
        onView(withId(R.id.buttonChangeShape)).perform(click())

        // Select the "Red" color from the AlertDialog
        onView(withText("Square")).perform(click())

        onView(withId(R.id.buttonChangeColor)).perform(click())

        // Select the "Blue" color from the AlertDialog
        onView(withText("Green")).perform(click())

        scenario.onFragment { fragment ->
            val selectVal = fragment.getPaintShape()
            assertEquals("square", selectVal)
        }

        scenario.onFragment { fragment ->
            val selectedColor = fragment.getPaintColor()
            assertEquals(Color.GREEN, selectedColor)
        }
    }

    @Test
    fun testColorShapeSizeChange() {
        val scenario = launchFragmentInContainer<DrawFragment>()

        // Click on color button
        onView(withId(R.id.buttonChangeShape)).perform(click())

        // Select the "Red" color from the AlertDialog
        onView(withText("Square")).perform(click())

        onView(withId(R.id.buttonChangeColor)).perform(click())

        // Select the "Blue" color from the AlertDialog
        onView(withText("Green")).perform(click())

        // Click on color button
        onView(withId(R.id.buttonChangeSize)).perform(click())

        // Select the "Red" color from the AlertDialog
        onView(withText("Large")).perform(click())

        scenario.onFragment { fragment ->
            val selectedSize = fragment.getPaintSize()
            assertEquals(20f, selectedSize)
        }

        scenario.onFragment { fragment ->
            val selectVal = fragment.getPaintShape()
            assertEquals("square", selectVal)
        }

        scenario.onFragment { fragment ->
            val selectedColor = fragment.getPaintColor()
            assertEquals(Color.GREEN, selectedColor)
        }
    }

    @Test
    fun testNavigateToDrawFragment() {
        // Launch the MainView fragment
        val scenario = launchFragmentInContainer<MainScreen>()

        // Click on the button that triggers navigation to DrawFragment
        onView(withId(R.id.button2)).perform(click())

        // Verify that the DrawFragment is displayed using FragmentManager
//        scenario.onFragment { fragment ->
//            Log.wtf("Fragment Test", "Current Fragment: ${fragment?.javaClass?.simpleName}")
//            Log.wtf("Fragment Test", "Expected Fragment: ${DrawFragment::class.java.simpleName}")
//            assertTrue(fragment.requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container) is DrawFragment)
//        }
    }

//        @Test
//        fun testColorChangeBlue() {
//            // Open DrawFragment
//            onView(withId(R.id.button2)).perform(click())
//
//            // Click on color button
//            onView(withId(R.id.buttonChangeColor)).perform(click())
//
//            // Select the "Red" color from the AlertDialog
//            onView(withText("Blue")).perform(click())
//
//            // Verify that the Paint color is updated to Red
//
//            assertEquals(Color.BLUE, drawViewModel.paintTool.value?.paint?.color)
//        }
//
//        @Test
//        fun testColorChangeGreen() {
//            // Open DrawFragment
//            onView(withId(R.id.button2)).perform(click())
//
//            // Click on color button
//            onView(withId(R.id.buttonChangeColor)).perform(click())
//
//            // Select the "Red" color from the AlertDialog
//            onView(withText("Green")).perform(click())
//
//            // Verify that the Paint color is updated to Red
//
//            assertEquals(Color.GREEN, drawViewModel.paintTool.value?.paint?.color)
//        }
//
//
//        @Test
//        fun testSizeChangeLarge() {
//            // Open DrawFragment
//            onView(withId(R.id.button2)).perform(click())
//
//            // Click on size button
//            onView(withId(R.id.buttonChangeSize)).perform(click())
//
//            // Select "Large" size from the AlertDialog
//            onView(withText("Large")).perform(click())
//
//            // Verify that the stroke width is updated to the correct size
//
//            assertEquals(20f, drawViewModel.paintTool.value?.paint?.strokeWidth)
//        }
//
//        @Test
//        fun testSizeChangeSmall() {
//            // Open DrawFragment
//            onView(withId(R.id.button2)).perform(click())
//
//            // Click on size button
//            onView(withId(R.id.buttonChangeSize)).perform(click())
//
//            // Select "Large" size from the AlertDialog
//            onView(withText("Large")).perform(click())
//
//            // Verify that the stroke width is updated to the correct size
//
//            assertEquals(5f, drawViewModel.paintTool.value?.paint?.strokeWidth)
//        }
//
//
//        @Test
//        fun testSizeChangeMed() {
//            // Open DrawFragment
//            onView(withId(R.id.button2)).perform(click())
//
//            // Click on size button
//            onView(withId(R.id.buttonChangeSize)).perform(click())
//
//            // Select "Large" size from the AlertDialog
//            onView(withText("Large")).perform(click())
//
//            // Verify that the stroke width is updated to the correct size
//
//            assertEquals(10f, drawViewModel.paintTool.value?.paint?.strokeWidth)
//        }
//
//        @Test
//        fun testShapeChangeCircle() {
//            // Open DrawFragment
//            onView(withId(R.id.button2)).perform(click())
//
//            // Click on shape button
//            onView(withId(R.id.buttonChangeShape)).perform(click())
//
//            // Select "Circle" from the AlertDialog
//            onView(withText("Circle")).perform(click())
//
//            // Verify that the shape is updated to "circle"
//
//            assertEquals("circle", drawViewModel.paintTool.value?.shape)
//        }
//
//        @Test
//        fun testShapeChangeSquare() {
//            // Open DrawFragment
//            onView(withId(R.id.button2)).perform(click())
//
//            // Click on shape button
//            onView(withId(R.id.buttonChangeShape)).perform(click())
//
//            // Select "Circle" from the AlertDialog
//            onView(withText("Square")).perform(click())
//
//            // Verify that the shape is updated to "circle"
//
//            assertEquals("square", drawViewModel.paintTool.value?.shape)
//        }
//
//        @Test
//        fun testShapeChangeDiamond() {
//            // Open DrawFragment
//            onView(withId(R.id.button2)).perform(click())
//
//            // Click on shape button
//            onView(withId(R.id.button2)).perform(click())
//
//            // Select "Circle" from the AlertDialog
//            onView(withText("Diamond")).perform(click())
//
//            // Verify that the shape is updated to "circle"
//
//            assertEquals("diamond", drawViewModel.paintTool.value?.shape)
//        }
//
//
//        @Test
//        fun testShapeChangeRect() {
//            // Open DrawFragment
//            onView(withId(R.id.button2)).perform(click())
//
//            // Click on shape button
//            onView(withId(R.id.buttonChangeShape)).perform(click())
//
//            // Select "Circle" from the AlertDialog
//            onView(withText("Rectangle")).perform(click())
//
//            // Verify that the shape is updated to "circle"
//
//            assertEquals("rectangle", drawViewModel.paintTool.value?.shape)
//        }
//
//        @Test
//        fun testShapeChangeLine() {
//            // Open DrawFragment
//            onView(withId(R.id.button2)).perform(click())
//
//            // Click on shape button
//            onView(withId(R.id.buttonChangeShape)).perform(click())
//
//            // Select "Circle" from the AlertDialog
//            onView(withText("Line")).perform(click())
//
//            // Verify that the shape is updated to "circle"
//
//            assertEquals("line", drawViewModel.paintTool.value?.shape)
//        }
    }
