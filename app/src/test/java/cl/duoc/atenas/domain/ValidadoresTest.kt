package cl.duoc.atenas.domain

import cl.duoc.atenas.domain.validation.Validadores
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * PRUEBAS UNITARIAS DE LOS VALIDADORES
 *
 * Se pueden ejecutar sin emulador ni dispositivo porque Validadores no
 * depende de Android: son funciones puras. Esa es exactamente la razón por
 * la que la validación se sacó de los Composables.
 *
 * Convención bajo prueba: null significa válido, un String es el mensaje de
 * error que verá el usuario.
 */
class ValidadoresTest {

    // ---------- Campos obligatorios ----------

    @Test
    fun `un campo vacio es invalido`() {
        assertNotNull(Validadores.obligatorio("", "El nombre"))
    }

    @Test
    fun `un campo con solo espacios es invalido`() {
        assertNotNull(Validadores.obligatorio("   ", "El nombre"))
    }

    @Test
    fun `un campo con contenido es valido`() {
        assertNull(Validadores.obligatorio("Feria municipal", "El nombre"))
    }

    // ---------- Longitud mínima ----------

    @Test
    fun `un texto mas corto que el minimo es invalido`() {
        assertNotNull(Validadores.texto("Expo", "El nombre", minimo = 5))
    }

    @Test
    fun `un texto que alcanza el minimo es valido`() {
        assertNull(Validadores.texto("Expo Sur", "El nombre", minimo = 5))
    }

    // ---------- Correo ----------

    @Test
    fun `un correo sin arroba es invalido`() {
        assertNotNull(Validadores.correo("contacto.empresa.cl"))
    }

    @Test
    fun `un correo sin dominio es invalido`() {
        assertNotNull(Validadores.correo("contacto@empresa"))
    }

    @Test
    fun `un correo bien formado es valido`() {
        assertNull(Validadores.correo("vcaceres@cerroverde.cl"))
    }

    // ---------- Teléfono ----------

    @Test
    fun `un telefono demasiado corto es invalido`() {
        assertNotNull(Validadores.telefono("12345"))
    }

    @Test
    fun `un telefono chileno con prefijo es valido`() {
        assertNull(Validadores.telefono("+56 9 7100 2233"))
    }

    // ---------- Público esperado ----------

    @Test
    fun `el publico esperado no puede ser cero`() {
        assertNotNull(Validadores.enteroPositivo("0", "El público esperado"))
    }

    @Test
    fun `el publico esperado no puede ser texto`() {
        assertNotNull(Validadores.enteroPositivo("mil", "El público esperado"))
    }

    @Test
    fun `un publico esperado positivo es valido`() {
        assertNull(Validadores.enteroPositivo("2500", "El público esperado"))
    }

    // ---------- Costo referencial ----------

    @Test
    fun `el costo puede quedar vacio porque la participacion puede no tener costo`() {
        assertNull(Validadores.montoNoNegativo("", "El costo"))
    }

    @Test
    fun `el costo no puede ser negativo`() {
        assertNotNull(Validadores.montoNoNegativo("-1000", "El costo"))
    }

    @Test
    fun `un costo positivo es valido`() {
        assertNull(Validadores.montoNoNegativo("180000", "El costo"))
    }

    // ---------- Coherencia entre fechas ----------

    @Test
    fun `una fecha posterior anterior a la de referencia es invalida`() {
        val mensaje = "El evento no puede ocurrir antes de haberse detectado"
        val resultado = Validadores.fechaPosteriorA(
            posterior = 1_000L,
            anterior = 2_000L,
            mensaje = mensaje
        )
        assertEquals(mensaje, resultado)
    }

    @Test
    fun `una fecha posterior correcta es valida`() {
        assertNull(
            Validadores.fechaPosteriorA(
                posterior = 3_000L,
                anterior = 2_000L,
                mensaje = "no debería aparecer"
            )
        )
    }

    @Test
    fun `si falta una de las dos fechas no se valida la coherencia`() {
        assertNull(Validadores.fechaPosteriorA(null, 2_000L, "no debería aparecer"))
    }

    // ---------- Selecciones ----------

    @Test
    fun `una seleccion sin elegir es invalida`() {
        assertNotNull(Validadores.seleccionRequerida(null, "una comuna"))
    }

    @Test
    fun `una seleccion elegida es valida`() {
        assertNull(Validadores.seleccionRequerida(4L, "una comuna"))
    }
}
