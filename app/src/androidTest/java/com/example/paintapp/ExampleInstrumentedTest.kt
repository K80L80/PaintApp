package com.example.paintapp

import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.viewModels
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.NavHostFragment.Companion.create
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.mockito.Mockito.*
import org.mockito.kotlin.any


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)


@MediumTest
class testanimation {

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
@MediumTest
class VmTests {

    @get:Rule
    val composeTestRule = createComposeRule()
    val lifeCycleOwner = TestLifecycleOwner()
    private lateinit var repository: DrawRepository

    private lateinit var drawViewModel: DrawViewModel
    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        val appContext =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as DrawApp

        repository = appContext.drawRepository
        drawViewModel = DrawViewModel(repository)
        navController = TestNavHostController(appContext)
    }



    @Test
    fun `testAdd`() {
        composeTestRule.setContent {
            drawViewModel.createNewDrawing()
        }
        assert(drawViewModel.drawings.value?.isNotEmpty() == true)
    }

    @Test
    fun `testSetCircleShape`() {
        val shape = "circle"
        composeTestRule.setContent {
            drawViewModel.setShape(shape)
        }
        assert(drawViewModel.paintTool.value?.currentShape == shape)
    }

    @Test
    fun `testSetSquareShape`() {
        val shape = "square"
        composeTestRule.setContent {
            drawViewModel.setShape(shape)
        }
        assert(drawViewModel.paintTool.value?.currentShape == shape)
    }

    @Test
    fun `testSetLineShape`() {
        val shape = "line"
        composeTestRule.setContent {
            drawViewModel.setShape(shape)
        }
        assert(drawViewModel.paintTool.value?.currentShape == shape)
    }

    @Test
    fun `testSetDiamondShape`() {
        val shape = "diamond"
        composeTestRule.setContent {
            drawViewModel.setShape(shape)
        }
        assert(drawViewModel.paintTool.value?.currentShape == shape)
    }

    @Test
    fun `testSetRectShape`() {
        val shape = "rectangle"
        composeTestRule.setContent {
            drawViewModel.setShape(shape)
        }
        assert(drawViewModel.paintTool.value?.currentShape == shape)
    }


    @Test
    fun `testFreeShape`() {
        val shape = "free"
        composeTestRule.setContent {
            drawViewModel.setShape(shape)
        }
        assert(drawViewModel.paintTool.value?.currentShape == shape)
    }

    @Test
    fun `checkSize`() {
        var size = 0f
        composeTestRule.setContent {
             size = drawViewModel.getSize()!!
        }
        assert(size == 30f)
    }

    @Test
    fun `checkColor`() {
        var color = Color.BLUE
        composeTestRule.setContent {
            color = drawViewModel.getColor()!!
        }
        assert(color == Color.BLACK)
    }


    @Test
    fun `testClearNotDelete`() {
        composeTestRule.setContent {
            drawViewModel.createNewDrawing()
            drawViewModel.resetDrawing(800, 600)
        }
        assert(drawViewModel.drawings.value?.isNotEmpty() == true)
    }

    @Test
    fun createDefaultDraw(){
        var drawing = createDefaultDrawing(1,1000,1000,"default.png")
        assert(drawing.id.toInt() == 1)
        assert(drawing.fileName == "default.png")
    }

    @Test
    fun testMainScreen(){
        composeTestRule.setContent {
            GalleryOfDrawings(generateTestDrawings(),navController,drawViewModel)
        }

        composeTestRule.onNodeWithText("New Drawing").isDisplayed()
    }
}

//@MediumTest
//class DrawFragmentTest {
//
//    @get:Rule
//    val composeTestRule = createComposeRule()
//
////    @Test
////    fun testNavigation() {
////        composeTestRule.setContent {
////            val navController = rememberNavController()
////                ShowSplashScreenAnimation{}
////            composeTestRule.mainClock.advanceTimeBy(3500)
////        }
////        }
////    }
//
//    @Test
//    fun testDrawFragment() {
//        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
//        val drawScreen = launchFragmentInContainer<DrawFragment>()
//
//        drawScreen.onFragment { fragment ->
//            navController.setGraph(R.navigation.nav_graph)
//            Navigation.setViewNavController(fragment.requireView(), navController)
//        }
//        onView(withId(R.id.buttonReset)).perform(click()).check(matches(isClickable()))
//    }
//}

