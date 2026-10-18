package cl.duoc.atenas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = SobreAzulPrimario,
    primaryContainer = ContenedorPrimario,
    onPrimaryContainer = SobreContenedorPrimario,
    secondary = AzulSecundario,
    onSecondary = SobreAzulSecundario,
    secondaryContainer = ContenedorSecundario,
    onSecondaryContainer = SobreContenedorSecundario,
    tertiary = AmbarTerciario,
    onTertiary = SobreAmbarTerciario,
    tertiaryContainer = ContenedorTerciario,
    onTertiaryContainer = SobreContenedorTerciario,
    error = RojoError,
    onError = SobreRojoError,
    errorContainer = ContenedorError,
    onErrorContainer = SobreContenedorError,
    background = FondoClaro,
    onBackground = SobreFondoClaro,
    surface = FondoClaro,
    onSurface = SobreFondoClaro,
    surfaceVariant = VarianteSuperficieClara,
    onSurfaceVariant = SobreVarianteSuperficieClara,
    outline = ContornoClaro
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulPrimarioOscuro,
    onPrimary = SobreAzulPrimarioOscuro,
    primaryContainer = ContenedorPrimarioOscuro,
    onPrimaryContainer = SobreContenedorPrimarioOscuro,
    secondary = AzulSecundarioOscuro,
    onSecondary = SobreAzulSecundarioOscuro,
    secondaryContainer = ContenedorSecundarioOscuro,
    onSecondaryContainer = SobreContenedorSecundarioOscuro,
    tertiary = AmbarTerciarioOscuro,
    onTertiary = SobreAmbarTerciarioOscuro,
    tertiaryContainer = ContenedorTerciarioOscuro,
    onTertiaryContainer = SobreContenedorTerciarioOscuro,
    error = RojoErrorOscuro,
    onError = SobreRojoErrorOscuro,
    errorContainer = ContenedorErrorOscuro,
    onErrorContainer = SobreContenedorErrorOscuro,
    background = FondoOscuro,
    onBackground = SobreFondoOscuro,
    surface = FondoOscuro,
    onSurface = SobreFondoOscuro,
    surfaceVariant = VarianteSuperficieOscura,
    onSurfaceVariant = SobreVarianteSuperficieOscura,
    outline = ContornoOscuro
)

/**
 * Tema de la aplicación.
 *
 * El color dinámico de Android 12+ queda deshabilitado a propósito: los
 * estados de la gestión se comunican con colores semánticos propios y el
 * color del sistema los reemplazaría.
 */
@Composable
fun AtenasTheme(
    temaOscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (temaOscuro) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}
