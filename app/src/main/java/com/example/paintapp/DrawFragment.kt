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

        val view = inflater.inflate(R.layout.fragment_draw, container, false)
        customDrawView = view.findViewById(R.id.customDrawView)

        // Check if there's a saved Bitmap in the ViewModel
        drawViewModel.userDataBitMap?.let {
            Log.d("DrawFragment", "restoring old drawing")
            customDrawView.setBitmap(it) // Call to restore the saved Bitmap
        }

        // Set color button
        val buttonChangeColor: Button = view.findViewById(R.id.buttonChangeColor)
        buttonChangeColor.setOnClickListener {
            val colors = arrayOf("Black", "Red", "Green", "Blue")
            val colorValues = arrayOf(Color.BLACK, Color.RED, Color.GREEN, Color.BLUE)

            AlertDialog.Builder(requireContext())
                .setTitle("Select Color")
                .setItems(colors) { _, which ->
                    customDrawView.setColor(colorValues[which])
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
                    customDrawView.setSize(sizeValues[which])
                }
                .show()
        }

        // Set shape button
        val buttonChangeShape: Button = view.findViewById(R.id.buttonChangeShape)
        buttonChangeShape.setOnClickListener {
            val shapes = arrayOf( "Line", "Circle", "Square", "Rectangle", "Diamond")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Shape")
                .setItems(shapes) { _, which ->
                    val selectedShape = shapes[which].replace(" ", "").lowercase(Locale.getDefault())
                    customDrawView.setShape(selectedShape)
                }
                .show()
        }

        // Clear button
        val buttonReset: Button = view.findViewById(R.id.buttonReset)
        buttonReset.setOnClickListener {
            customDrawView.resetDrawing() // Call the resetDrawing method to clear the canvas
        }

        return view
    }

    //Saving the Bitmap Before a Screen Rotation
    override fun onPause() {
        super.onPause()
        Log.d("DrawFragment", "on pause() called..... should be called during rotation to save drawing data")
        drawViewModel.userDataBitMap = customDrawView.getBitmap()!!
    }
}
