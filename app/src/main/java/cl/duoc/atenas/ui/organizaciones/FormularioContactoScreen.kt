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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.domain.model.AreaContacto
import cl.duoc.atenas.domain.model.MedioContacto
import cl.duoc.atenas.ui.components.CampoTexto
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.ListaDesplegable
import cl.duoc.atenas.ui.theme.Espaciado

/** Formulario para crear o editar un contacto dentro de una organización. */
@Composable
fun FormularioContactoScreen(
    alTerminar: () -> Unit,
    mostrarMensaje: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FormularioContactoViewModel = viewModel(factory = FormularioContactoViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.guardadoConExito) {
        if (estado.guardadoConExito) {
            mostrarMensaje(if (estado.esEdicion) "Contacto actualizado" else "Contacto creado")
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
            text = if (estado.esEdicion) "Editar contacto" else "Nuevo contacto",
            style = MaterialTheme.typography.headlineSmall
        )

        // La organización no es editable: viene fija desde la ficha de donde
        // se abrió el formulario.
        Text(
            text = "Organización: ${estado.nombreOrganizacion}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            etiqueta = "Nombre completo",
            error = estado.errores.nombre
        )

        CampoTexto(
            valor = estado.cargo,
            alCambiar = viewModel::alCambiarCargo,
            etiqueta = "Cargo",
            error = estado.errores.cargo,
            ayuda = "Jefa de Bienestar, Coordinador de Eventos…"
        )

        ListaDesplegable(
            etiqueta = "Área",
            opciones = AreaContacto.entries,
            seleccionada = estado.area,
            textoDe = { it.etiqueta },
            alSeleccionar = viewModel::alCambiarArea
        )

        CampoTexto(
            valor = estado.correo,
            alCambiar = viewModel::alCambiarCorreo,
            etiqueta = "Correo",
            error = estado.errores.correo
        )

        CampoTexto(
            valor = estado.telefono,
            alCambiar = viewModel::alCambiarTelefono,
            etiqueta = "Teléfono",
            error = estado.errores.telefono,
            ayuda = "Formato: +56 9 1234 5678"
        )

        ListaDesplegable(
            etiqueta = "Medio de contacto preferente",
            opciones = MedioContacto.entries,
            seleccionada = estado.medioPreferente,
            textoDe = { it.etiqueta },
            alSeleccionar = viewModel::alCambiarMedio
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Contacto activo", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Desactívalo si la persona ya no trabaja en la organización",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = estado.activo, onCheckedChange = viewModel::alCambiarActivo)
        }

        CampoTexto(
            valor = estado.observaciones,
            alCambiar = viewModel::alCambiarObservaciones,
            etiqueta = "Observaciones",
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
                    Text(if (estado.esEdicion) "Guardar cambios" else "Crear contacto")
                }
            }
        }
    }
}
