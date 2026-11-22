package cl.duoc.atenas.ui.login

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.di.FabricasViewModel
import cl.duoc.atenas.ui.components.BotonContorno
import cl.duoc.atenas.ui.components.BotonDegradado
import cl.duoc.atenas.ui.components.coloresCampo
import cl.duoc.atenas.ui.theme.DegradadoMarca
import cl.duoc.atenas.ui.theme.Espaciado

/**
 * Forma del encabezado: un rectángulo cuyo borde inferior es una curva
 * suave, como el de la pantalla de bienvenida de la referencia.
 */
private val FormaEncabezado = GenericShape { tamano, _ ->
    val profundidad = tamano.height * 0.16f
    moveTo(0f, 0f)
    lineTo(tamano.width, 0f)
    lineTo(tamano.width, tamano.height - profundidad)
    quadraticTo(
        tamano.width / 2f, tamano.height + profundidad,
        0f, tamano.height - profundidad
    )
    close()
}

/**
 * PANTALLA DE INICIO DE SESIÓN
 *
 * Encabezado con el degradado de marca detrás de la barra de estado, el
 * mensaje de bienvenida en grande y el formulario abajo. El botón principal
 * usa el degradado; el de ayuda, la píldora con borde.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(factory = FabricasViewModel.login)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
    ) {
        // ---------- Encabezado ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(FormaEncabezado)
                .background(DegradadoMarca),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Handshake,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Text(
                    text = "Atenas",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }

        // ---------- Bienvenida y formulario ----------
        Column(
            modifier = Modifier.padding(horizontal = Espaciado.lg, vertical = Espaciado.md),
            verticalArrangement = Arrangement.spacedBy(Espaciado.md)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                Text(
                    text = "Gestiona tus oportunidades corporativas",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Detecta eventos con anticipación y sigue cada activación desde un solo lugar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = estado.usuario,
                onValueChange = viewModel::alCambiarUsuario,
                label = { Text("Usuario") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = estado.errorUsuario != null,
                supportingText = estado.errorUsuario?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = MaterialTheme.shapes.medium,
                colors = coloresCampo(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado.clave,
                onValueChange = viewModel::alCambiarClave,
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = viewModel::alternarVisibilidadClave) {
                        Icon(
                            imageVector = if (estado.mostrarClave) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (estado.mostrarClave) {
                                "Ocultar contraseña"
                            } else {
                                "Mostrar contraseña"
                            }
                        )
                    }
                },
                visualTransformation = if (estado.mostrarClave) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                isError = estado.errorClave != null,
                supportingText = estado.errorClave?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                shape = MaterialTheme.shapes.medium,
                colors = coloresCampo(),
                modifier = Modifier.fillMaxWidth()
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
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Espaciado.md),
                        horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null)
                        Text(
                            text = estado.errorGeneral.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            BotonDegradado(
                texto = "Ingresar",
                onClick = viewModel::iniciarSesion,
                cargando = estado.cargando,
                modifier = Modifier.padding(top = Espaciado.sm)
            )

            BotonContorno(onClick = viewModel::alternarAyuda) {
                Text(
                    text = if (estado.mostrarAyuda) "Ocultar cuentas de prueba" else "Ver cuentas de prueba",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            AnimatedVisibility(
                visible = estado.mostrarAyuda,
                enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                Card(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(Espaciado.tarjeta),
                        verticalArrangement = Arrangement.spacedBy(Espaciado.xs)
                    ) {
                        Text("Cuentas ficticias", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "cvaldes · rfuentes · psoto — Ejecutivo comercial",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "msilva — Supervisor comercial",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Contraseña para todas: atenas123",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
