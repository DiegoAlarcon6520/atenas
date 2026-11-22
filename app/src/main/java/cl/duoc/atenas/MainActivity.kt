package cl.duoc.atenas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.atenas.core.ModoTema
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
            // El modo de tema se lee de las preferencias guardadas. Si el
            // usuario eligió "según el sistema", se delega en el ajuste del
            // teléfono; si eligió claro u oscuro, manda su elección.
            val contenedor = (application as AtenasApp).contenedor
            val modoTema by contenedor.preferencias.modoTema.collectAsStateWithLifecycle()
            val temaOscuro = when (modoTema) {
                ModoTema.SISTEMA -> isSystemInDarkTheme()
                ModoTema.CLARO -> false
                ModoTema.OSCURO -> true
            }

            // Los íconos de la barra de estado siguen al tema de la app, no al
            // del teléfono: si el usuario fuerza el modo oscuro, se dibujan
            // claros para que se vean sobre el fondo oscuro.
            LaunchedEffect(temaOscuro) {
                val estilo = if (temaOscuro) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                enableEdgeToEdge(statusBarStyle = estilo, navigationBarStyle = estilo)
            }

            AtenasTheme(temaOscuro = temaOscuro) {
                // Sin márgenes del sistema acá: cada pantalla decide cómo
                // tratar la barra de estado. Así el encabezado del login puede
                // extenderse detrás de ella, como en una app comercial.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavegacionRaiz()
                }
            }
        }
    }
}
