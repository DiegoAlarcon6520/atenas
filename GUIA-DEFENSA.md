# Guía interna del proyecto Atenas

Documento de apoyo para la defensa y el live coding. No es parte del entregable
funcional: es el mapa para moverse rápido por el código.

---

## 1. Mapa del proyecto

```
app/src/main/java/cl/duoc/atenas/
│
├── AtenasApp.kt          Application. Crea el contenedor y siembra la base.
├── MainActivity.kt       Única Activity. Aplica el tema y llama a la navegación.
│
├── core/                 Utilidades transversales
│   ├── Fechas.kt         Formateo y cálculo de fechas (Calendar, no java.time)
│   ├── Seguridad.kt      Hash SHA-256 de las claves
│   └── SesionManager.kt  Usuario autenticado actual (StateFlow)
│
├── domain/               Reglas de negocio, sin nada de Android
│   ├── model/Enums.kt            Roles, estados, prioridades, tipos
│   ├── rules/Permisos.kt         Quién puede hacer qué
│   └── validation/Validadores.kt Validaciones de formulario (funciones puras)
│
├── data/                 Acceso a datos
│   ├── local/
│   │   ├── AtenasDatabase.kt   Declara las 8 tablas y la versión del esquema
│   │   ├── Convertidores.kt    Enum <-> TEXT para SQLite
│   │   ├── entity/Entidades.kt Las 8 entidades con sus claves y relaciones
│   │   ├── dao/                Consultas SQL
│   │   └── seed/               Datos ficticios iniciales
│   └── repository/             Puerta de entrada a los datos para los ViewModel
│
├── di/AppContainer.kt    Inyección de dependencias manual
│
└── ui/
    ├── theme/            Colores, tipografía, formas y espaciado
    ├── components/       Piezas reutilizables entre pantallas
    ├── navigation/       Rutas y grafo de navegación
    ├── login/  dashboard/  supervisor/  oportunidades/  perfil/
                          Una carpeta por módulo, cada una con Screen + ViewModel
```

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

## 3. Recetas para el live coding

### Cambiar el color de un botón

Los botones toman su color del tema, no de la pantalla.

1. Abre `ui/theme/Color.kt`
2. Cambia `AzulPrimario` (modo claro) y/o `AzulPrimarioOscuro` (modo oscuro)
3. Cambia el valor hexadecimal y vuelve a ejecutar

Eso afecta a todos los botones, la barra superior y los acentos a la vez.
Si te piden cambiar **un solo** botón, se hace en esa pantalla:

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

### Cambiar un texto visible

Los textos están en los Composables, junto al elemento que los muestra. El
nombre de la app está en `res/values/strings.xml`.

### Agregar un campo nuevo al formulario de oportunidad

Cuatro pasos, en este orden:

1. `data/local/entity/Entidades.kt` → agrega la propiedad a `Oportunidad`
2. `data/local/AtenasDatabase.kt` → sube `version = 1` a `version = 2`
3. `ui/oportunidades/FormularioOportunidadViewModel.kt` → agrega el campo al
   `UiState`, su handler `alCambiar...`, su validación en `validar()` y su
   valor en `guardar()`
4. `ui/oportunidades/FormularioOportunidadScreen.kt` → agrega el `CampoTexto`

### Agregar una validación

`domain/validation/Validadores.kt` → escribe la función nueva.
Después llámala desde el `validar()` del ViewModel correspondiente.

### Cambiar el espaciado o el tamaño de letra

`ui/theme/Espaciado.kt` para los márgenes, `ui/theme/Type.kt` para la
tipografía, `ui/theme/Formas.kt` para los radios de esquina.

### Agregar una pantalla nueva

1. Crea la carpeta en `ui/` con `MiPantallaScreen.kt` y `MiPantallaViewModel.kt`
2. Agrega la constante en `ui/navigation/Rutas.kt`
3. Agrega el `composable(...)` en `ui/navigation/NavegacionRaiz.kt`
4. Si va en la barra inferior, agrégala a `itemsDeNavegacion()`

---

## 4. Preguntas probables y cómo responderlas

**¿Por qué MVVM y no poner todo en el Composable?**
Porque el estado sobrevive a la rotación de pantalla, la lógica se puede probar
sin levantar la interfaz, y la misma pantalla sirve para dos roles cambiando
solo un parámetro del ViewModel.

**¿Por qué la región no está en la tabla oportunidad?**
Porque la región depende de la comuna. Si guardara ambas, la región dependería
transitivamente de la clave a través de la comuna, y eso rompe la tercera forma
normal. Por eso existe el catálogo `region` → `comuna`.

**¿Por qué el color dinámico de Android 12 está desactivado?**
Porque los estados de la gestión se comunican con colores semánticos propios
(verde confirmado, rojo descartado, ámbar en espera). El color dinámico del
sistema los reemplazaría y se perdería el significado.

**¿Dónde está el control de acceso?**
En `domain/rules/Permisos.kt`. La UI solo consulta. Además el ViewModel del
detalle verifica que un ejecutivo no pueda abrir una oportunidad que no le fue
asignada, así que la restricción no depende de ocultar un botón.

**¿Por qué las claves se guardan hasheadas si son datos ficticios?**
Porque guardar contraseñas en texto plano es un error que no se justifica ni en
un MVP, y el costo de hacerlo bien era una función de cinco líneas.

**¿Por qué las fechas se guardan como Long?**
SQLite no tiene tipo fecha. Se guardan los milisegundos desde 1970 y se
formatean al mostrarlas, en `core/Fechas.kt`. Se usa `Calendar` y no
`java.time` porque el `minSdk` del proyecto es 24 y `java.time` exige API 26.

**¿Qué pasa si cambias el esquema de la base?**
Hay que subir el número de versión. El proyecto está configurado con
`fallbackToDestructiveMigration`, que recrea la base y vuelve a sembrarla: es
aceptable en un MVP académico, pero en producción se escribiría una migración.

**¿Dónde están las animaciones?**
Cuatro mecanismos distintos:
- `Crossfade` en `ui/navigation/NavegacionRaiz.kt` (login ↔ aplicación)
- `AnimatedVisibility` en el login, el panel de filtros y los avisos de error
- `animateFloatAsState` en las barras de avance del tablero
- `animateContentSize` en las secciones plegables del detalle
