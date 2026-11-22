# Atenas — Gestión móvil de oportunidades y activaciones corporativas

Aplicación Android desarrollada para la asignatura **Desarrollo de Aplicaciones
Móviles (DSY1105)** de Duoc UC, Sede Puente Alto, a partir del caso de
**Agencia Atenas**.

## Integrantes

- Diego Alarcón
- Sebastián Pérez

## Descripción

Agencia Atenas comercializa convenios y beneficios, y está abriendo una línea
de convenios corporativos dirigida a empresas, municipalidades e instituciones.
Hoy detecta las oportunidades de participar en eventos de forma manual y
dispersa, lo que la lleva a enterarse tarde y a perder activaciones.

Atenas centraliza ese proceso en el teléfono del equipo comercial: permite
registrar un evento apenas se detecta, asociarlo a la organización responsable
y a su contacto, priorizarlo, asignarle un ejecutivo, registrar cada gestión
realizada y llevar la oportunidad por el embudo hasta su cierre, con alertas de
lo que está por vencer y de lo que ya se atrasó.

Toda la información de organizaciones, contactos, usuarios y eventos es
**ficticia**, según las restricciones del caso.

## Funcionalidades

**Autenticación y perfiles**
- Inicio y cierre de sesión con cuentas locales ficticias
- Dos perfiles con pantallas y permisos distintos: ejecutivo y supervisor
- Claves almacenadas con hash SHA-256, nunca en texto plano

**Oportunidades**
- Tablero del ejecutivo con sus oportunidades en gestión, eventos próximos,
  avance por estado y gestiones atrasadas
- Panel del supervisor con la cartera completa, carga por ejecutivo,
  distribución por estado y oportunidades sin responsable
- Listado con búsqueda por evento, organización o comuna, y filtros por estado,
  prioridad, potencial comercial, tipo, fecha del evento, región, comuna,
  organización y, para el supervisor, responsable
- Alertas en los tableros: gestiones atrasadas, propuestas sin respuesta y
  oportunidades sin contacto, con acceso directo a cada oportunidad
- Pantalla de propuestas enviadas con su estado y los días que llevan esperando
  respuesta
- Solo el supervisor fija la prioridad: al ejecutivo se le asigna la sugerida
- Una oportunidad cerrada queda sellada: no admite gestiones ni ediciones
- Evaluación de cada oportunidad por potencial comercial y afinidad con el
  público objetivo, con una prioridad sugerida a partir de ambos criterios
- Ficha de detalle con todos los antecedentes y el historial de gestiones
- Formulario de creación y edición con validaciones y reglas de negocio
- Asignación y reasignación de responsables, exclusiva del supervisor
- Eliminación de una oportunidad registrada por error, exclusiva del supervisor
  y solo mientras no tenga gestiones (si ya se gestionó, se descarta)
- Leyendas explicativas en cada indicador de los tableros, para que el número
  deje claro qué está contando

**Organizaciones y contactos**
- Catálogo de organizaciones con búsqueda y filtro por tipo
- Ficha con sus contactos, las oportunidades gestionadas con ella y el
  historial de gestiones con la organización
- La fecha de último contacto se actualiza sola al registrar una gestión con
  el contacto; los contactos desactivados no se ofrecen en oportunidades nuevas
- Formularios de organización y de contacto, con validación de correo y teléfono
- Eliminación de organizaciones sin oportunidades (supervisor; arrastra sus
  contactos) y de contactos que no estén asignados a ninguna oportunidad
- Toda eliminación pide confirmación y la decide el servidor: si el registro
  ya tiene historial responde 409 y la app muestra el motivo sin borrar nada

**Seguimiento**
- Registro de gestiones con tipo de acción, resultado y próxima acción
- Cambios de estado sujetos al perfil del usuario: el ejecutivo solo avanza en
  el embudo, y únicamente el supervisor puede devolver una oportunidad a una
  etapa anterior cuando se registró por error
- Autorrelleno del formulario con los datos de la gestión anterior, para las
  gestiones de seguimiento que repiten tipo de acción y contraparte
- Al cerrar una oportunidad no se exige próxima acción ni fecha de seguimiento:
  una gestión pendiente sobre algo cerrado reaparecería como atrasada
- Historial completo con la trazabilidad de cada cambio
- La gestión, el cambio de estado y la fecha de último contacto se guardan en
  una sola transacción
- Al registrar un envío de propuesta se sugiere el estado "Propuesta enviada"

