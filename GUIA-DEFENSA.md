# Guía interna del proyecto Atenas

Documento de apoyo para la defensa y el live coding. No es parte del entregable
funcional: es el mapa para moverse rápido por el código.

---

## 1. Mapa del proyecto

```
app/src/main/
│
├── AndroidManifest.xml       Permiso de cámara y declaración del FileProvider
├── res/xml/rutas_archivos.xml  Carpetas que el FileProvider puede compartir
├── res/drawable/ic_launcher_*  Ícono adaptativo de la aplicación
│
└── java/cl/duoc/atenas/
    │
    ├── AtenasApp.kt          Application. Crea el contenedor y siembra la base.
    ├── MainActivity.kt       Única Activity. Aplica el tema y llama a la navegación.
    │
    ├── core/                 Utilidades transversales
    │   ├── Fechas.kt         Fechas y horas (Calendar, no java.time)
    │   ├── Seguridad.kt      Hash SHA-256 de las claves
    │   ├── SesionManager.kt  Usuario autenticado actual (StateFlow)
    │   ├── ArchivosImagen.kt Archivos de las fotos, sus URI y la copia
    │   │                     de las imágenes traídas desde la galería
    │   ├── CalendarioApp.kt  Intents hacia el calendario del dispositivo
    │   └── PreferenciasApp.kt Preferencia de tema (SharedPreferences)
    │
    ├── domain/               Reglas de negocio, sin nada de Android
    │   ├── model/Enums.kt              Roles, estados, prioridades, tipos
    │   ├── rules/Permisos.kt           Quién puede hacer qué
    │   ├── rules/TransicionesEstado.kt Embudo de estados y quién puede moverlo
    │   └── validation/Validadores.kt   Validaciones (funciones puras)
    │
    ├── data/
    │   ├── local/
    │   │   ├── AtenasDatabase.kt   Las 8 tablas y la versión del esquema
    │   │   ├── Convertidores.kt    Enum <-> TEXT para SQLite
    │   │   ├── entity/Entidades.kt Las 8 entidades con claves y relaciones
    │   │   ├── dao/                7 DAOs con las consultas SQL
    │   │   └── seed/               Datos ficticios iniciales
    │   └── repository/             7 repositorios: puerta de entrada a los datos
    │
    ├── di/                   AppContainer y acceso desde las fábricas
    │
    └── ui/
        ├── theme/            Color, Type, Formas, Espaciado, ColoresSemanticos
        ├── components/       Etiquetas, tarjetas, campos de formulario
        ├── navigation/       Rutas.kt y NavegacionRaiz.kt
        ├── login/            Autenticación
        ├── dashboard/        Tablero del ejecutivo
        ├── supervisor/       Panel del equipo
        ├── oportunidades/    Listado, detalle y formulario
        ├── organizaciones/   Listado, ficha y formularios de contacto
        ├── seguimiento/      Registro de gestiones
        ├── evidencias/       Cámara y fotografías
        └── perfil/           Datos del usuario y cierre de sesión

app/src/test/java/cl/duoc/atenas/domain/   Pruebas unitarias
```

**13 pantallas · 14 ViewModels · 8 entidades · 7 DAOs · 7 repositorios · 39 pruebas unitarias**

---

## 2. Cómo fluyen los datos

```
Usuario toca algo
      ↓
Screen (Composable)  ── llama una función del ──►  ViewModel
      ▲                                                 │
      │ observa el StateFlow                            │ pide datos
      │                                                 ▼
      └──────────── UiState ◄──── combine() ◄──── Repository
                                                        │
                                                        ▼
                                                  DAO (Room) ──► SQLite
```

Regla que se repite en todo el proyecto: **la pantalla nunca decide nada**.
Solo dibuja el `UiState` que recibe y avisa los eventos. Toda la lógica está en
el ViewModel, y toda la regla de negocio en `domain/`.

---

## 3. Dónde está cada requisito del caso

