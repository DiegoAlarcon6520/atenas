package cl.duoc.atenas.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.ui.theme.DegradadoBoton
import cl.duoc.atenas.ui.theme.DegradadoMarca
import cl.duoc.atenas.ui.theme.Espaciado
import cl.duoc.atenas.ui.theme.VeloTarjeta

/*
 * COMPONENTES DE MARCA
 *
 * Piezas visuales que dan identidad a la aplicación: el botón con degradado,
 * el avatar, el encabezado de saludo y la tarjeta destacada. Todas leen sus
 * colores del tema, así que funcionan en claro y en oscuro.
 */

/** Ícono que representa cada tipo de oportunidad en tarjetas y encabezados. */
fun iconoDeTipo(tipo: TipoOportunidad): ImageVector = when (tipo) {
    TipoOportunidad.EVENTO_CORPORATIVO -> Icons.Default.BusinessCenter
    TipoOportunidad.FERIA_EMPRESARIAL -> Icons.Default.Storefront
    TipoOportunidad.ACTIVIDAD_MUNICIPAL -> Icons.Default.AccountBalance
    TipoOportunidad.JORNADA_BENEFICIOS -> Icons.Default.VolunteerActivism
    TipoOportunidad.FERIA_INSTITUCIONAL -> Icons.Default.School
    TipoOportunidad.ACTIVIDAD_TRABAJADORES -> Icons.Default.Groups
    TipoOportunidad.ACTIVACION_COMERCIAL -> Icons.Default.Campaign
    TipoOportunidad.OTRA -> Icons.Default.Event
}

/**
 * Botón principal con el degradado de marca y forma de píldora.
 * Se usa una sola vez por pantalla, para la acción más importante.
 */
@Composable
fun BotonDegradado(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    icono: ImageVector? = null
) {
    val activo = habilitado && !cargando
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            // La transparencia va antes del fondo para atenuar todo el botón.
            .alpha(if (activo) 1f else 0.5f)
            .clip(CircleShape)
            .background(DegradadoBoton)
            .clickable(enabled = activo, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icono?.let { Icon(it, contentDescription = null, tint = Color.White) }
                Text(texto, style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    }
}

/** Botón secundario en píldora con borde de degradado, como "Register" en la referencia. */
@Composable
fun BotonContorno(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    contenido: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        shape = CircleShape,
        border = BorderStroke(1.5.dp, DegradadoMarca),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        content = contenido
    )
}

/** Círculo con degradado y las iniciales del usuario. */
@Composable
fun Avatar(nombre: String, modifier: Modifier = Modifier, tamano: Dp = 46.dp) {
    val iniciales = nombre.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(DegradadoMarca),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iniciales,
            style = if (tamano > 56.dp) {
                MaterialTheme.typography.headlineSmall
            } else {
                MaterialTheme.typography.titleSmall
            },
            color = Color.White
        )
    }
}

/**
 * Encabezado de los tableros: avatar, saludo y campana de alertas con el
 * total pendiente. Reemplaza a la barra superior en la pestaña de inicio.
 */
@Composable
fun EncabezadoSaludo(
    saludo: String,
    nombre: String,
    alertas: Int,
    alTocarAlertas: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espaciado.md)
    ) {
        Avatar(nombre)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = saludo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = alTocarAlertas) {
                BadgedBox(
                    badge = {
                        if (alertas > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) { Text(if (alertas > 9) "9+" else alertas.toString()) }
                        }
                    }
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Alertas: $alertas")
                }
            }
        }
    }
}

/**
 * Tarjeta destacada con el degradado de marca, como "Last added" en la
 * referencia. Muestra la oportunidad más urgente de la pantalla.
 */
@Composable
fun TarjetaDestacada(
    etiquetaSuperior: String,
    oportunidad: OportunidadResumen,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(DegradadoMarca)
            .background(VeloTarjeta)
            .clickable(onClick = onClick)
            .padding(Espaciado.tarjeta + 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EtiquetaSobreDegradado(etiquetaSuperior)
                Text(
                    text = oportunidad.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${oportunidad.organizacionNombre} · ${oportunidad.comunaNombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${Fechas.aTexto(oportunidad.fechaInicioEvento)} · " +
                        "${oportunidad.publicoEsperado} asistentes",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .padding(start = Espaciado.md)
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconoDeTipo(oportunidad.tipo),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/** Pastilla clara para usar encima de un degradado. */
@Composable
fun EtiquetaSobreDegradado(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        maxLines = 1,
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

/**
 * Ícono dentro de un cuadro redondeado con el degradado de marca, como las
 * miniaturas de la referencia. Se usa en encabezados y accesos rápidos.
 */
@Composable
fun IconoMarca(
    icono: ImageVector,
    modifier: Modifier = Modifier,
    tamano: Dp = 56.dp,
    tamanoIcono: Dp = 26.dp
) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(tamano / 3.2f))
            .background(DegradadoMarca),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(tamanoIcono))
    }
}

/**
 * Chip en forma de píldora, como los filtros "All / Design / Financial" de la
 * referencia. El seleccionado lleva el degradado; los demás, fondo de tarjeta.
 */
@Composable
fun ChipPildora(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (seleccionado) {
                    Modifier.background(DegradadoBoton)
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = if (seleccionado) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
