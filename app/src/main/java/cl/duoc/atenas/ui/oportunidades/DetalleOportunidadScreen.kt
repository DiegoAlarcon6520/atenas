package cl.duoc.atenas.ui.oportunidades

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.core.CalendarioApp
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.OportunidadDetalle
import cl.duoc.atenas.data.local.entity.Seguimiento
import cl.duoc.atenas.di.FabricasViewModel
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.rules.GuiaGestion
import cl.duoc.atenas.domain.rules.SugerenciaPrioridad
import cl.duoc.atenas.domain.rules.TransicionesEstado
import cl.duoc.atenas.ui.components.Avatar
import cl.duoc.atenas.ui.components.BotonContorno
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.EtiquetaEstado
import cl.duoc.atenas.ui.components.EtiquetaSobreDegradado
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.ListaDesplegable
import cl.duoc.atenas.ui.components.iconoDeTipo
import cl.duoc.atenas.ui.theme.DegradadoMarca
import cl.duoc.atenas.ui.theme.DegradadoTarjeta
import cl.duoc.atenas.ui.theme.Espaciado
import cl.duoc.atenas.ui.theme.colorDeEstado
import java.util.Locale

/** Pestañas del detalle. */
private enum class PestanaDetalle(val etiqueta: String) { RESUMEN("Resumen"), GESTIONES("Gestiones"), CONTACTO("Contacto") }

/**
 * DETALLE DE OPORTUNIDAD
 *
 * Responde, en este orden, lo que el usuario necesita al abrirla:
 *  1. ¿Qué es? Ícono del tipo, nombre y organización.
 *  2. ¿En qué va? Indicador de avance por etapas.
 *  3. ¿Qué hago ahora? Tarjeta de siguiente paso con su acción (llamar,
 *     registrar gestión).
 *  4. El resto de la información, ordenada en pestañas (Resumen, Gestiones,
 *     Contacto) en vez de un único scroll largo.
 */
