package cl.duoc.atenas.domain.rules

import cl.duoc.atenas.domain.model.RolUsuario

/**
 * Reglas de autorización por perfil. Vive en el dominio, fuera de la UI,
 * para que las pantallas solo consulten y nunca decidan.
 */
object Permisos {

    /** El supervisor ve la cartera completa; el ejecutivo solo lo asignado a él. */
    fun puedeVerTodasLasOportunidades(rol: RolUsuario): Boolean =
        rol == RolUsuario.SUPERVISOR

    fun puedeAsignarResponsable(rol: RolUsuario): Boolean =
        rol == RolUsuario.SUPERVISOR

    /** Confirmar o descartar definitivamente queda reservado al supervisor. */
    fun puedeConfirmarODescartar(rol: RolUsuario): Boolean =
        rol == RolUsuario.SUPERVISOR

    fun puedeCambiarPrioridad(rol: RolUsuario): Boolean =
        rol == RolUsuario.SUPERVISOR

    /**
     * Devolver una oportunidad a una etapa anterior corrige el embudo, y
     * corregir el embudo es responsabilidad de quien lo supervisa. El
     * ejecutivo solo avanza.
     */
    fun puedeRetrocederEstado(rol: RolUsuario): Boolean =
        rol == RolUsuario.SUPERVISOR

    fun puedeCrearOportunidad(rol: RolUsuario): Boolean = true

    fun puedeRegistrarSeguimiento(rol: RolUsuario): Boolean = true

    fun puedeVerPanelDeEquipo(rol: RolUsuario): Boolean =
        rol == RolUsuario.SUPERVISOR
}
