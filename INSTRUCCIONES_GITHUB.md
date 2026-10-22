# Instrucciones para crear el repositorio y compilar Nexora

## A. Subir el proyecto

Descomprime `Nexora-2.0-Beta07-Repositorio-Limpio.zip`. Sube todos los archivos y carpetas del interior directamente a la raíz de un repositorio nuevo. No crees una carpeta contenedora adicional en GitHub.

La raíz debe mostrar al menos:
- `.github/workflows/main.yml`
- `app/`
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`

## B. Workflow de compilación

El archivo `.github/workflows/main.yml` ya contiene el código para configurar Java 17, instalar Gradle 8.9, compilar y publicar el APK.

```yaml
name: Compilar Nexora APK
on:
  workflow_dispatch:
  push:
    branches: [main, master]
permissions:
  contents: read
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
          cache: gradle
      - uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.9'
      - run: gradle --no-daemon clean assembleDebug
      - uses: actions/upload-artifact@v4
        with:
          name: NEXORA-APK-v2.0-beta07
          path: app/build/outputs/apk/debug/app-debug.apk
          if-no-files-found: error
          retention-days: 14
```

## C. Descargar el APK

En GitHub, abre Actions, entra en la ejecución más reciente con marca verde y descarga `NEXORA-APK-v2.0-beta07` desde Artifacts. Verifica la fecha de ejecución antes de instalarlo para no confundirlo con una APK vieja.
