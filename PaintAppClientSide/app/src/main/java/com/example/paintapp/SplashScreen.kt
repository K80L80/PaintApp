
/**Initial splash screen that persists for two seconds.
 * Date: 09/08/2024
 *
 */
package com.example.paintapp
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.paintapp.databinding.FragmentSplashScreenBinding
import kotlinx.coroutines.delay

//Splash Screen (Animation in Composable)
class SplashScreenFragment : Fragment() {

    private var _binding: FragmentSplashScreenBinding? = null
    private val viewModel: LoginViewModel by activityViewModels()

    private val binding get() = _binding!!
    private val navigationCallback: (() -> Unit) = {
        findNavController().navigate(R.id.action_splash_to_mainScreen2)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn) {
                binding.composeView.setContent {
                    ShowSplashScreenAnimation{
                        //after splash screen finishes uses nav graph action to navigate from splash screen to mainscren2
                        navigationCallback.invoke()
                    }
                }
            }
        }
    }
}
//This method handles the timing and animation logic
@Composable
fun ShowSplashScreenAnimation(onAnimationComplete: () -> Unit) {

    //is needed to make the UI react to changes.
    var isVisible by remember { mutableStateOf(true) }

    //Displays the spash screen for 3 seconds total; 1 sec fade in, 1 sec hold, 1 sec fade out
    //Acts as timer, starts immediately when main activity starts
    LaunchedEffect(Unit) {
        delay(1000)
        isVisible = false
        delay(1000)
        onAnimationComplete()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 1000)),  // Fade in over 1.5 seconds
        exit = fadeOut(animationSpec = tween(durationMillis = 1000))   // Fade out over 1.5 seconds
    ) {
        SplashScreenComposable()
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

