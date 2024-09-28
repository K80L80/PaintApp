
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.example.paintapp.databinding.ActivitySplashScreenBinding
import com.example.paintapp.databinding.FragmentSplashScreenBinding

//TODO: Spencer –– Splash Screen (Animation) Composable
class SplashScreenFragment : Fragment() {

    private var _binding: FragmentSplashScreenBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.composeView.setContent {
            SplashScreenComposable()
        }
    }
}
@Composable
fun SplashScreenComposable() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "👏Welcome to the Paint App!\nLet's start drawing ✌️!",
            fontSize = 25.sp,
            color = Color.Blue,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
