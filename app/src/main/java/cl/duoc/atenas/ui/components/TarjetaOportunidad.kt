package cl.duoc.atenas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.ui.theme.Espaciado
import cl.duoc.atenas.ui.theme.colorDePrioridad
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * TARJETA DE OPORTUNIDAD
 *
 * La fecha va a la izquierda en un bloque grande ("03 / DIC"): en esta app
 * el dato que más importa es cuándo ocurre el evento, así que se lee
 * primero al recorrer la lista. A la derecha, el estado y la prioridad
 * arriba, el nombre en negrita, la organización y el potencial como una
 * barra de tres tramos.
 *
 * El parámetro onClick es opcional: cuando viene null la tarjeta es solo
 * informativa, y cuando trae una función la tarjeta se vuelve tocable.
 */
@Composable
fun TarjetaOportunidad(
    oportunidad: OportunidadResumen,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    mostrarResponsable: Boolean = false
) {
    val contenido: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(Espaciado.md),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BloqueFecha(oportunidad.fechaInicioEvento)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EtiquetaEstado(oportunidad.estado)
                    Box(modifier = Modifier.weight(1f))
                    PuntoPrioridad(oportunidad)
                }
                Text(
                    text = oportunidad.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${oportunidad.organizacionNombre} · ${oportunidad.tipo.etiqueta}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Potencial",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Medidor(nivel = oportunidad.potencial.peso)
                    if (mostrarResponsable) {
                        Text(
                            text = "· ${oportunidad.responsableNombre ?: "Sin responsable"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            contenido()
        }
    } else {
        Card(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { contenido() }
    }
}

/** Punto de color con la prioridad en texto, para no depender solo del color. */
@Composable
private fun PuntoPrioridad(oportunidad: OportunidadResumen) {
    val color = colorDePrioridad(oportunidad.prioridad)
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(oportunidad.prioridad.etiqueta, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

/**
 * Bloque con el día en grande y el mes abreviado debajo ("03 / DIC"), como
 * la hoja de un calendario de escritorio.
 */
@Composable
fun BloqueFecha(fecha: Long, modifier: Modifier = Modifier) {
    val locale = Locale.forLanguageTag("es-CL")
    val dia = SimpleDateFormat("dd", locale).format(Date(fecha))
    val mes = SimpleDateFormat("MMM", locale).format(Date(fecha)).replace(".", "").uppercase()
    Column(
        modifier = modifier
            .width(54.dp)
            .height(60.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(dia, style = MaterialTheme.typography.titleLarge)
        Text(mes, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Barra de tres tramos para niveles bajo / medio / alto. */
@Composable
fun Medidor(nivel: Int, modifier: Modifier = Modifier, maximo: Int = 3) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(maximo) { i ->
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(
                        if (i < nivel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}