**Preferencias**
- Selector de tema claro, oscuro o según el sistema, desde el perfil del usuario
- La preferencia se conserva entre ejecuciones de la aplicación

**Recursos del dispositivo**
- Captura de evidencias fotográficas con la cámara, mediante FileProvider
- Carga de evidencias desde la galería del dispositivo, copiando el archivo al
  almacenamiento privado de la aplicación
- Vista previa de las fotografías asociadas a cada oportunidad
- Agenda de eventos y recordatorios en el calendario del dispositivo
- Feriados de Chile en la agenda, obtenidos de una API externa: aviso del
  próximo feriado, días feriados marcados en la semana y advertencia en los
  eventos que caen en feriado (permisos del recinto y disponibilidad de personal)

## Diseño y experiencia de uso

El rediseño parte de cómo trabaja el equipo comercial, no solo del aspecto:

- **Inicio "Qué hacer hoy"**: lista de tareas (atrasadas, que vencen hoy,
  propuestas sin respuesta, oportunidades sin contacto) con la acción directa
  en cada tarjeta, y el embudo como una barra de colores
- **Agenda**: eventos por fecha, franja de la semana y aviso cuando hay eventos
  cercanos sin confirmar, para anticiparse
- **Cartera por etapas**: pestañas siempre visibles (Por contactar, En curso,
  Confirmadas, Cerradas), filtros en una hoja inferior y chips de filtros
  activos que se quitan con un toque
- **Detalle con siguiente paso**: indicador de avance por etapas, tarjeta con
  la próxima acción (llamar, registrar gestión) y pestañas Resumen, Gestiones
  y Contacto
- **Registrar gestión** eligiendo el tipo con íconos y la fecha del próximo
  seguimiento con atajos (mañana, en 3 días, en 1 semana)
- **Nueva oportunidad en 4 pasos** con barra de progreso y aviso del margen
  que queda hasta el evento
- **Panel del supervisor** que parte por lo que requiere su decisión, con
  confirmar o descartar en la misma tarjeta
- Barra inferior con botón "+" central para registrar rápido en terreno; el
  perfil se abre desde el avatar
- Tema oscuro azul noche con acento en degradado de menta a azul, y su
  equivalente claro; tipografía Plus Jakarta Sans incluida en `res/font`
  (licencia SIL Open Font License, ver `licencias/`), sin dependencias nuevas

## Arquitectura

Patrón **MVVM** sobre Jetpack Compose y Material Design 3:

```
                                         ┌→ DAO (Room) → SQLite                 lecturas y copia local
UI (Composables) → ViewModel (StateFlow) → Repository
                                         └→ AtenasApi (Retrofit) → atenas-backend   escrituras
```

La interfaz no toma decisiones: observa el estado que publica el ViewModel y le
informa los eventos del usuario. Las reglas de negocio viven en `domain`, sin
ninguna dependencia de Android, lo que permite probarlas con tests unitarios.

```
app/src/main/java/cl/duoc/atenas/
├── AtenasApp.kt          Application: construye el contenedor y siembra la base
├── MainActivity.kt       Única Activity del proyecto
├── core/                 Fechas, seguridad, sesión, archivos, calendario
│                         y preferencias de la aplicación
├── domain/
│   ├── model/            Enums del dominio
│   ├── rules/            Permisos por rol y máquina de estados
│   └── validation/       Validadores de formulario (funciones puras)
├── data/
│   ├── local/            Base de datos Room, entidades, DAOs y datos semilla
│   ├── remote/           Retrofit: endpoints del backend y DTO del JSON
│   └── repository/       Única puerta de acceso a datos para los ViewModel
├── di/                   Inyección de dependencias manual
└── ui/
    ├── theme/            Colores, tipografía, formas y espaciado centralizados
    ├── components/       Componentes compartidos entre pantallas
    ├── navigation/       Rutas y grafo de navegación
    └── <módulo>/         Una carpeta por módulo: Screen + ViewModel
```

## Modelo de datos

Ocho entidades en tercera forma normal:

| Entidad | Rol | Relaciones |
|---|---|---|
| `usuario` | cuentas con perfil y zona | 1-N oportunidad, 1-N seguimiento |
| `region` | catálogo | 1-N comuna |
| `comuna` | catálogo | 1-N organizacion, 1-N oportunidad |
| `organizacion` | empresa, municipalidad o institución | 1-N contacto, 1-N oportunidad |
| `contacto` | persona de contacto | N-1 organizacion |
| `oportunidad` | evento y su gestión comercial | FK a usuario, organizacion, contacto y comuna |
| `seguimiento` | historial de gestiones | N-1 oportunidad, N-1 usuario |
| `evidencia` | fotografías del evento | N-1 oportunidad |