//@MediumTest
//class MainScreenTest {
//
//    @get:Rule
//    val composeTestRule = createComposeRule()
//
//    @Test
//    fun testMainFragment() {
//        val appContext =
//            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as DrawApp
//        val navController = TestNavHostController(appContext)
//
//        val scenario = launchFragmentInContainer<MainScreen>()
//        drawFragment.onFragment { fragment ->
//            var navControl = findNavController(fragment)
//            Navigation.setViewNavController(fragment.requireView(), navControl)
//        }
//
//        onView(withId(R.id.button2)).perform(click()).check(matches(isClickable()))

//    }
//}



/**Previous tests below this point no longer work with changed functionality.
 *
 */
//class instrumentedTest {
//
//
//    val lifeCycleOwner = TestLifecycleOwner()
//    private lateinit var vm: DrawViewModel
//    private lateinit var repository: DrawRepository
//
//    @Before
//    fun setup() {
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as DrawApp
//        repository = appContext.drawRepository
//
//        vm = DrawViewModel(repository) // error under appContext Too many arguments for public constructor DrawViewModel(drawRepository: DrawRepository) defined in com.example.paintapp.DrawViewModel
//    }
//
//    @Test
//    fun testNav() {
//        val navController = TestNavHostController(
//            ApplicationProvider.getApplicationContext()
//        )
//        val scenario = launchFragmentInContainer<MainScreen>()
//
//        scenario.onFragment { fragment ->
//            navController.setGraph(R.navigation.nav_graph)
//            Navigation.setViewNavController(fragment.requireView(), navController)
//
//            Log.wtf("Fragment Test", "Current Fragment: ${fragment?.javaClass?.simpleName}")
//            assertTrue(fragment?.javaClass?.simpleName == "MainScreen")
//        }
//    }
//
//    @Test
//    fun startsCorrectly() {
//        assertEquals(vm.getColor(), Color.BLACK)
//        assertEquals(vm.getSize(), 5f)
//        assertEquals(vm.getShape(), "free")
//    }
//    @Test
//    fun basicTesting(){
//        val before = vm.getColor()
//        vm.setColor(Color.BLUE)
//        assertNotEquals(before, vm.getColor())
//        val new = vm.getColor()
//        assertEquals(new, vm.getColor())
//        vm.setColor(Color.GREEN)
//        val new2 = vm.getColor()
//        assertEquals(new2, vm.getColor())
//        vm.setSize(10f)
//        assertEquals(10f, vm.getSize())
//        vm.setSize(5f)
//        assertEquals(5f, vm.getSize())
//    }
//
//    @Test
//    fun testNavigate() {
//        // Launch the MainView fragment
//        val scenario = launchFragmentInContainer<MainScreen>()
//
//        scenario.onFragment { fragment ->
//            Log.wtf("Fragment Test", "Current Fragment: ${fragment?.javaClass?.simpleName}")
//            assertTrue(fragment?.javaClass?.simpleName == "MainScreen")
//        }
//    }
//
//    @Test
//    fun testNavigateMainButton() {
//        val scenario = launchFragmentInContainer<MainScreen>()
//        onView(withId(R.id.button2)).perform(click()).check(matches(isClickable()))
//
//    }
//
//    @Test
//        fun testColorChangeRed() {
//        val scenario = launchFragmentInContainer<DrawFragment>()
//            // Click on color button
//            onView(withId(R.id.buttonChangeColor)).perform(click())
//
//            // Select the "Red" color from the AlertDialog
//            onView(withText("Red")).perform(click())
//
//        scenario.onFragment { fragment ->
//        val selectedColor = fragment.getPaintColor()
//        assertEquals(Color.RED, selectedColor)
//    }
//    }
//
//    @Test
//    fun testColorChangeBlue() {
//        val scenario = launchFragmentInContainer<DrawFragment>()
//
//        // Click on color button
//        onView(withId(R.id.buttonChangeColor)).perform(click())
//
//
//        // Select the "Blue" color from the AlertDialog
//        onView(withText("Blue")).perform(click())
//
//        scenario.onFragment { fragment ->
//            val selectedColor = fragment.getPaintColor()
//            assertEquals(Color.BLUE, selectedColor)
//        }
//    }
//
//    @Test
//    fun testColorChangeGreen() {
//        val scenario = launchFragmentInContainer<DrawFragment>()
//
//        // Click on color button
//        onView(withId(R.id.buttonChangeColor)).perform(click())
//
//
//        // Select the "Green" color from the AlertDialog
//        onView(withText("Green")).perform(click())
//
//        scenario.onFragment { fragment ->
//            val selectedColor = fragment.getPaintColor()
//            assertEquals(Color.GREEN, selectedColor)
//        }
//    }
//
//
//
//    @Test
//    fun testColorSizeChange() {
//        // Launch the DrawFragment in a container
//        val scenario = launchFragmentInContainer<DrawFragment>()
//
//        // Click on the size button to show the slider
//        onView(withId(R.id.buttonChangeSize)).check(matches(isDisplayed()))
//
//
//        // Verify the size value in the fragment's ViewModel
//        scenario.onFragment { fragment ->
//            val selectedSize = fragment.getPaintSize()
//            assertEquals(5f, selectedSize)
//        }
//    }
//
//        @Test
//    fun testShapeChange1() {
//        val scenario = launchFragmentInContainer<DrawFragment>()
//
//        // Click on shape button
//        onView(withId(R.id.buttonChangeShape)).perform(click())
//
//        // Select the "Square" shape from the AlertDialog
//        onView(withText("Square")).perform(click())
//
//        onView(withId(R.id.buttonChangeColor)).perform(click())
//
//        // Select the "Green" color from the AlertDialog
//        onView(withText("Green")).perform(click())
//
//        scenario.onFragment { fragment ->
//            val selectVal = fragment.getPaintShape()
//            assertEquals("square", selectVal)
//        }
//
//        scenario.onFragment { fragment ->
//            val selectedColor = fragment.getPaintColor()
//            assertEquals(Color.GREEN, selectedColor)
//        }
//    }
//
//    @Test
//    fun testShapeChange2() {
//        val scenario = launchFragmentInContainer<DrawFragment>()
//
//        // Click on shape button
//        onView(withId(R.id.buttonChangeShape)).perform(click())
//
//        // Select the "Circle" shape from the AlertDialog
//        onView(withText("Circle")).perform(click())
//
//        scenario.onFragment { fragment ->
//            val selectVal = fragment.getPaintShape()
//            assertEquals("circle", selectVal)
//        }
//
//    }
//
//
//    @Test
//    fun testShapeChange3() {
//        val scenario = launchFragmentInContainer<DrawFragment>()
//
//        // Click on shape button
//        onView(withId(R.id.buttonChangeShape)).perform(click())
//
//        // Select the "diamond" shape from the AlertDialog
//        onView(withText("Diamond")).perform(click())
//
//        scenario.onFragment { fragment ->
//            val selectVal = fragment.getPaintShape()
//            assertEquals("diamond", selectVal)
//        }
//
//    }
//
//    @Test
//    fun testShapeChangeMulti() {
//        val scenario = launchFragmentInContainer<DrawFragment>()
//
//        // Click on shape button
//        onView(withId(R.id.buttonChangeShape)).perform(click())
//
//        // Select the "diamond" shape from the AlertDialog
//        onView(withText("Diamond")).perform(click())
//
//        // Click on shape button
//        onView(withId(R.id.buttonChangeShape)).perform(click())
//
//        // Select the "line" shape from the AlertDialog
//        onView(withText("Line")).perform(click())
//
//        scenario.onFragment { fragment ->
//            val selectVal = fragment.getPaintShape()
//            assertEquals("line", selectVal)
//        }
//
//    }



    //TESTS BELOW: Previous versions of code tests. Left just in case we adjust
    //back to previous versions, or change something to a previous format.
    //Were also implementing a color wheel and will require updating these

    //    @Test
