package cl.duoc.atenas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import cl.duoc.atenas.data.local.entity.Seguimiento
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.TipoAccion
import kotlinx.coroutines.flow.Flow

/** Gestión pendiente con el nombre de su oportunidad, para alertas y tableros. */
data class GestionPendiente(
    val id: Long,
    val oportunidadId: Long,
    val oportunidadNombre: String,
    val proximaAccion: String?,
    val fechaProximoSeguimiento: Long,
    val responsableNombre: String?
)

/** Gestión realizada con alguna oportunidad de una organización, para su ficha (HU-14). */
data class GestionDeOrganizacion(
    val id: Long,
    val oportunidadId: Long,
    val oportunidadNombre: String,
    val tipoAccion: TipoAccion,
    val fechaAccion: Long,
    val resultado: String,
    val usuarioNombre: String
)

/** Propuesta registrada como gestión de tipo "Envío de propuesta" (HU-16). */
data class PropuestaEnviada(
    val id: Long,
    val oportunidadId: Long,
    val oportunidadNombre: String,
    val organizacionNombre: String,
    val fechaEnvio: Long,
    val resultado: String,
    val estadoActual: EstadoOportunidad,
    val responsableNombre: String?
)

/*
 * DAO DE SEGUIMIENTOS (GESTIONES)
 *
 * Es una clase abstracta y no una interfaz porque registrarGestion() tiene
 * cuerpo: agrupa varias escrituras en una sola transacción. Si cualquiera de
 * ellas falla, Room deshace todas y la base no queda a medio actualizar
 * (por ejemplo, una gestión guardada sin el cambio de estado que traía).
 */
@Dao
abstract class SeguimientoDao {

    /**
     * Oportunidades cuya ÚLTIMA gestión comprometió una próxima acción que
     * ya venció.
     *
     * Solo cuenta la última gestión de cada oportunidad: si después de una
     * gestión vencida se registró otra, la primera ya fue atendida y no debe
     * seguir apareciendo. Se excluyen los estados en que la gestión comercial
     * terminó.
     *
     * [hoy] es la medianoche de hoy: una acción comprometida para hoy todavía
     * está a tiempo, recién se atrasa mañana.
     */
    @Query(
        """
        SELECT s.id AS id,
               s.oportunidadId AS oportunidadId,
               o.nombre AS oportunidadNombre,
               s.proximaAccion AS proximaAccion,
               s.fechaProximoSeguimiento AS fechaProximoSeguimiento,
               u.nombreCompleto AS responsableNombre
        FROM seguimiento s
        INNER JOIN oportunidad o ON o.id = s.oportunidadId
        LEFT JOIN usuario u ON u.id = o.responsableId
        WHERE s.id = (
                SELECT s2.id FROM seguimiento s2
                WHERE s2.oportunidadId = s.oportunidadId
                ORDER BY s2.fechaAccion DESC, s2.id DESC
                LIMIT 1
              )
          AND s.fechaProximoSeguimiento IS NOT NULL
          AND s.fechaProximoSeguimiento < :hoy
          AND o.estado NOT IN ('DESCARTADA', 'CERRADA', 'ACTIVACION_REALIZADA')
          AND (:responsableId IS NULL OR o.responsableId = :responsableId)
        ORDER BY s.fechaProximoSeguimiento ASC
        """
    )
    abstract fun observarGestionesAtrasadas(hoy: Long, responsableId: Long?): Flow<List<GestionPendiente>>

    @Query("SELECT * FROM seguimiento WHERE oportunidadId = :oportunidadId ORDER BY fechaAccion DESC, id DESC")
    abstract fun observarHistorial(oportunidadId: Long): Flow<List<Seguimiento>>

    /**
     * Historial de gestiones con todas las oportunidades de una organización.
     * [responsableId] nulo trae todo (supervisor); con valor, solo las
     * oportunidades de ese ejecutivo.
     */
    @Query(
        """
        SELECT s.id AS id,
               s.oportunidadId AS oportunidadId,
               o.nombre AS oportunidadNombre,
               s.tipoAccion AS tipoAccion,
               s.fechaAccion AS fechaAccion,
               s.resultado AS resultado,
               u.nombreCompleto AS usuarioNombre
        FROM seguimiento s
        INNER JOIN oportunidad o ON o.id = s.oportunidadId
        INNER JOIN usuario u ON u.id = s.usuarioId
        WHERE o.organizacionId = :organizacionId
          AND (:responsableId IS NULL OR o.responsableId = :responsableId)
        ORDER BY s.fechaAccion DESC, s.id DESC
        """
    )
    abstract fun observarPorOrganizacion(organizacionId: Long, responsableId: Long?): Flow<List<GestionDeOrganizacion>>

    /** Todas las propuestas enviadas, de la más reciente a la más antigua. */
    @Query(
        """
        SELECT s.id AS id,
               s.oportunidadId AS oportunidadId,
               o.nombre AS oportunidadNombre,
               org.nombre AS organizacionNombre,
               s.fechaAccion AS fechaEnvio,
               s.resultado AS resultado,
               o.estado AS estadoActual,
               u.nombreCompleto AS responsableNombre
        FROM seguimiento s
        INNER JOIN oportunidad o ON o.id = s.oportunidadId
        INNER JOIN organizacion org ON org.id = o.organizacionId
        LEFT JOIN usuario u ON u.id = o.responsableId
        WHERE s.tipoAccion = 'ENVIO_PROPUESTA'
          AND (:responsableId IS NULL OR o.responsableId = :responsableId)
        ORDER BY s.fechaAccion DESC, s.id DESC
        """
    )
    abstract fun observarPropuestas(responsableId: Long?): Flow<List<PropuestaEnviada>>

    /** Última gestión registrada, para poder reutilizar sus datos. */
    @Query(
        """
        SELECT * FROM seguimiento
        WHERE oportunidadId = :oportunidadId
        ORDER BY fechaAccion DESC, id DESC
        LIMIT 1
        """
    )
    abstract suspend fun ultima(oportunidadId: Long): Seguimiento?

    @Insert
    abstract suspend fun insertar(seguimiento: Seguimiento): Long

    @Insert
    abstract suspend fun insertarTodos(seguimientos: List<Seguimiento>): List<Long>

    @Query("UPDATE oportunidad SET estado = :estado, fechaActualizacion = :fecha WHERE id = :oportunidadId")
    abstract suspend fun actualizarOportunidad(oportunidadId: Long, estado: EstadoOportunidad, fecha: Long)

    /** Solo avanza la fecha: una gestión antigua registrada tarde no la hace retroceder. */
    @Query(
        """
        UPDATE contacto SET fechaUltimoContacto = :fecha
        WHERE id = :contactoId
          AND (fechaUltimoContacto IS NULL OR fechaUltimoContacto < :fecha)
        """
    )
    abstract suspend fun actualizarUltimoContacto(contactoId: Long, fecha: Long)

    /**
     * Registra una gestión completa en una sola transacción:
     *  1. inserta el seguimiento,
     *  2. deja la oportunidad en el estado nuevo y marca su actualización,
     *  3. si la gestión fue con el contacto, actualiza su fecha de último contacto.
     */
    @Transaction
    open suspend fun registrarGestion(
        seguimiento: Seguimiento,
        contactoId: Long?,
        actualizaContacto: Boolean,
        ahora: Long
    ): Long {
        val id = insertar(seguimiento)
        actualizarOportunidad(seguimiento.oportunidadId, seguimiento.estadoNuevo, ahora)
        if (contactoId != null && actualizaContacto) {
            actualizarUltimoContacto(contactoId, seguimiento.fechaAccion)
        }
        return id
    }
}
