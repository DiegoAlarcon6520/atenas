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
 * Degradado para tarjetas con texto blanco encima (resumen del día,
 * siguiente paso, tarjeta destacada). Es más profundo que el de marca para
 * que el texto supere el contraste mínimo de 4,5:1 en toda la superficie.
 */
val DegradadoTarjeta: Brush = Brush.linearGradient(
    colors = listOf(Color(0xFF157A6B), Color(0xFF165F86), Color(0xFF174C8C)),
    start = Offset.Zero,
    end = Offset.Infinite
)

/**
 * Variante horizontal para botones: arranca más oscuro para que el texto
 * blanco se lea bien en todo el ancho.
 */
val DegradadoBoton: Brush = Brush.horizontalGradient(
    colors = listOf(AzulProfundoMarca, Color(0xFF16707A))
)

/** Velo oscuro que se pone sobre un degradado para dar profundidad a una tarjeta. */
val VeloTarjeta: Brush = Brush.verticalGradient(
    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f))
)
