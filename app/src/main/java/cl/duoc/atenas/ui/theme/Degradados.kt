package cl.duoc.atenas.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/*
 * DEGRADADOS DE MARCA
 *
 * El acento de la aplicación es un degradado diagonal de menta a azul. Se
 * usa en pocos lugares a propósito (botón principal, tarjeta destacada,
 * avatar, encabezado del login) para que destaque.
 */

/** Degradado principal: menta arriba a la izquierda, azul abajo a la derecha. */
val DegradadoMarca: Brush = Brush.linearGradient(
    colors = listOf(MentaMarca, TurquesaMarca, AzulMarca),
    start = Offset.Zero,
    end = Offset.Infinite
)

/**
 * Variante horizontal para botones: arranca más oscuro para que el texto
 * blanco se lea bien en todo el ancho.
 */
val DegradadoBoton: Brush = Brush.horizontalGradient(
    colors = listOf(AzulProfundoMarca, AzulMarca, TurquesaMarca)
)

/** Velo oscuro que se pone sobre un degradado para dar profundidad a una tarjeta. */
val VeloTarjeta: Brush = Brush.verticalGradient(
    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f))
)
