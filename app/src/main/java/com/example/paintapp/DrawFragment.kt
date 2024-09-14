/**Fragment that contains the button setters, observers, and model + custom view.
 * Date:09/12/2024
 *
 */
package com.example.paintapp

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.slider.Slider
import java.util.Locale


class DrawFragment : Fragment() {

    private lateinit var customDrawView: CustomDrawView
    private val drawViewModel: DrawViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_draw, container, false)
        customDrawView = view.findViewById(R.id.customDrawView)

        //sets up observers
        customDrawView.setUpViewModelObservers(drawViewModel)

        // Set color button handling
        val buttonChangeColor: Button = view.findViewById(R.id.buttonChangeColor)
        buttonChangeColor.setOnClickListener {
            val colors = arrayOf("Black", "Red", "Green", "Blue")
            val colorValues = arrayOf(Color.BLACK, Color.RED, Color.GREEN, Color.BLUE)

            AlertDialog.Builder(requireContext())
                .setTitle("Select Color")
                .setItems(colors) { _, which ->
                    drawViewModel.setColor(colorValues[which])
                }
                .show()
        }

        // Below handles all of the size button and slider functionality.
        //See draw_bar_dialogue for layout
        val buttonChangeSize: Button = view.findViewById(R.id.buttonChangeSize)
        buttonChangeSize.setOnClickListener {
            //get all of the layout pieces
            val dialogView = layoutInflater.inflate(R.layout.draw_bar_dialogue, null)
            val slider: Slider = dialogView.findViewById(R.id.sizeSlider)
            val sliderValueText: TextView = dialogView.findViewById(R.id.sliderValue)

            // This sets the starting value of the slider and displays it
            //this will impact our draw size at the start
            slider.value = drawViewModel.getSize() ?: 5f
            sliderValueText.text = "${slider.value.toInt()}"

            //listener for handling slider changes and displaying
            slider.addOnChangeListener { _, value, _ ->
                sliderValueText.text = "${value.toInt()}"
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
            val shapes = arrayOf("Free", "Line", "Circle", "Square", "Rectangle", "Diamond")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Shape")
                .setItems(shapes) { _, which ->
                    val selectedShape = shapes[which].replace(" ", "").lowercase(Locale.getDefault())
                    drawViewModel.setShape(selectedShape)
                }
                .show()
        }

        // Clear button
        val buttonReset: Button = view.findViewById(R.id.buttonReset)
        buttonReset.setOnClickListener {
            customDrawView.getBitmap()?.let { bitmap ->
                // Pass the width and height of the CustomDrawView to reset properly
                customDrawView.resetDrawing()
                drawViewModel.resetDrawing(customDrawView.width, customDrawView.height)
            }

            // After reset, tell ViewModel to reset the flag to avoid future resets
            drawViewModel.resetComplete()
        }

        return view
    }

    /**Saves the bitmap for rotation
     *
     */
    override fun onPause() {
        super.onPause()
        Log.d("DrawFragment", "on pause() called..... should be called during rotation to save drawing data")
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

}
