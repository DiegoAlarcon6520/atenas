package cl.duoc.atenas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.Prioridad

/** Color asociado a cada estado del embudo comercial. */
@Composable
fun colorDeEstado(estado: EstadoOportunidad): Color {
    val oscuro = isSystemInDarkTheme()
    return when (estado) {
        EstadoOportunidad.IDENTIFICADA,
        EstadoOportunidad.CERRADA -> if (oscuro) NeutroOscuro else NeutroClaro

        EstadoOportunidad.CONTACTO_PENDIENTE,
        EstadoOportunidad.EN_ESPERA_RESPUESTA -> if (oscuro) AdvertenciaOscuro else AdvertenciaClaro

        EstadoOportunidad.CONTACTO_REALIZADO,
        EstadoOportunidad.PROPUESTA_ENVIADA -> if (oscuro) InformacionOscuro else InformacionClaro

        EstadoOportunidad.CONFIRMADA,
        EstadoOportunidad.ACTIVACION_REALIZADA -> if (oscuro) ExitoOscuro else ExitoClaro

        EstadoOportunidad.DESCARTADA -> if (oscuro) PeligroOscuro else PeligroClaro
    }
}

/** Color asociado al nivel de prioridad. */
@Composable
fun colorDePrioridad(prioridad: Prioridad): Color {
    val oscuro = isSystemInDarkTheme()
    return when (prioridad) {
        Prioridad.ALTA -> if (oscuro) PeligroOscuro else PeligroClaro
        Prioridad.MEDIA -> if (oscuro) AdvertenciaOscuro else AdvertenciaClaro
        Prioridad.BAJA -> if (oscuro) NeutroOscuro else NeutroClaro
    }
}