| Requisito | Archivos |
|---|---|
| Login y roles | `ui/login/`, `core/SesionManager.kt`, `domain/rules/Permisos.kt` |
| Pantallas según rol | `ui/navigation/Rutas.kt` (itemsDeNavegacion) y `NavegacionRaiz.kt` |
| Búsqueda y filtros | `data/local/dao/OportunidadDao.kt` (consulta `buscar`) |
| Validaciones | `domain/validation/Validadores.kt` y el `validar()` de cada ViewModel |
| Estados y transiciones | `domain/rules/TransicionesEstado.kt` |
| Retroceso de etapa | `TransicionesEstado.anterioresPosibles()` + `Permisos.puedeRetrocederEstado()` |
| Galería | `core/ArchivosImagen.kt` (`copiarDesdeGaleria`), `ui/evidencias/` |
| Tema claro/oscuro | `core/PreferenciasApp.kt`, `MainActivity.kt`, `ui/perfil/` |
| Asignar responsable | `ui/oportunidades/DetalleOportunidadViewModel.kt` |
| Cámara y fotos | `core/ArchivosImagen.kt`, `ui/evidencias/`, manifest y `rutas_archivos.xml` |
| Calendario | `core/CalendarioApp.kt`, botones en `DetalleOportunidadScreen.kt` |
| Colores de estado | `ui/theme/ColoresSemanticos.kt` |
| Animaciones | ver la última pregunta de la sección 5 |

---

## 4. Recetas para el live coding

### Cambiar el color de un botón

Los botones toman su color del tema, no de la pantalla.

1. Abre `ui/theme/Color.kt`
2. Cambia `AzulPrimario` (modo claro) o `AzulPrimarioOscuro` (modo oscuro)
3. Vuelve a ejecutar

Eso afecta a todos los botones y acentos a la vez. Si te piden cambiar **un
solo** botón, se hace en esa pantalla:

```kotlin
Button(
    onClick = { },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.tertiary
    )
) { Text("Guardar") }
```

### Cambiar el color de un estado de oportunidad

`ui/theme/ColoresSemanticos.kt` → función `colorDeEstado()`. Ahí está el `when`
que asigna un color a cada estado. Cambiar una línea actualiza las etiquetas en
el tablero, el listado, el detalle y el panel del supervisor.

### Cambiar el fondo de las tarjetas

`ui/theme/Color.kt` → los valores `Superficie*`. Material 3 usa esos roles para
las tarjetas, la barra inferior y los menús.

### Cambiar un texto visible

Los textos están en el Composable que los muestra. El nombre de la aplicación
está en `res/values/strings.xml`.

### Agregar un campo nuevo al formulario de oportunidad

Cuatro pasos, en este orden:

1. `data/local/entity/Entidades.kt` → agrega la propiedad a `Oportunidad`
2. `data/local/AtenasDatabase.kt` → sube el número de `version`
3. `ui/oportunidades/FormularioOportunidadViewModel.kt` → agrega el campo al
   `UiState`, su handler `alCambiar...`, su validación en `validar()` y su
   valor en `guardar()`
4. `ui/oportunidades/FormularioOportunidadScreen.kt` → agrega el `CampoTexto`

### Agregar una validación

`domain/validation/Validadores.kt` → escribe la función nueva y llámala desde
el `validar()` del ViewModel correspondiente.

### Agregar un estado nuevo al embudo

1. `domain/model/Enums.kt` → agrégalo a `EstadoOportunidad` con su etiqueta y orden
2. `domain/rules/TransicionesEstado.kt` → declara desde dónde se llega a él
3. `ui/theme/ColoresSemanticos.kt` → asígnale un color en el `when`

### Cambiar el tema que se aplica al abrir la aplicación

`core/PreferenciasApp.kt` → el valor por omisión de `leerModoTema()`
(`ModoTema.SISTEMA`). `MainActivity.kt` observa ese `StateFlow` y decide si pasa
`darkTheme = true` o `false` al tema; el selector del perfil llama a
`cambiarModoTema()`.

### Agregar una preferencia nueva

