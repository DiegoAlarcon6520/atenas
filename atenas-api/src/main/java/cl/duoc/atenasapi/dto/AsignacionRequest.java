package cl.duoc.atenasapi.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Asignar (o quitar, con responsableId null) el responsable de una
 * oportunidad. usuarioId es quien hace la asignación: debe ser supervisor.
 */
public record AsignacionRequest(
    Long responsableId,
    @NotNull(message = "Indica quién hace la asignación") Long usuarioId
) {
}
