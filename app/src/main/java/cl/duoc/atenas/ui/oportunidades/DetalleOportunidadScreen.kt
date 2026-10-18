package cl.duoc.atenas.ui.oportunidades

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.OportunidadDetalle
import cl.duoc.atenas.data.local.entity.Seguimiento
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.EtiquetaEstado
import cl.duoc.atenas.ui.components.EtiquetaPrioridad
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.ListaDesplegable
import cl.duoc.atenas.ui.theme.Espaciado
import java.util.Locale

@Composable
fun DetalleOportunidadScreen(
    alEditar: (Long) -> Unit,
    alRegistrarGestion: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalleOportunidadViewModel = viewModel(factory = DetalleOportunidadViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
    }

    val detalle = estado.detalle
    if (detalle == null) {
        EstadoVacio(
            mensaje = if (estado.sinAcceso) {
                "No tienes acceso a esta oportunidad. Solo puedes consultar las que te fueron asignadas."
            } else {
                "La oportunidad ya no está disponible."
            },
            modifier = modifier.fillMaxSize()
        )
        return
    }

    val oportunidad = detalle.oportunidad

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Espaciado.md),
        verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                Text(oportunidad.nombre, style = MaterialTheme.typography.headlineSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                    EtiquetaEstado(oportunidad.estado)
                    EtiquetaPrioridad(oportunidad.prioridad)
                }
                Text(
                    text = oportunidad.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Tanto el ejecutivo a cargo como el supervisor pueden editar
                // los antecedentes; lo que cambia según el rol son los estados
                // y la asignación, que se manejan aparte.
                Button(
                    onClick = { alRegistrarGestion(oportunidad.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null)
                    Text(
                        text = "Registrar gestión",
                        modifier = Modifier.padding(start = Espaciado.sm)
                    )
                }
                OutlinedButton(
                    onClick = { alEditar(oportunidad.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text(
                        text = "Editar antecedentes",
                        modifier = Modifier.padding(start = Espaciado.sm)
                    )
                }
            }
        }

        // Asignación de responsable: solo la ve el supervisor.
        if (estado.puedeAsignar) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(Espaciado.md),
                        verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AssignmentInd, contentDescription = null)
                            Text(
                                text = "Asignación de responsable",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        ListaDesplegable(
                            etiqueta = "Ejecutivo a cargo",
                            opciones = estado.ejecutivos,
                            seleccionada = estado.ejecutivos.firstOrNull {
                                it.id == oportunidad.responsableId
                            },
                            textoDe = { "${it.nombreCompleto} · ${it.zona}" },
                            alSeleccionar = { viewModel.asignarResponsable(it.id) },
                            textoVacio = "Sin asignar"
                        )
                        if (oportunidad.responsableId != null) {
                            OutlinedButton(
                                onClick = { viewModel.asignarResponsable(null) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Quitar asignación") }
                        }
                    }
                }
            }
        }

        item {
            SeccionDatos(
                titulo = "Evento",
                datos = listOf(
                    "Tipo" to oportunidad.tipo.etiqueta,
                    "Fecha de inicio" to Fechas.aTexto(oportunidad.fechaInicioEvento),
                    "Fecha de término" to (oportunidad.fechaTerminoEvento
                        ?.let { Fechas.aTexto(it) } ?: "Un solo día"),
                    "Horario" to Fechas.rangoHorario(oportunidad.horaInicio, oportunidad.horaTermino),
                    "Dirección" to oportunidad.direccion,
                    "Comuna" to "${detalle.comunaNombre}, ${detalle.regionNombre}",
                    "Público objetivo" to oportunidad.publicoObjetivo,
                    "Público esperado" to "${oportunidad.publicoEsperado} asistentes"
                )
            )
        }

        item {
            SeccionDatos(
                titulo = "Organización y contacto",
                datos = buildList {
                    add("Organización" to detalle.organizacionNombre)
                    add("Tipo" to detalle.organizacionTipo.etiqueta)
                    add("Rubro" to detalle.organizacionRubro)
                    if (detalle.contactoNombre != null) {
                        add("Contacto" to detalle.contactoNombre)
                        add("Cargo" to (detalle.contactoCargo ?: "—"))
                        add("Correo" to (detalle.contactoCorreo ?: "—"))
                        add("Teléfono" to (detalle.contactoTelefono ?: "—"))
                    } else {
                        add("Contacto" to "Sin contacto identificado")
                    }
                }
            )
        }

        item {
            SeccionDatos(
                titulo = "Gestión comercial",
                datos = listOf(
                    "Responsable" to (detalle.responsableNombre ?: "Sin asignar"),
                    "Registrada por" to detalle.creadorNombre,
                    "Fuente" to oportunidad.fuenteInformacion,
                    "Detectada el" to Fechas.aTexto(oportunidad.fechaDeteccion),
                    "Costo referencial" to formatearMonto(oportunidad.costoReferencial),
                    "Condiciones" to (oportunidad.condiciones ?: "Sin condiciones registradas"),
                    "Requisitos" to (oportunidad.requisitos ?: "Sin requisitos registrados"),
                    "Observaciones" to (oportunidad.observaciones ?: "Sin observaciones"),
                    "Última actualización" to Fechas.aTextoConHora(oportunidad.fechaActualizacion)
                )
            )
        }

        item {
            Text(
                text = "Historial de gestiones",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = Espaciado.sm)
            )
        }

        if (estado.historial.isEmpty()) {
            item { EstadoVacio("Aún no se registran gestiones para esta oportunidad.") }
        } else {
            items(estado.historial, key = { it.id }) { seguimiento ->
                TarjetaSeguimiento(seguimiento)
            }
        }
    }
}

/** Bloque de datos plegable: reduce la saturación visual de la pantalla. */
@Composable
private fun SeccionDatos(titulo: String, datos: List<Pair<String, String>>) {
    var expandida by remember { mutableStateOf(true) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .animateContentSize(animationSpec = tween(250))
                .padding(Espaciado.md)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandida = !expandida },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Icon(
                    imageVector = if (expandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expandida) "Contraer $titulo" else "Expandir $titulo"
                )
            }

            if (expandida) {
                HorizontalDivider(modifier = Modifier.padding(vertical = Espaciado.sm))
                datos.forEachIndexed { indice, (etiqueta, valor) ->
                    if (indice > 0) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = Espaciado.sm))
                    }
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
                        Text(
                            text = valor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaSeguimiento(seguimiento: Seguimiento) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Espaciado.md),
            verticalArrangement = Arrangement.spacedBy(Espaciado.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(seguimiento.tipoAccion.etiqueta, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = Fechas.aTexto(seguimiento.fechaAccion),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(seguimiento.resultado, style = MaterialTheme.typography.bodyMedium)
            if (seguimiento.estadoAnterior != seguimiento.estadoNuevo) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EtiquetaEstado(seguimiento.estadoAnterior)
                    Text("→", style = MaterialTheme.typography.labelLarge)
                    EtiquetaEstado(seguimiento.estadoNuevo)
                }
            }
            seguimiento.proximaAccion?.let { proxima ->
                Text(
                    text = "Próxima acción: $proxima" + (
                        seguimiento.fechaProximoSeguimiento
                            ?.let { " (${Fechas.aTexto(it)})" } ?: ""
                        ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun formatearMonto(valor: Double): String =
    if (valor <= 0.0) "Sin costo" else "$" + String.format(Locale.US, "%,.0f", valor).replace(',', '.')
