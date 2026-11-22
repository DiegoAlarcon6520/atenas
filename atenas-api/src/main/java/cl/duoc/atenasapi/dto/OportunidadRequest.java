package cl.duoc.atenasapi.dto;

import cl.duoc.atenasapi.domain.AfinidadPublico;
import cl.duoc.atenasapi.domain.PotencialComercial;
import cl.duoc.atenasapi.domain.Prioridad;
import cl.duoc.atenasapi.domain.TipoOportunidad;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Alta o edición de una oportunidad.
 *
 * No trae estado: una oportunidad nueva siempre entra como IDENTIFICADA, y
 * después el estado solo cambia registrando una gestión (así cada cambio
 * queda en el historial). Al editar se ignoran responsableId y creadoPorId:
 * el responsable cambia con PATCH /oportunidades/{id}/responsable.
 */
public record OportunidadRequest(
    @NotBlank(message = "El nombre del evento es obligatorio")
    @Size(min = 5, max = 150, message = "El nombre del evento debe tener entre 5 y 150 caracteres") String nombre,
    @NotNull(message = "El tipo es obligatorio") TipoOportunidad tipo,
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 1000, message = "La descripción debe tener entre 10 y 1000 caracteres") String descripcion,
    @NotBlank(message = "La fuente es obligatoria") String fuenteInformacion,
    @NotNull(message = "La fecha de detección es obligatoria") Long fechaDeteccion,
    @NotNull(message = "La fecha del evento es obligatoria") Long fechaInicioEvento,
    Long fechaTerminoEvento,
    @NotNull(message = "La hora de inicio es obligatoria")
    @Min(value = 0, message = "Hora no válida") @Max(value = 1439, message = "Hora no válida") Integer horaInicio,
    @NotNull(message = "La hora de término es obligatoria")
    @Min(value = 0, message = "Hora no válida") @Max(value = 1439, message = "Hora no válida") Integer horaTermino,
    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres") String direccion,
    @NotNull(message = "Selecciona una comuna") Long comunaId,
    @NotNull(message = "Selecciona una organización") Long organizacionId,
    Long contactoId,
    Long responsableId,
    @NotNull(message = "Indica quién crea la oportunidad") Long creadoPorId,
    @NotBlank(message = "El público objetivo es obligatorio") String publicoObjetivo,
    @NotNull(message = "El público esperado es obligatorio")
    @Positive(message = "El público esperado debe ser mayor que cero") Integer publicoEsperado,
    @NotNull(message = "La prioridad es obligatoria") Prioridad prioridad,
    PotencialComercial potencial,
    AfinidadPublico afinidad,
    @PositiveOrZero(message = "El costo referencial no puede ser negativo") Double costoReferencial,
    String condiciones,
    String requisitos,
    String espaciosDisponibles,
    String observaciones
) {
}
