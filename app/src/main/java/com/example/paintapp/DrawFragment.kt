/**Fragment that contains the button setters, observers, and model + custom view.
 * Date:09/12/2024
 *
 */
package com.example.paintapp

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.slider.Slider
import yuku.ambilwarna.AmbilWarnaDialog
import java.util.Locale

//TODO – add a save button, and have user enter a filename to save their drawing
class DrawFragment : Fragment() {

    private lateinit var customDrawView: CustomDrawView
    val repository = DrawRepository();
    private val drawViewModel: DrawViewModel by activityViewModels() {VMFactory(DrawRepository())}
    private var fragmentSetupComplete = false  // New flag to track if fragment setup is done

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("DrawFragment - KS", "creating fragment")
        val view = inflater.inflate(R.layout.fragment_draw, container, false)
        customDrawView = view.findViewById(R.id.customDrawView)

        val fileName = arguments?.getString("fileName")
        // Use the fileName for whatever logic you need
        Log.i("DrawFragment - KS", "00 Received fileName: $fileName")


        // Fragments set lambda variables in custom view
        Log.i("DrawFragment - KS", "0a (setup) - onCreateView() STARTED")

        // 3) Fragment: Relaying Touch to ViewModel
        //The view's member variable of type lambda ('onShapDrawAction') in the custom view class is assigned here in fragment, so when it’s invoked (inside the on touchEvent in the view) it actually calls the logic defined in the fragment, which in turn notifies the ViewModel.
        Log.i("DrawFragment - KS", "0b (setup) - Fragment setting custom view lambda variable")
        customDrawView.onShapeDrawAction = { x, y, event ->
            // The Fragment acts as the mediator here, passing the touch data to the ViewModel and delegating the drawing logic to the ViewModel
            Log.i("DrawFragment - KS", "2 - relaying coordinates and delegate logic to view model")
            drawViewModel.onUserDraw(x, y, event)  // Delegate touch event to ViewModel
        }

        // Relay the size change to the ViewModel via the fragment
        //fragment relays to viewmodel that there has been a change like rotation and the screen size changed, passing on to the view model to execute logic to resize the bitmap to the appropriate canvas size the user is on now
        Log.i("DrawFragment - KS", "0c (setup) - Fragments setting onSizeChangedCallback  ")
        customDrawView.onSizeChangedCallback = { width, height ->
            // Only trigger size change logic after setup is complete
            Log.i("DrawFragment - KS", "3a onSizeChangeCallback set by fragment")

            drawViewModel.getOrCreateBitmap(width, height)

        }
        customDrawView.onResetCallback = {
            drawViewModel.clearBitmap()
        }


        Log.i("DrawFragment - KS", "0d (setup) - onCreateView() ENDED")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //sets up observers
        //customDrawView.setUpViewModelObservers(drawViewModel)
        // Fragments set lambda variables in custom view
        Log.i("DrawFragment - KS", "1a (setup) - onCreateView() STARTED")

        //OBSERVING DRAWING DATA
        //5 The fragment observes the bitmap meaning it watches for updates and receives notification with the bitmap is modified
        //When the fragment receives word that the bitmap changed it calls on the view to update the UI (calling customDrawingView.updateBitmap(bitmap)
        Log.i("DrawFragment - KS", "1b - (setup) bitmap observer set")
        drawViewModel.selectedDrawing.observe(viewLifecycleOwner) { drawing ->
            drawing?.bitmap?.let { //Unresolved reference: bitmap
                Log.i("DrawFragment - KS", "5a (observing) fragment was told of changed bitmap (by view model) and in turn tells custom view to update itself")
                customDrawView.updateBitmap(it)
            }
        }
        fragmentSetupComplete = true

        //OBSERVING COLOR CHANGES
        drawViewModel.paintTool.observe(viewLifecycleOwner){
            Log.i("DrawFragment - KS", "fragment observing a change to paint tool!!")
            //TODO: eventually want the paint tool and color to display on screen??
        }

        // Set color button handling
        // Open color picker when the user selects it
        val buttonChangeColor: Button = view.findViewById(R.id.buttonChangeColor)
        buttonChangeColor.setOnClickListener {
            openColorPicker()
        }

