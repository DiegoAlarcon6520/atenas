package cl.duoc.atenas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cl.duoc.atenas.data.local.dao.CatalogoDao
import cl.duoc.atenas.data.local.dao.ContactoDao
import cl.duoc.atenas.data.local.dao.EvidenciaDao
import cl.duoc.atenas.data.local.dao.OportunidadDao
import cl.duoc.atenas.data.local.dao.OrganizacionDao
import cl.duoc.atenas.data.local.dao.SeguimientoDao
import cl.duoc.atenas.data.local.dao.UsuarioDao
import cl.duoc.atenas.data.local.entity.Comuna
import cl.duoc.atenas.data.local.entity.Contacto
import cl.duoc.atenas.data.local.entity.Evidencia
import cl.duoc.atenas.data.local.entity.Oportunidad
import cl.duoc.atenas.data.local.entity.Organizacion
import cl.duoc.atenas.data.local.entity.Region
import cl.duoc.atenas.data.local.entity.Seguimiento
import cl.duoc.atenas.data.local.entity.Usuario

@Database(
    entities = [
        Usuario::class,
        Region::class,
        Comuna::class,
        Organizacion::class,
        Contacto::class,
        Oportunidad::class,
        Seguimiento::class,
        Evidencia::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Convertidores::class)
abstract class AtenasDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun catalogoDao(): CatalogoDao
    abstract fun organizacionDao(): OrganizacionDao
    abstract fun contactoDao(): ContactoDao
    abstract fun oportunidadDao(): OportunidadDao
    abstract fun seguimientoDao(): SeguimientoDao
    abstract fun evidenciaDao(): EvidenciaDao

    companion object {

        @Volatile
        private var instancia: AtenasDatabase? = null

        fun obtener(context: Context): AtenasDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AtenasDatabase::class.java,
                    "atenas.db"
                )
                    // MVP académico: ante un cambio de esquema se recrea la base y se vuelve a sembrar.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
    }
}
