
/**Initial load screen with draw button to start draw fragment.
 * Date: 09/08/2024
 *
 */
package com.example.paintapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//import androidx.compose.material.Text
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.paintapp.databinding.ActivityMainScreenBinding

//TODO: Welcome screen, should display a list of files already created, for new drawings have user enter text for the filename
//TODO: Use Lazy Column to show files already created.  (use COMPOSABLE HERE) display the file list as just names then eventually show mini snapshots of the drawings)
@Suppress("UNREACHABLE_CODE")
class MainScreen : Fragment() {

    private var buttonFunction: () -> Unit = {}

    /**Sets the button Function
     *
     */
    fun setButtonFunction(newFunc: () -> Unit) {
        buttonFunction = newFunc
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ActivityMainScreenBinding.inflate(inflater, container, false)
        Log.i("MainScreen", "main screen created")

        // Add ComposeView to show a LazyColumn
        binding.composeView.setContent {
            FileListScreen(getFileNames())
        }

        //this is the button that moves to the draw screen
        binding.button2.setOnClickListener {
            buttonFunction()
        }
        return binding.root

    }
    // Mock function to get a list of file names (you should replace it with real data source)
    fun getFileNames(): List<String> {
        // In real application, fetch the list of files from Room or file storage
        return listOf("Drawing1", "Drawing2", "Drawing3") // Example file names
    }
}   // Composable function to display the file list using LazyColumn
    @Composable
    fun FileListScreen(fileNames: List<String>) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(fileNames) { fileName ->
                FileItem(fileName)
            }
        }
    }

    // Composable function to display a single file item
    @Composable
    fun FileItem(fileName: String) {
        Text(text = fileName, modifier = Modifier.padding(8.dp))
    }