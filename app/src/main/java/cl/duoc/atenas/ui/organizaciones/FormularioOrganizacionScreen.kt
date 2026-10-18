package cl.duoc.atenas.ui.organizaciones

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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import cl.duoc.atenas.domain.model.TipoOrganizacion
import cl.duoc.atenas.ui.components.CampoTexto
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.ListaDesplegable
import cl.duoc.atenas.ui.theme.Espaciado

/** Formulario para crear o editar una organización. */
@Composable
fun FormularioOrganizacionScreen(
    alTerminar: () -> Unit,
    mostrarMensaje: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FormularioOrganizacionViewModel = viewModel(factory = FormularioOrganizacionViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.guardadoConExito) {
        if (estado.guardadoConExito) {
            mostrarMensaje(
                if (estado.esEdicion) "Organización actualizada" else "Organización creada"
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
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(Espaciado.md),
        verticalArrangement = Arrangement.spacedBy(Espaciado.md)
    ) {
        Text(
            text = if (estado.esEdicion) "Editar organización" else "Nueva organización",
            style = MaterialTheme.typography.headlineSmall
        )

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

        CampoTexto(
            valor = estado.nombre,
            alCambiar = viewModel::alCambiarNombre,
            etiqueta = "Nombre de la organización",
            icono = Icons.Default.Business,
            error = estado.errores.nombre
        )

        ListaDesplegable(
            etiqueta = "Tipo de organización",
            icono = Icons.Default.Category,
            opciones = TipoOrganizacion.entries,
            seleccionada = estado.tipo,
            textoDe = { it.etiqueta },
            alSeleccionar = viewModel::alCambiarTipo
        )

        CampoTexto(
            valor = estado.rubro,
            alCambiar = viewModel::alCambiarRubro,
            etiqueta = "Rubro",
            icono = Icons.Default.WorkOutline,
            error = estado.errores.rubro,
            ayuda = "Construcción, retail, gobierno local…"
        )

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

        CampoTexto(
            valor = estado.sitioWeb,
            alCambiar = viewModel::alCambiarSitioWeb,
            etiqueta = "Sitio web",
            icono = Icons.Default.Language,
            ayuda = "Opcional"
        )

        CampoTexto(
            valor = estado.publicoAsociado,
            alCambiar = viewModel::alCambiarPublicoAsociado,
            etiqueta = "Público asociado",
            icono = Icons.Default.Groups,
            ayuda = "Opcional: trabajadores, afiliados, vecinos…"
        )

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
                    Text(if (estado.esEdicion) "Guardar cambios" else "Crear")
                }
            }
        }
    }
}
