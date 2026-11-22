package cl.duoc.atenas.ui.seguimiento

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.di.FabricasViewModel
import cl.duoc.atenas.domain.model.TipoAccion
import cl.duoc.atenas.ui.components.BotonDegradado
import cl.duoc.atenas.ui.components.CampoTexto
import cl.duoc.atenas.ui.components.ChipPildora
import cl.duoc.atenas.ui.components.EtiquetaEstado
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.SelectorFecha
import cl.duoc.atenas.ui.theme.Espaciado

/**
 * PANTALLA DE REGISTRO DE GESTIÓN
 *
 * Permite anotar una acción comercial sobre una oportunidad y, opcionalmente,
 * moverla de estado. Los estados que se ofrecen los decide el dominio según
 * el rol: si eres ejecutivo, "Participación confirmada" simplemente no
 * aparece entre las opciones.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegistrarSeguimientoScreen(
    alTerminar: () -> Unit,
    mostrarMensaje: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegistrarSeguimientoViewModel = viewModel(factory = FabricasViewModel.registrarSeguimiento)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var verTodas by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(estado.guardadoConExito) {
        if (estado.guardadoConExito) {
            mostrarMensaje("Gestión registrada")
            alTerminar()
        }
    }

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = Espaciado.pantalla, vertical = Espaciado.md),
        verticalArrangement = Arrangement.spacedBy(Espaciado.lg)
    ) {
        // ---------- Encabezado ----------
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Registrar gestión", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = estado.nombreOportunidad,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f, fill = false)
                )
                EtiquetaEstado(estado.estadoActual)
            }
        }

        AnimatedVisibility(
            visible = estado.errorGeneral != null,
            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = estado.errorGeneral.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(Espaciado.md)
                )
            }
        }

        // ---------- ¿Qué hiciste? ----------
        // Íconos en vez de una lista desplegable: se elige con un toque y se
        // reconoce de un vistazo. Las acciones menos frecuentes quedan
        // detrás de "Ver más".
        Bloque("¿Qué hiciste?") {
            val visibles = if (verTodas) TipoAccion.entries else accionesFrecuentes
            visibles.chunked(3).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                    fila.forEach { tipo ->
                        BotonAccion(
                            tipo = tipo,
                            elegido = estado.tipoAccion == tipo,
                            onClick = { viewModel.alCambiarTipoAccion(tipo) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - fila.size) { Box(Modifier.weight(1f)) }
                }
            }
            TextButton(onClick = { verTodas = !verTodas }) {
                Text(if (verTodas) "Ver menos" else "Ver más tipos de gestión")
            }
        }

        // ---------- Resultado ----------
        Bloque("Resultado") {
            CampoTexto(
                valor = estado.resultado,
                alCambiar = viewModel::alCambiarResultado,
                etiqueta = "Qué pasó y qué dijo la contraparte",
                error = estado.errores.resultado,
                lineas = 3
            )
            if (estado.hayGestionAnterior) {
                TextButton(onClick = viewModel::autorrellenarConAnterior) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Copiar la gestión anterior", modifier = Modifier.padding(start = Espaciado.sm))
                }
            }
            SelectorFecha(
                etiqueta = "Fecha de la gestión",
                valor = estado.fechaAccion,
                alSeleccionar = viewModel::alCambiarFechaAccion,
                error = estado.errores.fechaAccion
            )
        }

        // ---------- Próximo seguimiento ----------
        // Si el estado resultante cierra la oportunidad, no se pide próxima
        // acción: dejarla comprometida la haría aparecer como atrasada.
        if (estado.esCierre) {
            Text(
                text = "Esta gestión cierra la oportunidad, así que no se pide una próxima acción.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Bloque("Próximo seguimiento") {
                CampoTexto(
                    valor = estado.proximaAccion,
                    alCambiar = viewModel::alCambiarProximaAccion,
                    etiqueta = "Qué hay que hacer después (opcional)",
                    icono = Icons.AutoMirrored.Filled.Assignment
                )
                // Atajos de fecha: los plazos habituales con un toque.
                Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                    plazos.forEach { (etiqueta, dias) ->
                        ChipPildora(
                            texto = etiqueta,
                            seleccionado = estado.fechaProximoSeguimiento == Fechas.masDias(Fechas.hoy(), dias),
                            onClick = { viewModel.alElegirPlazo(dias) }
                        )
                    }
                }
                SelectorFecha(
                    etiqueta = "Otra fecha",
                    valor = estado.fechaProximoSeguimiento,
                    alSeleccionar = viewModel::alCambiarFechaProximoSeguimiento,
                    error = estado.errores.fechaProximoSeguimiento,
                    ayuda = "Si pasa esta fecha sin gestión, la oportunidad aparece como atrasada"
                )
            }
        }

        // ---------- Etapa ----------
        Bloque("Avanzar a") {
            if (estado.estadosDisponibles.isEmpty()) {
                Text(
                    text = "Desde esta etapa no puedes avanzar con tu perfil.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Opcional. Si no eliges ninguna, la oportunidad se queda en su etapa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
                    verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
                ) {
                    estado.estadosDisponibles.forEach { posible ->
                        ChipPildora(
                            texto = posible.etiqueta,
                            seleccionado = estado.nuevoEstado == posible,
                            onClick = { viewModel.alSeleccionarNuevoEstado(posible) }
                        )
                    }
                }
            }

            // Retroceso: solo lo ve el supervisor, separado del avance para
            // que no se confunda con el flujo normal del embudo.
            if (estado.estadosAnteriores.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = Espaciado.sm)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Devolver a una etapa anterior", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
                    verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
                ) {
                    estado.estadosAnteriores.forEach { anterior ->
                        ChipPildora(
                            texto = anterior.etiqueta,
                            seleccionado = estado.nuevoEstado == anterior,
                            onClick = { viewModel.alSeleccionarNuevoEstado(anterior) }
                        )
                    }
                }
            }

            // Aviso al ejecutivo de lo que no está en sus manos.
            estado.mensajeRestriccion?.let { mensaje ->
                Text(mensaje, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        CampoTexto(
            valor = estado.observaciones,
            alCambiar = viewModel::alCambiarObservaciones,
            etiqueta = "Observaciones (opcional)",
            icono = Icons.AutoMirrored.Filled.Notes,
            lineas = 2
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = alTerminar, enabled = !estado.guardando) { Text("Cancelar") }
            BotonDegradado(
                texto = "Guardar gestión",
                onClick = viewModel::guardar,
                // Una oportunidad cerrada no admite gestiones nuevas.
                habilitado = estado.admiteCambios,
                cargando = estado.guardando,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Acciones que se muestran sin abrir "Ver más": las de uso diario. */
