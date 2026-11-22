package cl.duoc.atenasapi.dto;

import java.util.List;

/**
 * Todo lo que la app necesita guardar en Room para trabajar sin conexión, en
 * una sola llamada. Las oportunidades y su historial ya vienen filtrados por
 * perfil: un ejecutivo recibe solo su cartera; el supervisor, la de todos.
 */
public record SincronizacionResponse(
    long generadoEn,
    List<UsuarioResponse> usuarios,
    List<RegionResponse> regiones,
    List<ComunaResponse> comunas,
    List<OrganizacionResponse> organizaciones,
    List<ContactoResponse> contactos,
    List<OportunidadResponse> oportunidades,
    List<SeguimientoResponse> seguimientos,
    List<EvidenciaResponse> evidencias
) {
}
