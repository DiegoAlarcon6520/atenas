package cl.duoc.atenas.ui.oportunidades

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.data.local.entity.Comuna
import cl.duoc.atenas.data.local.entity.Organizacion
import cl.duoc.atenas.data.local.entity.Region
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.di.FabricasViewModel
import cl.duoc.atenas.domain.model.EtapaCartera
import cl.duoc.atenas.domain.model.PotencialComercial
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.RangoFecha
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.ui.components.BotonDegradado
import cl.duoc.atenas.ui.components.ChipPildora
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.ListaDesplegable
import cl.duoc.atenas.ui.components.TarjetaOportunidad
import cl.duoc.atenas.ui.components.coloresCampo
import cl.duoc.atenas.ui.theme.Espaciado

/**
 * CARTERA DE OPORTUNIDADES
 *
 * Las etapas del embudo van en pestañas siempre visibles (Todas, Por
 * contactar, En curso, Confirmadas, Cerradas), cada una con su total: es el
 * filtro que más se usa, así que no debe quedar escondido. El resto de los
 * filtros se abre en una hoja inferior, y los activos se muestran como chips
 * que se quitan con un toque.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListaOportunidadesScreen(
    alAbrirDetalle: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ListaOportunidadesViewModel = viewModel(factory = FabricasViewModel.listaOportunidades)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
    }

    if (estado.mostrarFiltros) {
        HojaFiltros(estado = estado, viewModel = viewModel)
    }

    val chipsActivos = chipsDeFiltros(estado, viewModel)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Espaciado.pantalla,
            end = Espaciado.pantalla,
            top = Espaciado.xs,
            bottom = Espaciado.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Espaciado.entreTarjetas)
    ) {
        // ---------- Buscador y botón de filtros ----------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = estado.filtros.texto,
                    onValueChange = viewModel::alBuscar,
                    placeholder = { Text("Buscar evento u organización") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (estado.filtros.texto.isNotEmpty()) {
                            IconButton(onClick = { viewModel.alBuscar("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    singleLine = true,
                    shape = CircleShape,
                    colors = coloresCampo(),
                    modifier = Modifier.weight(1f)
                )
                BadgedBox(
                    badge = {
                        if (estado.filtros.cantidadActivos > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) { Text(estado.filtros.cantidadActivos.toString()) }
                        }
                    }
                ) {
                    FilledTonalIconButton(
                        onClick = viewModel::alternarPanelFiltros,
                        modifier = Modifier.size(54.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Filtros")
                    }
                }
            }
        }

        // ---------- Pestañas de etapa ----------
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                item {
                    ChipPildora(
                        texto = "Todas · ${estado.totalSinEtapa}",
                        seleccionado = estado.etapa == null,
                        onClick = { viewModel.alElegirEtapa(null) }
                    )
                }
                items(EtapaCartera.entries) { etapa ->
                    ChipPildora(
                        texto = "${etapa.etiqueta} · ${estado.conteoPorEtapa[etapa] ?: 0}",
                        seleccionado = estado.etapa == etapa,
                        onClick = { viewModel.alElegirEtapa(etapa) }
                    )
                }
            }
        }

        // ---------- Filtros activos ----------
        if (chipsActivos.isNotEmpty()) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
                    verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
                ) {
                    chipsActivos.forEach { (texto, quitar) ->
                        InputChip(
                            selected = false,
                            onClick = quitar,
                            label = { Text(texto) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Quitar filtro $texto",
                                    modifier = Modifier.size(InputChipDefaults.IconSize)
                                )
                            },
                            shape = CircleShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }
            }
        }

        // ---------- Resultados ----------
        if (estado.oportunidades.isEmpty()) {
            item {
                EstadoVacio(
                    mensaje = if (estado.filtros.hayAlguno || estado.etapa != null) {
                        "Nada coincide con esta búsqueda. Prueba con otra etapa o quita algún filtro."
                    } else {
                        "Todavía no hay oportunidades. Toca + para registrar la primera."
                    }
                )
            }
        } else {
            items(estado.oportunidades, key = { it.id }) { oportunidad ->
                TarjetaOportunidad(
                    oportunidad = oportunidad,
                    onClick = { alAbrirDetalle(oportunidad.id) },
                    mostrarResponsable = estado.esSupervisor
                )
            }
        }
    }
}

/** Chips de los filtros activos, cada uno con la acción que lo quita. */
private fun chipsDeFiltros(
    estado: ListaOportunidadesUiState,
    viewModel: ListaOportunidadesViewModel
): List<Pair<String, () -> Unit>> {
    val f = estado.filtros
    return buildList {
        f.rangoFecha?.let { add(it.etiqueta to viewModel::quitarRangoFecha) }
        f.prioridad?.let { add("Prioridad ${it.etiqueta.lowercase()}" to viewModel::quitarPrioridad) }
        f.potencial?.let { add("Potencial ${it.etiqueta.lowercase()}" to viewModel::quitarPotencial) }
        f.tipo?.let { add(it.etiqueta to viewModel::quitarTipo) }
        f.regionId?.let { id ->
            estado.regiones.firstOrNull { it.id == id }?.let { add(it.nombre to { viewModel.alSeleccionarRegion(null) }) }
        }
        f.comunaId?.let { id ->
            estado.comunasDisponibles.firstOrNull { it.id == id }?.let { add(it.nombre to { viewModel.alSeleccionarComuna(null) }) }
        }
        f.organizacionId?.let { id ->
            estado.organizaciones.firstOrNull { it.id == id }
                ?.let { add(it.nombre to { viewModel.alSeleccionarOrganizacion(null) }) }
        }
        f.responsableId?.let { id ->
            estado.ejecutivos.firstOrNull { it.id == id }
                ?.let { add(it.nombreCompleto to { viewModel.alSeleccionarResponsable(null) }) }
        }
    }
}

