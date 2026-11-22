package cl.duoc.atenas.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.ConteoPorEstado
import cl.duoc.atenas.ui.components.ChipPildora
import cl.duoc.atenas.ui.components.EncabezadoSaludo
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.TarjetaDestacada
import cl.duoc.atenas.ui.components.TarjetaGestionAtrasada
import cl.duoc.atenas.ui.components.TarjetaLeyenda
import cl.duoc.atenas.ui.components.TarjetaMetrica
import cl.duoc.atenas.ui.components.TarjetaOportunidad
import cl.duoc.atenas.ui.components.TituloSeccion
import cl.duoc.atenas.ui.theme.Espaciado
import cl.duoc.atenas.ui.theme.colorDeEstado
import kotlinx.coroutines.launch

/** Listas del tablero que se alternan con los chips, en vez de apilarlas todas. */
private enum class SeccionTablero(val etiqueta: String) {
    PROXIMOS("Próximos"),
    ATRASADAS("Atrasadas"),
    PROPUESTAS("Propuestas"),
    SIN_CONTACTO("Sin contacto")
}

/** Posición del bloque de chips en la lista, para desplazarse a él desde la campana. */
private const val INDICE_CHIPS = 4

/**
 * TABLERO DEL EJECUTIVO
 *
 * Saludo con avatar y campana de alertas, el próximo evento destacado con el
 * degradado de marca, tres indicadores y una sola lista a la vez elegida con
 * chips (próximos, atrasadas, propuestas, sin contacto). Mostrar una lista a
 * la vez es lo que hace que la pantalla respire.
 */
