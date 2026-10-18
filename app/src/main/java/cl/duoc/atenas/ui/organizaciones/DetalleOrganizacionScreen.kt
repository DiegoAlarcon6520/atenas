package cl.duoc.atenas.ui.organizaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.entity.Contacto
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.Etiqueta
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.TarjetaOportunidad
import cl.duoc.atenas.ui.components.TituloSeccion
import cl.duoc.atenas.ui.theme.Espaciado

/**
 * FICHA DE ORGANIZACIÓN
 *
 * Muestra los datos de la organización, su cartera de contactos y el historial
 * de oportunidades que se han gestionado con ella. Desde acá se editan sus
 * datos y se agregan o editan contactos.
 */
@Composable
fun DetalleOrganizacionScreen(
    alEditarOrganizacion: (Long) -> Unit,
    alAgregarContacto: (Long) -> Unit,
    alEditarContacto: (Long, Long) -> Unit,
    alAbrirOportunidad: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalleOrganizacionViewModel = viewModel(factory = DetalleOrganizacionViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
    }

    val detalle = estado.detalle
    if (detalle == null) {
        EstadoVacio("La organización ya no está disponible.", modifier.fillMaxSize())
        return
    }

    val organizacion = detalle.organizacion

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Espaciado.md),
        verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                Text(organizacion.nombre, style = MaterialTheme.typography.headlineSmall)
                Etiqueta(
                    texto = organizacion.tipo.etiqueta,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedButton(
                    onClick = { alEditarOrganizacion(organizacion.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text("Editar organización", modifier = Modifier.padding(start = Espaciado.sm))
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Espaciado.md)) {
                    FilaDato("Rubro", organizacion.rubro)
                    HorizontalDivider(modifier = Modifier.padding(vertical = Espaciado.sm))
                    FilaDato("Ubicación", "${detalle.comunaNombre}, ${detalle.regionNombre}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = Espaciado.sm))
                    FilaDato("Sitio web", organizacion.sitioWeb ?: "No registrado")
                    HorizontalDivider(modifier = Modifier.padding(vertical = Espaciado.sm))
                    FilaDato("Público asociado", organizacion.publicoAsociado ?: "No registrado")
                    HorizontalDivider(modifier = Modifier.padding(vertical = Espaciado.sm))
                    FilaDato("Observaciones", organizacion.observaciones ?: "Sin observaciones")
                }
            }
        }

        item { TituloSeccion("Contactos (${estado.contactos.size})") }

        item {
            OutlinedButton(
                onClick = { alAgregarContacto(organizacion.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Text("Agregar contacto", modifier = Modifier.padding(start = Espaciado.sm))
            }
        }

        if (estado.contactos.isEmpty()) {
            item { EstadoVacio("Esta organización todavía no tiene contactos identificados.") }
        } else {
            items(estado.contactos, key = { it.id }) { contacto ->
                TarjetaContacto(contacto) { alEditarContacto(organizacion.id, contacto.id) }
            }
        }

        item { TituloSeccion("Oportunidades asociadas (${estado.oportunidades.size})") }

        if (estado.oportunidades.isEmpty()) {
            item { EstadoVacio("Aún no se registran oportunidades con esta organización.") }
        } else {
            items(estado.oportunidades, key = { "op-${it.id}" }) { oportunidad ->
                TarjetaOportunidad(
                    oportunidad = oportunidad,
                    onClick = { alAbrirOportunidad(oportunidad.id) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaContacto(contacto: Contacto, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Espaciado.md),
            verticalArrangement = Arrangement.spacedBy(Espaciado.xs)
        ) {
            Text(contacto.nombre, style = MaterialTheme.typography.titleSmall)
            Text(
                text = "${contacto.cargo} · ${contacto.area.etiqueta}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(contacto.correo, style = MaterialTheme.typography.bodySmall)
            Text(contacto.telefono, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                Etiqueta(
                    texto = "Prefiere ${contacto.medioPreferente.etiqueta.lowercase()}",
                    color = MaterialTheme.colorScheme.secondary
                )
                contacto.fechaUltimoContacto?.let { fecha ->
                    Etiqueta(
                        texto = "Último contacto ${Fechas.aTexto(fecha)}",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Espaciado.md)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(valor, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.6f))
    }
}
