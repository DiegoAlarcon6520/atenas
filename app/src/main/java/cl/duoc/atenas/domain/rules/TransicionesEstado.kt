package cl.duoc.atenas.domain.rules

import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.RolUsuario

/**
 * MÁQUINA DE ESTADOS DEL EMBUDO COMERCIAL
 *
 * Define qué estados puede tomar una oportunidad a partir del estado en que
 * se encuentra, y cuáles de esas transiciones están reservadas al supervisor.
 *
 * Vive en el dominio, no en la UI, por tres razones:
 *  1. la regla es la misma mire quien mire la pantalla,
 *  2. se puede probar con tests unitarios,
 *  3. la pantalla solo pregunta "¿qué opciones muestro?" en vez de decidirlo.
 *
 * Es la implementación de dos requisitos del caso: respetar las transiciones
 * de estado, e impedir que un ejecutivo confirme o descarte por su cuenta.
 */
object TransicionesEstado {

    /**
     * Grafo de avance del embudo. Cada estado declara a cuáles puede pasar.
     * Descartar está disponible en casi todo el recorrido porque una
     * oportunidad puede caerse en cualquier momento de la gestión.
     */
    private val transicionesPermitidas: Map<EstadoOportunidad, List<EstadoOportunidad>> = mapOf(
        EstadoOportunidad.IDENTIFICADA to listOf(
            EstadoOportunidad.CONTACTO_PENDIENTE,
            EstadoOportunidad.DESCARTADA
        ),
        EstadoOportunidad.CONTACTO_PENDIENTE to listOf(
            EstadoOportunidad.CONTACTO_REALIZADO,
            EstadoOportunidad.DESCARTADA
        ),
        EstadoOportunidad.CONTACTO_REALIZADO to listOf(
            EstadoOportunidad.PROPUESTA_ENVIADA,
            EstadoOportunidad.DESCARTADA
        ),
        EstadoOportunidad.PROPUESTA_ENVIADA to listOf(
            EstadoOportunidad.EN_ESPERA_RESPUESTA,
            EstadoOportunidad.DESCARTADA
        ),
        EstadoOportunidad.EN_ESPERA_RESPUESTA to listOf(
            EstadoOportunidad.CONFIRMADA,
            EstadoOportunidad.DESCARTADA
        ),
        EstadoOportunidad.CONFIRMADA to listOf(
            EstadoOportunidad.ACTIVACION_REALIZADA,
            EstadoOportunidad.DESCARTADA
        ),
        EstadoOportunidad.ACTIVACION_REALIZADA to listOf(EstadoOportunidad.CERRADA),
        EstadoOportunidad.DESCARTADA to listOf(EstadoOportunidad.CERRADA),
        // Una oportunidad cerrada no se reabre: su historial queda sellado.
        EstadoOportunidad.CERRADA to emptyList()
    )

    /**
     * Estados que comprometen a la agencia o cierran la oportunidad.
     * Solo el supervisor comercial puede fijarlos.
     */
    private val reservadosAlSupervisor = setOf(
        EstadoOportunidad.CONFIRMADA,
        EstadoOportunidad.DESCARTADA,
        EstadoOportunidad.CERRADA
    )

    /**
     * Etapas de avance del embudo, en orden. Descartada y cerrada quedan
     * fuera porque no son etapas: son salidas.
     */
    private val etapasDeAvance = listOf(
        EstadoOportunidad.IDENTIFICADA,
        EstadoOportunidad.CONTACTO_PENDIENTE,
        EstadoOportunidad.CONTACTO_REALIZADO,
        EstadoOportunidad.PROPUESTA_ENVIADA,
        EstadoOportunidad.EN_ESPERA_RESPUESTA,
        EstadoOportunidad.CONFIRMADA
    )

    /**
     * Etapas anteriores a las que el supervisor puede devolver la
     * oportunidad.
     *
     * En la realidad una gestión retrocede: cambia el encargado en la
     * contraparte y hay que volver a contactar, o una oportunidad descartada
     * se reabre porque el evento se reprogramó. El embudo tiene que admitir
     * esa corrección, pero solo desde la supervisión: si el ejecutivo pudiera
     * retroceder a voluntad, el estado dejaría de significar algo.
     *
     * Una oportunidad cerrada no vuelve: su historial quedó sellado.
     */
    fun anterioresPosibles(actual: EstadoOportunidad, rol: RolUsuario): List<EstadoOportunidad> {
        if (!Permisos.puedeRetrocederEstado(rol)) return emptyList()
        return when (actual) {
            // Reabrir una oportunidad descartada la devuelve al inicio del embudo.
            EstadoOportunidad.DESCARTADA -> listOf(
                EstadoOportunidad.IDENTIFICADA,
                EstadoOportunidad.CONTACTO_PENDIENTE,
                EstadoOportunidad.CONTACTO_REALIZADO
            )
            // Una activación marcada por error vuelve a estar solo confirmada.
            EstadoOportunidad.ACTIVACION_REALIZADA -> listOf(EstadoOportunidad.CONFIRMADA)

            EstadoOportunidad.CERRADA -> emptyList()

            // En el tramo de avance, puede volver a cualquier etapa previa.
            else -> {
                val posicion = etapasDeAvance.indexOf(actual)
                if (posicion <= 0) emptyList() else etapasDeAvance.take(posicion)
            }
        }
    }

    /** Estados a los que [actual] puede avanzar considerando el [rol] de quien gestiona. */
    fun siguientesPosibles(actual: EstadoOportunidad, rol: RolUsuario): List<EstadoOportunidad> =
        transicionesPermitidas[actual].orEmpty().filter { destino ->
            destino !in reservadosAlSupervisor || Permisos.puedeConfirmarODescartar(rol)
        }

    /** Verificación usada antes de guardar, por si la UI dejara pasar algo. */
    fun esValida(desde: EstadoOportunidad, hacia: EstadoOportunidad, rol: RolUsuario): Boolean =
        hacia == desde ||
            hacia in siguientesPosibles(desde, rol) ||
            hacia in anterioresPosibles(desde, rol)

    /**
     * Estados en los que la gestión comercial terminó.
     * En ellos no tiene sentido comprometer una próxima acción.
     */
    fun esEstadoDeCierre(estado: EstadoOportunidad): Boolean = estado in setOf(
        EstadoOportunidad.DESCARTADA,
        EstadoOportunidad.ACTIVACION_REALIZADA,
        EstadoOportunidad.CERRADA
    )

    /** Explica por qué un ejecutivo no ve ciertas opciones. */
    fun mensajeRestriccion(rol: RolUsuario): String? =
        if (Permisos.puedeConfirmarODescartar(rol)) {
            null
        } else {
            "Confirmar o descartar una oportunidad es una decisión del supervisor comercial."
        }
}
