package cl.duoc.atenas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.atenas.domain.model.AreaContacto
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.MedioContacto
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.RolUsuario
import cl.duoc.atenas.domain.model.TipoAccion
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.domain.model.TipoOrganizacion

/*
 * Modelo relacional normalizado (3FN).
 *
 * region 1-N comuna
 * comuna 1-N organizacion, 1-N oportunidad
 * organizacion 1-N contacto, 1-N oportunidad
 * usuario 1-N oportunidad (responsable y creador), 1-N seguimiento
 * oportunidad 1-N seguimiento, 1-N evidencia
 *
 * La región NO se guarda en organizacion ni en oportunidad: dependería
 * transitivamente de la comuna y rompería la tercera forma normal.
 */

@Entity(
    tableName = "usuario",
    indices = [Index(value = ["nombreUsuario"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombreUsuario: String,
    val claveHash: String,
    val nombreCompleto: String,
    val rol: RolUsuario,
    val area: String,
    val zona: String,
    val activo: Boolean = true,
    val fechaCreacion: Long,
    val fechaActualizacion: Long
)

@Entity(
    tableName = "region",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class Region(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String
)

@Entity(
    tableName = "comuna",
    foreignKeys = [
        ForeignKey(
            entity = Region::class,
            parentColumns = ["id"],
            childColumns = ["regionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("regionId"), Index(value = ["nombre", "regionId"], unique = true)]
)
data class Comuna(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val regionId: Long
)

@Entity(
    tableName = "organizacion",
    foreignKeys = [
        ForeignKey(
            entity = Comuna::class,
            parentColumns = ["id"],
            childColumns = ["comunaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("comunaId")]
)
data class Organizacion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: TipoOrganizacion,
    val rubro: String,
    val comunaId: Long,
    val sitioWeb: String?,
    val publicoAsociado: String?,
    val observaciones: String?,
    val fechaCreacion: Long
)

@Entity(
    tableName = "contacto",
    foreignKeys = [
        ForeignKey(
            entity = Organizacion::class,
            parentColumns = ["id"],
            childColumns = ["organizacionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("organizacionId")]
)
data class Contacto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizacionId: Long,
    val nombre: String,
    val cargo: String,
    val area: AreaContacto,
    val correo: String,
    val telefono: String,
    val medioPreferente: MedioContacto,
    val activo: Boolean = true,
    val fechaUltimoContacto: Long?,
    val observaciones: String?
)

@Entity(
    tableName = "oportunidad",
    foreignKeys = [
        ForeignKey(
            entity = Organizacion::class,
            parentColumns = ["id"],
            childColumns = ["organizacionId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Contacto::class,
            parentColumns = ["id"],
            childColumns = ["contactoId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Comuna::class,
            parentColumns = ["id"],
            childColumns = ["comunaId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["responsableId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["creadoPorId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("organizacionId"), Index("contactoId"), Index("comunaId"),
        Index("responsableId"), Index("creadoPorId"), Index("estado")
    ]
)
data class Oportunidad(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: TipoOportunidad,
    val descripcion: String,
    val fuenteInformacion: String,
    val fechaDeteccion: Long,
    val fechaInicioEvento: Long,
    val fechaTerminoEvento: Long?,
    val horario: String,
    val direccion: String,
    val comunaId: Long,
    val organizacionId: Long,
    val contactoId: Long?,
    val responsableId: Long?,
    val creadoPorId: Long,
    val publicoObjetivo: String,
    val publicoEsperado: Int,
    val prioridad: Prioridad,
    val estado: EstadoOportunidad,
    val costoReferencial: Double,
    val condiciones: String?,
    val requisitos: String?,
    val observaciones: String?,
    val fechaCreacion: Long,
    val fechaActualizacion: Long
)

@Entity(
    tableName = "seguimiento",
    foreignKeys = [
        ForeignKey(
            entity = Oportunidad::class,
            parentColumns = ["id"],
            childColumns = ["oportunidadId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("oportunidadId"), Index("usuarioId")]
)
data class Seguimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val oportunidadId: Long,
    val usuarioId: Long,
    val tipoAccion: TipoAccion,
    val fechaAccion: Long,
    val resultado: String,
    val proximaAccion: String?,
    val fechaProximoSeguimiento: Long?,
    val estadoAnterior: EstadoOportunidad,
    val estadoNuevo: EstadoOportunidad,
    val observaciones: String?
)

@Entity(
    tableName = "evidencia",
    foreignKeys = [
        ForeignKey(
            entity = Oportunidad::class,
            parentColumns = ["id"],
            childColumns = ["oportunidadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("oportunidadId")]
)
data class Evidencia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val oportunidadId: Long,
    val uri: String,
    val descripcion: String?,
    val fechaCaptura: Long
)
