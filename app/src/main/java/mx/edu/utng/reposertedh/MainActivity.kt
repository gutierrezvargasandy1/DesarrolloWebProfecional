package mx.edu.utng.reposertedh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.edu.utng.reposertedh.ui.navigation.AppNavigation
import mx.edu.utng.reposertedh.ui.theme.ReposerteDHTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReposerteDHTheme {
                AppNavigation()
            }
        }
    }
}