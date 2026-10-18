package cl.duoc.atenas.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.atenas.data.local.entity.Organizacion
import cl.duoc.atenas.domain.model.TipoOrganizacion
import kotlinx.coroutines.flow.Flow

/**
 * Fila del listado de organizaciones.
 *
 * Los dos totales se calculan con subconsultas dentro del mismo SELECT: así
 * la lista llega lista para dibujar, sin que la pantalla tenga que pedir los
 * contactos y las oportunidades de cada organización por separado (lo que
 * sería el clásico problema N+1 consultas).
 */
data class OrganizacionResumen(
    val id: Long,
    val nombre: String,
    val tipo: TipoOrganizacion,
    val rubro: String,
    val comunaNombre: String,
    val regionNombre: String,
    val totalContactos: Int,
    val totalOportunidades: Int
)

/** Organización completa con su ubicación resuelta, para la pantalla de detalle. */
data class OrganizacionDetalle(
    @Embedded val organizacion: Organizacion,
    val comunaNombre: String,
    val regionNombre: String
)

/**
 * DAO DE ORGANIZACIONES
 *
 * Una organización es la empresa, municipalidad o institución dueña del
 * evento. De ella cuelgan sus contactos y sus oportunidades.
 */
@Dao
interface OrganizacionDao {

    @Query(
        """
        SELECT o.id AS id,
               o.nombre AS nombre,
               o.tipo AS tipo,
               o.rubro AS rubro,
               c.nombre AS comunaNombre,
               r.nombre AS regionNombre,
               (SELECT COUNT(*) FROM contacto ct WHERE ct.organizacionId = o.id) AS totalContactos,
               (SELECT COUNT(*) FROM oportunidad op WHERE op.organizacionId = o.id) AS totalOportunidades
        FROM organizacion o
        INNER JOIN comuna c ON c.id = o.comunaId
        INNER JOIN region r ON r.id = c.regionId
        WHERE (
                :texto = ''
                OR o.nombre LIKE '%' || :texto || '%'
                OR o.rubro LIKE '%' || :texto || '%'
                OR c.nombre LIKE '%' || :texto || '%'
              )
          AND (:tipo IS NULL OR o.tipo = :tipo)
        ORDER BY o.nombre
        """
    )
    fun observarResumen(texto: String, tipo: String?): Flow<List<OrganizacionResumen>>

    @Query(
        """
        SELECT o.*,
               c.nombre AS comunaNombre,
               r.nombre AS regionNombre
        FROM organizacion o
        INNER JOIN comuna c ON c.id = o.comunaId
        INNER JOIN region r ON r.id = c.regionId
        WHERE o.id = :id
        """
    )
    fun observarDetalle(id: Long): Flow<OrganizacionDetalle?>

    @Query("SELECT * FROM organizacion ORDER BY nombre")
    fun observarTodas(): Flow<List<Organizacion>>

    @Query("SELECT * FROM organizacion WHERE id = :id")
    suspend fun porId(id: Long): Organizacion?

    @Insert
    suspend fun insertar(organizacion: Organizacion): Long

    @Insert
    suspend fun insertarTodas(organizaciones: List<Organizacion>): List<Long>

    @Update
    suspend fun actualizar(organizacion: Organizacion)
}
