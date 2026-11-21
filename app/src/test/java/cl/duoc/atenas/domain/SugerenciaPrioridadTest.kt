package cl.duoc.atenas.domain

import cl.duoc.atenas.domain.model.AfinidadPublico
import cl.duoc.atenas.domain.model.PotencialComercial
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.rules.SugerenciaPrioridad
import org.junit.Assert.assertEquals
import org.junit.Test

class SugerenciaPrioridadTest {

    @Test
    fun `potencial alto con afinidad alta sugiere prioridad alta`() {
        assertEquals(
            Prioridad.ALTA,
            SugerenciaPrioridad.sugerir(PotencialComercial.ALTO, AfinidadPublico.ALTA)
        )
    }

    @Test
    fun `potencial alto con afinidad media sugiere prioridad alta`() {
        assertEquals(
            Prioridad.ALTA,
            SugerenciaPrioridad.sugerir(PotencialComercial.ALTO, AfinidadPublico.MEDIA)
        )
    }

    @Test
    fun `potencial medio con afinidad media sugiere prioridad media`() {
        assertEquals(
            Prioridad.MEDIA,
            SugerenciaPrioridad.sugerir(PotencialComercial.MEDIO, AfinidadPublico.MEDIA)
        )
    }

    @Test
    fun `un criterio alto y otro bajo se compensan en prioridad media`() {
        assertEquals(
            Prioridad.MEDIA,
            SugerenciaPrioridad.sugerir(PotencialComercial.ALTO, AfinidadPublico.BAJA)
        )
        assertEquals(
            Prioridad.MEDIA,
            SugerenciaPrioridad.sugerir(PotencialComercial.BAJO, AfinidadPublico.ALTA)
        )
    }

    @Test
    fun `potencial bajo con afinidad baja sugiere prioridad baja`() {
        assertEquals(
            Prioridad.BAJA,
            SugerenciaPrioridad.sugerir(PotencialComercial.BAJO, AfinidadPublico.BAJA)
        )
    }

    @Test
    fun `el puntaje suma los pesos de ambos criterios`() {
        assertEquals(6, SugerenciaPrioridad.puntaje(PotencialComercial.ALTO, AfinidadPublico.ALTA))
        assertEquals(2, SugerenciaPrioridad.puntaje(PotencialComercial.BAJO, AfinidadPublico.BAJA))
    }

    @Test
    fun `todas las combinaciones producen una sugerencia`() {
        // Ninguna combinación de potencial y afinidad queda sin prioridad.
        for (potencial in PotencialComercial.entries) {
            for (afinidad in AfinidadPublico.entries) {
                SugerenciaPrioridad.sugerir(potencial, afinidad)
            }
        }
    }
}
