package cl.duoc.atenas.ui.supervisor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.ui.components.Avatar
import cl.duoc.atenas.ui.components.ChipPildora
import cl.duoc.atenas.ui.components.EncabezadoSaludo
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.Etiqueta
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

/** Listas del panel que se alternan con los chips. */
private enum class SeccionPanel(val etiqueta: String) {
    SIN_ASIGNAR("Sin asignar"),
    ATRASADAS("Atrasadas"),
    PROPUESTAS("Propuestas"),
    SIN_CONTACTO("Sin contacto")
}

/** Posición del bloque de chips en la lista, para desplazarse a él desde la campana. */
private const val INDICE_CHIPS = 4

/**
 * PANEL DEL SUPERVISOR
 *
 * Misma estructura que el tablero del ejecutivo, pero con la cartera de todo
 * el equipo: lo que necesita asignación arriba, indicadores, una lista a la
 * vez con chips, y al final la carga por ejecutivo y la distribución.
 */
@Composable
fun PanelSupervisorScreen(
    alAbrirOportunidad: (Long) -> Unit,
    alVerPropuestas: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PanelSupervisorViewModel = viewModel(factory = PanelSupervisorViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var seccion by rememberSaveable { mutableStateOf(SeccionPanel.SIN_ASIGNAR) }
    var mostrarLeyenda by rememberSaveable { mutableStateOf(false) }
    val estadoLista = rememberLazyListState()
    val alcance = rememberCoroutineScope()

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
    }

    val totalAlertas = estado.sinAsignar.size + estado.atrasadas.size +
        estado.propuestasSinRespuesta.size + estado.sinContacto.size
    val porAsignar = estado.sinAsignar.firstOrNull()

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
                saludo = "Panel del equipo",
                nombre = estado.nombreUsuario,
                alertas = totalAlertas,
                alTocarAlertas = {
                    seccion = when {
                        estado.sinAsignar.isNotEmpty() -> SeccionPanel.SIN_ASIGNAR
                        estado.atrasadas.isNotEmpty() -> SeccionPanel.ATRASADAS
                        estado.propuestasSinRespuesta.isNotEmpty() -> SeccionPanel.PROPUESTAS
                        else -> SeccionPanel.SIN_CONTACTO
                    }
                    alcance.launch { estadoLista.animateScrollToItem(INDICE_CHIPS) }
                }
            )
        }

        // 1 ---------- Lo que pide asignación ----------
        item {
            if (porAsignar != null) {
                Column(verticalArrangement = Arrangement.spacedBy(Espaciado.entreTarjetas)) {
                    TituloSeccion("Necesita un responsable")
                    TarjetaDestacada(
                        etiquetaSuperior = "Sin asignar",
                        oportunidad = porAsignar,
                        onClick = { alAbrirOportunidad(porAsignar.id) }
                    )
                }
            } else {
                TituloSeccion("Toda la cartera tiene responsable")
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
                    titulo = "Sin asignar",
                    valor = estado.sinAsignar.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    titulo = "Confirmadas",
                    valor = estado.confirmadas.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3 ---------- Leyenda plegable ----------
        item {
            Column {
                TextButton(onClick = { mostrarLeyenda = !mostrarLeyenda }) {
                    Text(if (mostrarLeyenda) "Ocultar qué miden estos números" else "¿Qué miden estos números?")
                }
                if (mostrarLeyenda) {
                    TarjetaLeyenda(
                        "En gestión · oportunidades activas de todo el equipo, sin contar las descartadas ni las cerradas.",
                        "Sin asignar · todavía no tienen un ejecutivo a cargo que las trabaje.",
                        "Confirmadas · la agencia ya comprometió su participación, incluidas las activaciones ya realizadas."
                    )
                }
            }
        }

        // 4 ---------- Chips de sección ----------
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                items(SeccionPanel.entries) { opcion ->
                    val cantidad = when (opcion) {
                        SeccionPanel.SIN_ASIGNAR -> estado.sinAsignar.size
                        SeccionPanel.ATRASADAS -> estado.atrasadas.size
                        SeccionPanel.PROPUESTAS -> estado.propuestasSinRespuesta.size
                        SeccionPanel.SIN_CONTACTO -> estado.sinContacto.size
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
            SeccionPanel.SIN_ASIGNAR ->
                if (estado.sinAsignar.isEmpty()) {
                    item { EstadoVacio("Todas las oportunidades tienen responsable asignado.") }
                } else {
                    items(estado.sinAsignar, key = { "sin-${it.id}" }) { oportunidad ->
                        TarjetaOportunidad(oportunidad, onClick = { alAbrirOportunidad(oportunidad.id) })
                    }
                }

            SeccionPanel.ATRASADAS ->
                if (estado.atrasadas.isEmpty()) {
                    item { EstadoVacio("El equipo está al día con sus seguimientos.") }
                } else {
                    items(estado.atrasadas, key = { "atraso-${it.id}" }) { gestion ->
                        TarjetaGestionAtrasada(
                            gestion = gestion,
                            onClick = { alAbrirOportunidad(gestion.oportunidadId) },
                            mostrarResponsable = true
                        )
                    }
                }

            SeccionPanel.PROPUESTAS -> {
                if (estado.propuestasSinRespuesta.isEmpty()) {
                    item { EstadoVacio("No hay propuestas esperando respuesta.") }
                } else {
                    items(estado.propuestasSinRespuesta, key = { "prop-${it.id}" }) { oportunidad ->
                        TarjetaOportunidad(oportunidad, onClick = { alAbrirOportunidad(oportunidad.id) })
                    }
                }
                item {
                    TextButton(onClick = alVerPropuestas, modifier = Modifier.fillMaxWidth()) {
                        Text("Revisar todas las propuestas enviadas")
                    }
                }
            }

            SeccionPanel.SIN_CONTACTO ->
                if (estado.sinContacto.isEmpty()) {
                    item { EstadoVacio("Todas las oportunidades activas ya fueron contactadas.") }
                } else {
                    items(estado.sinContacto, key = { "sinc-${it.id}" }) { oportunidad ->
                        TarjetaOportunidad(oportunidad, onClick = { alAbrirOportunidad(oportunidad.id) })
                    }
                }
        }

        // ---------- Carga por ejecutivo ----------
        item { TituloSeccion("Carga por ejecutivo") }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(Espaciado.tarjeta)) {
                    estado.cargaPorEjecutivo.entries.forEachIndexed { indice, (nombre, cantidad) ->
                        if (indice > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = Espaciado.md),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Espaciado.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Avatar(nombre, tamano = 36.dp)
                            Text(
                                text = nombre,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$cantidad en gestión",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // ---------- Distribución por estado ----------
        item { TituloSeccion("Distribución por estado") }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(
                    modifier = Modifier.padding(Espaciado.tarjeta),
                    verticalArrangement = Arrangement.spacedBy(Espaciado.md)
                ) {
                    estado.conteos.forEach { conteo ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Etiqueta(
                                texto = conteo.estado.etiqueta,
                                color = colorDeEstado(conteo.estado)
                            )
                            Text(
                                text = conteo.total.toString(),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