/**
 * Hoja inferior con todos los filtros. Se aplican al momento (la lista de
 * atrás ya se ve filtrada) y el botón final dice cuántos resultados quedan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaFiltros(estado: ListaOportunidadesUiState, viewModel: ListaOportunidadesViewModel) {
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = viewModel::cerrarPanelFiltros,
        sheetState = estadoHoja,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = Espaciado.md),
            verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Espaciado.pantalla),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filtros", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (estado.filtros.cantidadActivos > 0) {
                    TextButton(onClick = viewModel::limpiarFiltros) { Text("Limpiar") }
                }
            }

            GrupoFiltros(
                titulo = "Fecha del evento",
                opciones = RangoFecha.entries,
                etiqueta = { it.etiqueta },
                seleccionada = estado.filtros.rangoFecha,
                alSeleccionar = viewModel::alSeleccionarRangoFecha
            )
            GrupoFiltros(
                titulo = "Prioridad",
                opciones = Prioridad.entries,
                etiqueta = { it.etiqueta },
                seleccionada = estado.filtros.prioridad,
                alSeleccionar = viewModel::alSeleccionarPrioridad
            )
            GrupoFiltros(
                titulo = "Potencial comercial",
                opciones = PotencialComercial.entries,
                etiqueta = { it.etiqueta },
                seleccionada = estado.filtros.potencial,
                alSeleccionar = viewModel::alSeleccionarPotencial
            )
            GrupoFiltros(
                titulo = "Tipo de evento",
                opciones = TipoOportunidad.entries,
                etiqueta = { it.etiqueta },
                seleccionada = estado.filtros.tipo,
                alSeleccionar = viewModel::alSeleccionarTipo
            )

            // Ubicación, organización y responsable tienen muchas opciones:
            // van en listas desplegables. La primera opción quita el filtro.
            Column(
                modifier = Modifier.padding(horizontal = Espaciado.pantalla, vertical = Espaciado.sm),
                verticalArrangement = Arrangement.spacedBy(Espaciado.xs)
            ) {
                ListaDesplegable<Region?>(
                    etiqueta = "Región",
                    icono = Icons.Default.Map,
                    opciones = listOf<Region?>(null) + estado.regiones,
                    seleccionada = estado.regiones.firstOrNull { it.id == estado.filtros.regionId },
                    textoDe = { it?.nombre ?: "Todas las regiones" },
                    alSeleccionar = viewModel::alSeleccionarRegion
                )
                ListaDesplegable<Comuna?>(
                    etiqueta = "Comuna",
                    icono = Icons.Default.LocationOn,
                    opciones = listOf<Comuna?>(null) + estado.comunasDisponibles,
                    seleccionada = estado.comunasDisponibles.firstOrNull { it.id == estado.filtros.comunaId },
                    textoDe = { it?.nombre ?: "Todas las comunas" },
                    alSeleccionar = viewModel::alSeleccionarComuna
                )
                ListaDesplegable<Organizacion?>(
                    etiqueta = "Organización",
                    icono = Icons.Default.Business,
                    opciones = listOf<Organizacion?>(null) + estado.organizaciones,
                    seleccionada = estado.organizaciones.firstOrNull { it.id == estado.filtros.organizacionId },
                    textoDe = { it?.nombre ?: "Todas las organizaciones" },
                    alSeleccionar = viewModel::alSeleccionarOrganizacion
                )
                if (estado.esSupervisor) {
                    ListaDesplegable<Usuario?>(
                        etiqueta = "Responsable",
                        icono = Icons.Default.Person,
                        opciones = listOf<Usuario?>(null) + estado.ejecutivos,
                        seleccionada = estado.ejecutivos.firstOrNull { it.id == estado.filtros.responsableId },
                        textoDe = { it?.nombreCompleto ?: "Todos los ejecutivos" },
                        alSeleccionar = viewModel::alSeleccionarResponsable
                    )
                }
            }

            BotonDegradado(
                texto = when (estado.oportunidades.size) {
                    1 -> "Ver 1 resultado"
                    else -> "Ver ${estado.oportunidades.size} resultados"
                },
                onClick = viewModel::cerrarPanelFiltros,
                modifier = Modifier.padding(horizontal = Espaciado.pantalla)
            )
        }
    }
}

/** Grupo de chips de un filtro, desplazable en horizontal. Tocar el elegido lo quita. */
@Composable
private fun <T> GrupoFiltros(
    titulo: String,
    opciones: List<T>,
    etiqueta: (T) -> String,
    seleccionada: T?,
    alSeleccionar: (T) -> Unit
) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = Espaciado.pantalla, top = Espaciado.sm)
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = Espaciado.pantalla),
        horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)
    ) {
        items(opciones) { opcion ->
            ChipPildora(
                texto = etiqueta(opcion),
                seleccionado = seleccionada == opcion,
                onClick = { alSeleccionar(opcion) }
            )
        }
    }
}
