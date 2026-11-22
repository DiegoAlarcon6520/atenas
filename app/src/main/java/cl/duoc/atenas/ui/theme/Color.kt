package cl.duoc.atenas.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * PALETA DE ATENAS
 *
 * Inspirada en una interfaz nocturna: fondo azul noche casi negro, tarjetas
 * apenas más claras y un acento con degradado de menta a azul. El acento se
 * reserva para lo importante (botón principal, tarjeta destacada, pestaña
 * activa) y así la pantalla "respira".
 *
 * Ninguna pantalla declara colores hexadecimales: todo se consume desde
 * MaterialTheme.colorScheme, desde los degradados de Degradados.kt o desde
 * los colores semánticos de ColoresSemanticos.kt.
 */

// ---- Marca (compartida por ambos temas) ----
val MentaMarca = Color(0xFF5EE6B8)
val TurquesaMarca = Color(0xFF2CC2A6)
val AzulMarca = Color(0xFF1B6FB6)
val AzulProfundoMarca = Color(0xFF145A9C)

// ---- Esquema oscuro (el de la referencia) ----
val FondoOscuro = Color(0xFF12131A)
val SobreFondoOscuro = Color(0xFFF3F5F9)
val TextoSecundarioOscuro = Color(0xFF9CA3B4)
val TarjetaOscura = Color(0xFF1D1F2A)
val TarjetaAltaOscura = Color(0xFF252836)
val TarjetaBajaOscura = Color(0xFF181A23)
val ContornoOscuro = Color(0xFF4A4F63)
val ContornoVarianteOscuro = Color(0xFF2B2E3D)
val PrimarioOscuro = Color(0xFF5EE6B8)
val SobrePrimarioOscuro = Color(0xFF052A20)
val ContenedorPrimarioOscuro = Color(0xFF123B3A)
val SobreContenedorPrimarioOscuro = Color(0xFFB5F5DD)
val SecundarioOscuro = Color(0xFF6CB4F0)
val SobreSecundarioOscuro = Color(0xFF06223B)
val ContenedorSecundarioOscuro = Color(0xFF17304A)
val SobreContenedorSecundarioOscuro = Color(0xFFCDE5FB)
val TerciarioOscuro = Color(0xFFFFC46B)
val SobreTerciarioOscuro = Color(0xFF3D2500)
val ContenedorTerciarioOscuro = Color(0xFF3A2A12)
val SobreContenedorTerciarioOscuro = Color(0xFFFFE2B8)
val ErrorOscuro = Color(0xFFFF8A80)
val SobreErrorOscuro = Color(0xFF4A0703)
val ContenedorErrorOscuro = Color(0xFF3A1A1E)
val SobreContenedorErrorOscuro = Color(0xFFFFD6D2)

// ---- Esquema claro (misma identidad, lienzo luminoso) ----
val FondoClaro = Color(0xFFF4F6FA)
val SobreFondoClaro = Color(0xFF12131A)
val TextoSecundarioClaro = Color(0xFF5E6577)
val TarjetaClara = Color(0xFFFFFFFF)
val TarjetaAltaClara = Color(0xFFEDF1F7)
val TarjetaBajaClara = Color(0xFFF9FAFC)
val ContornoClaro = Color(0xFF8A90A0)
val ContornoVarianteClaro = Color(0xFFE1E5EE)
val PrimarioClaro = Color(0xFF0E8F74)
val SobrePrimarioClaro = Color(0xFFFFFFFF)
val ContenedorPrimarioClaro = Color(0xFFD3F6EA)
val SobreContenedorPrimarioClaro = Color(0xFF00382B)
val SecundarioClaro = Color(0xFF1B6FB6)
val SobreSecundarioClaro = Color(0xFFFFFFFF)
val ContenedorSecundarioClaro = Color(0xFFDCEBFA)
val SobreContenedorSecundarioClaro = Color(0xFF062A4A)
val TerciarioClaro = Color(0xFF9A5B00)
val SobreTerciarioClaro = Color(0xFFFFFFFF)
val ContenedorTerciarioClaro = Color(0xFFFFE7C6)
val SobreContenedorTerciarioClaro = Color(0xFF2E1A00)
val ErrorClaro = Color(0xFFC62828)
val SobreErrorClaro = Color(0xFFFFFFFF)
val ContenedorErrorClaro = Color(0xFFFFE3E0)
val SobreContenedorErrorClaro = Color(0xFF450A07)

// ---- Colores semánticos (estados de la gestión) ----
val ExitoClaro = Color(0xFF1E7A4F)
val ExitoOscuro = Color(0xFF6BE3A4)
val AdvertenciaClaro = Color(0xFF9A5B00)
val AdvertenciaOscuro = Color(0xFFFFC46B)
val PeligroClaro = Color(0xFFC62828)
val PeligroOscuro = Color(0xFFFF8A80)
val InformacionClaro = Color(0xFF1B6FB6)
val InformacionOscuro = Color(0xFF6CB4F0)
val NeutroClaro = Color(0xFF5E6577)
val NeutroOscuro = Color(0xFFA9B0C0)