1. `core/PreferenciasApp.kt` → una clave, un `MutableStateFlow`, su lectura
   inicial y un `cambiar...()` que escribe en `SharedPreferences` y actualiza el
   flow
2. `ui/perfil/PerfilViewModel.kt` → expónla en el `UiState`
3. `ui/perfil/PerfilScreen.kt` → el control que la cambia

### Cambiar espaciado, tipografía o esquinas

`ui/theme/Espaciado.kt`, `ui/theme/Type.kt`, `ui/theme/Formas.kt`.

### Agregar una pantalla nueva

1. Crea la carpeta en `ui/` con `MiPantallaScreen.kt` y `MiPantallaViewModel.kt`
2. Agrega la constante en `ui/navigation/Rutas.kt`
3. Agrega el `composable(...)` en `ui/navigation/NavegacionRaiz.kt`
4. Si va en la barra inferior, agrégala a `itemsDeNavegacion()`

---

## 5. Preguntas probables y cómo responderlas

**¿Por qué MVVM y no poner todo en el Composable?**
Porque el estado sobrevive a la rotación de pantalla, la lógica se puede probar
sin levantar la interfaz, y la misma pantalla sirve para dos roles cambiando
solo un parámetro del ViewModel.

**¿Por qué la región no está en la tabla oportunidad?**
Porque la región depende de la comuna. Si guardara ambas, la región dependería
transitivamente de la clave a través de la comuna, y eso rompe la tercera forma
normal. Por eso existe el catálogo `region` → `comuna`.

**¿Dónde está el control de acceso?**
En `domain/rules/Permisos.kt` y `domain/rules/TransicionesEstado.kt`. La UI solo
consulta. Además el ViewModel del detalle verifica que un ejecutivo no pueda
abrir una oportunidad que no le fue asignada, y el de seguimiento vuelve a
validar la transición antes de guardar. La restricción no depende de esconder
un botón, y hay pruebas unitarias que lo demuestran.

**¿Por qué el color dinámico de Android 12 está desactivado?**
Porque los estados de la gestión se comunican con colores semánticos propios
(verde confirmado, rojo descartado, ámbar en espera). El color dinámico del
sistema los reemplazaría y se perdería el significado.

**¿Por qué las claves se guardan hasheadas si son datos ficticios?**
Porque guardar contraseñas en texto plano es un error que no se justifica ni en
un MVP, y el costo de hacerlo bien era una función de cinco líneas.

**¿Por qué las fechas se guardan como Long y las horas como Int?**
SQLite no tiene tipo fecha. Las fechas son milisegundos desde 1970; las horas
del día son minutos desde medianoche (600 = 10:00). Un entero se ordena, se
compara y se valida directo, y permite combinar fecha y hora para crear el
evento en el calendario. Se usa `Calendar` y no `java.time` porque el `minSdk`
es 24 y `java.time` exige API 26.

**¿Por qué las fotos no se guardan en la base de datos?**
Porque meter imágenes en SQLite la hace crecer sin control y vuelve lentas
todas las consultas. El archivo vive en el almacenamiento privado de la
aplicación y en la tabla solo se guarda su URI.

**¿Qué es el FileProvider y por qué hace falta?**
Desde Android 7 está prohibido pasar rutas de archivo (`file://`) entre
aplicaciones: lanza `FileUriExposedException`. El FileProvider genera una URI
`content://` con permiso acotado solo a esa foto y solo mientras dura la
operación. Está declarado en el manifest y su alcance en `rutas_archivos.xml`.

**¿Por qué el calendario se abre con un Intent en vez de escribir directo?**
Escribir directo exigiría los permisos `READ_CALENDAR` y `WRITE_CALENDAR` y
dejaría eventos en el teléfono del usuario sin que él los vea. El Intent no
necesita ningún permiso y es el usuario quien decide guardar.

**¿Por qué usaron Coil en vez de cargar la imagen a mano?**
Las fotos de cámara traen orientación EXIF y vienen a resolución completa:
cargarlas enteras en una miniatura puede agotar la memoria. Coil resuelve la
rotación, el submuestreo y el cacheo. Hacerlo a mano son unas 40 líneas
frágiles.

