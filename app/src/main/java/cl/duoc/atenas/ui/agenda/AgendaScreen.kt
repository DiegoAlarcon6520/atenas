package cl.duoc.atenas.ui.agenda

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.ui.components.BloqueFecha
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.Etiqueta
import cl.duoc.atenas.ui.components.EtiquetaEstado
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.theme.DegradadoBoton
import cl.duoc.atenas.ui.theme.Espaciado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AGENDA DE EVENTOS
 *
 * Franja con los días de la semana (los que tienen eventos llevan un punto),
 * un aviso cuando hay eventos cercanos sin confirmar y la lista de eventos
 * agrupada por cercanía. Cada evento muestra cuántos días faltan, con color
 * según el margen que queda para prepararlo.
 */
@Composable
fun AgendaScreen(
    alAbrirOportunidad: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AgendaViewModel = viewModel(factory = AgendaViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
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
            Column {
                Text(
                    text = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-CL"))
                        .format(Date())
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("Agenda", style = MaterialTheme.typography.headlineMedium)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                estado.semana.forEach { dia ->
                    BotonDia(
                        dia = dia,
                        elegido = estado.diaElegido == dia.fecha,
                        onClick = { viewModel.alElegirDia(dia.fecha) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (estado.sinConfirmarPronto > 0) {
            item { AvisoAnticipacion(estado.sinConfirmarPronto) }
        }

        if (estado.grupos.isEmpty()) {
            item {
                EstadoVacio(
                    mensaje = if (estado.diaElegido != null) {
                        "No hay eventos ese día. Toca el día otra vez para ver todos."
                    } else {
                        "No hay eventos próximos en tu cartera."
                    },
                    icono = Icons.Default.EventAvailable
                )
            }
        }

        estado.grupos.forEach { grupo ->
            item(key = "titulo-${grupo.titulo}") {
                Text(
                    text = grupo.titulo.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Espaciado.sm)
                )
            }
            items(grupo.eventos, key = { "ev-${it.oportunidad.id}" }) { evento ->
                TarjetaEvento(evento) { alAbrirOportunidad(evento.oportunidad.id) }
            }
        }
    }
}

@Composable
private fun BotonDia(dia: DiaSemana, elegido: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val resaltado = elegido || (dia.esHoy && !elegido)
    val fondo = when {
        elegido -> Modifier.background(DegradadoBoton)
        dia.esHoy -> Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
        else -> Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
    }
    Column(
        modifier = modifier
            .height(66.dp)
            .clip(MaterialTheme.shapes.medium)
            .then(fondo)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Día ${dia.numero}" + if (dia.tieneEventos) ", con eventos" else "" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dia.inicial,
            style = MaterialTheme.typography.labelSmall,
            color = if (elegido) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = dia.numero.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = if (elegido) Color.White else if (resaltado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(
                    if (dia.tieneEventos) {
                        if (elegido) Color.White else MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    }
                )
        )
    }
}

@Composable
private fun AvisoAnticipacion(cantidad: Int) {
    val color = MaterialTheme.colorScheme.tertiary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(color.copy(alpha = 0.12f))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = color)
        Text(
            text = if (cantidad == 1) {
                "1 evento en menos de $DIAS_AVISO días todavía sin participación confirmada."
            } else {
                "$cantidad eventos en menos de $DIAS_AVISO días todavía sin participación confirmada."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TarjetaEvento(evento: EventoAgenda, onClick: () -> Unit) {
    val oportunidad = evento.oportunidad
    // Color del margen: rojo con menos de dos semanas, ámbar con menos de un mes.
    val colorMargen = when {
        evento.diasRestantes < DIAS_AVISO -> MaterialTheme.colorScheme.error
        evento.diasRestantes <= 31 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val textoMargen = when (evento.diasRestantes) {
        0 -> "Hoy"
        1 -> "Mañana"
        else -> "En ${evento.diasRestantes} días"
    }
    Card(onClick = onClick, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BloqueFecha(oportunidad.fechaInicioEvento)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = oportunidad.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${oportunidad.organizacionNombre} · ${oportunidad.comunaNombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Etiqueta(textoMargen, colorMargen)
                    EtiquetaEstado(oportunidad.estado)
                }
            }
        }
    }
}
