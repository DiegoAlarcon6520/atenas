package cl.duoc.atenas.domain

import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.RolUsuario
import cl.duoc.atenas.domain.rules.TransicionesEstado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PRUEBAS UNITARIAS DE LA MÁQUINA DE ESTADOS
 *
 * Verifican la regla central del caso: un ejecutivo comercial no puede
 * confirmar ni descartar una oportunidad por su cuenta.
 *
 * Estas pruebas son la evidencia de que la restricción es real y no solo
 * un botón escondido en la interfaz.
 */
class TransicionesEstadoTest {

    @Test
    fun `el ejecutivo no puede confirmar una oportunidad`() {
        val posibles = TransicionesEstado.siguientesPosibles(
            actual = EstadoOportunidad.EN_ESPERA_RESPUESTA,
            rol = RolUsuario.EJECUTIVO
        )
        assertFalse(posibles.contains(EstadoOportunidad.CONFIRMADA))
    }

    @Test
    fun `el supervisor si puede confirmar una oportunidad`() {
        val posibles = TransicionesEstado.siguientesPosibles(
            actual = EstadoOportunidad.EN_ESPERA_RESPUESTA,
            rol = RolUsuario.SUPERVISOR
        )
        assertTrue(posibles.contains(EstadoOportunidad.CONFIRMADA))
    }

    @Test
    fun `el ejecutivo no puede descartar una oportunidad`() {
        val posibles = TransicionesEstado.siguientesPosibles(
            actual = EstadoOportunidad.IDENTIFICADA,
            rol = RolUsuario.EJECUTIVO
        )
        assertFalse(posibles.contains(EstadoOportunidad.DESCARTADA))
    }

    @Test
    fun `ambos perfiles pueden avanzar el embudo en los pasos operativos`() {
        val delEjecutivo = TransicionesEstado.siguientesPosibles(
            EstadoOportunidad.IDENTIFICADA,
            RolUsuario.EJECUTIVO
        )
        val delSupervisor = TransicionesEstado.siguientesPosibles(
            EstadoOportunidad.IDENTIFICADA,
            RolUsuario.SUPERVISOR
        )
        assertTrue(delEjecutivo.contains(EstadoOportunidad.CONTACTO_PENDIENTE))
        assertTrue(delSupervisor.contains(EstadoOportunidad.CONTACTO_PENDIENTE))
    }

    @Test
    fun `una oportunidad cerrada no admite nuevos estados`() {
        val posibles = TransicionesEstado.siguientesPosibles(
            EstadoOportunidad.CERRADA,
            RolUsuario.SUPERVISOR
        )
        assertTrue(posibles.isEmpty())
    }

    @Test
    fun `no se puede saltar etapas del embudo`() {
        // Pasar de recién identificada directo a propuesta enviada se salta
        // el contacto, que es justamente lo que el embudo debe impedir.
        assertFalse(
            TransicionesEstado.esValida(
                desde = EstadoOportunidad.IDENTIFICADA,
                hacia = EstadoOportunidad.PROPUESTA_ENVIADA,
                rol = RolUsuario.SUPERVISOR
            )
        )
    }

    @Test
    fun `mantener el mismo estado siempre es valido`() {
        // Registrar una gestión sin cambiar de estado es el caso más común.
        assertTrue(
            TransicionesEstado.esValida(
                desde = EstadoOportunidad.CONTACTO_REALIZADO,
                hacia = EstadoOportunidad.CONTACTO_REALIZADO,
                rol = RolUsuario.EJECUTIVO
            )
        )
    }

    @Test
    fun `al ejecutivo se le explica por que no ve ciertas opciones`() {
        assertTrue(TransicionesEstado.mensajeRestriccion(RolUsuario.EJECUTIVO) != null)
    }

    @Test
    fun `al supervisor no se le muestra ninguna restriccion`() {
        assertTrue(TransicionesEstado.mensajeRestriccion(RolUsuario.SUPERVISOR) == null)
    }

