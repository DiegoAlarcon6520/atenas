package cl.duoc.atenas

import android.app.Application
import cl.duoc.atenas.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * CLASE APPLICATION
 *
 * Es lo primero que se crea cuando arranca la aplicación, antes que cualquier
 * pantalla. Acá se construye el contenedor de dependencias (base de datos,
 * repositorios y sesión) para que exista uno solo durante toda la vida de la
 * app, y se siembran los datos ficticios la primera vez.
 *
 * Está declarada en AndroidManifest.xml con android:name=".AtenasApp".
 */
class AtenasApp : Application() {

    lateinit var contenedor: AppContainer
        private set

    private val alcanceApp = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        contenedor = AppContainer(this)
        alcanceApp.launch { contenedor.sembrarDatosIniciales() }
    }
}
