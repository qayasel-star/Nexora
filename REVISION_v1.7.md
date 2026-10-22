# Registro Informático 2.0 Beta — Revisión v1.7

## Módulo 1: bienvenida y carga
- Se integra el splash nativo de Android mediante AndroidX Core SplashScreen para mostrar identidad visual desde el lanzamiento, antes de que Compose termine de dibujar.
- Se eliminó la espera artificial de 1,9 segundos; la pantalla de marca pasa al inicio tras una presentación breve de 350 ms y una transición suave.
- El nombre, logo y lema se dibujan visibles desde el primer fotograma de la pantalla Compose, sin fade-in que los oculte al inicio.
- Se define el tema inicial oscuro azul marino y el tema posterior de la aplicación.

## Módulo 2: pantalla principal y navegación
- Los iconos de la barra inferior y los accesos rápidos ahora corresponden a sus funciones: Reportes (Assignment), Análisis (QueryStats), Informes (Assessment), Ajustes (Settings) y Nuevo reporte (NoteAdd).
- El acceso superior izquierdo muestra el icono de Ajustes en lugar de un menú genérico.
- El indicador total usa un icono de reportes, y el acceso de Informes usa el mismo icono semántico que la navegación.
- Se mantienen las secciones y flujos existentes.

## Validación pendiente
No se pudo ejecutar Gradle en este entorno. El proyecto debe compilarse con el workflow de GitHub Actions antes de instalar el APK.
