package cl.duoc.atenas.domain

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.RangoFecha
import cl.duoc.atenas.domain.model.RolUsuario
import cl.duoc.atenas.domain.model.TipoAccion
import cl.duoc.atenas.domain.rules.Permisos
import cl.duoc.atenas.domain.rules.TransicionesEstado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de las reglas agregadas al corregir los defectos de la auditoría funcional. */
class ReglasAuditoriaTest {

    // ---------- D3: prioridad ----------

    @Test
    fun `el ejecutivo no puede cambiar la prioridad`() {
        assertFalse(Permisos.puedeCambiarPrioridad(RolUsuario.EJECUTIVO))
    }

    @Test
    fun `el supervisor si puede cambiar la prioridad`() {
        assertTrue(Permisos.puedeCambiarPrioridad(RolUsuario.SUPERVISOR))
    }

    // ---------- D4: oportunidad cerrada ----------

    @Test
    fun `una oportunidad cerrada no admite cambios`() {
        assertFalse(TransicionesEstado.admiteCambios(EstadoOportunidad.CERRADA))
    }

    @Test
    fun `cualquier otro estado admite cambios`() {
        EstadoOportunidad.entries
            .filter { it != EstadoOportunidad.CERRADA }
            .forEach { assertTrue(it.name, TransicionesEstado.admiteCambios(it)) }
    }

    // ---------- D5: último contacto ----------

    @Test
    fun `la investigacion interna no cuenta como contacto`() {
        assertFalse(TipoAccion.INVESTIGACION_INICIAL.implicaContacto)
        assertFalse(TipoAccion.BUSQUEDA_CONTACTO.implicaContacto)
    }

    @Test
    fun `llamadas reuniones y propuestas cuentan como contacto`() {
        assertTrue(TipoAccion.LLAMADA.implicaContacto)
        assertTrue(TipoAccion.REUNION.implicaContacto)
        assertTrue(TipoAccion.ENVIO_PROPUESTA.implicaContacto)
    }

    // ---------- HU-18: filtro por fecha ----------

    @Test
    fun `los proximos 7 dias van de hoy a una semana mas`() {
        val hoy = Fechas.hoy()
        val (desde, hasta) = RangoFecha.PROXIMOS_7.limites(hoy)
        assertEquals(hoy, desde)
        assertEquals(Fechas.masDias(hoy, 7), hasta)
    }

    @Test
    fun `los eventos ya ocurridos terminan antes de hoy y no tienen inicio`() {
        val hoy = Fechas.hoy()
        val (desde, hasta) = RangoFecha.PASADOS.limites(hoy)
        assertNull(desde)
        assertTrue(hasta!! < hoy)
    }
}