@Composable
fun DetalleOportunidadScreen(
    alEditar: (Long) -> Unit,
    alAbrirOrganizacion: (Long) -> Unit,
    alRegistrarGestion: (Long) -> Unit,
    alVerEvidencias: (Long) -> Unit,
    mostrarMensaje: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalleOportunidadViewModel = viewModel(factory = FabricasViewModel.detalleOportunidad)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var pestana by rememberSaveable { mutableStateOf(PestanaDetalle.RESUMEN) }

    // Resultado de la asignación de responsable (éxito o error del servidor).
    LaunchedEffect(Unit) { viewModel.mensajes.collect { mostrarMensaje(it) } }

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

    // Abre otra aplicación del teléfono; si no hay ninguna que lo resuelva,
    // se avisa en vez de cerrar la app con una excepción.
    val abrir: (Intent, String) -> Unit = { intent, siFalla ->
        try {
            contexto.startActivity(intent)
        } catch (excepcion: ActivityNotFoundException) {
            mostrarMensaje(siFalla)
        }
    }
    val llamar: (() -> Unit)? = detalle.contactoTelefono?.let { telefono ->
        { abrir(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${telefono.replace(" ", "")}")), "No hay una aplicación de teléfono") }
    }
    val escribir: (() -> Unit)? = detalle.contactoCorreo?.let { correo ->
        { abrir(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$correo")), "No hay una aplicación de correo") }
    }
    val agendar: () -> Unit = {
        abrir(
            CalendarioApp.intentNuevoEvento(
                titulo = oportunidad.nombre,
                descripcion = oportunidad.descripcion,
                lugar = "${oportunidad.direccion}, ${detalle.comunaNombre}",
                inicio = Fechas.fechaConHora(oportunidad.fechaInicioEvento, oportunidad.horaInicio),
                termino = Fechas.fechaConHora(
                    oportunidad.fechaTerminoEvento ?: oportunidad.fechaInicioEvento,
                    oportunidad.horaTermino
                )
            ),
            "No hay una aplicación de calendario disponible"
        )
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
        // 1 ---------- Qué es ----------
        item {
            Encabezado(
                detalle = detalle,
                admiteCambios = admiteCambios,
                alEditar = { alEditar(oportunidad.id) },
                alAbrirOrganizacion = { alAbrirOrganizacion(oportunidad.organizacionId) }
            )
        }

        // 2 ---------- En qué va ----------
        item { IndicadorEtapa(oportunidad.estado) }

        // 3 ---------- Qué hago ahora ----------
        item {
            SiguientePaso(
                detalle = detalle,
                ultima = estado.historial.firstOrNull(),
                admiteCambios = admiteCambios,
                alLlamar = llamar,
                alRegistrar = { alRegistrarGestion(oportunidad.id) }
            )
        }

        // 4 ---------- Pestañas ----------
        item {
            SelectorPestanas(
                pestanas = PestanaDetalle.entries,
                actual = pestana,
                etiqueta = { if (it == PestanaDetalle.GESTIONES) "Gestiones · ${estado.historial.size}" else it.etiqueta },
                alElegir = { pestana = it }
            )
        }

        when (pestana) {
            PestanaDetalle.RESUMEN -> {
                item { CuadriculaDatos(detalle) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.entreTarjetas)) {
                        AccesoRapido(Icons.Default.PhotoLibrary, "Evidencias", { alVerEvidencias(oportunidad.id) }, Modifier.weight(1f))
                        AccesoRapido(Icons.Default.CalendarMonth, "Agendar", agendar, Modifier.weight(1f))
                    }
                }
                if (estado.puedeAsignar) {
                    item { TarjetaAsignacion(estado, oportunidad.responsableId, viewModel) }
                }
                item {
                    SeccionDatos(
                        titulo = "Evento",
                        datos = listOf(
                            "Tipo" to oportunidad.tipo.etiqueta,
                            "Fecha de término" to (oportunidad.fechaTerminoEvento?.let { Fechas.aTexto(it) } ?: "Un solo día"),
                            "Dirección" to oportunidad.direccion,
                            "Comuna" to "${detalle.comunaNombre}, ${detalle.regionNombre}",
                            "Público objetivo" to oportunidad.publicoObjetivo,
                            "Descripción" to oportunidad.descripcion
                        )
                    )
                }
                item {
                    SeccionDatos(
                        titulo = "Gestión comercial",
                        inicialmenteAbierta = false,
                        datos = listOf(
                            "Responsable" to (detalle.responsableNombre ?: "Sin asignar"),
                            "Registrada por" to detalle.creadorNombre,
                            "Fuente" to oportunidad.fuenteInformacion,
                            "Detectada el" to Fechas.aTexto(oportunidad.fechaDeteccion),
                            "Prioridad sugerida" to SugerenciaPrioridad.sugerir(oportunidad.potencial, oportunidad.afinidad).etiqueta,
                            "Condiciones" to (oportunidad.condiciones ?: "Sin condiciones registradas"),
                            "Requisitos" to (oportunidad.requisitos ?: "Sin requisitos registrados"),
                            "Espacios disponibles" to (oportunidad.espaciosDisponibles ?: "Sin información de espacios"),
                            "Observaciones" to (oportunidad.observaciones ?: "Sin observaciones"),
                            "Última actualización" to Fechas.aTextoConHora(oportunidad.fechaActualizacion)
                        )
                    )
                }
            }

            PestanaDetalle.GESTIONES -> {
                if (estado.historial.isEmpty()) {
                    item { EstadoVacio("Aún no se registran gestiones para esta oportunidad.") }
                } else {
                    items(estado.historial, key = { it.id }) { seguimiento ->
                        TarjetaSeguimiento(
                            seguimiento = seguimiento,
                            alAgendarRecordatorio = { fecha ->
                                abrir(
                                    CalendarioApp.intentRecordatorioGestion(
                                        tituloOportunidad = oportunidad.nombre,
                                        proximaAccion = seguimiento.proximaAccion.orEmpty(),
                                        fecha = fecha
                                    ),
                                    "No hay una aplicación de calendario disponible"
                                )
                            }
                        )
                    }
                }
            }

            PestanaDetalle.CONTACTO -> {
                item { TarjetaContacto(detalle, llamar, escribir) }
                item {
                    Card(
                        onClick = { alAbrirOrganizacion(oportunidad.organizacionId) },
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(Espaciado.tarjeta),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Organización", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(detalle.organizacionNombre, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${detalle.organizacionTipo.etiqueta} · ${detalle.organizacionRubro}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Ver ficha de la organización",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = Espaciado.sm)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Encabezado(
    detalle: OportunidadDetalle,
    admiteCambios: Boolean,
    alEditar: () -> Unit,
    alAbrirOrganizacion: () -> Unit
) {
    val oportunidad = detalle.oportunidad
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(DegradadoTarjeta),
            contentAlignment = Alignment.Center
        ) {
            Icon(iconoDeTipo(oportunidad.tipo), contentDescription = oportunidad.tipo.etiqueta, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(oportunidad.nombre, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "${detalle.organizacionNombre} · ${detalle.comunaNombre}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClickLabel = "Abrir organización", onClick = alAbrirOrganizacion)
            )
        }
        // Una oportunidad cerrada ya no se edita.
        if (admiteCambios) {
            FilledTonalIconButton(
                onClick = alEditar,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Editar antecedentes")
            }
        }
    }
}

/**
 * Indicador de avance: círculos unidos por una línea, completos hasta la
 * etapa actual. Si la oportunidad salió del embudo (descartada o cerrada),
 * se muestra en su lugar una franja con el estado.
 */
@Composable
private fun IndicadorEtapa(estadoActual: EstadoOportunidad) {
    val indice = GuiaGestion.indiceEtapa(estadoActual)
    Card(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        if (indice == null) {
            Row(
                modifier = Modifier.padding(Espaciado.md),
                horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EtiquetaEstado(estadoActual)
                Text(
                    text = if (estadoActual == EstadoOportunidad.CERRADA) "Historial sellado" else "Fuera del embudo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val primario = MaterialTheme.colorScheme.primary
            val apagado = MaterialTheme.colorScheme.outlineVariant
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = Espaciado.md), verticalAlignment = Alignment.Top) {
                GuiaGestion.etapasVisibles.forEachIndexed { i, nombre ->
                    val completa = i < indice
                    val actual = i == indice
                    Column(modifier = Modifier.width(58.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (completa) primario else MaterialTheme.colorScheme.surfaceContainerHigh)
                                .then(if (actual) Modifier.border(2.dp, primario, CircleShape) else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                completa -> Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                                actual -> Box(Modifier.size(8.dp).clip(CircleShape).background(primario))
                            }
                        }
                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (actual) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    if (i < GuiaGestion.etapasVisibles.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 10.dp)
                                .height(2.dp)
                                .background(if (i < indice) primario else apagado)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de siguiente paso. Si la última gestión comprometió una próxima
 * acción, se muestra esa con su vencimiento; si no, la sugerencia según el
 * estado (GuiaGestion).
 */
@Composable
private fun SiguientePaso(
    detalle: OportunidadDetalle,
    ultima: Seguimiento?,
    admiteCambios: Boolean,
    alLlamar: (() -> Unit)?,
    alRegistrar: () -> Unit
) {
    val comprometida = ultima?.takeIf { it.fechaProximoSeguimiento != null }?.proximaAccion
    val texto = comprometida ?: GuiaGestion.siguientePaso(detalle.oportunidad.estado, detalle.contactoNombre) ?: return
    val vencimiento = ultima?.fechaProximoSeguimiento
        ?.takeIf { comprometida != null }
        ?.let { GuiaGestion.textoVencimiento(Fechas.diasEntre(Fechas.hoy(), it)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(DegradadoTarjeta)
            .padding(Espaciado.md)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm), verticalAlignment = Alignment.CenterVertically) {
                Text("SIGUIENTE PASO", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
                vencimiento?.let { EtiquetaSobreDegradado(it) }
            }
            Text(texto, style = MaterialTheme.typography.titleMedium, color = Color.White)
            if (admiteCambios) {
                Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                    if (alLlamar != null) {
                        BotonSobreDegradado("Llamar", Icons.Default.Phone, true, alLlamar, Modifier.weight(1f))
                    }
                    BotonSobreDegradado("Registrar gestión", Icons.AutoMirrored.Filled.NoteAdd, alLlamar == null, alRegistrar, Modifier.weight(1f))
                }
            }
        }
    }
}

/** Botón en píldora sobre un degradado: relleno blanco el principal, translúcido el secundario. */
@Composable
private fun BotonSobreDegradado(
    texto: String,
    icono: ImageVector,
    principal: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contenido = if (principal) Color(0xFF123A4A) else Color.White
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(if (principal) Color.White else Color.White.copy(alpha = 0.16f))
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = contenido, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.labelLarge, color = contenido)
    }
}

/** Control segmentado: una píldora con las opciones y la elegida resaltada. */
@Composable
private fun <T> SelectorPestanas(pestanas: List<T>, actual: T, etiqueta: (T) -> String, alElegir: (T) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(4.dp)
    ) {
        pestanas.forEach { p ->
            val elegida = p == actual
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(if (elegida) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent)
                    .clickable(role = Role.Tab, onClick = { alElegir(p) }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = etiqueta(p),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (elegida) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Los cuatro datos que más se consultan, en una cuadrícula de 2 x 2. */
@Composable
private fun CuadriculaDatos(detalle: OportunidadDetalle) {
    val o = detalle.oportunidad
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DatoDestacado(Icons.Default.Event, "Fecha y horario", "${Fechas.aTexto(o.fechaInicioEvento)}\n${Fechas.rangoHorario(o.horaInicio, o.horaTermino)}", Modifier.weight(1f))
            DatoDestacado(Icons.Default.Groups, "Asistentes", "${o.publicoEsperado} personas", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DatoDestacado(Icons.Default.Insights, "Potencial · Afinidad", "${o.potencial.etiqueta} · ${o.afinidad.etiqueta}", Modifier.weight(1f))
            DatoDestacado(Icons.Default.AttachMoney, "Costo referencial", formatearMonto(o.costoReferencial), Modifier.weight(1f))
        }
    }
}

@Composable
private fun DatoDestacado(icono: ImageVector, etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Card(shape = MaterialTheme.shapes.large, modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun AccesoRapido(icono: ImageVector, texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, shape = MaterialTheme.shapes.large, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Asignación de responsable: solo la ve el supervisor. */
@Composable
private fun TarjetaAsignacion(estado: DetalleOportunidadUiState, responsableId: Long?, viewModel: DetalleOportunidadViewModel) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(Espaciado.tarjeta), verticalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Responsable", style = MaterialTheme.typography.titleSmall)
            }
            ListaDesplegable(
                etiqueta = "Ejecutivo a cargo",
                opciones = estado.ejecutivos,
                seleccionada = estado.ejecutivos.firstOrNull { it.id == responsableId },
                textoDe = { "${it.nombreCompleto} · ${it.zona}" },
                alSeleccionar = { viewModel.asignarResponsable(it.id) },
                textoVacio = "Sin asignar"
            )
            if (responsableId != null) {
                BotonContorno(onClick = { viewModel.asignarResponsable(null) }) {
                    Text("Quitar asignación", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

/** Ficha del contacto con acciones directas: llamar y escribir. */
@Composable
private fun TarjetaContacto(detalle: OportunidadDetalle, alLlamar: (() -> Unit)?, alEscribir: (() -> Unit)?) {
    Card(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        if (detalle.contactoNombre == null) {
            EstadoVacio("Esta oportunidad todavía no tiene un contacto asignado. Agrégalo al editar los antecedentes.")
            return@Card
        }
        Column(modifier = Modifier.padding(Espaciado.tarjeta), verticalArrangement = Arrangement.spacedBy(Espaciado.md)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(detalle.contactoNombre, tamano = 52.dp)
                Column {
                    Text(detalle.contactoNombre, style = MaterialTheme.typography.titleMedium)
                    Text(detalle.contactoCargo ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            detalle.contactoTelefono?.let { FilaContacto(Icons.Default.Phone, it) }
            detalle.contactoCorreo?.let { FilaContacto(Icons.Default.Email, it) }
            Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                if (alLlamar != null) AccesoRapido(Icons.Default.Phone, "Llamar", alLlamar, Modifier.weight(1f))
                if (alEscribir != null) AccesoRapido(Icons.Default.Email, "Correo", alEscribir, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FilaContacto(icono: ImageVector, texto: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Bloque de datos plegable: reduce la saturación visual de la pantalla. */
@Composable
private fun SeccionDatos(titulo: String, datos: List<Pair<String, String>>, inicialmenteAbierta: Boolean = true) {
    var expandida by remember { mutableStateOf(inicialmenteAbierta) }
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
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(valor, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

/** Gestión del historial, con el cambio de estado si lo hubo. */
@Composable
private fun TarjetaSeguimiento(seguimiento: Seguimiento, alAgendarRecordatorio: (Long) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(Espaciado.tarjeta), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(DegradadoMarca)
                )
                Text(
                    text = seguimiento.tipoAccion.etiqueta,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .padding(start = Espaciado.sm)
                        .weight(1f)
                )
                Text(Fechas.aTexto(seguimiento.fechaAccion), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(seguimiento.resultado, style = MaterialTheme.typography.bodyMedium)
            if (seguimiento.estadoAnterior != seguimiento.estadoNuevo) {
                Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.xs), verticalAlignment = Alignment.CenterVertically) {
                    EtiquetaEstado(seguimiento.estadoAnterior)
                    Text("→", style = MaterialTheme.typography.labelLarge, color = colorDeEstado(seguimiento.estadoNuevo))
                    EtiquetaEstado(seguimiento.estadoNuevo)
                }
            }
            seguimiento.proximaAccion?.let { proxima ->
                Text(
                    text = "Próxima acción: $proxima" + (seguimiento.fechaProximoSeguimiento?.let { " (${Fechas.aTexto(it)})" } ?: ""),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                seguimiento.fechaProximoSeguimiento?.let { fecha ->
                    Text(
                        text = "Agendar recordatorio",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(role = Role.Button) { alAgendarRecordatorio(fecha) }
                            .padding(vertical = 6.dp)
                    )
                }
            }
        }
    }
}

private fun formatearMonto(valor: Double): String =
    if (valor <= 0.0) "Sin costo" else "$" + String.format(Locale.US, "%,.0f", valor).replace(',', '.')
