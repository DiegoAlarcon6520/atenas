package cl.duoc.atenas.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import cl.duoc.atenas.R

/*
 * TIPOGRAFÍA
 *
 * Plus Jakarta Sans: una geométrica moderna, legible en tamaños chicos y con
 * carácter en los títulos. Es la misma familia de la referencia visual.
 * Los archivos viven en res/font (licencia SIL Open Font License), así que
 * no se agregó ninguna dependencia.
 *
 * Criterio de la escala: títulos en negrita y apretados, cuerpo con más
 * interlineado para que el texto respire.
 */
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold)
)

private fun estilo(
    peso: FontWeight,
    tamano: Int,
    interlineado: Int,
    espaciado: Double = 0.0
) = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = peso,
    fontSize = tamano.sp,
    lineHeight = interlineado.sp,
    letterSpacing = espaciado.sp
)

/** Jerarquía tipográfica única de la aplicación. */
val Tipografia = Typography(
    displaySmall = estilo(FontWeight.Bold, 34, 42, -0.5),
    headlineLarge = estilo(FontWeight.Bold, 30, 38, -0.4),
    headlineMedium = estilo(FontWeight.Bold, 26, 34, -0.3),
    headlineSmall = estilo(FontWeight.Bold, 22, 30, -0.2),
    titleLarge = estilo(FontWeight.Bold, 20, 28, -0.1),
    titleMedium = estilo(FontWeight.Bold, 16, 22),
    titleSmall = estilo(FontWeight.SemiBold, 14, 20),
    bodyLarge = estilo(FontWeight.Normal, 16, 25),
    bodyMedium = estilo(FontWeight.Normal, 14, 22),
    bodySmall = estilo(FontWeight.Normal, 12, 18),
    labelLarge = estilo(FontWeight.SemiBold, 14, 20),
    labelMedium = estilo(FontWeight.SemiBold, 12, 16),
    labelSmall = estilo(FontWeight.Medium, 11, 14, 0.2)
)
