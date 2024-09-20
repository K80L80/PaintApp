
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
import androidx.fragment.app.Fragment
import com.example.paintapp.databinding.ActivityMainScreenBinding

//TODO: Welcome screen, list of files already created, for new drawings have user enter text for the filename
//TODO: Use Lazy Column COMPOSABLE HERE - to display the file list (bitmap)
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
        //this is the button that moves to the draw screen
        binding.button2.setOnClickListener {
            buttonFunction()
        }
        return binding.root
    }
}