La región **no** se almacena en `organizacion` ni en `oportunidad`: depende de
la comuna, y duplicarla introduciría una dependencia transitiva que rompe la
tercera forma normal. Por eso existe el catálogo `region` → `comuna`.

Las fotografías se guardan como archivos en el almacenamiento privado de la
aplicación; en la base solo se registra su URI.

## Cuentas de prueba

| Usuario | Clave | Perfil | Zona |
|---|---|---|---|
| `cvaldes` | `atenas123` | Ejecutivo comercial | Región Metropolitana |
| `rfuentes` | `atenas123` | Ejecutivo comercial | Valparaíso |
| `psoto` | `atenas123` | Ejecutivo comercial | Biobío |
| `msilva` | `atenas123` | Supervisor comercial | Nacional |

La base se crea y se siembra sola en el primer arranque, con 6 organizaciones,
8 contactos, 10 oportunidades repartidas en todos los estados del embudo y sus
gestiones asociadas.

## Requisitos

| Herramienta | Versión |
|---|---|
| Android Studio | Rabbit (2026.2.1) o superior |
| JDK | 21 (incluido en Android Studio) |
| Gradle | 9.5.0 (incluido en el wrapper) |
| Android Gradle Plugin | 9.0.1 |
| Kotlin | 2.2.10 |
| compileSdk | 36.1 · minSdk 24 · targetSdk 36 |

Dependencias principales: Jetpack Compose con Material 3, Navigation Compose,
Room 2.8.5 con KSP, Lifecycle ViewModel y Coil para la vista previa de imágenes.
La preferencia de tema se guarda con `SharedPreferences`, sin dependencias
adicionales.

## Cómo ejecutar

1. Clonar el repositorio.
2. Abrir la carpeta raíz en Android Studio (`File → Open`).
3. Esperar el *Gradle Sync*. No aceptar la propuesta de actualizar el AGP: el
   proyecto está fijado a la versión 9.0.1.
4. Seleccionar un emulador o un dispositivo físico con depuración USB activada.
5. Ejecutar la configuración `app`.
6. Iniciar sesión con cualquiera de las cuentas de prueba.

Para probar la captura de evidencias se requiere un dispositivo o emulador con
cámara, y conceder el permiso cuando la aplicación lo solicite. La carga desde
la galería funciona también sin cámara.

### Restablecer los datos de prueba

```
adb shell pm clear cl.duoc.atenas
```

Al volver a abrir la aplicación la base se siembra nuevamente desde cero.

## Pruebas unitarias y cobertura

Hay **260 pruebas unitarias** (JUnit 4, patrón Arrange–Act–Assert) que corren
en la JVM, sin emulador. Se agrupan en tres capas:

| Capa | Archivo | Pruebas | Qué verifica |
|---|---|---|---|
| Dominio | `domain/ValidadoresTest` | 21 | Validaciones de formulario y coherencia entre campos |
| | `domain/TransicionesEstadoTest` | 18 | Máquina de estados: avances por perfil, retrocesos del supervisor, cierre |
| | `domain/SugerenciaPrioridadTest` | 7 | Prioridad sugerida según potencial y afinidad |
| | `domain/ReglasAuditoriaTest` | 8 | Reglas agregadas tras la auditoría funcional |
| | `domain/GuiaGestionTest` | 13 | Siguiente paso, etapas, conversión, vencimientos |
| | `domain/ReglasComplementariasTest` | 10 | Casos de borde: cada rama de los `when` y valores límite |
| | `core/FechasTest` | 5 | Conversión de fechas entre el calendario (UTC) y la hora local |
| Datos | `data/RepositoriosTest` | 25 | Traducción de filtros a la base, rangos de fechas, autenticación, transacción de gestiones, sesión |
| | `data/FeriadosRepositoryTest` | 4 | API externa: fecha a medianoche local, solo feriados nacionales, una descarga por año, falla de red sin romper la agenda |
| | `data/ServidorTest` | 13 | Login con y sin conexión, sincronización que conserva la clave local, mensajes de error del servidor, escrituras rechazadas que no tocan la copia local |
| Pantallas | `ui/LoginViewModelTest` | 10 | Validación de campos y credenciales; descarga de la cartera al entrar, con y sin conexión |
| | `ui/DashboardEjecutivoViewModelTest` | 7 | "Qué hacer hoy": orden por urgencia, sin repetidos, embudo |
| | `ui/PanelSupervisorViewModelTest` | 7 | Métricas del equipo, conversión, confirmar y descartar, decisión rechazada por el servidor |
| | `ui/AgendaViewModelTest` | 7 | Agrupación por cercanía, eventos en riesgo, alcance por perfil, eventos en feriado y próximo feriado |
| | `ui/ListaOportunidadesViewModelTest` | 10 | Pestañas por etapa, cada filtro, región y comuna |
| | `ui/DetallesViewModelTest` | 18 | Acceso por perfil al detalle, asignación (y su error), eliminación de oportunidades y organizaciones (permiso, historial, rechazo del servidor), ficha y listado de organizaciones |
| | `ui/FormularioOportunidadViewModelTest` | 30 | Pasos, validaciones, prioridad sugerida, organizaciones y contactos registrados desde el paso 3, crear, editar, oportunidad cerrada, errores de la base y del servidor |
| | `ui/RegistrarSeguimientoViewModelTest` | 21 | Transiciones por perfil, próxima acción, cierre, reutilizar gestión, errores |
| | `ui/AlertasYPropuestasViewModelTest` | 6 | Alertas de la campana por perfil y filtro de propuestas |
| | `ui/FormulariosOrganizacionViewModelTest` | 20 | Alta, edición y eliminación de organizaciones y contactos |