private val accionesFrecuentes = listOf(
    TipoAccion.LLAMADA,
    TipoAccion.REUNION,
    TipoAccion.PRIMER_CONTACTO,
    TipoAccion.ENVIO_PROPUESTA,
    TipoAccion.SEGUIMIENTO,
    TipoAccion.RECEPCION_RESPUESTA
)

/** Plazos rápidos para el próximo seguimiento, en días desde hoy. */
private val plazos = listOf("Mañana" to 1, "En 3 días" to 3, "En 1 semana" to 7)

private fun iconoDeAccion(tipo: TipoAccion): ImageVector = when (tipo) {
    TipoAccion.INVESTIGACION_INICIAL -> Icons.Default.Search
    TipoAccion.BUSQUEDA_CONTACTO -> Icons.Default.PersonSearch
    TipoAccion.PRIMER_CONTACTO -> Icons.Default.Handshake
    TipoAccion.ENVIO_PRESENTACION -> Icons.Default.Slideshow
    TipoAccion.ENVIO_PROPUESTA -> Icons.AutoMirrored.Filled.Send
    TipoAccion.SOLICITUD_ANTECEDENTES -> Icons.Default.Description
    TipoAccion.REUNION -> Icons.Default.Groups
    TipoAccion.LLAMADA -> Icons.Default.Phone
    TipoAccion.SEGUIMIENTO -> Icons.Default.Autorenew
    TipoAccion.RECEPCION_RESPUESTA -> Icons.Default.MarkEmailRead
    TipoAccion.CONFIRMACION -> Icons.Default.TaskAlt
    TipoAccion.CIERRE -> Icons.Default.Flag
}

/** Botón cuadrado con ícono y nombre para elegir el tipo de gestión. */
@Composable
private fun BotonAccion(tipo: TipoAccion, elegido: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val primario = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier
            .height(78.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(if (elegido) primario.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(
                width = if (elegido) 2.dp else 1.dp,
                color = if (elegido) primario else MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.medium
            )
            .selectable(selected = elegido, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = iconoDeAccion(tipo),
            contentDescription = null,
            tint = if (elegido) primario else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = tipo.etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = if (elegido) primario else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** Bloque titulado del formulario. */
@Composable
private fun Bloque(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        contenido()
    }
}
