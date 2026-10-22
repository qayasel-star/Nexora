# Módulo 4 — mejoras del filtro y comparaciones

- Se sustituyó el filtro fijo de 30 días por: 1 mes, 2 meses, 3 meses, 6 meses, 1 año y todo el historial.
- Los intervalos de meses se calculan con `Calendar.add(MONTH, ...)`, no convirtiendo meses en una cantidad fija de días.
- La fecha usada para asignar un reporte a un período es su fecha de cierre si está resuelto y tiene `closedAt`; si sigue abierto, se usa la fecha de apertura. Esto incluye en el período reciente un reporte abierto hace más tiempo pero resuelto recientemente.
- Se puede comparar el período actual con el intervalo inmediatamente anterior de igual duración.
- Se muestra la diferencia absoluta y porcentual de reportes totales y de reportes resueltos.
- El selector "Todo" desactiva la comparación porque no existe una duración fija para comparar de forma equivalente.
- Se incrementó versionCode a 5 y versionName a 2.0-beta03.

## Validación
Se inspeccionaron las referencias del código y se empaquetó el proyecto. No fue posible ejecutar Gradle en este entorno porque el comando `gradle` no está instalado; la compilación final debe verificarse en GitHub Actions.


## Cambio de identidad
Nombre visible de la aplicación actualizado a Nexora. Se conserva el identificador Android existente.