Los ViewModel se prueban con **DAO falsos en memoria**
(`app/src/test/.../prueba/DaosFalsos.kt`) y un **servidor falso**
(`ApiFalsa.kt`): reemplazan a Room y a Retrofit, permiten fijar exactamente
qué datos ve la pantalla y simular que la base o el servidor rechazan una
escritura, o que no hay conexión. `ReglaCorrutinas` reemplaza el hilo principal de Android por un
despachador de prueba (`kotlinx-coroutines-test`) para que cada prueba sea
determinista.

### Cómo ejecutarlas

En Android Studio: clic derecho sobre la carpeta `app/src/test` → *Run tests*.
Por consola (Windows):

```
gradlew.bat :app:testDebugUnitTest
```

### Reporte de cobertura (Kover)

```
gradlew.bat :app:koverHtmlReportDebug
```

El reporte queda en `app/build/reports/kover/htmlDebug/index.html`. Para que
la compilación falle si la cobertura de ramas baja del 80 %:

```
gradlew.bat :app:koverVerifyDebug
```

**Qué se mide y por qué.** El reporte incluye solo el código que contiene
lógica de negocio (configurado en `app/build.gradle.kts`, bloque `kover`):

- `domain` completo: validaciones, permisos, máquina de estados y reglas.
- `core.Fechas` y `core.SesionManager`.
- `data.repository`: la traducción entre el dominio, la base y el servidor
  (qué hacer sin conexión, cómo mostrar un error del servidor).
- Los ViewModel de `ui`: deciden qué ve cada perfil y qué se puede guardar.

Queda fuera, a propósito, lo que no tiene decisiones que probar o que solo se
puede verificar en un dispositivo: `MainActivity`, `AtenasApp`, las pantallas
Compose, el tema, la navegación, la base Room (`data.local`, que Room genera),
los datos semilla, la inyección de dependencias (`di`, donde viven las
fábricas de ViewModel), la configuración de Retrofit y los DTO (`data.remote`,
que solo describen la forma del JSON), cámara, calendario y preferencias. También se excluyen
`PerfilViewModel` y `EvidenciasViewModel`, que solo reenvían llamadas sin
tomar decisiones, y `CriteriosBusqueda`, que es solo un contenedor de datos.

## Conexión con el backend

