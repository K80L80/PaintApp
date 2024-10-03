/**Initial load screen with draw button to start draw fragment.
 * Date: 09/08/2024
 *
 */
package com.example.paintapp
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.example.paintapp.databinding.ActivityMainScreenBinding

//Welcome screen, should display a list of files already created, for new drawings have user enter text for the filename
class MainScreen : Fragment() {

    private var buttonFunction: (() -> Unit)? = null

    private val drawVM : DrawViewModel  by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ActivityMainScreenBinding.inflate(inflater, container, false)
        Log.i("MainScreen", "main screen created")


        val navController = findNavController()
        // Add ComposeView to show a LazyColumn
        binding.composeView.setContent {
            //load in all drawings from the view model and display is gallary
            val drawings by drawVM.drawings.observeAsState(emptyList())
            GalleryOfDrawings(drawings,navController,drawVM) //Required: List<Drawing> Found: LiveData<List<Drawing>>
            //LazyGrid(testDrawings)
        }

        //this is the button that moves to the draw screen
        binding.button2.setOnClickListener {
            //creates a new bitmap and adds it to drawing list
            drawVM.createNewDrawing()
            navController.navigate(R.id.action_mainScreen_to_drawFragment)
            Log.d("KT MainScreen", "navigate using action pass arguments using view model instead of safe-args ")
        }
        return binding.root

    }
    // Mock function to get a list of file names (you should replace it with real data source)
    fun getFileNames(): List<String> {
        // In real application, fetch the list of files from Room or file storage
        return listOf("Drawing1", "Drawing2", "Drawing3") // Example file names
    }
}

// Composable function to display the file list using LazyColumn
@Composable
fun GalleryOfDrawings(drawings: List<Drawing>, navController: NavController,vm:DrawViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(drawings) { drawing ->
            FileGridItem(drawing,navController, vm)
        }
    }
}

@Composable
fun FileGridItem(drawing: Drawing,navController: NavController, vm: DrawViewModel){
    val aspectRatio = getAspectRatioForOrientation()
    //creates box effect holds drawing and file name
    Column (
        horizontalAlignment = Alignment.CenterHorizontally, // Center align content
        modifier = Modifier
            .fillMaxWidth() // Adjust the width of each column
            .padding(8.dp)  // Add padding between grid items
            .border(BorderStroke(2.dp, Color.Gray)) // Add a border with 2dp thickness and gray color
            .clickable {
                //Use jetpack navigation and load in picture into custom draw
                vm.selectDrawing(drawing)
                navController.navigate(R.id.action_mainScreen_to_drawFragment)
        },
    ){
        //displays drawing
        Drawing(drawing.bitmap, aspectRatio)
        //displays file name
        Text(text = drawing.fileName)
    }
}
// Composable function to display a single file item
@Composable
fun Drawing(bitmap: Bitmap,  aspectRatio: Float) {
    // Convert Bitmap to ImageBitmap to use in Compose
    val imageBitmap = bitmap.asImageBitmap()

    // Display the Bitmap as an Image
    Image(
        bitmap = imageBitmap,
        contentDescription = "Drawing Thumbnail",
        contentScale = ContentScale.Crop, //to scale the image
        modifier = Modifier
            .aspectRatio(aspectRatio) // Keep each item square
            .padding(8.dp)
    )
}

@Composable
fun getAspectRatioForOrientation(): Float {
    val configuration = LocalConfiguration.current
    return if (configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
        9f / 16f // Portrait aspect ratio
    } else {
        16f / 9f // Landscape aspect ratio
    }
}



