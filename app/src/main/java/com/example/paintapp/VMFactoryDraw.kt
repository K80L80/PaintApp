import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.paintapp.DrawRepository
import com.example.paintapp.DrawViewModel

class VMFactory(private val drawRepository: DrawRepository) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass : Class<T>) : T {
        if(modelClass.isAssignableFrom(DrawViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return DrawViewModel(drawRepository) as T
        }
        throw IllegalArgumentException("Unknown View Model Class")
    }
}