    // ---------------------------------------------------------------
    // Retroceso de etapa: corregir el embudo es tarea de la supervisión.
    // ---------------------------------------------------------------

    @Test
    fun `el ejecutivo no puede devolver una oportunidad a una etapa anterior`() {
        val anteriores = TransicionesEstado.anterioresPosibles(
            actual = EstadoOportunidad.PROPUESTA_ENVIADA,
            rol = RolUsuario.EJECUTIVO
        )
        assertTrue(anteriores.isEmpty())
    }

    @Test
    fun `el supervisor puede devolver una oportunidad a cualquier etapa previa`() {
        val anteriores = TransicionesEstado.anterioresPosibles(
            actual = EstadoOportunidad.PROPUESTA_ENVIADA,
            rol = RolUsuario.SUPERVISOR
        )
        assertTrue(anteriores.contains(EstadoOportunidad.CONTACTO_REALIZADO))
        assertTrue(anteriores.contains(EstadoOportunidad.IDENTIFICADA))
        // No puede "retroceder" a una etapa que todavía no alcanzó.
        assertFalse(anteriores.contains(EstadoOportunidad.CONFIRMADA))
    }

    @Test
    fun `una oportunidad descartada puede reabrirse`() {
        val anteriores = TransicionesEstado.anterioresPosibles(
            actual = EstadoOportunidad.DESCARTADA,
            rol = RolUsuario.SUPERVISOR
        )
        assertTrue(anteriores.contains(EstadoOportunidad.IDENTIFICADA))
    }

    @Test
    fun `una oportunidad cerrada no se puede reabrir`() {
        val anteriores = TransicionesEstado.anterioresPosibles(
            actual = EstadoOportunidad.CERRADA,
            rol = RolUsuario.SUPERVISOR
        )
        assertTrue(anteriores.isEmpty())
    }

    @Test
    fun `una activacion marcada por error vuelve a estar solo confirmada`() {
        val anteriores = TransicionesEstado.anterioresPosibles(
            actual = EstadoOportunidad.ACTIVACION_REALIZADA,
            rol = RolUsuario.SUPERVISOR
        )
        assertEquals(listOf(EstadoOportunidad.CONFIRMADA), anteriores)
    }

    @Test
    fun `retroceder es una transicion valida para el supervisor`() {
        assertTrue(
            TransicionesEstado.esValida(
                desde = EstadoOportunidad.EN_ESPERA_RESPUESTA,
                hacia = EstadoOportunidad.CONTACTO_REALIZADO,
                rol = RolUsuario.SUPERVISOR
            )
        )
    }

    @Test
    fun `retroceder no es una transicion valida para el ejecutivo`() {
        assertFalse(
            TransicionesEstado.esValida(
                desde = EstadoOportunidad.EN_ESPERA_RESPUESTA,
                hacia = EstadoOportunidad.CONTACTO_REALIZADO,
                rol = RolUsuario.EJECUTIVO
            )
        )
    }

    // ---------------------------------------------------------------
    // Estados de cierre: en ellos no se pide próxima acción, porque una
    // gestión pendiente sobre algo cerrado reaparecería como atrasada.
    // ---------------------------------------------------------------

    @Test
    fun `descartada activacion realizada y cerrada son estados de cierre`() {
        assertTrue(TransicionesEstado.esEstadoDeCierre(EstadoOportunidad.DESCARTADA))
        assertTrue(TransicionesEstado.esEstadoDeCierre(EstadoOportunidad.ACTIVACION_REALIZADA))
        assertTrue(TransicionesEstado.esEstadoDeCierre(EstadoOportunidad.CERRADA))
    }

    @Test
    fun `las etapas de avance no son estados de cierre`() {
        assertFalse(TransicionesEstado.esEstadoDeCierre(EstadoOportunidad.IDENTIFICADA))
        assertFalse(TransicionesEstado.esEstadoDeCierre(EstadoOportunidad.PROPUESTA_ENVIADA))
        assertFalse(TransicionesEstado.esEstadoDeCierre(EstadoOportunidad.CONFIRMADA))
    }
}
