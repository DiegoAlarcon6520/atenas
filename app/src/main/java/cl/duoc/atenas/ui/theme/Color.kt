package cl.duoc.atenas.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Paleta centralizada. Ninguna pantalla debe declarar colores hexadecimales:
 * todo se consume desde MaterialTheme.colorScheme o desde los colores
 * semánticos definidos más abajo.
 */

// ---- Esquema claro ----
val AzulPrimario = Color(0xFF14557A)
val SobreAzulPrimario = Color(0xFFFFFFFF)
val ContenedorPrimario = Color(0xFFCFE5F7)
val SobreContenedorPrimario = Color(0xFF001E31)
val AzulSecundario = Color(0xFF4E616D)
val SobreAzulSecundario = Color(0xFFFFFFFF)
val ContenedorSecundario = Color(0xFFD1E5F3)
val SobreContenedorSecundario = Color(0xFF0A1D28)
val AmbarTerciario = Color(0xFF8A5100)
val SobreAmbarTerciario = Color(0xFFFFFFFF)
val ContenedorTerciario = Color(0xFFFFDDB7)
val SobreContenedorTerciario = Color(0xFF2C1700)
val RojoError = Color(0xFFBA1A1A)
val SobreRojoError = Color(0xFFFFFFFF)
val ContenedorError = Color(0xFFFFDAD6)
val SobreContenedorError = Color(0xFF410002)
val FondoClaro = Color(0xFFF2F5FA)
val SobreFondoClaro = Color(0xFF161A1D)
val VarianteSuperficieClara = Color(0xFFDDE3EA)
val SobreVarianteSuperficieClara = Color(0xFF444B51)
val ContornoClaro = Color(0xFF6E767D)
val ContornoVarianteClaro = Color(0xFFD3DAE2)

/*
 * Roles de contenedor de superficie.
 *
 * Material 3 los usa para decidir el color de fondo de tarjetas, barras y
 * menús. Si no se declaran, Compose los deriva de su paleta por defecto y
 * todo queda con el tinte lila genérico de Material.
 *
 * Acá las tarjetas quedan blancas sobre un lienzo levemente azulado, que es
 * lo que da la sensación de limpieza y separación entre elementos.
 */
val SuperficieMinimaClara = Color(0xFFFFFFFF)
val SuperficieBajaClara = Color(0xFFFBFCFE)
val SuperficieMediaClara = Color(0xFFF4F7FB)
val SuperficieAltaClara = Color(0xFFEDF1F7)
val SuperficieMaximaClara = Color(0xFFFFFFFF)

// ---- Esquema oscuro ----
val AzulPrimarioOscuro = Color(0xFF94CCF4)
val SobreAzulPrimarioOscuro = Color(0xFF003351)
val ContenedorPrimarioOscuro = Color(0xFF004B73)
val SobreContenedorPrimarioOscuro = Color(0xFFCFE5F7)
val AzulSecundarioOscuro = Color(0xFFB6C9D8)
val SobreAzulSecundarioOscuro = Color(0xFF21333E)
val ContenedorSecundarioOscuro = Color(0xFF374955)
val SobreContenedorSecundarioOscuro = Color(0xFFD1E5F3)
val AmbarTerciarioOscuro = Color(0xFFFFB95C)
val SobreAmbarTerciarioOscuro = Color(0xFF492900)
val ContenedorTerciarioOscuro = Color(0xFF693C00)
val SobreContenedorTerciarioOscuro = Color(0xFFFFDDB7)
val RojoErrorOscuro = Color(0xFFFFB4AB)
val SobreRojoErrorOscuro = Color(0xFF690005)
val ContenedorErrorOscuro = Color(0xFF93000A)
val SobreContenedorErrorOscuro = Color(0xFFFFDAD6)
val FondoOscuro = Color(0xFF0E1214)
val SobreFondoOscuro = Color(0xFFE2E4E7)
val VarianteSuperficieOscura = Color(0xFF41484D)
val SobreVarianteSuperficieOscura = Color(0xFFC1C7CE)
val ContornoOscuro = Color(0xFF8B9198)
val ContornoVarianteOscuro = Color(0xFF353B40)

val SuperficieMinimaOscura = Color(0xFF090C0E)
val SuperficieBajaOscura = Color(0xFF161A1D)
val SuperficieMediaOscura = Color(0xFF1A1F22)
val SuperficieAltaOscura = Color(0xFF242A2E)
val SuperficieMaximaOscura = Color(0xFF1E2427)

// ---- Colores semánticos (estados de la gestión) ----
val ExitoClaro = Color(0xFF2E6B3E)
val ExitoOscuro = Color(0xFF8FD6A3)
val AdvertenciaClaro = Color(0xFF9A5B00)
val AdvertenciaOscuro = Color(0xFFFFC46B)
val PeligroClaro = Color(0xFFB3261E)
val PeligroOscuro = Color(0xFFFFB4AB)
val InformacionClaro = Color(0xFF1565A0)
val InformacionOscuro = Color(0xFF8FC7F0)
val NeutroClaro = Color(0xFF5A6570)
val NeutroOscuro = Color(0xFFAAB4BE)
