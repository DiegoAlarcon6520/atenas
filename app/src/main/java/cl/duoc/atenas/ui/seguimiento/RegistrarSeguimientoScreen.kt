package cl.duoc.atenas.ui.seguimiento

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.domain.model.TipoAccion
import cl.duoc.atenas.ui.components.CampoTexto
import cl.duoc.atenas.ui.components.EtiquetaEstado
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.ListaDesplegable
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
    viewModel: RegistrarSeguimientoViewModel = viewModel(factory = RegistrarSeguimientoViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

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
            .padding(Espaciado.md),
        verticalArrangement = Arrangement.spacedBy(Espaciado.md)
    ) {
        Text("Registrar gestión", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = estado.nombreOportunidad,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)
        ) {
            Text("Estado actual:", style = MaterialTheme.typography.bodySmall)
            EtiquetaEstado(estado.estadoActual)
        }

        // Atajo: reutiliza los datos de la gestión anterior cuando el
        // cambio es leve. Solo precarga el formulario; no guarda nada.
        if (estado.hayGestionAnterior) {
            OutlinedButton(
                onClick = viewModel::autorrellenarConAnterior,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Text(
                    text = "Copiar datos de la gestión anterior",
                    modifier = Modifier.padding(start = Espaciado.sm)
                )
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = estado.errorGeneral.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(Espaciado.md)
                )
            }
        }

        ListaDesplegable(
            etiqueta = "Tipo de acción",
            icono = Icons.Default.Category,
            opciones = TipoAccion.entries,
            seleccionada = estado.tipoAccion,
            textoDe = { it.etiqueta },
            alSeleccionar = viewModel::alCambiarTipoAccion
        )

        SelectorFecha(
            etiqueta = "Fecha de la gestión",
            valor = estado.fechaAccion,
            alSeleccionar = viewModel::alCambiarFechaAccion,
            error = estado.errores.fechaAccion
        )

        CampoTexto(
            valor = estado.resultado,
            alCambiar = viewModel::alCambiarResultado,
            etiqueta = "Resultado",
            icono = Icons.Default.Description,
            error = estado.errores.resultado,
            lineas = 3,
            ayuda = "Qué pasó y qué dijo la contraparte"
        )

        // Si el estado resultante cierra la oportunidad, no se pide próxima
        // acción: no queda nada por hacer y dejarla comprometida haría que
        // la oportunidad apareciera como atrasada más adelante.
        if (estado.esCierre) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Esta gestión cierra la oportunidad, así que no se pide una próxima acción.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(Espaciado.md)
                )
            }
        } else {
            CampoTexto(
                valor = estado.proximaAccion,
                alCambiar = viewModel::alCambiarProximaAccion,
                etiqueta = "Próxima acción",
                icono = Icons.AutoMirrored.Filled.Assignment,
                ayuda = "Opcional: qué hay que hacer después"
            )

            SelectorFecha(
                etiqueta = "Fecha del próximo seguimiento",
                valor = estado.fechaProximoSeguimiento,
                alSeleccionar = viewModel::alCambiarFechaProximoSeguimiento,
                error = estado.errores.fechaProximoSeguimiento,
                ayuda = "Si pasa esta fecha sin gestión, la oportunidad aparece como atrasada"
            )
        }

        // ---------- Cambio de estado ----------
        Text(
            text = "Cambiar estado",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        if (estado.estadosDisponibles.isEmpty()) {
            Text(
                text = "Esta oportunidad no admite más cambios de estado.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Opcional. Si no eliges ninguno, la oportunidad se queda como está.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // FlowRow acomoda los chips en varias líneas según el ancho de
            // la pantalla, en vez de cortarlos o salirse del borde.
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                estado.estadosDisponibles.forEach { posible ->
                    FilterChip(
                        selected = estado.nuevoEstado == posible,
                        onClick = { viewModel.alSeleccionarNuevoEstado(posible) },
                        label = { Text(posible.etiqueta) }
                    )
                }
            }
        }

        // Retroceso: solo lo ve el supervisor, y se separa visualmente del
        // avance para que no se confunda con el flujo normal del embudo.
        if (estado.estadosAnteriores.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Espaciado.xs),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Devolver a una etapa anterior",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Úsalo cuando la gestión se reabre o hubo un error de registro.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                estado.estadosAnteriores.forEach { anterior ->
                    FilterChip(
                        selected = estado.nuevoEstado == anterior,
                        onClick = { viewModel.alSeleccionarNuevoEstado(anterior) },
                        label = { Text(anterior.etiqueta) }
                    )
                }
            }
        }

        // Aviso al ejecutivo de lo que no está en sus manos.
        estado.mensajeRestriccion?.let { mensaje ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(Espaciado.md)
                )
            }
        }

        CampoTexto(
            valor = estado.observaciones,
            alCambiar = viewModel::alCambiarObservaciones,
            etiqueta = "Observaciones",
            icono = Icons.AutoMirrored.Filled.Notes,
            lineas = 2
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)
        ) {
            OutlinedButton(
                onClick = alTerminar,
                enabled = !estado.guardando,
                modifier = Modifier.weight(1f)
            ) { Text("Cancelar") }

            Button(
                onClick = viewModel::guardar,
                enabled = !estado.guardando,
                modifier = Modifier.weight(1f)
            ) {
                if (estado.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Registrar")
                }
            }
        }
    }
}
