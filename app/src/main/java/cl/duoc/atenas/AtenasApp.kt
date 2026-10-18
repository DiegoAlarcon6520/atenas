package cl.duoc.atenas

import android.app.Application
import cl.duoc.atenas.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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
