# atenas-api — Microservicio de Atenas

API REST que guarda los datos de la app Atenas: usuarios, catálogo de
ubicación, organizaciones, contactos, oportunidades, gestiones de seguimiento
y evidencias. Es la **fuente de verdad**. La app guarda una copia en Room para
consultar sin conexión, pero toda escritura pasa por aquí.

## Stack

| Pieza | Para qué |
|---|---|
| Spring Boot 4.1.2 · Java 21 · Maven | Base del microservicio |
| Spring Web MVC | Endpoints REST |
| Spring Data JPA (Hibernate) | Entidades y repositorios; crea las tablas |
| Bean Validation | Valida los datos que llegan (`@NotBlank`, `@Email`...) |
| MySQL (MariaDB de XAMPP) | Base de datos |
| spring-security-crypto | Solo el cifrado BCrypt de las claves |
| Lombok | Getters y setters de las entidades |
| springdoc (Swagger) | Documentación y pruebas en el navegador |
| JUnit 5 + Mockito | Pruebas unitarias |

## Cómo ejecutarlo

1. Abre **XAMPP** y presiona **Start** en **MySQL**. No hace falta crear la
   base: se crea sola (`atenas_db`).
2. En **IntelliJ**: *File → Open…* y elige la carpeta `atenas-api` (la que
   tiene el `pom.xml`). Espera a que Maven descargue las dependencias.
3. Si IntelliJ no detecta el JDK: *File → Project Structure → SDK* → Java 21.
4. Abre `AtenasApiApplication.java` y presiona ▶ (Run).
5. En la consola debe aparecer `Tomcat started on port 8080`.
6. Abre http://localhost:8080/swagger-ui.html para ver y probar todos los
   endpoints.

La primera vez se cargan los mismos datos de prueba que trae la app.

Si tu MySQL tiene clave para `root`, escríbela en
`src/main/resources/application.properties` (`spring.datasource.password`).

### Cuentas de prueba (clave `atenas123`)

| Usuario | Nombre | Perfil |
|---|---|---|
| `cvaldes` | Camila Valdés | Ejecutiva |
| `rfuentes` | Rodrigo Fuentes | Ejecutivo |
| `psoto` | Paulina Soto | Ejecutiva |
| `msilva` | Marcelo Silva | Supervisor |

### Desde la app

| Dónde corre la app | URL base para Retrofit |
|---|---|
| Emulador de Android Studio | `http://10.0.2.2:8080/` |
| Celular físico (misma red wifi) | `http://<IP de tu PC>:8080/` |

En el emulador, `localhost` es el propio emulador, no tu PC. La app necesita
el permiso `INTERNET` y permitir HTTP sin cifrar hacia esa dirección
(`network_security_config`), porque el servidor local no usa HTTPS.

## Endpoints

Todos bajo `/api`. Las fechas son milisegundos epoch y las horas, minutos
desde la medianoche (600 = 10:00), igual que en la app.

| Recurso | Método y ruta | Qué hace |
|---|---|---|
| Sesión | `POST /auth/login` | Valida usuario y clave; devuelve el usuario con su rol |
| Usuarios | `GET /usuarios[?rol=EJECUTIVO]` · `GET /usuarios/{id}` | Consultar |
| | `POST /usuarios` · `PUT /usuarios/{id}` · `DELETE /usuarios/{id}` | Crear, editar, eliminar |
| Catálogo | `GET /regiones` · `GET /regiones/{id}` | Solo lectura |
| | `GET /comunas[?regionId=1]` · `GET /comunas/{id}` | Solo lectura |
| Organizaciones | `GET` · `GET /{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` | CRUD completo |
| Contactos | `GET [?organizacionId=1]` · `GET /{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` | CRUD completo |
| Oportunidades | `GET [?responsableId=1]` · `GET /{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` | CRUD completo |
| | `PATCH /oportunidades/{id}/responsable` | Asignar responsable (solo supervisor) |
| | `GET /oportunidades/{id}/seguimientos` | Historial de gestiones |
| | `POST /oportunidades/{id}/seguimientos` | Registrar una gestión (y cambiar el estado) |
| Gestiones | `GET /seguimientos[?responsableId=1]` · `GET /seguimientos/{id}` | Consultar |
| Evidencias | `GET [?oportunidadId=1]` · `GET /{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` | CRUD completo |
| Sincronización | `GET /sincronizacion?usuarioId=1` | Copia completa para Room, filtrada por perfil |

### Respuestas de error

Todas tienen la misma forma, para que la app las lea siempre igual:

```json
{
  "estado": 400,
  "error": "Bad Request",
  "mensaje": "Revisa los campos marcados",
  "campos": { "correo": "Ingresa un correo con formato válido" },
  "fecha": 1795345087230
}
```

| Código | Cuándo |
|---|---|
| 400 | Un dato no cumple una regla (campo vacío, fecha incoherente, id que no existe) |
| 401 | Usuario o clave incorrectos |
| 403 | El perfil no puede hacer esa acción (un ejecutivo que confirma o asigna) |
| 404 | El recurso de la URL no existe |
| 409 | La acción choca con el estado de los datos (oportunidad cerrada, borrar algo con historial) |

## Decisiones de diseño

- **Las reglas de negocio se repiten en el servidor.** La máquina de estados,
  los permisos por perfil y la coherencia de fechas ya existen en la app,
  pero el servidor es la fuente de verdad: si alguien llama a la API sin pasar
  por la app, las reglas se siguen cumpliendo.
- **El estado solo cambia registrando una gestión.** El `PUT` de una
  oportunidad no toca el estado. Así cada movimiento del embudo queda en el
  historial, con fecha y autor.
- **El historial no se edita ni se borra.** Las gestiones no tienen `PUT` ni
  `DELETE`, para que la trazabilidad no se pueda reescribir.
- **Registrar una gestión es una transacción.** Guarda la gestión, cambia el
  estado y actualiza el último contacto juntos. Si una de las tres falla, no
  se guarda ninguna.
- **No se borra lo que tiene historial.** Una oportunidad con gestiones, un
  contacto asignado o un usuario con actividad responden 409: se descartan o
  se desactivan, como en la app.
- **El catálogo de regiones y comunas es de solo lectura.** Es un dato fijo
  que los usuarios consultan pero no administran.
- **Fechas en milisegundos epoch, como en la app.** Así no hay conversiones
  de zona horaria entre el teléfono y el servidor, que fue la causa del
  defecto D1 de la auditoría.
- **Las evidencias guardan la referencia de la foto, no el archivo.** La foto
  queda en el teléfono; subir imágenes al servidor queda fuera del alcance.
- **Sin JWT.** El caso pide iniciar sesión con cuentas ficticias. El login
  valida con BCrypt, pero las rutas no exigen token: quién hace cada acción
  se envía en el cuerpo (`usuarioId`) y el servidor verifica su rol. En un
  sistema real se agregaría un token para que ese dato no se pueda falsificar.

## Pruebas

```
mvn test
```

O en IntelliJ: clic derecho en `src/test/java` → *Run 'All Tests'*.

| Archivo | Qué verifica |
|---|---|
| `domain/TransicionesEstadoTest` | Misma máquina de estados que la app: quién avanza, quién retrocede, cierre |
| `domain/ReglasNegocioTest` | Coherencia de fechas del evento y de la gestión, normalización de datos |
| `service/SeguimientoServiceTest` | Registrar una gestión: permisos por perfil, oportunidad cerrada, saltos inválidos, actualización del contacto |

Las pruebas no necesitan XAMPP encendido: no usan la base de datos.