@Composable
fun DashboardEjecutivoScreen(
    alAbrirOportunidad: (Long) -> Unit,
    alVerPropuestas: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardEjecutivoViewModel = viewModel(factory = DashboardEjecutivoViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var seccion by rememberSaveable { mutableStateOf(SeccionTablero.PROXIMOS) }
    var mostrarLeyenda by rememberSaveable { mutableStateOf(false) }
    val estadoLista = rememberLazyListState()
    val alcance = rememberCoroutineScope()

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
    }

    val totalAlertas = estado.atrasadas.size + estado.propuestasSinRespuesta.size + estado.sinContacto.size
    val proximo = estado.proximas.firstOrNull()

    LazyColumn(
        state = estadoLista,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = Espaciado.pantalla,
            end = Espaciado.pantalla,
            top = Espaciado.md,
            bottom = Espaciado.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Espaciado.entreTarjetas)
    ) {
        // 0 ---------- Saludo ----------
        item {
            EncabezadoSaludo(
                saludo = "Bienvenido de vuelta,",
                nombre = estado.nombreUsuario,
                alertas = totalAlertas,
                alTocarAlertas = {
                    // La campana lleva a la primera lista con algo pendiente.
                    seccion = when {
                        estado.atrasadas.isNotEmpty() -> SeccionTablero.ATRASADAS
                        estado.propuestasSinRespuesta.isNotEmpty() -> SeccionTablero.PROPUESTAS
                        estado.sinContacto.isNotEmpty() -> SeccionTablero.SIN_CONTACTO
                        else -> SeccionTablero.PROXIMOS
                    }
                    alcance.launch { estadoLista.animateScrollToItem(INDICE_CHIPS) }
                }
            )
        }

        // 1 ---------- Próximo evento destacado ----------
        item {
            if (proximo != null) {
                val dias = Fechas.diasEntre(Fechas.hoy(), proximo.fechaInicioEvento)
                Column(verticalArrangement = Arrangement.spacedBy(Espaciado.entreTarjetas)) {
                    TituloSeccion("Tu próximo evento")
                    TarjetaDestacada(
                        etiquetaSuperior = when (dias) {
                            0 -> "Hoy"
                            1 -> "Mañana"
                            else -> "En $dias días"
                        },
                        oportunidad = proximo,
                        onClick = { alAbrirOportunidad(proximo.id) }
                    )
                }
            } else {
                TituloSeccion("Sin eventos en los próximos 30 días")
            }
        }

        // 2 ---------- Indicadores ----------
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Espaciado.entreTarjetas),
                modifier = Modifier.padding(top = Espaciado.sm)
            ) {
                TarjetaMetrica(
                    titulo = "En gestión",
                    valor = estado.enGestion.toString(),
                    destacada = true,
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    titulo = "Próximos 30 días",
                    valor = estado.proximas.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    titulo = "Atrasadas",
                    valor = estado.atrasadas.size.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3 ---------- Leyenda plegable ----------
        // Los números por sí solos no dicen qué están contando: la leyenda
        // los define, pero queda plegada para no recargar el tablero.
        item {
            Column {
                TextButton(onClick = { mostrarLeyenda = !mostrarLeyenda }) {
                    Text(if (mostrarLeyenda) "Ocultar qué miden estos números" else "¿Qué miden estos números?")
                }
                if (mostrarLeyenda) {
                    TarjetaLeyenda(
                        "En gestión · oportunidades asignadas a ti que siguen activas, sin contar las descartadas ni las cerradas.",
                        "Próximos 30 días · eventos que ocurren dentro del próximo mes y todavía están en juego.",
                        "Atrasadas · oportunidades cuya última gestión comprometió una acción con fecha ya vencida."
                    )
                }
            }
        }

        // 4 ---------- Chips de sección ----------
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                items(SeccionTablero.entries) { opcion ->
                    val cantidad = when (opcion) {
                        SeccionTablero.PROXIMOS -> estado.proximas.size
                        SeccionTablero.ATRASADAS -> estado.atrasadas.size
                        SeccionTablero.PROPUESTAS -> estado.propuestasSinRespuesta.size
                        SeccionTablero.SIN_CONTACTO -> estado.sinContacto.size
                    }
                    ChipPildora(
                        texto = "${opcion.etiqueta} · $cantidad",
                        seleccionado = seccion == opcion,
                        onClick = { seccion = opcion }
                    )
                }
            }
        }

        // ---------- Lista elegida ----------
        when (seccion) {
            SeccionTablero.PROXIMOS ->
                if (estado.proximas.isEmpty()) {
                    item { EstadoVacio("No hay eventos en los próximos 30 días.") }
                } else {
                    items(estado.proximas, key = { "prox-${it.id}" }) { oportunidad ->
                        TarjetaOportunidad(oportunidad, onClick = { alAbrirOportunidad(oportunidad.id) })
                    }
                }

            SeccionTablero.ATRASADAS ->
                if (estado.atrasadas.isEmpty()) {
                    item { EstadoVacio("Estás al día con tus seguimientos.") }
                } else {
                    items(estado.atrasadas, key = { "atraso-${it.id}" }) { gestion ->
                        TarjetaGestionAtrasada(
                            gestion = gestion,
                            onClick = { alAbrirOportunidad(gestion.oportunidadId) }
                        )
                    }
                }

            SeccionTablero.PROPUESTAS -> {
                if (estado.propuestasSinRespuesta.isEmpty()) {
                    item { EstadoVacio("No tienes propuestas esperando respuesta.") }
                } else {
                    items(estado.propuestasSinRespuesta, key = { "prop-${it.id}" }) { oportunidad ->
                        TarjetaOportunidad(oportunidad, onClick = { alAbrirOportunidad(oportunidad.id) })
                    }
                }
                item {
                    TextButton(onClick = alVerPropuestas, modifier = Modifier.fillMaxWidth()) {
                        Text("Ver todas las propuestas enviadas")
                    }
                }
            }

            SeccionTablero.SIN_CONTACTO ->
                if (estado.sinContacto.isEmpty()) {
                    item { EstadoVacio("Ya contactaste a todas tus oportunidades activas.") }
                } else {
                    items(estado.sinContacto, key = { "sinc-${it.id}" }) { oportunidad ->
                        TarjetaOportunidad(oportunidad, onClick = { alAbrirOportunidad(oportunidad.id) })
                    }
                }
        }

        // ---------- Avance por estado ----------
        item { TituloSeccion("Avance por estado") }
        item {
            if (estado.conteos.isEmpty()) {
                EstadoVacio("Todavía no tienes oportunidades asignadas.")
            } else {
                Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Column(
                        modifier = Modifier.padding(Espaciado.tarjeta),
                        verticalArrangement = Arrangement.spacedBy(Espaciado.md)
                    ) {
                        val maximo = estado.conteos.maxOf { it.total }.coerceAtLeast(1)
                        estado.conteos.forEach { conteo ->
                            BarraEstado(conteo = conteo, maximo = maximo)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarraEstado(conteo: ConteoPorEstado, maximo: Int) {
    val color = colorDeEstado(conteo.estado)
    val progreso by animateFloatAsState(
        targetValue = conteo.total.toFloat() / maximo,
        animationSpec = tween(durationMillis = 600),
        label = "barra-${conteo.estado.name}"
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(conteo.estado.etiqueta, style = MaterialTheme.typography.bodySmall)
            Text(conteo.total.toString(), style = MaterialTheme.typography.labelLarge)
        }
        LinearProgressIndicator(
            progress = { progreso },
            color = color,
            trackColor = color.copy(alpha = 0.14f),
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        )
    }
}
