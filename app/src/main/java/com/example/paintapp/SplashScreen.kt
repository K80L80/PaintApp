
/**Initial splash screen that persists for two seconds.
 * Date: 09/08/2024
 *
 */
package com.example.paintapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.paintapp.databinding.ActivitySplashScreenBinding
import com.example.paintapp.databinding.FragmentSplashScreenBinding

//TODO: Spencer –– Splash Screen (Animation) Composable
class SplashScreenFragment : Fragment() {

    private var _binding: FragmentSplashScreenBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }
}