//    fun testColorShapeChange() {
//        val scenario = launchFragmentInContainer<DrawFragment>()
//
//        // Click on color button
//        onView(withId(R.id.buttonChangeShape)).perform(click())
//
//        // Select the "Red" color from the AlertDialog
//        onView(withText("Square")).perform(click())
//
//        onView(withId(R.id.buttonChangeColor)).perform(click())
//
//        // Select the "Blue" color from the AlertDialog
//        onView(withText("Green")).perform(click())
//
//        scenario.onFragment { fragment ->
//            val selectVal = fragment.getPaintShape()
//            assertEquals("square", selectVal)
//        }
//
//        scenario.onFragment { fragment ->
//            val selectedColor = fragment.getPaintColor()
//            assertEquals(Color.GREEN, selectedColor)
//        }
//    }

    //    @Test
//    fun testColorSizeChange() {
//        val scenario = launchFragmentInContainer<DrawFragment>()
//
//        // Click on color button
//        onView(withId(R.id.buttonChangeSize)).perform(click())
//        onView(withId(R.id.sizeSlider)).perform(swipeLeft())
//
//        // Select the "Red" color from the AlertDialog
////        onView(withText("Small")).perform(click())
//
//        scenario.onFragment { fragment ->
//            val selectedSize = fragment.getPaintSize()
//            assertEquals(5f, selectedSize)
//        }
//    }

    //    @Test
