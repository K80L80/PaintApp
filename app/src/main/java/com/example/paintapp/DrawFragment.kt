package com.example.paintapp

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import java.util.Locale


class DrawFragment : Fragment() {

    private lateinit var customDrawView: CustomDrawView
    private val drawViewModel: DrawViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //inflates the view
        val view = inflater.inflate(R.layout.fragment_draw, container, false)
        customDrawView = view.findViewById(R.id.customDrawView)

        //sets up observers
        customDrawView.setUpViewModelObservers(drawViewModel)

        // Set color button
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

        // Set size button
        val buttonChangeSize: Button = view.findViewById(R.id.buttonChangeSize)
        buttonChangeSize.setOnClickListener {
            val sizes = arrayOf("Small", "Medium", "Large")
            val sizeValues = arrayOf(5f, 10f, 20f)

            AlertDialog.Builder(requireContext())
                .setTitle("Select Size")
                .setItems(sizes) { _, which ->
                    drawViewModel.setSize(sizeValues[which])
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
    
    //Saving the Bitmap Before a Screen Rotation
    override fun onPause() {
        super.onPause()
        Log.d("DrawFragment", "on pause() called..... should be called during rotation to save drawing data")
        drawViewModel.setBitmap(customDrawView.getBitmap())
    }
}
