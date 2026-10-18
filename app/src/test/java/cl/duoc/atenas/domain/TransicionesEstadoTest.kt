package cl.duoc.atenas.domain

import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.RolUsuario
import cl.duoc.atenas.domain.rules.TransicionesEstado
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
}
