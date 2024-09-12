/**The main activity to handle startup of the application.
 * Date: 09/08/2024
 *
 */
package com.example.paintapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.paintapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)

        // Check if fragmentContainerView contains a fragment already due to rotation
        if (savedInstanceState == null) {
            // Add MainScreen fragment initially if none exists
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MainScreen(), "mainScreenTag")
                .commitNow()
        }

        // Set up button function inside MainScreen to replace with PaintScreen
        val mainScreen = supportFragmentManager.findFragmentByTag("mainScreenTag") as? MainScreen
        mainScreen?.setButtonFunction {
            val drawFragment = DrawFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, drawFragment, "drawFragmentTag")
            transaction.addToBackStack(null)
            transaction.commit()
        }

        setContentView(binding.root)
    }
}
