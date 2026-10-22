# atm-console

Aplicación de consola del cajero automático. Consume por HTTP el microservicio
[`atm-service`](../atm-service), que debe estar en ejecución en `localhost:8080`.

Las instrucciones de compilación, ejecución y prueba, junto con los datos de prueba, están en el
[README del repositorio](../README.md).

## Uso

Desde esta carpeta, en `cmd`:

```
atm login <tarjeta>
atm extraer <tarjeta> <cuenta> <importe>
atm depositar <tarjeta> <cbu> <importe>
atm saldo <tarjeta> <cuenta>
```

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
