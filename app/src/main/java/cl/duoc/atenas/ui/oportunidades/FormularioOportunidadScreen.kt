package cl.duoc.atenas.ui.oportunidades

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.di.FabricasViewModel
import cl.duoc.atenas.domain.model.AfinidadPublico
import cl.duoc.atenas.domain.model.PotencialComercial
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.ui.components.BotonContorno
import cl.duoc.atenas.ui.components.BotonDegradado
import cl.duoc.atenas.ui.components.CampoTexto
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.ListaDesplegable
import cl.duoc.atenas.ui.components.SelectorFecha
import cl.duoc.atenas.ui.components.SelectorHora
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
 * @param alCrearOrganizacion abre el registro de organización desde el paso 3.
 * @param alCrearContacto abre el registro de contacto para la organización elegida.
 * @param mostrarMensaje muestra el Snackbar de confirmación.
 */
@Composable
fun FormularioOportunidadScreen(
    alTerminar: () -> Unit,
    alCrearOrganizacion: () -> Unit,
    alCrearContacto: (organizacionId: Long) -> Unit,
    mostrarMensaje: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FormularioOportunidadViewModel = viewModel(factory = FabricasViewModel.formularioOportunidad)
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
            .padding(horizontal = Espaciado.pantalla, vertical = Espaciado.md),
        verticalArrangement = Arrangement.spacedBy(Espaciado.md)
    ) {

        // ---------- Progreso del asistente ----------
        // Dividir el formulario en cuatro pasos baja la carga: cada pantalla
        // pide un tema (qué, cuándo y dónde, con quién, cuánto vale) y la
        // barra muestra cuánto falta.
        EncabezadoPaso(paso = estado.paso, esEdicion = estado.esEdicion)

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

        // ---------- Paso 1: el evento ----------
        if (estado.paso == 0) {

            CampoTexto(
                valor = estado.nombre,
                alCambiar = viewModel::alCambiarNombre,
                etiqueta = "Nombre del evento",
                icono = Icons.Default.Event,
                error = estado.errores.nombre
            )

            CampoTexto(
                valor = estado.descripcion,
                alCambiar = viewModel::alCambiarDescripcion,
                etiqueta = "Descripción",
                icono = Icons.Default.Description,
                error = estado.errores.descripcion,
                lineas = 3
            )

            ListaDesplegable(
                etiqueta = "Tipo de evento",
                icono = Icons.Default.Category,
                opciones = TipoOportunidad.entries,
                seleccionada = estado.tipo,
                textoDe = { it.etiqueta },
                alSeleccionar = viewModel::alCambiarTipo
            )

            CampoTexto(
                valor = estado.fuenteInformacion,
                alCambiar = viewModel::alCambiarFuente,
                etiqueta = "Fuente de información",
                icono = Icons.Default.Link,
                error = estado.errores.fuenteInformacion,
                ayuda = "Dónde se detectó: sitio web, redes sociales, contacto directo…"
            )
        }

        // ---------- Paso 2: fecha y lugar ----------
        if (estado.paso == 1) {

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

            // Las dos horas van lado a lado: se leen como un rango, que es como
            // el usuario piensa el horario del evento.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)
            ) {
                SelectorHora(
                    etiqueta = "Hora de inicio",
                    minutosDelDia = estado.horaInicio,
                    alSeleccionar = viewModel::alCambiarHoraInicio,
                    modifier = Modifier.weight(1f)
                )
                SelectorHora(
                    etiqueta = "Hora de término",
                    minutosDelDia = estado.horaTermino,
                    alSeleccionar = viewModel::alCambiarHoraTermino,
                    modifier = Modifier.weight(1f)
                )
            }

            // El error del rango se muestra una sola vez bajo ambos campos.
            estado.errores.horario?.let { mensaje ->
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            CampoTexto(
                valor = estado.direccion,
                alCambiar = viewModel::alCambiarDireccion,
                etiqueta = "Dirección o ubicación",
                icono = Icons.Default.LocationOn,
                error = estado.errores.direccion
            )

            // Región y comuna son dos listas encadenadas: elegir región recalcula
            // las comunas disponibles. Así se respeta la normalización del modelo.
            ListaDesplegable(
                etiqueta = "Región",
                icono = Icons.Default.Map,
                opciones = estado.regiones,
                seleccionada = estado.regiones.firstOrNull { it.id == estado.regionId },
                textoDe = { it.nombre },
                alSeleccionar = { viewModel.alCambiarRegion(it.id) }
            )

            ListaDesplegable(
                etiqueta = "Comuna",
                icono = Icons.Default.Place,
                opciones = estado.comunasDisponibles,
                seleccionada = estado.comunasDisponibles.firstOrNull { it.id == estado.comunaId },
                textoDe = { it.nombre },
                alSeleccionar = { viewModel.alCambiarComuna(it.id) },
                error = estado.errores.comuna,
                textoVacio = if (estado.regionId == null) "Primero elige una región" else "Selecciona una comuna"
            )
            // Con la fecha elegida se calcula cuánto margen queda para preparar
            // la activación: es el dato que más pesa en esta etapa.
            estado.fechaInicioEvento?.let { inicio ->
                val dias = Fechas.diasEntre(Fechas.hoy(), inicio)
                AvisoMargen(dias)
            }
        }

        // ---------- Paso 3: organización y contacto ----------
        if (estado.paso == 2) {

            ListaDesplegable(
                etiqueta = "Organización",
                icono = Icons.Default.Business,
                opciones = estado.organizaciones,
                seleccionada = estado.organizaciones.firstOrNull { it.id == estado.organizacionId },
                textoDe = { it.nombre },
                alSeleccionar = { viewModel.alCambiarOrganizacion(it.id) },
                error = estado.errores.organizacion
            )

            ListaDesplegable(
                etiqueta = "Contacto",
                icono = Icons.Default.Person,
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

            // Si la organización o el contacto todavía no están registrados,
            // se crean aquí mismo: al volver aparecen en las listas para
            // elegirlos y lo ya escrito en el formulario se conserva.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonContorno(
                    onClick = alCrearOrganizacion,
                    habilitado = estado.admiteCambios,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Nueva organización", modifier = Modifier.padding(start = 6.dp))
                }
                BotonContorno(
                    // Un contacto siempre pertenece a una organización.
                    onClick = { estado.organizacionId?.let(alCrearContacto) },
                    habilitado = estado.admiteCambios && estado.organizacionId != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Nuevo contacto", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }

        // ---------- Paso 4: evaluación y antecedentes ----------
        if (estado.paso == 3) {

            CampoTexto(
                valor = estado.publicoObjetivo,
                alCambiar = viewModel::alCambiarPublicoObjetivo,
                etiqueta = "Público objetivo",
                icono = Icons.Default.Groups,
                error = estado.errores.publicoObjetivo,
                ayuda = "A quién apunta el evento: trabajadores, vecinos, afiliados…"
            )

            CampoTexto(
                valor = estado.publicoEsperado,
                alCambiar = viewModel::alCambiarPublicoEsperado,
                etiqueta = "Público esperado",
                icono = Icons.Default.People,
                error = estado.errores.publicoEsperado,
                soloNumeros = true,
                ayuda = "Cantidad estimada de asistentes"
            )

            // Evaluación de la oportunidad (HU-06): potencial y afinidad.
            ListaDesplegable(
                etiqueta = "Potencial comercial",
                icono = Icons.Default.Insights,
                opciones = PotencialComercial.entries,
                seleccionada = estado.potencial,
                textoDe = { it.etiqueta },
                alSeleccionar = viewModel::alCambiarPotencial
            )

            ListaDesplegable(
                etiqueta = "Afinidad con el público objetivo",
                icono = Icons.Default.ThumbUp,
                opciones = AfinidadPublico.entries,
                seleccionada = estado.afinidad,
                textoDe = { it.etiqueta },
                alSeleccionar = viewModel::alCambiarAfinidad
            )

            if (estado.puedeCambiarPrioridad) {
                ListaDesplegable(
                    etiqueta = "Prioridad",
                    icono = Icons.Default.Flag,
                    opciones = Prioridad.entries,
                    seleccionada = estado.prioridad,
                    textoDe = { it.etiqueta },
                    alSeleccionar = viewModel::alCambiarPrioridad
                )

                // La sugerencia se recalcula sola al cambiar potencial o afinidad,
                // pero el supervisor decide si aplicarla.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Prioridad sugerida: ${estado.prioridadSugerida.etiqueta}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = Espaciado.sm)
                    )
                    if (estado.prioridad != estado.prioridadSugerida) {
                        TextButton(onClick = viewModel::aplicarPrioridadSugerida) {
                            Text("Aplicar")
                        }
                    }
                }
            } else {
                // El ejecutivo no elige la prioridad (regla HU-05): al crear se
                // le asigna la sugerida y al editar se conserva la vigente.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column(modifier = Modifier.padding(start = Espaciado.sm)) {
                        Text(
                            text = "Prioridad: ${estado.prioridad.etiqueta}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (estado.esEdicion) {
                                "La prioridad la ajusta el supervisor comercial."
                            } else {
                                "Se asigna según la evaluación; el supervisor puede ajustarla."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            CampoTexto(
                valor = estado.costoReferencial,
                alCambiar = viewModel::alCambiarCosto,
                etiqueta = "Costo referencial",
                icono = Icons.Default.AttachMoney,
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
                icono = Icons.AutoMirrored.Filled.Assignment,
                lineas = 2
            )

            CampoTexto(
                valor = estado.requisitos,
                alCambiar = viewModel::alCambiarRequisitos,
                etiqueta = "Requisitos",
                icono = Icons.Default.AssignmentTurnedIn,
                lineas = 2
            )

            CampoTexto(
                valor = estado.espaciosDisponibles,
                alCambiar = viewModel::alCambiarEspacios,
                etiqueta = "Espacios disponibles",
                icono = Icons.Default.Storefront,
                lineas = 2,
                ayuda = "Stands, módulos o metros que ofrece el organizador"
            )

            CampoTexto(
                valor = estado.observaciones,
                alCambiar = viewModel::alCambiarObservaciones,
                etiqueta = "Observaciones",
                icono = Icons.AutoMirrored.Filled.Notes,
                lineas = 2
            )

            Text(
                text = "Detectada el ${Fechas.aTexto(estado.fechaDeteccion)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ---------- Navegación entre pasos ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Espaciado.sm),
            horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonContorno(
                onClick = if (estado.paso == 0) alTerminar else viewModel::pasoAnterior,
                habilitado = !estado.guardando,
                modifier = Modifier.width(120.dp)
            ) {
                Text(
                    text = if (estado.paso == 0) "Cancelar" else "Atrás",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (estado.paso < FormularioOportunidadViewModel.TOTAL_PASOS - 1) {
                BotonDegradado(
                    texto = "Siguiente",
                    icono = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = viewModel::pasoSiguiente,
                    modifier = Modifier.weight(1f)
                )
            } else {
                BotonDegradado(
                    texto = if (estado.esEdicion) "Guardar cambios" else "Crear oportunidad",
                    onClick = viewModel::guardar,
                    habilitado = estado.admiteCambios,
                    cargando = estado.guardando,
                    modifier = Modifier.weight(1f)
                )
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

/** Títulos y textos de apoyo de cada paso del asistente. */
private val pasosAsistente = listOf(
    "¿Qué evento es?" to "Nombre, tipo y de dónde salió la oportunidad.",
    "¿Cuándo y dónde?" to "Con la fecha calculamos cuánto margen tienes para preparar la activación.",
    "¿Con quién?" to "La organización responsable y, si lo tienes, el contacto.",
    "¿Cuánto vale?" to "Público, evaluación comercial y condiciones de participación."
)

/** Barra de progreso de cuatro tramos, "Paso X de 4" y el título del paso. */
@Composable
private fun EncabezadoPaso(paso: Int, esEdicion: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
        Text(
            text = (if (esEdicion) "Editar · " else "") + "Paso ${paso + 1} de ${pasosAsistente.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            pasosAsistente.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(
                            if (i <= paso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
        Text(
            text = pasosAsistente[paso].first,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = Espaciado.sm)
        )
        Text(
            text = pasosAsistente[paso].second,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Aviso del margen que queda hasta el evento, con color según la urgencia. */
@Composable
private fun AvisoMargen(dias: Int) {
    val color = when {
        dias < 15 -> MaterialTheme.colorScheme.error
        dias <= 30 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    val texto = when {
        dias < 0 -> "La fecha ya pasó."
        dias == 0 -> "El evento es hoy: no hay margen para preparar una propuesta."
        dias < 15 -> "Faltan $dias días: margen justo para enviar la propuesta."
        dias <= 30 -> "Faltan $dias días: buen momento para el primer contacto."
        else -> "Faltan $dias días: margen holgado para preparar la activación."
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(color.copy(alpha = 0.12f))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}
