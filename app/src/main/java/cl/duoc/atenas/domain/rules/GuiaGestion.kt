package cl.duoc.atenas.domain.rules

import cl.duoc.atenas.domain.model.EstadoOportunidad

/**
 * GUÍA DE LA GESTIÓN
 *
 * Reglas que orientan al usuario sobre qué hacer y dónde está cada
 * oportunidad. Viven en el dominio para que sean las mismas en todas las
 * pantallas y se puedan probar con tests unitarios.
 */
object GuiaGestion {

    /** Etapas que muestra el indicador de avance del detalle, en orden. */
    val etapasVisibles = listOf("Identificada", "Contacto", "Propuesta", "Respuesta", "Confirmada")

    /**
     * Posición de la oportunidad en el indicador de avance: las etapas con
     * índice menor quedan completas y la del índice es la actual. Una
     * activación realizada devuelve el total (todas completas). Descartada y
     * cerrada no tienen posición: salieron del embudo.
     */
    fun indiceEtapa(estado: EstadoOportunidad): Int? = when (estado) {
        EstadoOportunidad.IDENTIFICADA -> 0
        EstadoOportunidad.CONTACTO_PENDIENTE -> 1
        EstadoOportunidad.CONTACTO_REALIZADO -> 2
        EstadoOportunidad.PROPUESTA_ENVIADA -> 3
        EstadoOportunidad.EN_ESPERA_RESPUESTA -> 3
        EstadoOportunidad.CONFIRMADA -> 4
        EstadoOportunidad.ACTIVACION_REALIZADA -> etapasVisibles.size
        EstadoOportunidad.DESCARTADA, EstadoOportunidad.CERRADA -> null
    }

    /**
     * Siguiente paso sugerido cuando la última gestión no dejó una próxima
     * acción comprometida. [contacto] personaliza el texto si se conoce.
     * Devuelve null cuando ya no queda nada por hacer.
     */
    fun siguientePaso(estado: EstadoOportunidad, contacto: String?): String? = when (estado) {
        EstadoOportunidad.IDENTIFICADA ->
            "Identificar a la persona responsable del evento"
        EstadoOportunidad.CONTACTO_PENDIENTE ->
            if (contacto != null) "Hacer el primer contacto con $contacto" else "Conseguir un contacto en la organización"
        EstadoOportunidad.CONTACTO_REALIZADO ->
            "Preparar y enviar la propuesta de participación"
        EstadoOportunidad.PROPUESTA_ENVIADA ->
            "Confirmar que la propuesta llegó y resolver dudas"
        EstadoOportunidad.EN_ESPERA_RESPUESTA ->
            if (contacto != null) "Hacer seguimiento con $contacto para conocer la respuesta" else "Hacer seguimiento de la respuesta"
        EstadoOportunidad.CONFIRMADA ->
            "Preparar la activación y registrarla cuando ocurra"
        EstadoOportunidad.ACTIVACION_REALIZADA ->
            "Registrar el resultado y cerrar la oportunidad"
        EstadoOportunidad.DESCARTADA ->
            "Cerrar la oportunidad para sellar su historial"
        EstadoOportunidad.CERRADA -> null
    }

    /**
     * Tasa de conversión del equipo: oportunidades ganadas (confirmadas o con
     * la activación realizada) sobre las que ya tienen desenlace (ganadas más
     * descartadas). Las cerradas no cuentan porque pudieron cerrarse por
     * cualquiera de los dos caminos. Devuelve null si aún no hay desenlaces.
     */
    fun tasaConversion(conteos: Map<EstadoOportunidad, Int>): Int? {
        val ganadas = (conteos[EstadoOportunidad.CONFIRMADA] ?: 0) +
            (conteos[EstadoOportunidad.ACTIVACION_REALIZADA] ?: 0)
        val perdidas = conteos[EstadoOportunidad.DESCARTADA] ?: 0
        val total = ganadas + perdidas
        return if (total == 0) null else Math.round(ganadas * 100.0 / total).toInt()
    }

    /**
     * Texto relativo para una fecha comprometida respecto de hoy:
     * "Vence hoy", "Vence mañana", "Vence en 3 días", "Vencida hace 2 días".
     */
    fun textoVencimiento(diasHastaVencer: Int): String = when {
        diasHastaVencer == 0 -> "Vence hoy"
        diasHastaVencer == 1 -> "Vence mañana"
        diasHastaVencer > 1 -> "Vence en $diasHastaVencer días"
        diasHastaVencer == -1 -> "Vencida ayer"
        else -> "Vencida hace ${-diasHastaVencer} días"
    }
}
