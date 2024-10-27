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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.paintapp.databinding.ActivityMainScreenBinding
import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share //share in app
import androidx.compose.material.icons.filled.IosShare //share outside of app (ie text message
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//Welcome screen, should display a list of files already created, for new drawings have user enter text for the filename
data class DrawingActions(
    val onFileNameChange: (Long, String) -> Unit,
    val onDrawingSelect: (Drawing) -> Unit,
    val navigateToDrawScreen: (() -> Unit),
    val onClickNewDrawingBtn: (String) -> Unit, // Callback for creating a new drawing
    val  shareOutsideApp: (String) -> Unit,
    val shareWithinApp: (Drawing) -> Unit
)

class MainScreen : Fragment() {
    private lateinit var menuVM: GalleryViewModel

    //setup all the callbacks to handle user interactinon
    val actions = DrawingActions(
        onFileNameChange = ::onFileNameChange, //callback to let user re-name their file
        onDrawingSelect = ::onDrawingSelect, //callback to let user user to 'select' a drawing to edit
        navigateToDrawScreen = ::navigateToDrawScreen, //callback to navigate from here gallary to draw screen
        onClickNewDrawingBtn = ::onClickNewDrawingBtn,// Callback for creating a new drawing
        shareOutsideApp = ::shareOutsideApp,
        shareWithinApp = ::shareWithinApp
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val app = requireActivity().application as DrawApp
        val factory = MainMenuVMFactory(app.drawRepository)
        menuVM = ViewModelProvider(this, factory).get(GalleryViewModel::class.java) //Expecting member declaratio

        val binding = ActivityMainScreenBinding.inflate(inflater, container, false)

        // Add ComposeView to show a LazyColumn
        binding.composeView.setContent {
            //load in all drawings from the view model and display is gallary
            val drawings by menuVM.drawings.observeAsState(emptyList())

            // Observe share intent
            val shareIntent by menuVM.shareIntent.observeAsState()

            // Trigger the share intent when it's available
            shareIntent?.let {
                context?.startActivity(it)
            }

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

        binding.download.setOnClickListener {
            val filesList = arrayOf("fileTest1.png", "FileTest2.png")
            AlertDialog.Builder(requireContext())
                .setTitle("Select file to download.")
                .setItems(filesList) { _, which ->
                    val selectedFile = filesList[which]
                    //PlaceHolder for files.
//                    lifecycleScope.launch {
//                        menuVM.importDrawImage(selectedFile)
//                    }
                }
                .show()
        }
        return binding.root
    }

    //callback to navigate from gallary to draw screen
    private fun navigateToDrawScreen() {
        val navController = findNavController()
        navController.navigate(R.id.action_mainScreen_to_drawFragment)
    }

    fun onDrawingSelected(drawing: Drawing) {
        menuVM.selectDrawing(drawing)
        val navController = findNavController()
        navController.navigate(R.id.action_mainScreen_to_drawFragment)
    }

    //callback to let user rename the file
    private fun onFileNameChange(drawingId: Long, newFileName: String) {
        menuVM.updateDrawingFileName(drawingId, newFileName)
    }

    //called when user clicks a specific drawing tile from the gallary
    private fun onDrawingSelect(drawing: Drawing) {
        menuVM.selectDrawing(drawing)
    }

    //When the user clicks 'new drawing button'
    private fun onClickNewDrawingBtn(fileName :String?){
        menuVM.createNewDrawing(fileName)   //creates a new bitmap and adds it to drawing list
       //TODO: fix this hacky solution (issue is that bitmap is null if you navigate right away, the draw screen recieves a null bitmap because it starts before the selected drawing is full set)
        lifecycleScope.launch {
            delay(500L)  //the integer does not conform to the expected type
            actions.navigateToDrawScreen.invoke()  // Navigate after the delay
        }
    }

    //Shares drawing to others outside of app (ie messages, ect)
    private fun shareOutsideApp(fileName: String){
        menuVM.shareDrawingOutsideApp(fileName)
    }

    //Handles sharing drawing with others paintApp users
    private fun shareWithinApp(drawing: Drawing) {
        // Example: Notify the view model or repository to update with this shared drawing
        menuVM.shareWithinApp(drawing)

        // For example, show a toast to confirm sharing within the app
        Toast.makeText(context, "Drawing shared within the app!", Toast.LENGTH_SHORT).show()
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

//    @Composable
//    fun SendPostButton() {
//        val context = LocalContext.current
//
//        androidx.compose.material3.Button(onClick = {
//            Log.d("SendPostButton", "Button clicked. Starting coroutine...")
//            // Launching in a coroutine to avoid blocking the main thread
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    // Sending a POST request
//                    Log.d("SendPostButton", "Attempting to send POST request...")
//                    val response: HttpResponse =
//                        httpClient.post("http://10.0.2.2:8080/books") {
//                            contentType(io.ktor.http.ContentType.Application.Json) //Sets the content type of the request to JSON.
//                            setBody(book) //Serializes the Book object and sets it as the request body.
//                        }
//                    Log.d("SendPostButton", "POST request successful. Response: ${response.status}")
//                    // Display result or success message if needed
//                    withContext(Dispatchers.Main) {
//                        Log.d("SendPostButton", "POST sent! Response: ${response.status}")
//                        Toast.makeText(
//                            context,
//                            "POST sent! Response: ${response.status}",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                } catch (e: Exception) {
//                    Log.e("SendPostButton", "Error occurred during POST request: ${e.message}", e)
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }) {
//            androidx.compose.material3.Text("Send POST Request")
//        }
//    }

}
// Composable function to display the file list using LazyColumn
@Composable
fun TitleGallary(drawings: List<Drawing>, actions: DrawingActions) {
    val focusManager = LocalFocusManager.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable {
                // Clear focus when clicking anywhere outside the tiles
                focusManager.clearFocus()
            }

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
        drawing.bitmap?.let {
            Drawing(
                bitmap = it,
                aspectRatio = aspectRatio,
                onBitmapClick = {
                    Log.d("GalleryOfDrawings", "Bitmap clicked for drawing: ${drawing.id}")
                    //Use jetpack navigation and load in picture into custom draw
                    actions.onDrawingSelect(drawing) //Need to refactor this doesn't have access to drawing just bitmap
                    actions.navigateToDrawScreen()
                }
            )
        }
        //displays file name
        fileNameDisplay(
            fileName = drawing.imageTitle,
            onFileNameChange = { newFileName ->
                Log.d("fileNameDisplay", "filename change for: ${drawing.id}")
                actions.onFileNameChange(drawing.id, newFileName)
            }
        )
        Row {
            //share outside of app butto (ie messages)
            ExportButton {
                actions.shareOutsideApp(drawing.fileName)
            }
            //share within app button (ie to another user within paint app)
            ShareButton {
                actions.shareWithinApp(drawing)
            }
        }
    }
}
//universal looking share button
@Composable
fun ExportButton(onClick: () -> Unit) {
    IconButton(onClick = { onClick() }) {
        Icon(imageVector = Icons.Filled.IosShare, contentDescription = "Share")
    }
}

