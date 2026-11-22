package cl.duoc.atenas.ui.oportunidades

import android.content.ActivityNotFoundException
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.core.CalendarioApp
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.entity.Seguimiento
import cl.duoc.atenas.domain.rules.SugerenciaPrioridad
import cl.duoc.atenas.domain.rules.TransicionesEstado
import cl.duoc.atenas.ui.components.BotonContorno
import cl.duoc.atenas.ui.components.BotonDegradado
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.EtiquetaEstado
import cl.duoc.atenas.ui.components.EtiquetaSobreDegradado
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.ListaDesplegable
import cl.duoc.atenas.ui.components.iconoDeTipo
import cl.duoc.atenas.ui.theme.DegradadoMarca
import cl.duoc.atenas.ui.theme.Espaciado
import cl.duoc.atenas.ui.theme.VeloTarjeta
import java.util.Locale

@Composable
fun DetalleOportunidadScreen(
    alEditar: (Long) -> Unit,
    alRegistrarGestion: (Long) -> Unit,
    alVerEvidencias: (Long) -> Unit,
    mostrarMensaje: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalleOportunidadViewModel = viewModel(factory = DetalleOportunidadViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    // Se necesita el contexto para abrir la aplicación de calendario.
    val contexto = LocalContext.current

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

    val admiteCambios = TransicionesEstado.admiteCambios(oportunidad.estado)

    // Abre la aplicación de calendario con el evento ya armado. Se combinan
    // la fecha y las horas guardadas para el instante exacto de inicio y fin.
    val agendarEvento: () -> Unit = {
        val inicio = Fechas.fechaConHora(oportunidad.fechaInicioEvento, oportunidad.horaInicio)
        val termino = Fechas.fechaConHora(
            oportunidad.fechaTerminoEvento ?: oportunidad.fechaInicioEvento,
            oportunidad.horaTermino
        )
        val intent = CalendarioApp.intentNuevoEvento(
            titulo = oportunidad.nombre,
            descripcion = oportunidad.descripcion,
            lugar = "${oportunidad.direccion}, ${detalle.comunaNombre}",
            inicio = inicio,
            termino = termino
        )
        // Un dispositivo puede no tener aplicación de calendario: hay que
        // avisarlo en vez de que la app se caiga con una excepción.
        try {
            contexto.startActivity(intent)
        } catch (excepcion: ActivityNotFoundException) {
            mostrarMensaje("No hay una aplicación de calendario disponible")
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Espaciado.pantalla,
            end = Espaciado.pantalla,
            top = Espaciado.sm,
            bottom = Espaciado.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Espaciado.entreTarjetas)
    ) {
        // ---------- Portada con el degradado de marca ----------
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(DegradadoMarca)
                    .background(VeloTarjeta)
                    .padding(Espaciado.tarjeta)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    EtiquetaSobreDegradado(oportunidad.estado.etiqueta)
                    EtiquetaSobreDegradado("Prioridad ${oportunidad.prioridad.etiqueta.lowercase()}")
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(76.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconoDeTipo(oportunidad.tipo),
                        contentDescription = oportunidad.tipo.etiqueta,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        // ---------- Accesos rápidos, como las miniaturas de la referencia ----------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Espaciado.entreTarjetas)
            ) {
                AccesoRapido(
                    icono = Icons.Default.PhotoLibrary,
                    texto = "Evidencias",
                    onClick = { alVerEvidencias(oportunidad.id) },
                    modifier = Modifier.weight(1f)
                )
                AccesoRapido(
                    icono = Icons.Default.CalendarMonth,
                    texto = "Agendar",
                    onClick = agendarEvento,
                    modifier = Modifier.weight(1f)
                )
                // Una oportunidad cerrada ya no se edita.
                AccesoRapido(
                    icono = Icons.Default.Edit,
                    texto = "Editar",
                    onClick = { alEditar(oportunidad.id) },
                    habilitado = admiteCambios,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ---------- Título y descripción ----------
        item {
            Column(
                modifier = Modifier.padding(top = Espaciado.sm),
                verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
            ) {
                Text(
                    text = "${Fechas.aTexto(oportunidad.fechaInicioEvento)} · " +
                        Fechas.rangoHorario(oportunidad.horaInicio, oportunidad.horaTermino),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(oportunidad.nombre, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = "${detalle.organizacionNombre} · ${detalle.comunaNombre}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = oportunidad.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ---------- Acción principal ----------
        item {
            if (admiteCambios) {
                BotonDegradado(
                    texto = "Registrar gestión",
                    icono = Icons.AutoMirrored.Filled.NoteAdd,
                    onClick = { alRegistrarGestion(oportunidad.id) },
                    modifier = Modifier.padding(vertical = Espaciado.sm)
                )
            } else {
                Text(
                    text = TransicionesEstado.MENSAJE_CERRADA,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Espaciado.sm)
                )
            }
        }

        // Asignación de responsable: solo la ve el supervisor.
        if (estado.puedeAsignar) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Column(
                        modifier = Modifier.padding(Espaciado.tarjeta),
                        verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AssignmentInd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
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
                            BotonContorno(onClick = { viewModel.asignarResponsable(null) }) {
                                Text(
                                    "Quitar asignación",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
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
                    "Potencial comercial" to oportunidad.potencial.etiqueta,
                    "Afinidad con el público" to oportunidad.afinidad.etiqueta,
                    "Prioridad sugerida" to SugerenciaPrioridad
                        .sugerir(oportunidad.potencial, oportunidad.afinidad).etiqueta,
                    "Costo referencial" to formatearMonto(oportunidad.costoReferencial),
                    "Condiciones" to (oportunidad.condiciones ?: "Sin condiciones registradas"),
                    "Requisitos" to (oportunidad.requisitos ?: "Sin requisitos registrados"),
                    "Espacios disponibles" to (oportunidad.espaciosDisponibles ?: "Sin información de espacios"),
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
                TarjetaSeguimiento(
                    seguimiento = seguimiento,
                    alAgendarRecordatorio = { fecha ->
                        val intent = CalendarioApp.intentRecordatorioGestion(
                            tituloOportunidad = oportunidad.nombre,
                            proximaAccion = seguimiento.proximaAccion.orEmpty(),
                            fecha = fecha
                        )
                        try {
                            contexto.startActivity(intent)
                        } catch (excepcion: ActivityNotFoundException) {
                            mostrarMensaje("No hay una aplicación de calendario disponible")
                        }
                    }
                )
            }
        }
    }
}

/** Bloque de datos plegable: reduce la saturación visual de la pantalla. */
@Composable
private fun SeccionDatos(titulo: String, datos: List<Pair<String, String>>) {
    var expandida by remember { mutableStateOf(true) }

    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier
                .animateContentSize(animationSpec = tween(250))
                .padding(Espaciado.tarjeta)
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
                    contentDescription = if (expandida) "Contraer $titulo" else "Expandir $titulo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expandida) {
                Spacer(modifier = Modifier.height(Espaciado.md))
                datos.forEachIndexed { indice, (etiqueta, valor) ->
                    if (indice > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = Espaciado.sm + 2.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
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
private fun TarjetaSeguimiento(
    seguimiento: Seguimiento,
    alAgendarRecordatorio: (Long) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier.padding(Espaciado.tarjeta),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(DegradadoMarca)
                    )
                    Text(seguimiento.tipoAccion.etiqueta, style = MaterialTheme.typography.titleSmall)
                }
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
                // Solo tiene sentido agendar si hay una fecha comprometida.
                seguimiento.fechaProximoSeguimiento?.let { fecha ->
                    TextButton(onClick = { alAgendarRecordatorio(fecha) }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Text(
                            text = "Agendar recordatorio",
                            modifier = Modifier.padding(start = Espaciado.xs)
                        )
                    }
                }
            }
        }
    }
}

private fun formatearMonto(valor: Double): String =
    if (valor <= 0.0) "Sin costo" else "$" + String.format(Locale.US, "%,.0f", valor).replace(',', '.')

/**
 * Acceso rápido en un cuadro redondeado: ícono con el color de marca y su
 * nombre debajo. Reemplaza a la columna de botones contorneados.
 */
@Composable
private fun AccesoRapido(
    icono: ImageVector,
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    Card(
        onClick = onClick,
        enabled = habilitado,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Espaciado.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (habilitado) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(texto, style = MaterialTheme.typography.labelMedium)
        }
    }
}
