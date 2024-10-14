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
import android.widget.EditText
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.paintapp.databinding.ActivityMainScreenBinding
import android.app.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged

//Welcome screen, should display a list of files already created, for new drawings have user enter text for the filename
data class DrawingActions(
    val onFileNameChange: (Long, String) -> Unit,
    val onDrawingSelect: (Drawing) -> Unit,
    val navigateToDrawScreen: (() -> Unit),
    val onClickNewDrawingBtn: (String) -> Unit // Callback for creating a new drawing
)

class MainScreen : Fragment() {

    //makes the view-model accessible in the fragment
    private val drawVM: DrawViewModel by activityViewModels {
        VMFactory((requireActivity().application as DrawApp).drawRepository)
    }

    //setup all the callbacks to handle user interactinon
    val actions = DrawingActions(
        onFileNameChange = ::onFileNameChange, //callback to let user re-name their file
        onDrawingSelect = ::onDrawingSelect, //callback to let user user to 'select' a drawing to edit
        navigateToDrawScreen = ::navigateToDrawScreen, //callback to navigate from here gallary to draw screen
        onClickNewDrawingBtn = ::onClickNewDrawingBtn// Callback for creating a new drawing
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = ActivityMainScreenBinding.inflate(inflater, container, false)

        // Add ComposeView to show a LazyColumn
        binding.composeView.setContent {
            //load in all drawings from the view model and display is gallary
            val drawings by drawVM.drawings.observeAsState(emptyList())

            //setup all the callbacks to handle user interactinon
            TitleGallary(drawings, actions)
        }

        //create new drawing button
        binding.button2.setOnClickListener {
            // Show the dialog and handle the file name entered by the user
            showNewDrawingDialog{ enteredFileName ->
                onClickNewDrawingBtn(enteredFileName)
            }
        }
        return binding.root
    }

    //callback to navigate from gallary to draw screen
    private fun navigateToDrawScreen() {
        val navController = findNavController()
        navController.navigate(R.id.action_mainScreen_to_drawFragment)
    }

    //callback to let user rename the file
    private fun onFileNameChange(drawingId: Long, newFileName: String) {
        drawVM.updateDrawingFileName(drawingId, newFileName)
    }

    //called when user clicks a specific drawing tile from the gallary
    private fun onDrawingSelect(drawing: Drawing) {
        drawVM.selectDrawing(drawing)
    }

    //When the user clicks 'new drawing button'
    private fun onClickNewDrawingBtn(fileName :String?){
        drawVM.createNewDrawing(fileName)   //creates a new bitmap and adds it to drawing list
        actions.navigateToDrawScreen.invoke()
    }

    // Method to show the dialog
    private fun showNewDrawingDialog(onFileNameEntered: (String?) -> Unit) {
        // Create an EditText for user input
        val input = EditText(requireContext())
        input.hint = "Enter file name"

        // Create an AlertDialog with the EditText
        AlertDialog.Builder(requireContext())
            .setTitle("Enter File Name")
            .setView(input) // Set the EditText into the dialog
            .setPositiveButton("OK") { dialog, _ ->
                val fileName = input.text.toString()
                // Pass the file name to your ViewModel or handle it
                onFileNameEntered(fileName) //Failing here but why do I need to do any sort of check if it is empty or null it should be handled already right?
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}
// Composable function to display the file list using LazyColumn
@Composable
fun TitleGallary(drawings: List<Drawing>, actions: DrawingActions) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)

    ) {
        items(drawings.reversed()) { drawing ->
            // Adding a delay before loading each drawing
            Tile(drawing,actions)
        }
    }
}
//The parent owns the state?
@Composable
fun Tile(drawing: Drawing, actions: DrawingActions){
    val aspectRatio = getAspectRatioForOrientation()
    //creates box effect holds drawing and file name
    Column (
        horizontalAlignment = Alignment.CenterHorizontally, // Center align content
        modifier = Modifier
            .fillMaxWidth() // Adjust the width of each column
            .padding(8.dp)  // Add padding between grid items
            .border(BorderStroke(2.dp, Color.Gray)) // Add a border with 2dp thickness and gray color
    ){
        //displays drawing
        Drawing(
            bitmap = drawing.bitmap,
            aspectRatio = aspectRatio,
            onBitmapClick = {
                Log.d("GalleryOfDrawings", "Bitmap clicked for drawing: ${drawing.id}")
                //Use jetpack navigation and load in picture into custom draw
                actions.onDrawingSelect(drawing) //Need to refactor this doesn't have access to drawing just bitmap
                actions.navigateToDrawScreen()
            }
        )
        //displays file name
        fileNameDisplay(
            fileName = drawing.userChosenFileName,
            onFileNameChange = { newFileName ->
                Log.d("fileNameDisplay", "filename change for: ${drawing.id}")
                actions.onFileNameChange(drawing.id, newFileName)
            }
        )
    }
}
//Composable to display the file name
//The composable can manage local state for focus and edit mode, but the final updates should be propagated back to the ViewModel only when necessary (e.g., when the user completes editing).
@Composable
fun fileNameDisplay(fileName: String, onFileNameChange: (String) -> Unit) {
    // Track whether the TextField is in edit mode
    var isEditing by remember { mutableStateOf(false) }
    var localFileName by remember { mutableStateOf(fileName) }

    val focusRequester = FocusRequester()
    // Display File name in edit mode
    if (isEditing) {
        // Automatically request focus when the TextField becomes visible
        Log.d("MainScreen", "in editing mode")
        LaunchedEffect(Unit) {
            Log.d("MainScreen", "Text Field requesting focus")
            focusRequester.requestFocus()
        }
        TextField(
            modifier = Modifier
                .focusRequester(focusRequester) //register focus requester
                .onFocusChanged { focusState->
                    if (focusState.isFocused){
                        Log.d("MainScreen", "TextField Focused changed Active")
                    } else {
                        Log.d("MainScreen", "TextField Focused changed Inactive")
                    }
                },
            value = localFileName,
            onValueChange = { localFileName = it},
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                isEditing = false
                onFileNameChange(localFileName) // Save the new name
            }),

        )
    } else {
        // Display the file name as text (display mode)
        Text(
            text = localFileName,
            modifier = Modifier
                .clickable {
                    Log.d("MainScreen", "Text clicked, entering editing mode")
                    isEditing = true // Enter edit mode on click
                }
        )
    }
}

