# Atenas — Gestión móvil de oportunidades y activaciones corporativas

Aplicación Android desarrollada para la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105)**,
Duoc UC Sede Puente Alto, a partir del caso de **Agencia Atenas**.

## Integrantes

- Diego Alarcón
- Ignacio (completar apellido)

## Descripción

MVP que permite al equipo comercial de la agencia centralizar la detección, priorización
y seguimiento de oportunidades de activación en eventos corporativos, ferias empresariales,
actividades municipales y jornadas de beneficios.

Todos los datos de organizaciones, contactos, usuarios y eventos son **ficticios**.

## Arquitectura

MVVM sobre Jetpack Compose y Material Design 3:

```
UI (Composables) -> ViewModel (StateFlow) -> Repository -> DAO (Room) -> SQLite
```

- `domain/model` — enums y reglas de negocio, sin dependencias de Android.
- `data/local` — entidades Room, DAOs, convertidores de tipo y datos semilla.
- `data/repository` — única puerta de entrada a los datos desde los ViewModel.
- `di/AppContainer` — inyección de dependencias manual.
- `ui/theme` — colores, tipografía, espaciado y colores semánticos centralizados.

### Modelo de datos

Ocho entidades en tercera forma normal: `usuario`, `region`, `comuna`, `organizacion`,
`contacto`, `oportunidad`, `seguimiento`, `evidencia`.

La región no se almacena en `organizacion` ni en `oportunidad`: depende de la comuna,
y guardarla en ambas tablas introduciría una dependencia transitiva.

## Cuentas de prueba

| Usuario | Clave | Rol |
|---|---|---|
| `cvaldes` | `atenas123` | Ejecutivo comercial |
| `rfuentes` | `atenas123` | Ejecutivo comercial |
| `psoto` | `atenas123` | Ejecutivo comercial |
| `msilva` | `atenas123` | Supervisor comercial |

## Requisitos de ejecución

| Herramienta | Versión |
|---|---|
| Android Studio | Rabbit 1 (2026.2.1) o superior |
| JDK | 21 (JBR embebido en Android Studio) |
| Gradle | 9.5.0 (incluido en el wrapper) |
| Android Gradle Plugin | 9.0.1 |
| Kotlin | 2.2.10 |
| compileSdk | 36.1 · minSdk 24 · targetSdk 36 |

## Cómo ejecutar

1. Clonar el repositorio.
2. Abrir la carpeta raíz en Android Studio (`File → Open`).
3. Esperar el *Gradle Sync*. No aceptar la propuesta de actualizar el AGP.
4. Seleccionar un emulador o un dispositivo con depuración USB activada.
5. Ejecutar la configuración `app`.

La base de datos se crea y se siembra con datos ficticios en el primer arranque.
