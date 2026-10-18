package cl.duoc.atenas.data.local

import androidx.room.TypeConverter
import cl.duoc.atenas.domain.model.AreaContacto
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.MedioContacto
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.RolUsuario
import cl.duoc.atenas.domain.model.TipoAccion
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.domain.model.TipoOrganizacion

/** Convierte los enum del dominio a TEXT para SQLite y viceversa. */
class Convertidores {

    @TypeConverter fun desdeRol(valor: RolUsuario): String = valor.name
    @TypeConverter fun aRol(valor: String): RolUsuario = RolUsuario.valueOf(valor)

    @TypeConverter fun desdeTipoOrganizacion(valor: TipoOrganizacion): String = valor.name
    @TypeConverter fun aTipoOrganizacion(valor: String): TipoOrganizacion = TipoOrganizacion.valueOf(valor)

    @TypeConverter fun desdeAreaContacto(valor: AreaContacto): String = valor.name
    @TypeConverter fun aAreaContacto(valor: String): AreaContacto = AreaContacto.valueOf(valor)

    @TypeConverter fun desdeMedioContacto(valor: MedioContacto): String = valor.name
    @TypeConverter fun aMedioContacto(valor: String): MedioContacto = MedioContacto.valueOf(valor)

    @TypeConverter fun desdeTipoOportunidad(valor: TipoOportunidad): String = valor.name
    @TypeConverter fun aTipoOportunidad(valor: String): TipoOportunidad = TipoOportunidad.valueOf(valor)

    @TypeConverter fun desdePrioridad(valor: Prioridad): String = valor.name
    @TypeConverter fun aPrioridad(valor: String): Prioridad = Prioridad.valueOf(valor)

    @TypeConverter fun desdeEstado(valor: EstadoOportunidad): String = valor.name
    @TypeConverter fun aEstado(valor: String): EstadoOportunidad = EstadoOportunidad.valueOf(valor)

    @TypeConverter fun desdeTipoAccion(valor: TipoAccion): String = valor.name
    @TypeConverter fun aTipoAccion(valor: String): TipoAccion = TipoAccion.valueOf(valor)
}