//    fun testColorSizeChange() {
//        val scenario = launchFragmentInContainer<DrawFragment>()
//
//        // Click on color button
//        onView(withId(R.id.buttonChangeSize)).perform(click())
//        onView(withId(R.id.sizeSlider)).perform(swipeLeft())
//
//        // Select the "Red" color from the AlertDialog
////        onView(withText("Small")).perform(click())
//
//        scenario.onFragment { fragment ->
//            val selectedSize = fragment.getPaintSize()
//            assertEquals(5f, selectedSize)
//        }
//    }

//    @Test
//    fun testSizeChange() {
//        val scenario = launchFragmentInContainer<DrawFragment>()
//
//
//        scenario.onFragment { fragment ->
//            val selectedSize = fragment.getPaintSize()
//            assertEquals(20f, selectedSize)
//        }
//
//        scenario.onFragment { fragment ->
//            val selectVal = fragment.getPaintShape()
//            assertEquals("square", selectVal)
//        }
//
//        scenario.onFragment { fragment ->
//            val selectedColor = fragment.getPaintColor()
//            assertEquals(Color.GREEN, selectedColor)
//        }
//    }

//    @Test
//    fun testColorShapeSizeChange() {
//        val scenario = launchFragmentInContainer<DrawFragment>()
//
//        // Click on color button
//        onView(withId(R.id.buttonChangeShape)).perform(click())
//
//        // Select the "Red" color from the AlertDialog
//        onView(withText("Square")).perform(click())
//
//        onView(withId(R.id.buttonChangeColor)).perform(click())
//
//        // Select the "Blue" color from the AlertDialog
//        onView(withText("Green")).perform(click())
//
//        // Click on color button
//        onView(withId(R.id.buttonChangeSize)).perform(click())
//
//        // Select the "Red" color from the AlertDialog
//        onView(withText("Large")).perform(click())
//
//        scenario.onFragment { fragment ->
//            val selectedSize = fragment.getPaintSize()
//            assertEquals(20f, selectedSize)
//        }
//
//        scenario.onFragment { fragment ->
//            val selectVal = fragment.getPaintShape()
//            assertEquals("square", selectVal)
//        }
//
//        scenario.onFragment { fragment ->
//            val selectedColor = fragment.getPaintColor()
//            assertEquals(Color.GREEN, selectedColor)
//        }
//    }



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
