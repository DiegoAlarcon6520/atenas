package cl.duoc.atenasapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cifrado de claves con BCrypt.
 *
 * Se usa solo la librería de cifrado de Spring Security, sin activar su
 * configuración web: no hay filtros ni tokens, porque el caso pide inicio de
 * sesión con cuentas ficticias y no protección de la API.
 */
@Configuration
public class CifradoConfig {

    @Bean
    public PasswordEncoder cifradorDeClaves() {
        return new BCryptPasswordEncoder();
    }
}
