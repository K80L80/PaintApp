package com.example.paintapp

import android.graphics.Color
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
    fun createDefaultDraw() {
        var drawing = createDefaultDrawing(1, 1000, 1000, "default.png")
        assert(drawing.id.toInt() == 1)
        assert(drawing.fileName == "default.png")
    }

    @Test
    fun testMainScreen() {
        composeTestRule.setContent {
            TitleGallary(generateTestDrawings(), {}, drawViewModel)
        }
        composeTestRule.onNodeWithText("New Drawing").isDisplayed()
    }
}