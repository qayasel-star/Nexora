# Registro Informático 2.0 Beta — Revisión 1.8

## Módulo 3: Reportes
- ID visible de generación automática con formato RI-YYMMDD-HHMMSS-XXX.
- Eliminado el campo y selector «Tipo de trabajo» del formulario.
- Eliminada la sección plegable «Detalles técnicos»; los campos requeridos quedan visibles directamente.
- Técnico responsable mediante desplegable: Yasel, Getma, Naybet, Yanelis y Selene.
- Conservados departamento/unidad comercial, prioridad, diagnóstico, observaciones y descripción del problema.
- Eliminada la entrada manual de duración.
- La duración se calcula automáticamente desde la apertura hasta que el estado cambia a «Resuelta» y queda guardada en minutos para las estadísticas.
- Si un reporte resuelto se reabre, se limpia el cierre y la duración se volverá a calcular al cerrarlo de nuevo.
- Añadido `closedAt` al almacenamiento JSON, compatible con reportes guardados anteriormente.

## Pendiente de verificación
La compilación de Gradle debe ejecutarse en GitHub Actions antes de instalar este ZIP. Las notificaciones periódicas se dejan pendientes de confirmar el horario final: el rango indicado «8:30 a. m. a 4:30 a. m.» puede significar 4:30 p. m. (16:30) o 4:30 a. m. del día siguiente.
