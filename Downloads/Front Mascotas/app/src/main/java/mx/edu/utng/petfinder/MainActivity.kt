package mx.edu.utng.petfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.edu.utng.petfinder.Navigation.AppNavigation
import mx.edu.utng.petfinder.ui.theme.PetFinderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PetFinderTheme {
                AppNavigation()
            }
        }
    }
}