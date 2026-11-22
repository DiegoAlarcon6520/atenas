package cl.duoc.atenasapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de Atenas.
 *
 * Es la fuente de verdad de los datos: la app guarda una copia en Room para
 * poder consultar sin conexión, pero toda escritura pasa por esta API, que
 * vuelve a aplicar las reglas de negocio antes de guardar.
 */
@SpringBootApplication
public class AtenasApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtenasApiApplication.class, args);
    }
}
