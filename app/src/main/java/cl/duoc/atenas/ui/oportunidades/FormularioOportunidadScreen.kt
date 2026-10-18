package cl.duoc.atenas.ui.oportunidades

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.ui.components.CampoTexto
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.ListaDesplegable
import cl.duoc.atenas.ui.components.SelectorFecha
import cl.duoc.atenas.ui.theme.Espaciado

/**
 * PANTALLA DE FORMULARIO DE OPORTUNIDAD (crear y editar)
 *
 * Esta pantalla NO contiene lógica de validación ni de guardado: solo dibuja
 * los campos, muestra lo que el ViewModel tenga en su estado y le avisa cuando
 * el usuario escribe o toca Guardar.
 *
 * @param alTerminar se llama cuando el guardado fue exitoso; la navegación
 *        decide qué hacer (en nuestro caso, volver atrás).
 * @param mostrarMensaje muestra el Snackbar de confirmación.
 */
@Composable
fun FormularioOportunidadScreen(
    alTerminar: () -> Unit,
    mostrarMensaje: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FormularioOportunidadViewModel = viewModel(factory = FormularioOportunidadViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    // LaunchedEffect reacciona a un cambio de estado una sola vez.
    // Cuando el guardado termina bien, confirmamos y volvemos atrás.
    LaunchedEffect(estado.guardadoConExito) {
        if (estado.guardadoConExito) {
            mostrarMensaje(
                if (estado.esEdicion) "Oportunidad actualizada correctamente"
                else "Oportunidad creada correctamente"
            )
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
            // verticalScroll permite que el formulario largo se recorra en
            // pantallas chicas; imePadding evita que el teclado tape el campo.
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(Espaciado.md),
        verticalArrangement = Arrangement.spacedBy(Espaciado.md)
    ) {

        Text(
            text = if (estado.esEdicion) "Editar oportunidad" else "Nueva oportunidad",
            style = MaterialTheme.typography.headlineSmall
        )

        // ---------- Aviso general de errores ----------
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
                Row(
                    modifier = Modifier.padding(Espaciado.md),
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null)
                    Text(estado.errorGeneral.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // ---------- Identificación del evento ----------
        TituloBloque("Identificación del evento")

        CampoTexto(
            valor = estado.nombre,
            alCambiar = viewModel::alCambiarNombre,
            etiqueta = "Nombre del evento",
            error = estado.errores.nombre
        )

        CampoTexto(
            valor = estado.descripcion,
            alCambiar = viewModel::alCambiarDescripcion,
            etiqueta = "Descripción",
            error = estado.errores.descripcion,
            lineas = 3
        )

        ListaDesplegable(
            etiqueta = "Tipo de evento",
            opciones = TipoOportunidad.entries,
            seleccionada = estado.tipo,
            textoDe = { it.etiqueta },
            alSeleccionar = viewModel::alCambiarTipo
        )

        CampoTexto(
            valor = estado.fuenteInformacion,
            alCambiar = viewModel::alCambiarFuente,
            etiqueta = "Fuente de información",
            error = estado.errores.fuenteInformacion,
            ayuda = "Dónde se detectó: sitio web, redes sociales, contacto directo…"
        )

        // ---------- Fecha y lugar ----------
        TituloBloque("Fecha y lugar")

        SelectorFecha(
            etiqueta = "Fecha de inicio del evento",
            valor = estado.fechaInicioEvento,
            alSeleccionar = viewModel::alCambiarFechaInicio,
            error = estado.errores.fechaInicioEvento
        )

        SelectorFecha(
            etiqueta = "Fecha de término",
            valor = estado.fechaTerminoEvento,
            alSeleccionar = viewModel::alCambiarFechaTermino,
            error = estado.errores.fechaTerminoEvento,
            ayuda = "Déjala vacía si el evento dura un solo día"
        )

        CampoTexto(
            valor = estado.horario,
            alCambiar = viewModel::alCambiarHorario,
            etiqueta = "Horario",
            error = estado.errores.horario,
            ayuda = "Por ejemplo: 10:00 - 18:00"
        )

        CampoTexto(
            valor = estado.direccion,
            alCambiar = viewModel::alCambiarDireccion,
            etiqueta = "Dirección o ubicación",
            error = estado.errores.direccion
        )

        // Región y comuna son dos listas encadenadas: elegir región recalcula
        // las comunas disponibles. Así se respeta la normalización del modelo.
        ListaDesplegable(
            etiqueta = "Región",
            opciones = estado.regiones,
            seleccionada = estado.regiones.firstOrNull { it.id == estado.regionId },
            textoDe = { it.nombre },
            alSeleccionar = { viewModel.alCambiarRegion(it.id) }
        )

        ListaDesplegable(
            etiqueta = "Comuna",
            opciones = estado.comunasDisponibles,
            seleccionada = estado.comunasDisponibles.firstOrNull { it.id == estado.comunaId },
            textoDe = { it.nombre },
            alSeleccionar = { viewModel.alCambiarComuna(it.id) },
            error = estado.errores.comuna,
            textoVacio = if (estado.regionId == null) "Primero elige una región" else "Selecciona una comuna"
        )

        // ---------- Organización y contacto ----------
        TituloBloque("Organización responsable")

        ListaDesplegable(
            etiqueta = "Organización",
            opciones = estado.organizaciones,
            seleccionada = estado.organizaciones.firstOrNull { it.id == estado.organizacionId },
            textoDe = { it.nombre },
            alSeleccionar = { viewModel.alCambiarOrganizacion(it.id) },
            error = estado.errores.organizacion
        )

        ListaDesplegable(
            etiqueta = "Contacto",
            opciones = estado.contactosDisponibles,
            seleccionada = estado.contactosDisponibles.firstOrNull { it.id == estado.contactoId },
            textoDe = { "${it.nombre} · ${it.cargo}" },
            alSeleccionar = { viewModel.alCambiarContacto(it.id) },
            textoVacio = if (estado.organizacionId == null) {
                "Primero elige una organización"
            } else {
                "Opcional: aún puede no estar identificado"
            }
        )

        // ---------- Evaluación comercial ----------
        TituloBloque("Evaluación comercial")

        CampoTexto(
            valor = estado.publicoObjetivo,
            alCambiar = viewModel::alCambiarPublicoObjetivo,
            etiqueta = "Público objetivo",
            error = estado.errores.publicoObjetivo,
            ayuda = "A quién apunta el evento: trabajadores, vecinos, afiliados…"
        )

        CampoTexto(
            valor = estado.publicoEsperado,
            alCambiar = viewModel::alCambiarPublicoEsperado,
            etiqueta = "Público esperado",
            error = estado.errores.publicoEsperado,
            soloNumeros = true,
            ayuda = "Cantidad estimada de asistentes"
        )

        ListaDesplegable(
            etiqueta = "Prioridad",
            opciones = Prioridad.entries,
            seleccionada = estado.prioridad,
            textoDe = { it.etiqueta },
            alSeleccionar = viewModel::alCambiarPrioridad
        )

        CampoTexto(
            valor = estado.costoReferencial,
            alCambiar = viewModel::alCambiarCosto,
            etiqueta = "Costo referencial",
            error = estado.errores.costoReferencial,
            soloNumeros = true,
            prefijo = "$ ",
            ayuda = "Déjalo vacío si la participación no tiene costo"
        )

        // ---------- Antecedentes adicionales ----------
        TituloBloque("Antecedentes adicionales")

        CampoTexto(
            valor = estado.condiciones,
            alCambiar = viewModel::alCambiarCondiciones,
            etiqueta = "Condiciones de participación",
            lineas = 2
        )

        CampoTexto(
            valor = estado.requisitos,
            alCambiar = viewModel::alCambiarRequisitos,
            etiqueta = "Requisitos",
            lineas = 2
        )

        CampoTexto(
            valor = estado.observaciones,
            alCambiar = viewModel::alCambiarObservaciones,
            etiqueta = "Observaciones",
            lineas = 2
        )

        Text(
            text = "Detectada el ${Fechas.aTexto(estado.fechaDeteccion)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ---------- Acciones ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)
        ) {
            OutlinedButton(
                onClick = alTerminar,
                enabled = !estado.guardando,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }

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
                    Text(if (estado.esEdicion) "Guardar cambios" else "Crear oportunidad")
                }
            }
        }
    }
}

/** Encabezado que separa visualmente los bloques del formulario. */
@Composable
private fun TituloBloque(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = Espaciado.sm)
    )
}
