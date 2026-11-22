package cl.duoc.atenasapi.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Las fechas viajan y se guardan como milisegundos desde 1970 (epoch), igual
 * que en la app. Así no hay conversión de zona horaria entre el teléfono y el
 * servidor, que fue justamente la causa del defecto D1 de la auditoría.
 *
 * "Hoy" se calcula en la hora de Chile, que es donde opera el equipo.
 */
public final class Fechas {

    public static final ZoneId ZONA = ZoneId.of("America/Santiago");

    private Fechas() {
    }

    public static long ahora() {
        return System.currentTimeMillis();
    }

    /** Medianoche de hoy en Chile. */
    public static long hoy() {
        return LocalDate.now(ZONA).atStartOfDay(ZONA).toInstant().toEpochMilli();
    }

    /** Suma días respetando los cambios de horario de verano. */
    public static long masDias(long base, int dias) {
        return Instant.ofEpochMilli(base).atZone(ZONA).plusDays(dias).toInstant().toEpochMilli();
    }
}
