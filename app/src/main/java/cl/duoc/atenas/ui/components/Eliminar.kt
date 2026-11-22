package cl.duoc.atenas.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Botón rojo de eliminar con confirmación.
 *
 * Eliminar es irreversible, así que siempre se pregunta antes. El servidor
 * tiene la última palabra: si el registro ya tiene historial, responde con
 * el motivo y la pantalla lo muestra en vez de borrar.
 */
@Composable
fun BotonEliminar(
    texto: String,
    tituloConfirmacion: String,
    mensajeConfirmacion: String,
    alConfirmar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    var preguntando by remember { mutableStateOf(false) }
    val rojo = MaterialTheme.colorScheme.error

    OutlinedButton(
        onClick = { preguntando = true },
        enabled = habilitado,
        shape = CircleShape,
        border = BorderStroke(1.dp, rojo.copy(alpha = 0.6f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = rojo),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(texto, modifier = Modifier.padding(start = 6.dp))
    }

    if (preguntando) {
        AlertDialog(
            onDismissRequest = { preguntando = false },
            title = { Text(tituloConfirmacion) },
            text = { Text(mensajeConfirmacion) },
            confirmButton = {
                TextButton(
                    onClick = {
                        preguntando = false
                        alConfirmar()
                    }
                ) { Text("Eliminar", color = rojo) }
            },
            dismissButton = {
                TextButton(onClick = { preguntando = false }) { Text("Cancelar") }
            }
        )
    }
}
