package cl.duoc.atenas.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.ui.components.Alerta
import cl.duoc.atenas.ui.components.EncabezadoSaludo
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.EtiquetaSobreDegradado
import cl.duoc.atenas.ui.components.GrupoAlertas
import cl.duoc.atenas.ui.components.HojaAlertas
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.TituloSeccion
import cl.duoc.atenas.ui.theme.DegradadoTarjeta
import cl.duoc.atenas.ui.theme.Espaciado
import cl.duoc.atenas.ui.theme.colorDeEstado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * INICIO DEL EJECUTIVO: "QUÉ HACER HOY"
 *
 * Responde a la primera pregunta del día: ¿qué tengo que hacer? Arriba, un
 * resumen con el total de pendientes; luego la lista de tareas, cada una con
 * su acción directa (registrar, insistir, contactar) para resolverla sin
 * entrar al detalle; al final, el embudo como barra de colores tocable.
 */
@Composable
fun DashboardEjecutivoScreen(
    alAbrirOportunidad: (Long) -> Unit,
    alRegistrarGestion: (Long) -> Unit,
    alAbrirPerfil: () -> Unit,
    alVerCartera: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardEjecutivoViewModel = viewModel(factory = DashboardEjecutivoViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var mostrarAlertas by rememberSaveable { mutableStateOf(false) }

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
    }

    // La campana abre el panel de alertas: siempre muestra algo, aunque no
    // haya pendientes, para que el toque nunca parezca no hacer nada.
    if (mostrarAlertas) {
        HojaAlertas(
            grupos = gruposDeAlertas(estado.tareas),
            alCerrar = { mostrarAlertas = false },
            alAbrirOportunidad = alAbrirOportunidad
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = Espaciado.pantalla,
            end = Espaciado.pantalla,
            top = Espaciado.md,
            bottom = Espaciado.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Espaciado.entreTarjetas)
    ) {
        item {
            EncabezadoSaludo(
                saludo = fechaDeHoy(),
                nombre = "Hola, ${estado.primerNombre}",
                nombreAvatar = estado.nombreUsuario,
                alertas = estado.tareas.size,
                alTocarAvatar = alAbrirPerfil,
                alTocarAlertas = { mostrarAlertas = true }
            )
        }

        item { ResumenDelDia(estado) }

        item {
            TituloSeccion(
                texto = "Qué hacer hoy",
                accion = "Ver cartera",
                alTocarAccion = alVerCartera
            )
        }

        if (estado.tareas.isEmpty()) {
            item {
                EstadoVacio(
                    mensaje = "No tienes pendientes. Buen momento para buscar nuevas oportunidades.",
                    icono = Icons.Default.Check
                )
            }
        } else {
            items(estado.tareas, key = { it.clave }) { tarea ->
                TarjetaTarea(
                    tarea = tarea,
                    alAbrir = { alAbrirOportunidad(tarea.oportunidadId) },
                    alActuar = { alRegistrarGestion(tarea.oportunidadId) }
                )
            }
        }

        item { TarjetaEmbudo(estado, alVerCartera) }
    }
}

/** Agrupa las tareas por motivo para el panel de alertas, en orden de urgencia. */
@Composable
private fun gruposDeAlertas(tareas: List<Tarea>): List<GrupoAlertas> =
    listOf(
        TipoTarea.ATRASADA to "Atrasadas",
        TipoTarea.HOY to "Vencen hoy",
        TipoTarea.PROPUESTA to "Propuestas sin respuesta",
        TipoTarea.SIN_CONTACTO to "Sin primer contacto"
    ).map { (tipo, titulo) ->
        val estilo = estiloDe(tipo)
        GrupoAlertas(
            titulo = titulo,
            alertas = tareas.filter { it.tipo == tipo }.map { t ->
                Alerta(t.clave, t.oportunidadId, t.titulo, "${t.detalle} · ${t.meta}", estilo.icono, estilo.color)
            }
        )
    }

