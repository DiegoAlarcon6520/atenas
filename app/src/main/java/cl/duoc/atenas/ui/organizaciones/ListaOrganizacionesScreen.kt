package cl.duoc.atenas.ui.organizaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.data.local.dao.OrganizacionResumen
import cl.duoc.atenas.domain.model.TipoOrganizacion
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.Etiqueta
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.theme.Espaciado

/** Listado de organizaciones con búsqueda y filtro por tipo. */
@Composable
fun ListaOrganizacionesScreen(
    alAbrirDetalle: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ListaOrganizacionesViewModel = viewModel(factory = ListaOrganizacionesViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
    }

    Column(modifier = modifier.fillMaxSize()) {

        OutlinedTextField(
            value = estado.filtros.texto,
            onValueChange = viewModel::alBuscar,
            placeholder = { Text("Buscar por nombre, rubro o comuna") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (estado.filtros.texto.isNotEmpty()) {
                    IconButton(onClick = { viewModel.alBuscar("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Espaciado.md, vertical = Espaciado.sm)
        )

        // Los tipos de organización son pocos, así que se muestran siempre
        // visibles en una fila horizontal en vez de esconderse en un panel.
        LazyRow(
            contentPadding = PaddingValues(horizontal = Espaciado.md),
            horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)
        ) {
            items(TipoOrganizacion.entries) { tipo ->
                FilterChip(
                    selected = estado.filtros.tipo == tipo,
                    onClick = { viewModel.alSeleccionarTipo(tipo) },
                    label = { Text(tipo.etiqueta) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Espaciado.md, vertical = Espaciado.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${estado.organizaciones.size} organizaciones",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (estado.filtros.hayAlguno) {
                TextButton(onClick = viewModel::limpiarFiltros) { Text("Limpiar") }
            }
        }

        if (estado.organizaciones.isEmpty()) {
            EstadoVacio(
                mensaje = if (estado.filtros.hayAlguno) {
                    "Ninguna organización coincide con la búsqueda."
                } else {
                    "Todavía no hay organizaciones registradas."
                }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = Espaciado.md,
                    end = Espaciado.md,
                    bottom = Espaciado.md
                ),
                verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
            ) {
                items(estado.organizaciones, key = { it.id }) { organizacion ->
                    TarjetaOrganizacion(organizacion) { alAbrirDetalle(organizacion.id) }
                }
            }
        }
    }
}

@Composable
private fun TarjetaOrganizacion(organizacion: OrganizacionResumen, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Espaciado.md),
            verticalArrangement = Arrangement.spacedBy(Espaciado.xs)
        ) {
            Text(organizacion.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${organizacion.rubro} · ${organizacion.comunaNombre}, ${organizacion.regionNombre}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                Etiqueta(
                    texto = organizacion.tipo.etiqueta,
                    color = MaterialTheme.colorScheme.primary
                )
                Etiqueta(
                    texto = "${organizacion.totalContactos} contactos",
                    color = MaterialTheme.colorScheme.secondary
                )
                Etiqueta(
                    texto = "${organizacion.totalOportunidades} oportunidades",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