// Composable function to display image thumbnail
@Composable
fun Drawing(bitmap: Bitmap, aspectRatio: Float, onBitmapClick: () -> Unit) {
    // Convert Bitmap to ImageBitmap to use in Compose
    val imageBitmap = bitmap.asImageBitmap()
    // Display the Bitmap as an Image
    Image(
        bitmap = imageBitmap,
        contentDescription = "Drawing Thumbnail",
        modifier = Modifier
            .aspectRatio(aspectRatio) // Keep each item square
            .padding(8.dp)
            .border(BorderStroke(2.dp, Color.Gray))
            //user clicks on the drawing they want to modify
            .clickable {
                onBitmapClick.invoke()
            }
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

@Composable
fun PopUpExample() {
    // State to control the visibility of the pop-up
    var showDialog by remember { mutableStateOf(false) }

    // Layout with a button
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = {
            showDialog = true // Show pop-up when button is clicked
        }) {
            Text("Show Pop-up")
        }

        // Show AlertDialog (Pop-up) when showDialog is true
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false }, // Close when clicking outside or back
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Pop-up Title") },
                text = { Text("This is a pop-up message!") }
            )
        }
    }
}


//
//package com.example.paintapp
//import android.graphics.Bitmap
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.activity.viewModels
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.aspectRatio
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.material.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.platform.LocalConfiguration
//import androidx.compose.ui.unit.dp
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.activityViewModels
//import androidx.fragment.app.viewModels
//import androidx.navigation.NavController
//import androidx.navigation.fragment.findNavController
//import com.example.paintapp.databinding.ActivityMainScreenBinding
//
////Welcome screen, should display a list of files already created, for new drawings have user enter text for the filename
//class MainScreen : Fragment() {
//
//    //makes the view-model accessible in the fragment
//    private val drawVM: DrawViewModel by activityViewModels { VMFactory((requireActivity().application as DrawApp).drawRepository) }
//
//    private var navigateToDrawFragment: () -> Unit  = {
//        val navController = findNavController()
//        navController.navigate(R.id.action_mainScreen_to_drawFragment)
//    }
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//
//        val binding = ActivityMainScreenBinding.inflate(inflater, container, false)
//
//
//        // Add ComposeView to show a LazyColumn
//        binding.composeView.setContent {
//            //load in all drawings from the view model and display is gallary
//            val drawings by drawVM.drawings.observeAsState(emptyList())
//            GalleryOfDrawings(drawings,navigateToDrawFragment,drawVM) //Required: List<Drawing> Found: LiveData<List<Drawing>>
//        }
//
//        //create new drawing button
//        binding.button2.setOnClickListener {
//            //creates a new bitmap and adds it to drawing list
//            drawVM.createNewDrawing()
//            navigateToDrawFragment.invoke()
//        }
//        return binding.root
//    }
//}
//
//// Composable function to display the file list using LazyColumn
//@Composable
//fun GalleryOfDrawings(drawings: List<Drawing>, navigateToDrawFragment: () -> Unit,vm:DrawViewModel) {
//    LazyVerticalGrid(
//        columns = GridCells.Fixed(2),
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        items(drawings) { drawing ->
//            FileGridItem(drawing,navigateToDrawFragment, vm)
//        }
//    }
//}
//
//@Composable
//fun FileGridItem(drawing: Drawing,navigateToDrawFragment: () -> Unit, vm: DrawViewModel){
//    val aspectRatio = getAspectRatioForOrientation()
//    //creates box effect holds drawing and file name
//    Column (
//        horizontalAlignment = Alignment.CenterHorizontally, // Center align content
//        modifier = Modifier
//            .fillMaxWidth() // Adjust the width of each column
//            .padding(8.dp)  // Add padding between grid items
//            .border(BorderStroke(2.dp, Color.Gray)) // Add a border with 2dp thickness and gray color
//            //user clicks on the drawing they want to modify
//            .clickable {
//                //Use jetpack navigation and load in picture into custom draw
//                vm.selectDrawing(drawing)
//                navigateToDrawFragment.invoke()
//            },
//    ){
//        //displays drawing
//        Drawing(drawing.bitmap, aspectRatio)
//        //displays file name
//        Text(text = drawing.fileName)
//    }
//}
//// Composable function to display a single file item
//@Composable
//fun Drawing(bitmap: Bitmap,  aspectRatio: Float) {
//    // Convert Bitmap to ImageBitmap to use in Compose
//    val imageBitmap = bitmap.asImageBitmap()
//    // Display the Bitmap as an Image
//    Image(
//        bitmap = imageBitmap,
//        contentDescription = "Drawing Thumbnail",
//        modifier = Modifier
//            .aspectRatio(aspectRatio) // Keep each item square
//            .padding(8.dp)
//            .border(BorderStroke(2.dp, Color.Gray))
//    )
//}
//
//@Composable
//fun getAspectRatioForOrientation(): Float {
//    val configuration = LocalConfiguration.current
//    return if (configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
//        9f / 16f // Portrait aspect ratio
//    } else {
//        16f / 9f // Landscape aspect ratio
//    }
//}