La app consume el microservicio
[atenas-backend](https://github.com/DiegoAlarcon6520/atenas-backend)
(Spring Boot + MySQL) con Retrofit.

- **El servidor es la fuente de verdad.** Crear, editar, asignar y registrar
  gestiones va primero al servidor, que vuelve a aplicar las reglas de
  negocio. Lo que confirma se guarda en Room.
- **Room es la copia para trabajar sin conexión.** Las pantallas leen siempre
  de Room, así que la cartera, la agenda y las alertas funcionan sin red.
- **Al iniciar sesión** se valida la clave en el servidor y se descarga la
  cartera (`GET /api/sincronizacion`). El ejecutivo recibe solo la suya; el
  supervisor, la de todo el equipo.
- **Sin conexión** se puede entrar con una cuenta que ya inició sesión en ese
  teléfono y consultar los datos. Las escrituras muestran "Sin conexión con
  el servidor" y no cambian nada.

Para probarla: levanta atenas-backend en IntelliJ (con MySQL de XAMPP) y
ejecuta la app en el emulador. La dirección del servidor está en
`data/remote/RetrofitClient.kt` (`http://10.0.2.2:8080/api/`, que en el
emulador apunta al PC). Para un celular físico, usa la IP del PC y agrégala
en `res/xml/network_security_config.xml`.

### Endpoints propios que usa la app

| Método | Ruta | Uso en la app |
|---|---|---|
| POST | `/api/auth/login` | Inicio de sesión |
| GET | `/api/sincronizacion?usuarioId=` | Descarga de la cartera al entrar |
| POST / PUT / DELETE | `/api/organizaciones[/{id}]` | Crear, editar y eliminar organizaciones |
| POST / PUT / DELETE | `/api/contactos[/{id}]` | Crear, editar y eliminar contactos |
| POST / PUT / DELETE | `/api/oportunidades[/{id}]` | Crear, editar y eliminar oportunidades |
| PATCH | `/api/oportunidades/{id}/responsable` | Asignar responsable |
| POST | `/api/oportunidades/{id}/seguimientos` | Registrar gestión y cambio de estado |
| POST / DELETE | `/api/evidencias[/{id}]` | Agregar y quitar evidencias |

La documentación completa (Swagger) está en el README de atenas-backend.

### API externa: feriados de Chile

La agenda consulta la API pública [Nager.Date](https://date.nager.at), que no
requiere clave:

```
GET https://date.nager.at/api/v3/PublicHolidays/{año}/CL
```

- **Para qué sirve:** una activación en feriado cambia la logística (recintos
  cerrados, personal con recargo, menos público en oficinas), así que la
  agenda avisa el próximo feriado y marca los eventos que caen en uno.
- **Implementación:** segundo cliente Retrofit en `RetrofitClient.feriados`
  (`data/remote/FeriadosApi.kt`) y `data/repository/FeriadosRepository.kt`.
  Se piden el año en curso y el siguiente, se usan solo los feriados
  nacionales (`global`) y cada año se descarga una vez por sesión.
- **Sin conexión:** si la API no responde, la agenda funciona igual, sin
  feriados, y se vuelve a intentar la próxima vez que se abre.

## Firma del APK release

El APK de entrega se firma con una **clave exclusivamente de demostración
académica**: sus contraseñas no se usan en ningún otro lado y no protegen
nada real. Aun así, ni el `.jks` ni sus contraseñas están en el repositorio;
se entregan aparte.

**Cómo está configurado** (`app/build.gradle.kts`, bloque `signingConfigs`):
Gradle lee la ruta del `.jks`, el alias y las contraseñas desde
`keystore.properties`, en la raíz del proyecto. Ese archivo y los `.jks`
están en `.gitignore`. Si no existe, el release se genera sin firmar y el
resto del proyecto compila igual, así que clonar el repo no requiere la clave.

**Para generar el APK firmado:**

1. Copia `keystore.properties.example` como `keystore.properties`.
2. Deja el `.jks` de demostración en `keystore/atenas-demo.jks` (o ajusta
   `storeFile`) y completa alias y contraseñas.
3. Ejecuta:

   ```
   .\gradlew.bat :app:assembleRelease
   ```

   El APK queda en `app/build/outputs/apk/release/app-release.apk`.
4. Para comprobar la firma (con las build-tools del SDK):

   ```
   apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
   ```

También se puede generar desde Android Studio con *Build → Generate Signed
App Bundle or APK*, eligiendo el mismo `.jks`.

**Versiones.** Cada APK publicado sube `versionCode` (entero que Android usa
para saber si es una actualización; no deja instalar uno menor encima de uno
mayor) y actualiza `versionName` (el texto que ve el usuario). La entrega
actual es la **1.1 (código 2)**.

**Descarga.** El APK firmado se publica en los
[Releases del repositorio](https://github.com/DiegoAlarcon6520/atenas/releases),
no dentro del código: así no engorda el historial de Git y cada APK queda
asociado a la versión exacta del código que lo generó.

## Nota sobre los datos

Todos los nombres de organizaciones, personas, correos, teléfonos, direcciones,
costos y resultados de activación son inventados para efectos académicos. La
aplicación solo se conecta a su propio backend y a la API pública de feriados,
que no recibe ningún dato de la app (solo el año consultado).
