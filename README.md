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
  prioridad y tipo
- Ficha de detalle con todos los antecedentes y el historial de gestiones
- Formulario de creación y edición con validaciones y reglas de negocio
- Asignación y reasignación de responsables, exclusiva del supervisor
- Leyendas explicativas en cada indicador de los tableros, para que el número
  deje claro qué está contando

**Organizaciones y contactos**
- Catálogo de organizaciones con búsqueda y filtro por tipo
- Ficha con sus contactos y las oportunidades gestionadas con ella
- Formularios de organización y de contacto, con validación de correo y teléfono

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

**Preferencias**
- Selector de tema claro, oscuro o según el sistema, desde el perfil del usuario
- La preferencia se conserva entre ejecuciones de la aplicación

**Recursos del dispositivo**
- Captura de evidencias fotográficas con la cámara, mediante FileProvider
- Carga de evidencias desde la galería del dispositivo, copiando el archivo al
  almacenamiento privado de la aplicación
- Vista previa de las fotografías asociadas a cada oportunidad
- Agenda de eventos y recordatorios en el calendario del dispositivo

## Arquitectura

Patrón **MVVM** sobre Jetpack Compose y Material Design 3:

```
UI (Composables) → ViewModel (StateFlow) → Repository → DAO (Room) → SQLite
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

## Pruebas unitarias

El proyecto incluye pruebas de las reglas de negocio, en
`app/src/test/java/cl/duoc/atenas/domain/`:

- `ValidadoresTest` — 21 pruebas de las validaciones de formulario y de las
  reglas de coherencia entre campos
- `TransicionesEstadoTest` — 18 pruebas de la máquina de estados: qué avances
  habilita cada perfil, qué retrocesos permite el supervisor, qué oportunidades
  ya no se pueden reabrir y cuáles son los estados de cierre

Se ejecutan sin emulador, porque ninguna de esas clases depende de Android.

## Nota sobre los datos

Todos los nombres de organizaciones, personas, correos, teléfonos, direcciones,
costos y resultados de activación son inventados para efectos académicos. La
aplicación no se conecta a ningún sistema externo ni a bases de datos reales.
