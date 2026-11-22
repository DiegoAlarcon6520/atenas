package cl.duoc.atenas.prueba

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * REGLA DE CORRUTINAS PARA PRUEBAS
 *
 * Los ViewModel lanzan su trabajo en viewModelScope, que usa el hilo
 * principal de Android. En una prueba local no existe ese hilo, así que esta
 * regla lo reemplaza por un despachador de prueba que ejecuta todo de
 * inmediato y en orden. Así cada prueba es determinista: después de llamar
 * a un método del ViewModel, el estado ya está actualizado.
 *
 * Uso: `@get:Rule val corrutinas = ReglaCorrutinas()` en la clase de prueba.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReglaCorrutinas(
    private val despachador: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(despachador)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/**
 * Mantiene suscrito un flujo durante la prueba.
 *
 * Los estados de pantalla usan stateIn(WhileSubscribed): solo se calculan
 * mientras alguien los observa, igual que cuando la pantalla está visible.
 * Esta función simula esa pantalla para que el valor se mantenga al día. La
 * suscripción vive en backgroundScope y se cancela sola al terminar la prueba.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.observar(flujo: Flow<*>) {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flujo.collect { } }
}

/**
 * Junta en una lista todo lo que emite un flujo de eventos (por ejemplo, los
 * mensajes de un snackbar) desde este momento hasta el final de la prueba.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> TestScope.recolectar(flujo: Flow<T>): List<T> {
    val recibidos = mutableListOf<T>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flujo.collect { recibidos += it } }
    return recibidos
}
