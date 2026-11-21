package cl.duoc.atenas.ui.oportunidades

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.data.local.entity.Comuna
import cl.duoc.atenas.data.local.entity.Organizacion
import cl.duoc.atenas.data.local.entity.Region
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.PotencialComercial
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.RangoFecha
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.ListaDesplegable
import cl.duoc.atenas.ui.components.TarjetaOportunidad
import cl.duoc.atenas.ui.theme.Espaciado

/**
 * LISTADO DE OPORTUNIDADES
 *
 * Toda la pantalla es una sola LazyColumn: el buscador, el panel de filtros y
 * el contador son elementos de la lista, no un encabezado fijo encima de ella.
 *
 * Por qué importa: con el teléfono en horizontal un encabezado fijo se come
 * casi toda la altura disponible y las tarjetas quedan tapadas por las barras.
 * Al formar parte del scroll, el contenido completo sigue siendo alcanzable en
 * cualquier orientación y en cualquier tamaño de pantalla.
 */
@Composable
fun ListaOportunidadesScreen(
    alAbrirDetalle: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ListaOportunidadesViewModel = viewModel(factory = ListaOportunidadesViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        // El espacio inferior extra deja leer la última tarjeta por encima
        // del botón flotante.
        contentPadding = PaddingValues(
            start = Espaciado.md,
            end = Espaciado.md,
            top = Espaciado.sm,
            bottom = Espaciado.xl * 3
        ),
        verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
    ) {

        // ---------- Buscador ----------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = estado.filtros.texto,
                    onValueChange = viewModel::alBuscar,
                    placeholder = { Text("Buscar por evento, organización o comuna") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (estado.filtros.texto.isNotEmpty()) {
                            IconButton(onClick = { viewModel.alBuscar("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                BadgedBox(
                    badge = {
                        if (estado.filtros.cantidadActivos > 0) {
                            Badge { Text(estado.filtros.cantidadActivos.toString()) }
                        }
                    }
                ) {
                    IconButton(onClick = viewModel::alternarPanelFiltros) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                    }
                }
            }
        }

        // ---------- Panel de filtros ----------
        item {
            AnimatedVisibility(
                visible = estado.mostrarFiltros,
                enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(vertical = Espaciado.sm),
                        verticalArrangement = Arrangement.spacedBy(Espaciado.xs)
                    ) {
                        GrupoFiltros(
                            titulo = "Estado",
                            opciones = EstadoOportunidad.entries,
                            etiqueta = { it.etiqueta },
                            seleccionada = estado.filtros.estado,
                            alSeleccionar = viewModel::alSeleccionarEstado
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
                        GrupoFiltros(
                            titulo = "Fecha del evento",
                            opciones = RangoFecha.entries,
                            etiqueta = { it.etiqueta },
                            seleccionada = estado.filtros.rangoFecha,
                            alSeleccionar = viewModel::alSeleccionarRangoFecha
                        )

                        // Ubicación, organización y responsable tienen muchas
                        // opciones: van en listas desplegables, no en chips.
                        // La primera opción de cada lista quita el filtro.
                        Column(
                            modifier = Modifier.padding(horizontal = Espaciado.md),
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
                        if (estado.filtros.hayAlguno) {
                            TextButton(
                                onClick = viewModel::limpiarFiltros,
                                modifier = Modifier.padding(start = Espaciado.sm)
                            ) {
                                Text("Limpiar filtros")
                            }
                        }
                    }
                }
            }
        }

        // ---------- Contador de resultados ----------
        item {
            Text(
                text = buildString {
                    append(estado.oportunidades.size)
                    append(if (estado.oportunidades.size == 1) " oportunidad" else " oportunidades")
                    if (!estado.esSupervisor) append(" asignadas a ti")
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ---------- Resultados ----------
        if (estado.oportunidades.isEmpty()) {
            item {
                EstadoVacio(
                    mensaje = if (estado.filtros.hayAlguno) {
                        "Ninguna oportunidad coincide con la búsqueda. Prueba ajustando los filtros."
                    } else {
                        "Todavía no hay oportunidades registradas."
                    }
                )
            }
        } else {
            items(estado.oportunidades, key = { it.id }) { oportunidad ->
                TarjetaOportunidad(
                    oportunidad = oportunidad,
                    onClick = { alAbrirDetalle(oportunidad.id) }
                )
            }
        }
    }
}

/**
 * Grupo de chips de un filtro.
 *
 * Es genérico para servir a estados, prioridades y tipos con el mismo código.
 * La fila es horizontalmente desplazable, así que la cantidad de opciones no
 * depende del ancho del dispositivo.
 */
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
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = Espaciado.md, top = Espaciado.xs)
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = Espaciado.md),
        horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)
    ) {
        items(opciones) { opcion ->
            FilterChip(
                selected = seleccionada == opcion,
                onClick = { alSeleccionar(opcion) },
                label = { Text(etiqueta(opcion)) }
            )
        }
    }
}
