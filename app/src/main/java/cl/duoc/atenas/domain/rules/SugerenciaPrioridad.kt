package cl.duoc.atenas.domain.rules

import cl.duoc.atenas.domain.model.AfinidadPublico
import cl.duoc.atenas.domain.model.PotencialComercial
import cl.duoc.atenas.domain.model.Prioridad

/**
 * SUGERENCIA DE PRIORIDAD
 *
 * Calcula una prioridad recomendada a partir de la evaluación de la
 * oportunidad (HU-06): su potencial comercial y la afinidad de su público con
 * el público objetivo de la agencia.
 *
 * Es solo una sugerencia: la prioridad final la decide una persona. Vive en
 * el dominio para que la regla sea una sola, la use la pantalla que la use, y
 * para poder probarla con tests unitarios.
 *
 * Regla: se suman los pesos de ambos criterios (cada uno vale 1, 2 o 3), así
 * que el puntaje va de 2 a 6.
 *  - 5 o 6 -> Alta   (por ejemplo, potencial alto con afinidad media o alta)
 *  - 4     -> Media  (por ejemplo, ambos medios, o uno alto y el otro bajo)
 *  - 2 o 3 -> Baja   (al menos un criterio bajo y ninguno alto)
 */
object SugerenciaPrioridad {

    const val PUNTAJE_ALTA = 5
    const val PUNTAJE_MEDIA = 4

    fun puntaje(potencial: PotencialComercial, afinidad: AfinidadPublico): Int =
        potencial.peso + afinidad.peso

    fun sugerir(potencial: PotencialComercial, afinidad: AfinidadPublico): Prioridad {
        val total = puntaje(potencial, afinidad)
        return when {
            total >= PUNTAJE_ALTA -> Prioridad.ALTA
            total >= PUNTAJE_MEDIA -> Prioridad.MEDIA
            else -> Prioridad.BAJA
        }
    }
}