@Composable
fun ShareButton(onClick: () -> Unit) {
    IconButton(onClick = { onClick() }) {
        Icon(imageVector = Icons.Filled.Share, contentDescription = "Share")
    }
}

//Composable to display the file     name
//The composable can manage local state for focus and edit mode, but the final updates should be propagated back to the ViewModel only when necessary (e.g., when the user completes editing).
@Composable
fun fileNameDisplay(fileName: String, onFileNameChange: (String) -> Unit) {
    // Track whether the TextField is in edit mode
    var isVisible by remember { mutableStateOf(false) }
    var localFileName by remember { mutableStateOf(fileName) }
    val focusRequester = FocusRequester()
    // Lambda for handling focus changes passed after click

    //File name dit mode
    if (isVisible) {
        Log.d("MainScreen", "in editing mode")
        BasicTextField(
            value = localFileName,
            onValueChange = { localFileName = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onFileNameChange(localFileName) // Save the new name
                isVisible = false
            }),
            modifier = Modifier.focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        Log.d("MainScreen", "turing off edit mode")
                        isVisible = false // Exit edit mode when focus is lost
                        onFileNameChange(localFileName)
                    }
                    else if(focusState.isFocused){
                        isVisible = true
                    }
                }
        )
        // Automatically request focus when the TextField becomes visible (this allows the user to not have do an extra click to start typing they immediately start typing)
        // Use DisposableEffect to request focus immediately when entering edit mode
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            Log.e("MainMenu", "Requesting focus for text field immediately")
        }
    } else {
        // Display the file name as text (display mode)
        Text(
            text = localFileName,
            modifier = Modifier
                .clickable {
                    Log.d("MainScreen", "Text clicked, entering editing mode")
                    isVisible = true // Enter edit mode on click
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
