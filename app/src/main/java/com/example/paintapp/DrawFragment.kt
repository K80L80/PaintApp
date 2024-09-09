/**Fragment to handle drawing in our application.
 * Uses the draw view model
 * Date: 09/08/2024
 *
 */
package com.example.paintapp
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.paintapp.databinding.FragmentDrawBinding
import androidx.appcompat.app.AlertDialog

class DrawFragment : Fragment() {

    private val drawViewModel: DrawViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDrawBinding.inflate(inflater, container, false)
        binding.customView.setViewModel(drawViewModel)

        //sets the view to observe the model and update in real time.
        binding.customView.viewTreeObserver.addOnGlobalLayoutListener {
            if (drawViewModel.getBitmap() == null) {
                val bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
                drawViewModel.setBitmap(bitmap)
            }
        }

        //handles our slider to adjust sizing.
        binding.sizeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //adjusts the size variable inside of the model
                drawViewModel.setSize(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        //Handles functionality for clicking on the color button.
        binding.colorButton.setOnClickListener {
            //set an array of color options we want to display.
            val colors = arrayOf("Red", "Green", "Blue", "Black")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Color")
                .setItems(colors) { _, which ->
                    val color = when (which) {
                        //red
                        0 -> 0xFFFF0000.toInt()
                        //green
                        1 -> 0xFF00FF00.toInt()
                        //blue
                        2 -> 0xFF0000FF.toInt()
                        //black
                        else -> 0xFF000000.toInt()
                    }
                    //pass chosen color to the model to update.
                    drawViewModel.setColor(color)
                }
                .show()
        }

        //handles functionality for clicking on a shape
        binding.shapeButton.setOnClickListener {
            //list of available options for the user.
            val shapes = arrayOf("Line", "Circle", "Rectangle","Diamond")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Shape")
                .setItems(shapes) { _, which ->
                    val shape = when (which) {
                        0 -> "line"
                        1 -> "circle"
                        2 -> "rectangle"
                        3->"diamond"
                        else -> "line"
                    }
                    //provide users choice to the model to update.
                    drawViewModel.setShape(shape)
                }
                .show()
        }

        //button to handle resetting the screen.
        binding.resetButton.setOnClickListener {
            drawViewModel.resetDrawing()
        }

        return binding.root
    }
}
