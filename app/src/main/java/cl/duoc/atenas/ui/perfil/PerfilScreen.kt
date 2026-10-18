package cl.duoc.atenas.ui.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.ui.components.Etiqueta
import cl.duoc.atenas.ui.theme.Espaciado

@Composable
fun PerfilScreen(
    usuario: Usuario,
    modifier: Modifier = Modifier,
    viewModel: PerfilViewModel = viewModel(factory = PerfilViewModel.Factory)
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Espaciado.md),
        verticalArrangement = Arrangement.spacedBy(Espaciado.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Espaciado.md)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Column {
                Text(usuario.nombreCompleto, style = MaterialTheme.typography.titleLarge)
                Etiqueta(
                    texto = usuario.rol.etiqueta,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Espaciado.md)) {
                FilaDato("Usuario", usuario.nombreUsuario)
                HorizontalDivider(modifier = Modifier.padding(vertical = Espaciado.sm))
                FilaDato("Área", usuario.area)
                HorizontalDivider(modifier = Modifier.padding(vertical = Espaciado.sm))
                FilaDato("Zona", usuario.zona)
                HorizontalDivider(modifier = Modifier.padding(vertical = Espaciado.sm))
                FilaDato("Estado de la cuenta", if (usuario.activo) "Activa" else "Inactiva")
            }
        }

        OutlinedButton(
            onClick = viewModel::cerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Text(
                text = "Cerrar sesión",
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
