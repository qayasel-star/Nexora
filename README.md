# NEXORA — Gestión inteligente de soporte técnico

Proyecto Android con Kotlin y Jetpack Compose.

- Nombre visible: **Nexora**
- Eslogan: **Cada incidencia bajo control**
- `applicationId`: `com.registroinformatico.app` (conservado)
- Versión fuente: `2.0-beta07` (`versionCode = 9`)

## Funciones incluidas en esta fuente

- Nombre visible Nexora configurado en `app/src/main/res/values/strings.xml` y en la bienvenida.
- Icono del lanzador en los recursos `mipmap-*`.
- Banner en `app/src/main/res/drawable-nodpi/nexora_banner.png`, referenciado desde la pantalla principal.
- Módulo Reportes: ID automático con prefijo `NX-`, departamento/unidad, técnico (Yasel, Getma, Naybet, Yanelis, Selene), prioridad, descripción del problema, diagnóstico y observaciones. El formulario no solicita Tipo de trabajo ni Detalles técnicos.
- Cálculo de tiempo transcurrido y duración de resolución usando apertura y cierre.
- Análisis con períodos de 1, 2, 3 y 6 meses, 1 año e historial completo; compara con el período anterior. Los reportes resueltos se agrupan por fecha de cierre y los no resueltos por fecha de apertura.
- Recordatorios de notificación con WorkManager, cada dos horas, de lunes a sábado, dentro de 08:30–16:30. Android puede aplazar tareas por ahorro de batería; el permiso de notificaciones debe estar autorizado.
- El archivo de workflow `.github/workflows/main.yml` compila con Java 17 y Gradle 8.9 y publica el artefacto `NEXORA-APK-v2.0-beta07`.

## Crear un repositorio nuevo desde este ZIP

1. Descarga y descomprime el ZIP en tu teléfono o computadora.
2. Sube **el contenido descomprimido**, no el ZIP entero, a la raíz del repositorio. Deben verse `settings.gradle.kts`, `build.gradle.kts`, `app/` y `.github/` directamente en la página principal del repositorio.
3. Haz commit en la rama `main`.
4. Abre **Actions → Compilar Nexora APK** y espera que termine en verde.
5. Abre la ejecución correcta y descarga el artefacto `NEXORA-APK-v2.0-beta07`.

## Compilar desde una terminal con Gradle instalado

Desde la carpeta raíz del proyecto:

```bash
gradle --no-daemon clean assembleDebug
```

El APK debug se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Importante: datos y firma

La aplicación conserva `applicationId` para mantener la identidad del paquete, pero una actualización sobre una instalación existente también requiere una firma compatible. No desinstales la versión que contiene tus reportes sin exportar antes una copia de seguridad. Este workflow genera una APK **debug**; no configura una clave de firma release permanente.

La integridad del ZIP no demuestra por sí sola que compile: la compilación debe confirmarse en GitHub Actions.
