# atm-console

Aplicación de consola del cajero automático. Consume por HTTP el microservicio
[`atm-service`](../atm-service).

Las instrucciones de compilación, ejecución y prueba, junto con los datos de prueba, están en el
[README del repositorio](../README.md).

## Uso rápido

Desde esta carpeta, en PowerShell o `cmd`:

```
.\atm.bat login <tarjeta>
.\atm.bat extraer <tarjeta> <cuenta> <importe>
.\atm.bat depositar <tarjeta> <cbu> <importe>
.\atm.bat saldo <tarjeta> <cuenta>
```

En Git Bash se reemplaza `.\atm.bat` por `./atm.bat`. Resumen por terminal:

| Terminal | Cómo se invoca |
|---|---|
| PowerShell | `.\atm.bat login <tarjeta>` |
| `cmd` | `atm login <tarjeta>` o `.\atm.bat login <tarjeta>` |
| Git Bash | `./atm.bat login <tarjeta>` |
| Cualquiera | `java -jar target/atm-console.jar login <tarjeta>` |

Más detalle y problemas frecuentes en el [README del repositorio](../README.md#3-ejecutar-la-aplicación-de-consola).

## Configuración

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `ATM_BASE_URL` | URL base del servicio | `http://localhost:8080` |

La sintaxis para definirla en cada terminal está en el [README del repositorio](../README.md#3-ejecutar-la-aplicación-de-consola).

Los tiempos de espera son 3 s para conectar y 10 s para leer la respuesta.

## Estructura

```
com.console.atm
├── App                    punto de entrada
├── cli/                   comandos (patrón Command), despachador y salida por consola
├── client/                AtmClient y su implementación HTTP (HttpURLConnection + Jackson)
├── dto/                   mensajes de request/response del servicio (Lombok)
├── config/                configuración de conexión
└── util/                  validación y formato de importes
```

Los comandos dependen de la interfaz `AtmClient`, por lo que se prueban sin el servicio.