        // Below handles all of the size button and slider functionality.
        //See draw_bar_dialogue for layout
        val buttonChangeSize: Button = view.findViewById(R.id.buttonChangeSize)
        buttonChangeSize.setOnClickListener {
            Log.i("DrawFragment - KS", "(button click) size changed button clicked")
            //get all of the layout pieces
            val dialogView = layoutInflater.inflate(R.layout.draw_bar_dialogue, null)
            val slider: Slider = dialogView.findViewById(R.id.sizeSlider)
            val sliderValueText: TextView = dialogView.findViewById(R.id.sliderValue)

            // This sets the starting value of the slider and displays it
            //this will impact our draw size at the start
            slider.value = drawViewModel.getSize()!!
            sliderValueText.text = "${slider.value.toInt()}"

            //listener for handling slider changes and displaying
            slider.addOnChangeListener { _, value, _ ->
                sliderValueText.text = "${value.toInt()}"
                Log.i("DrawFragment - KS", "user picks size: ${slider.value}")
                drawViewModel.setSize(slider.value)
            }

            // Below creates the popup for displaying the slider
            AlertDialog.Builder(requireContext())
                .setTitle("Select Brush Size")
                .setView(dialogView)
                //This will update the slider only if set is clicked.
                .setPositiveButton("Set Brush Size") { _, _ ->
                    drawViewModel.setSize(slider.value)
                }
                .show()
            Log.i("DrawFragment - KS", "(pop-up slider) user picks size ")
        }
//        Log.i("DrawFragment - KS", "setting on click listener")
        // Set shape button
        val buttonChangeShape: Button = view.findViewById(R.id.buttonChangeShape)
        buttonChangeShape.setOnClickListener {
            Log.i("DrawFragment", "button clicked")
            val shapes = arrayOf("free", "line", "circle", "square", "rectangle", "diamond")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Shape")
                .setItems(shapes) { _, which ->
                    val selectedShape = shapes[which]
                    Log.i("DrawFragment - KS", "selected shape: $selectedShape")
                    drawViewModel.setShape(selectedShape)
                }
                .show()
        }

        // Clear button
        val buttonReset: Button = view.findViewById(R.id.buttonReset)
        buttonReset.setOnClickListener {
            customDrawView.getBitmap()?.let { bitmap ->
                // Pass the width and height of the CustomDrawView to reset properly
                drawViewModel.clearBitmap()
            }
            // After reset, tell ViewModel to reset the flag to avoid future resets
            drawViewModel.resetComplete()
        }

        //save button, sets callback
        val saveButton: Button = view.findViewById(R.id.saveBtn)
        saveButton.setOnClickListener{
            customDrawView.getBitmap()?.let{
                // Assume customDrawView provides this method
                drawViewModel.saveCurrentDrawing(it) //Type mismatch.Required: Bitmap Found: Bitmap?
                Log.i("DrawFragment - KS", "Bitmap saved to drawing list")
            }
        }

        // Listen for when the fragment is navigated away from
        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.drawFragment) { // Replace with your fragment ID
                // Save the current bitmap when navigating away
                customDrawView.getBitmap()?.let { currentBitmap ->
                    drawViewModel.saveCurrentDrawing(currentBitmap)
                    Log.i("DrawFragment", "Bitmap saved via back navigation")
                } ?: run {
                    Log.e("DrawFragment", "Failed to save: Bitmap is null")
                }
            }
        }
        Log.i("DrawFragment - KS", "1c (setup) - onViewCreated() ENDED")
    }

    /**Saves the bitmap for rotation
     *
     */
    override fun onPause() {
        super.onPause()
        Log.d("DrawFragment - KS", "on pause() called because of rotation, fragment asks view to save drawing data")
        drawViewModel.setBitmap(customDrawView.getBitmap())
    }

    /**Methods below this are only used in testing in order to get values from the drawview
     * Model. These are not used in code implementation.
     *
     */
    fun getPaintColor(): Int?{
        return drawViewModel.getColor()
    }
    fun getPaintSize(): Float?{
        return drawViewModel.getSize()
    }

    fun getPaintShape(): String?{
        return drawViewModel.getShape()
    }

    private fun openColorPicker() {
        // Initialize the color picker with the current color
        val currentColor = drawViewModel.getColor()  // Replace with your current color logic

        AmbilWarnaDialog(requireContext(), currentColor!!, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {
                // User canceled the dialog
            }

            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                // User selected a color, update the ViewModel
                drawViewModel.setColor(color)
            }
        }).show()
    }




//    @Composable
//    fun CustomComposableView(modifier: Modifier = Modifier) {
//        Canvas(modifier = modifier.background(Color.Red)) {//Unresolved reference: Red
//            // Custom drawing logic goes here
//            // Example: Draw a circle at the center
//            drawCircle(
//                color = Color.Blue, //Unresolved reference: Blue
//                radius = 50f,
//                center = Offset(size.width / 2, size.height / 2)
//            )
//        }
//    }

 //   @Composable
//    fun ExampleScreen(onButtonOneClick: () -> Unit, onButtonTwoClick: () -> Unit) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            CustomComposableView(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Button(onClick = { onButtonOneClick() }) {
//                Text("Button One") //type mismatch Required: Context! Found: () → Unit
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Button(onClick = { onButtonTwoClick() }) { //Named arguments are not allowed for non-Kotlin functions Cannot find a parameter with this name: onClick
//                Text("Button Two") //Type mismatch. Required: Context!Found: () → Unit
//            }
//        }
//    }


}