/** "Sábado 21 de noviembre", con mayúscula inicial. */
private fun fechaDeHoy(): String =
    SimpleDateFormat("EEEE d 'de' MMMM", Locale.forLanguageTag("es-CL"))
        .format(Date())
        .replaceFirstChar { it.uppercase() }

@Composable
private fun ResumenDelDia(estado: DashboardEjecutivoUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(DegradadoTarjeta)
            .padding(Espaciado.tarjeta)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Espaciado.md)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tu día",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = when (estado.tareas.size) {
                            0 -> "Todo al día"
                            1 -> "1 tarea pendiente"
                            else -> "${estado.tareas.size} tareas pendientes"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                if (estado.atrasadas > 0) {
                    EtiquetaSobreDegradado(
                        if (estado.atrasadas == 1) "1 atrasada" else "${estado.atrasadas} atrasadas"
                    )
                }
                if (estado.propuestasPorInsistir > 0) {
                    EtiquetaSobreDegradado(
                        if (estado.propuestasPorInsistir == 1) {
                            "1 propuesta sin respuesta"
                        } else {
                            "${estado.propuestasPorInsistir} propuestas sin respuesta"
                        }
                    )
                }
            }
        }
    }
}

/** Ícono, color y texto del botón según el tipo de tarea. */
private data class EstiloTarea(val icono: ImageVector, val color: Color, val accion: String)

@Composable
private fun estiloDe(tipo: TipoTarea): EstiloTarea {
    val esquema = MaterialTheme.colorScheme
    return when (tipo) {
        TipoTarea.ATRASADA -> EstiloTarea(Icons.Default.Schedule, esquema.error, "Registrar")
        TipoTarea.HOY -> EstiloTarea(Icons.Default.Today, esquema.tertiary, "Registrar")
        TipoTarea.PROPUESTA -> EstiloTarea(Icons.AutoMirrored.Filled.Send, esquema.secondary, "Insistir")
        TipoTarea.SIN_CONTACTO -> EstiloTarea(Icons.Default.PersonSearch, esquema.primary, "Contactar")
    }
}

/**
 * Tarea con su acción a mano: tocar la tarjeta abre la oportunidad; el botón
 * de la derecha va directo a registrar la gestión.
 */
@Composable
private fun TarjetaTarea(tarea: Tarea, alAbrir: () -> Unit, alActuar: () -> Unit) {
    val estilo = estiloDe(tarea.tipo)
    Card(onClick = alAbrir, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(estilo.color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(estilo.icono, contentDescription = null, tint = estilo.color)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = tarea.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tarea.detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tarea.meta,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (tarea.tipo == TipoTarea.ATRASADA) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable(onClick = alActuar)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = estilo.accion,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Embudo como una barra segmentada: el ancho de cada tramo es proporcional
 * a la cantidad de oportunidades. Se entiende de un vistazo dónde se
 * acumula el trabajo.
 */
@Composable
private fun TarjetaEmbudo(estado: DashboardEjecutivoUiState, alVerCartera: () -> Unit) {
    val colores = listOf(
        colorDeEstado(EstadoOportunidad.IDENTIFICADA),
        colorDeEstado(EstadoOportunidad.CONTACTO_PENDIENTE),
        colorDeEstado(EstadoOportunidad.PROPUESTA_ENVIADA),
        colorDeEstado(EstadoOportunidad.CONFIRMADA)
    )
    Card(onClick = alVerCartera, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Espaciado.tarjeta),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tu embudo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    text = "${estado.totalActivas} activas",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (estado.totalActivas == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    estado.embudo.forEachIndexed { i, tramo ->
                        if (tramo.total > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(tramo.total.toFloat())
                                    .height(10.dp)
                                    .clip(CircleShape)
                                    .background(colores[i])
                            )
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                estado.embudo.forEachIndexed { i, tramo ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(tramo.total.toString(), style = MaterialTheme.typography.titleLarge)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colores[i])
                            )
                            Text(
                                text = tramo.etiqueta,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
