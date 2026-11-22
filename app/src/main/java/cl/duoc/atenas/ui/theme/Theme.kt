package cl.duoc.atenas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val EsquemaOscuro = darkColorScheme(
    primary = PrimarioOscuro,
    onPrimary = SobrePrimarioOscuro,
    primaryContainer = ContenedorPrimarioOscuro,
    onPrimaryContainer = SobreContenedorPrimarioOscuro,
    secondary = SecundarioOscuro,
    onSecondary = SobreSecundarioOscuro,
    secondaryContainer = ContenedorSecundarioOscuro,
    onSecondaryContainer = SobreContenedorSecundarioOscuro,
    tertiary = TerciarioOscuro,
    onTertiary = SobreTerciarioOscuro,
    tertiaryContainer = ContenedorTerciarioOscuro,
    onTertiaryContainer = SobreContenedorTerciarioOscuro,
    error = ErrorOscuro,
    onError = SobreErrorOscuro,
    errorContainer = ContenedorErrorOscuro,
    onErrorContainer = SobreContenedorErrorOscuro,
    background = FondoOscuro,
    onBackground = SobreFondoOscuro,
    surface = FondoOscuro,
    onSurface = SobreFondoOscuro,
    surfaceVariant = TarjetaAltaOscura,
    onSurfaceVariant = TextoSecundarioOscuro,
    outline = ContornoOscuro,
    outlineVariant = ContornoVarianteOscuro,
    // Material 3 usa surfaceContainerHighest como fondo de las Card: así
    // todas las tarjetas toman el tono de la referencia sin tocarlas una a una.
    surfaceContainerLowest = FondoOscuro,
    surfaceContainerLow = TarjetaBajaOscura,
    surfaceContainer = TarjetaBajaOscura,
    surfaceContainerHigh = TarjetaAltaOscura,
    surfaceContainerHighest = TarjetaOscura,
    surfaceBright = TarjetaAltaOscura,
    surfaceDim = FondoOscuro,
    inverseSurface = SobreFondoOscuro,
    inverseOnSurface = FondoOscuro,
    inversePrimary = PrimarioClaro,
    scrim = FondoOscuro
)

private val EsquemaClaro = lightColorScheme(
    primary = PrimarioClaro,
    onPrimary = SobrePrimarioClaro,
    primaryContainer = ContenedorPrimarioClaro,
    onPrimaryContainer = SobreContenedorPrimarioClaro,
    secondary = SecundarioClaro,
    onSecondary = SobreSecundarioClaro,
    secondaryContainer = ContenedorSecundarioClaro,
    onSecondaryContainer = SobreContenedorSecundarioClaro,
    tertiary = TerciarioClaro,
    onTertiary = SobreTerciarioClaro,
    tertiaryContainer = ContenedorTerciarioClaro,
    onTertiaryContainer = SobreContenedorTerciarioClaro,
    error = ErrorClaro,
    onError = SobreErrorClaro,
    errorContainer = ContenedorErrorClaro,
    onErrorContainer = SobreContenedorErrorClaro,
    background = FondoClaro,
    onBackground = SobreFondoClaro,
    surface = FondoClaro,
    onSurface = SobreFondoClaro,
    surfaceVariant = TarjetaAltaClara,
    onSurfaceVariant = TextoSecundarioClaro,
    outline = ContornoClaro,
    outlineVariant = ContornoVarianteClaro,
    surfaceContainerLowest = TarjetaClara,
    surfaceContainerLow = TarjetaBajaClara,
    surfaceContainer = TarjetaBajaClara,
    surfaceContainerHigh = TarjetaAltaClara,
    surfaceContainerHighest = TarjetaClara,
    surfaceBright = TarjetaClara,
    surfaceDim = TarjetaAltaClara,
    inverseSurface = SobreFondoClaro,
    inverseOnSurface = FondoClaro,
    inversePrimary = PrimarioOscuro
)

/**
 * Indica si la aplicación se está dibujando en oscuro.
 *
 * No basta con isSystemInDarkTheme(): el usuario puede forzar claro u oscuro
 * desde su perfil, y los colores semánticos tienen que seguir esa elección,
 * no la del teléfono.
 */
val LocalTemaOscuro = staticCompositionLocalOf { false }

/**
 * Tema de la aplicación.
 *
 * El color dinámico de Android 12+ queda deshabilitado a propósito: la
 * identidad visual y los colores semánticos de los estados son propios y el
 * color del sistema los reemplazaría.
 */
@Composable
fun AtenasTheme(
    temaOscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalTemaOscuro provides temaOscuro) {
        MaterialTheme(
            colorScheme = if (temaOscuro) EsquemaOscuro else EsquemaClaro,
            typography = Tipografia,
            shapes = Formas,
            content = content
        )
    }
}