**¿Qué es `android.disallowKotlinSourceSets=false` en gradle.properties?**
AGP 9 trae soporte Kotlin integrado y bloquea que un plugin registre fuentes
por `kotlin.sourceSets`. KSP, que es quien genera el código de Room, lo hace
así. La línea habilita esa compatibilidad; es la salida que el propio mensaje
de error de AGP propone.

**¿Qué pasa si cambian el esquema de la base?**
Hay que subir el número de versión. El proyecto usa
`fallbackToDestructiveMigration`, que recrea la base y la vuelve a sembrar: es
aceptable en un MVP académico, pero en producción se escribiría una migración
con `Migration(1, 2)`.

**¿Dónde están las animaciones?**
Cinco mecanismos distintos:
- `Crossfade` en `NavegacionRaiz.kt` (login ↔ aplicación)
- `AnimatedVisibility` en el login, el panel de filtros y los avisos de error
- `animateFloatAsState` en las barras de avance del tablero
- `animateContentSize` en las secciones plegables del detalle
- `scaleIn` en la aparición de las miniaturas de evidencia

**¿Por qué la imagen de la galería se copia en vez de guardar su URI?**
Porque la URI que entrega el selector es un préstamo: el permiso de lectura se
pierde al reiniciar la aplicación y el usuario puede borrar la foto original.
`copiarDesdeGaleria()` abre el `InputStream` y escribe una copia en el
almacenamiento privado, igual que las fotos de cámara. Así una evidencia nunca
queda apuntando a un archivo que ya no existe.

**¿Por qué la preferencia de tema usa SharedPreferences y no Room ni DataStore?**
Es un solo valor, de un solo usuario del teléfono, que hay que leer una vez al
arrancar y antes de dibujar la primera pantalla. Room implicaría una tabla, un
DAO y una consulta asíncrona para un `String`; DataStore sería una dependencia
nueva por el mismo dato. `SharedPreferences` lo lee sincrónicamente y se expone
como `StateFlow`, que es lo que Compose necesita para recomponer.

**¿Por qué solo el supervisor puede devolver una oportunidad a una etapa anterior?**
Porque retroceder reescribe el embudo, y el embudo es lo que mide el desempeño
del propio ejecutivo. Si el ejecutivo pudiera retroceder, podría deshacer sus
propios registros. El supervisor sí, porque su trabajo es corregir la cartera.
Está en `Permisos.puedeRetrocederEstado()`, y `TransicionesEstado` devuelve
lista vacía para el ejecutivo, con pruebas unitarias para ambos casos.

**¿Por qué una oportunidad cerrada no se puede reabrir?**
`anterioresPosibles(CERRADA, …)` devuelve lista vacía a propósito: el cierre es
el único estado terminal del embudo. `DESCARTADA` sí se puede reabrir, porque un
descarte suele ser una decisión de la contraparte que cambia, y una
`ACTIVACION_REALIZADA` marcada por error vuelve a `CONFIRMADA`.

**¿Por qué al cerrar una gestión no se pide próxima acción?**
Porque la próxima acción es lo que alimenta el panel de gestiones atrasadas. Si
se exigiera al cerrar, cada oportunidad terminada dejaría una gestión pendiente
que aparecería vencida para siempre. `TransicionesEstado.esEstadoDeCierre()`
identifica los tres estados de cierre, el ViewModel salta esas dos validaciones
y guarda `null`.

**¿Cómo se prueban las reglas de negocio?**
`app/src/test/java/cl/duoc/atenas/domain/` tiene 39 pruebas: `ValidadoresTest`
(21) y `TransicionesEstadoTest` (18). Corren sin emulador porque ninguna de esas
clases depende de Android: esa es la razón de haberlas dejado en `domain/`.
Se ejecutan con `./gradlew testDebugUnitTest`, o con clic derecho sobre la
carpeta `test` → *Run tests*.
