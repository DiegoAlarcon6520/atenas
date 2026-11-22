package cl.duoc.atenas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import cl.duoc.atenas.data.local.entity.Comuna
import cl.duoc.atenas.data.local.entity.Contacto
import cl.duoc.atenas.data.local.entity.Evidencia
import cl.duoc.atenas.data.local.entity.Oportunidad
import cl.duoc.atenas.data.local.entity.Organizacion
import cl.duoc.atenas.data.local.entity.Region
import cl.duoc.atenas.data.local.entity.Seguimiento
import cl.duoc.atenas.data.local.entity.Usuario

/**
 * Reemplaza la copia local completa por la que entregó el servidor.
 *
 * Todo ocurre en una transacción: si algo falla a mitad de camino, Room
 * deshace los cambios y la copia anterior queda intacta, en vez de una base
 * a medio borrar. Se borra de las tablas hijas a las padres y se inserta al
 * revés, para respetar las llaves foráneas.
 */
@Dao
abstract class SincronizacionDao {

    @Query("DELETE FROM evidencia")
    abstract suspend fun borrarEvidencias()

    @Query("DELETE FROM seguimiento")
    abstract suspend fun borrarSeguimientos()

    @Query("DELETE FROM oportunidad")
    abstract suspend fun borrarOportunidades()

    @Query("DELETE FROM contacto")
    abstract suspend fun borrarContactos()

    @Query("DELETE FROM organizacion")
    abstract suspend fun borrarOrganizaciones()

    @Query("DELETE FROM comuna")
    abstract suspend fun borrarComunas()

    @Query("DELETE FROM region")
    abstract suspend fun borrarRegiones()

    @Query("DELETE FROM usuario")
    abstract suspend fun borrarUsuarios()

    @Insert
    abstract suspend fun insertarUsuarios(usuarios: List<Usuario>)

    @Insert
    abstract suspend fun insertarRegiones(regiones: List<Region>)

    @Insert
    abstract suspend fun insertarComunas(comunas: List<Comuna>)

    @Insert
    abstract suspend fun insertarOrganizaciones(organizaciones: List<Organizacion>)

    @Insert
    abstract suspend fun insertarContactos(contactos: List<Contacto>)

    @Insert
    abstract suspend fun insertarOportunidades(oportunidades: List<Oportunidad>)

    @Insert
    abstract suspend fun insertarSeguimientos(seguimientos: List<Seguimiento>)

    @Insert
    abstract suspend fun insertarEvidencias(evidencias: List<Evidencia>)

    @Transaction
    open suspend fun reemplazarTodo(
        usuarios: List<Usuario>,
        regiones: List<Region>,
        comunas: List<Comuna>,
        organizaciones: List<Organizacion>,
        contactos: List<Contacto>,
        oportunidades: List<Oportunidad>,
        seguimientos: List<Seguimiento>,
        evidencias: List<Evidencia>
    ) {
        borrarEvidencias()
        borrarSeguimientos()
        borrarOportunidades()
        borrarContactos()
        borrarOrganizaciones()
        borrarComunas()
        borrarRegiones()
        borrarUsuarios()

        insertarUsuarios(usuarios)
        insertarRegiones(regiones)
        insertarComunas(comunas)
        insertarOrganizaciones(organizaciones)
        insertarContactos(contactos)
        insertarOportunidades(oportunidades)
        insertarSeguimientos(seguimientos)
        insertarEvidencias(evidencias)
    }
}
