/**Fragment that contains the button setters, observers, and model + custom view.
 * Date:09/12/2024
 *
 */
package com.example.paintapp

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.slider.Slider
import yuku.ambilwarna.AmbilWarnaDialog

//TODO – add a save button, and have user enter a filename to save their drawing
class DrawFragment : Fragment() {

    private lateinit var customDrawView: CustomDrawView

    private val drawViewModel: DrawViewModel by activityViewModels { VMFactory((requireActivity().application as DrawApp).drawRepository) }

    private var fragmentSetupComplete = false  // New flag to track if fragment setup is done

    private var listener: NavController.OnDestinationChangedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_draw, container, false)
        customDrawView = view.findViewById(R.id.customDrawView)

        // Fragment: Relaying Touch to ViewModel
        customDrawView.onShapeDrawAction = { x, y, event -> ////The view's member variable of type lambda ('onShapDrawAction') in the custom view class is assigned here in fragment, so when it’s invoked (inside the on touchEvent in the view) it actually calls the logic defined in the fragment, which in turn notifies the ViewModel.
            // The Fragment acts as the mediator here, passing the touch data to the ViewModel and delegating the drawing logic to the ViewModel
            drawViewModel.onUserDraw(x, y, event)  // Delegate touch event to ViewModel
        }

        // Relay the size change to the ViewModel via the fragment
        customDrawView.onSizeChangedCallback = { width, height -> //   //fragment relays to viewmodel that there has been a change like rotation and the screen size changed, passing on to the view model to execute logic to resize the bitmap to the appropriate canvas size the user is on now
            // Only trigger size change logic after setup is complete
            drawViewModel.respondToResizeEvent(width, height)
        }

        customDrawView.onResetCallback = {
            drawViewModel.clearBitmap()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i("DrawFragment", "Drawing Screen Created")
        super.onViewCreated(view, savedInstanceState)

        //observe the drawing picked by the user (ie., earliery by clicking thumbnail)
        drawViewModel.selectedDrawing.observe(viewLifecycleOwner) { drawing ->
            drawing?.bitmap?.let { ///    //When the fragment receives word that the bitmap changed it calls on the view to update the UI (calling customDrawingView.updateBitmap(bitmap)
                customDrawView.updateBitmap(it)
            }
        }
        fragmentSetupComplete = true

        //observing color changes
        drawViewModel.paintTool.observe(viewLifecycleOwner){
            //TODO: eventually want the paint tool and color to display on screen??
        }

        // Open color picker when the user selects it
        val buttonChangeColor: Button = view.findViewById(R.id.buttonChangeColor)
        buttonChangeColor.setOnClickListener {
            openColorPicker()
        }

        // Below handles all of the size button and slider functionality.
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
        }

        // Set shape button
        val buttonChangeShape: Button = view.findViewById(R.id.buttonChangeShape)
        buttonChangeShape.setOnClickListener {
            Log.i("DrawFragment", "button clicked")
            val shapes = arrayOf("free", "line", "circle", "square", "rectangle", "diamond")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Shape")
                .setItems(shapes) { _, which ->
                    val selectedShape = shapes[which]
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
            Log.e("DrawFragment - KS","custom view.getBitmap: ${customDrawView.getBitmap()}")
            customDrawView.getBitmap()?.let{
                // Assume customDrawView provides this method
                Log.e("DrawFragment - KS","dvm.saveCurrentDrawing(it): $it")  // Debug print statement
                drawViewModel.saveCurrentDrawing(it)
            }
        }

        // Listen for when the fragment is navigated away from
        listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.drawFragment) { // Replace with your fragment ID
                // Save the current bitmap when navigating away
                customDrawView.getBitmap()?.let { currentBitmap ->
                    Log.e("DrawFragment - KS", "navigating away !!!")
                    Log.e("DrawFragment - KS", "sending this to view-model to be save: {$currentBitmap}")
                    drawViewModel.saveCurrentDrawing(currentBitmap)
                } ?: run {
                    Log.e("DrawFragment - KS", "Failed to save: Bitmap is null")
                }
            }
        }
        // Register the listener
        listener?.let {findNavController().addOnDestinationChangedListener(it)}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.let { findNavController().removeOnDestinationChangedListener(it) } // Unregister the listener if necessary
    }

    /**Saves the bitmap for rotation
     *
     */
    override fun onPause() {
        super.onPause()
        Log.d("DrawFragment - KS", "on pause() called because of rotation, fragment asks view to save drawing data botmap: ${customDrawView.getBitmap()}")
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


}
