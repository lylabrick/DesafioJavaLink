# atm-console

Aplicación de consola del cajero automático. Consume por HTTP el microservicio
[`atm-service`](../atm-service).

Las instrucciones de compilación, ejecución y prueba, junto con los datos de prueba, están en el
[README del repositorio](../README.md).

## Uso rápido

```
atm login <tarjeta>
atm extraer <tarjeta> <cuenta> <importe>
atm depositar <tarjeta> <cbu> <importe>
atm saldo <tarjeta> <cuenta>
```

En Windows se usa `atm.bat`; en Linux/macOS, `./atm.sh`. También se puede llamar directamente:
`java -jar target/atm-console.jar <comando> ...`

## Configuración

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `ATM_BASE_URL` | URL base del servicio | `http://localhost:8080` |

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
