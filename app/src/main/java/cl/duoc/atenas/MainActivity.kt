package cl.duoc.atenas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import cl.duoc.atenas.ui.navigation.NavegacionRaiz
import cl.duoc.atenas.ui.theme.AtenasTheme

/**
 * ÚNICA ACTIVITY DE LA APLICACIÓN
 *
 * Toda la aplicación vive dentro de esta pantalla: no hay más Activities.
 * Lo único que hace es aplicar el tema y entregarle el control a
 * NavegacionRaiz, que decide qué Composable mostrar.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AtenasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavegacionRaiz(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
