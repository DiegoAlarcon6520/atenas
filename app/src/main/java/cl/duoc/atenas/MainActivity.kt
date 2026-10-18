package cl.duoc.atenas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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

            AtenasTheme(temaOscuro = temaOscuro) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavegacionRaiz(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
