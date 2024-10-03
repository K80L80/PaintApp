import android.app.Application
import androidx.room.Room
import com.example.paintapp.DrawDatabase
import com.example.paintapp.DrawRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DrawingApp : Application() {
    val scope = CoroutineScope(SupervisorJob()) //eager – ready to launch , use supervisor job allows a child routine to fail without bring everything else down
    val db by lazy { DrawDatabase.getDatabase(applicationContext) }//lazy – defer database creation till you need it, applicationContext in Android refers to a global context tied to the entire lifecycle of the app (applicationContext isn't tied to any specific screen or component.)
    //val drawRepository by lazy { DrawRepository(scope, db.drawDao()) } //lazy – defer repository creation till you need it
}