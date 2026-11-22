package cl.duoc.atenas.ui.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.core.ModoTema
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.ui.components.Avatar
import cl.duoc.atenas.ui.components.BotonContorno
import cl.duoc.atenas.ui.components.ChipPildora
import cl.duoc.atenas.ui.components.Etiqueta
import cl.duoc.atenas.ui.theme.Espaciado

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PerfilScreen(
    usuario: Usuario,
    modifier: Modifier = Modifier,
    viewModel: PerfilViewModel = viewModel(factory = PerfilViewModel.Factory)
) {
    val modoTema by viewModel.modoTema.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Espaciado.pantalla, vertical = Espaciado.md),
        verticalArrangement = Arrangement.spacedBy(Espaciado.md)
    ) {
        // Encabezado centrado con el avatar de marca.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Espaciado.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
        ) {
            Avatar(usuario.nombreCompleto, tamano = 88.dp)
            Text(usuario.nombreCompleto, style = MaterialTheme.typography.titleLarge)
            Etiqueta(
                texto = usuario.rol.etiqueta,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.padding(Espaciado.tarjeta)) {
                FilaDato("Usuario", usuario.nombreUsuario)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Espaciado.md),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                FilaDato("Área", usuario.area)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Espaciado.md),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                FilaDato("Zona", usuario.zona)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Espaciado.md),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                FilaDato("Estado de la cuenta", if (usuario.activo) "Activa" else "Inactiva")
            }
        }

        // ---------- Apariencia ----------
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier.padding(Espaciado.tarjeta),
                verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)
                ) {
                    Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Apariencia", style = MaterialTheme.typography.titleSmall)
                }
                Text(
                    text = "La aplicación tiene paleta clara y oscura. Puedes fijar una o dejar que siga el ajuste del teléfono.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Tres chips caben justo en un teléfono angosto: se permite
                // que bajen de línea si no alcanzan.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
                    verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
                ) {
                    ModoTema.entries.forEach { modo ->
                        ChipPildora(
                            texto = modo.etiqueta,
                            seleccionado = modoTema == modo,
                            onClick = { viewModel.cambiarModoTema(modo) }
                        )
                    }
                }
            }
        }

        BotonContorno(onClick = viewModel::cerrarSesion) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Cerrar sesión",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = Espaciado.sm)
            )
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = valor, style = MaterialTheme.typography.bodyMedium)
    }
}
