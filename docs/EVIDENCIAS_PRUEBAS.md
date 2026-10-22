# Evidencias de pruebas – Producto ATM

Este documento reúne los resultados de las pruebas que validan el proyecto. Todas las salidas son
reales, de la corrida del **21/10/2026**.

1. [Entorno](#1-entorno)
2. [Tests automatizados](#2-tests-automatizados)
3. [Pruebas de punta a punta con la consola](#3-pruebas-de-punta-a-punta-con-la-consola)
4. [Log transaccional generado](#4-log-transaccional-generado)
5. [Pruebas directas sobre la API](#5-pruebas-directas-sobre-la-api)
6. [Hallazgos](#hallazgos)

## 1. Entorno

| Componente | Versión |
|---|---|
| Sistema operativo | Windows 11 Pro (10.0.26300) |
| JDK | OpenJDK 1.8.0_504 (Amazon Corretto 8.504.04.1) |
| Maven | 3.9.11 |
| Spring Boot | 2.7.18 |

## 2. Tests automatizados

Se ejecutó `mvn clean package` en cada proyecto (compila y corre todos los tests).

### 2.1 `atm-service`: 67 tests, 0 fallos

```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 - in com.servicio.atm.app.service.impl.BalanceServiceImplTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 - in com.servicio.atm.app.service.impl.DepositServiceImplTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 - in com.servicio.atm.app.service.impl.ExtractServiceImplTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 - in com.servicio.atm.app.service.impl.LoginServiceImplTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 - in com.servicio.atm.app.service.TransactionConcurrencyTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 - in com.servicio.atm.AtmServiceApplicationTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0 - in com.servicio.atm.domain.entity.AccountTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 - in com.servicio.atm.infra.in.controller.AccountControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 - in com.servicio.atm.infra.in.controller.AuthControllerTest
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0 - in com.servicio.atm.infra.in.controller.TransactionControllerTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 - in com.servicio.atm.infra.out.logging.TransactionLoggerTest
[INFO] Tests run: 67, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

> Durante la corrida aparece una línea `ERROR ... GlobalExceptionHandler - Error no controlado`. Es
> esperada: la produce el test que verifica que un error inesperado devuelve 500 sin exponer la traza.

| Clase | Tipo | Tests | Qué valida |
|---|---|---|---|
| `AccountTest` | Unitario de dominio | 10 | `acreditar` y `debitar` rechazan importes nulos, cero o negativos; el débito deja el saldo exacto en 0; el saldo insuficiente no modifica la cuenta. |
| `LoginServiceImplTest` | Servicio (Mockito) | 4 | Tarjeta activa → éxito; inactiva, sin estado o inexistente → no exitoso. |
| `BalanceServiceImplTest` | Servicio (Mockito) | 2 | Devuelve el saldo; cuenta no asociada → `CuentaNoEncontradaException`. |
| `ExtractServiceImplTest` | Servicio (Mockito) | 6 | Extracción OK; cuenta no asociada; tarjeta inactiva (no llega a bloquear la cuenta); cuenta inexistente al bloquear; saldo insuficiente; importe inválido. Cada rechazo queda registrado en el log. |
| `DepositServiceImplTest` | Servicio (Mockito) | 6 | Depósito OK; la cuenta destino se busca solo por CBU (puede ser de un tercero); tarjeta inactiva; tarjeta inexistente; CBU inexistente; importe inválido. |
| `TransactionConcurrencyTest` | Integración (Spring + H2) | 3 | Ver [2.3](#23-concurrencia). |
| `AtmServiceApplicationTest` | Integración | 1 | El contexto de Spring levanta. |
| `AuthControllerTest` | Web (`@WebMvcTest`) | 3 | 200 con `success=true`, 200 con `success=false`, 400 sin tarjeta. |
| `AccountControllerTest` | Web (`@WebMvcTest`) | 3 | 200 con saldo, 400 por datos faltantes, 404 cuenta no encontrada. |
| `TransactionControllerTest` | Web (`@WebMvcTest`) | 21 | Extracción: 200, 400 (cuerpo vacío, importe 0, más de 2 decimales, no numérico, JSON mal formado), 403, 404, 422, 503 y 500. Depósito: 200, 400 (vacío, negativo, regla de dominio), 403, 404 por tarjeta, 404 por CBU y 503. Además 405 y 415. |
| `TransactionLoggerTest` | Unitario | 8 | Formato de la línea, rechazos, login `EXITOSO` / `NO_EXITOSO` y enmascarado de la tarjeta: el número completo nunca aparece, incluso con números cortos o nulos. |

### 2.2 `atm-console`: 45 tests, 0 fallos

```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 - in com.console.atm.cli.BalanceCommandTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0 - in com.console.atm.cli.CommandDispatcherTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 - in com.console.atm.cli.ConsolePrinterTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 - in com.console.atm.cli.DepositCommandTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 - in com.console.atm.cli.ExtractCommandTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 - in com.console.atm.cli.LoginCommandTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0 - in com.console.atm.client.HttpAtmClientTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 - in com.console.atm.util.AmountFormatterTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0 - in com.console.atm.util.AmountParserTest
[INFO] Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Clase | Tests | Qué valida |
|---|---|---|
| `CommandDispatcherTest` | 9 | Sin argumentos, comando desconocido, cantidad de argumentos incorrecta, nombre sin distinguir mayúsculas, traducción de errores a mensajes y códigos de salida. |
| `ConsolePrinterTest` | 4 | Codificación de salida: usa la de la consola si Java la soporta (`cp850`) y UTF-8 si la consola está en UTF-8 (`cp65001`), si la salida está redirigida o si la codificación es desconocida. |
| `LoginCommandTest` | 4 | `Ingreso exitoso`; `Ingreso no exitoso` por `success=false` y por HTTP 403; otros errores se propagan. |
| `ExtractCommandTest` / `DepositCommandTest` / `BalanceCommandTest` | 5 | Mensajes de éxito y armado del request con el importe normalizado. |
| `HttpAtmClientTest` | 10 | Contra un servidor HTTP embebido: requests y responses JSON, errores con `message` y `details`, error no JSON, servicio inaccesible y respuesta ilegible. |
| `AmountParserTest` | 11 | Acepta `.` y `,`; rechaza texto, negativos, cero, más de 2 decimales y más de 13 enteros. |
| `AmountFormatterTest` | 2 | Formato `99999,99` con coma decimal. |

### 2.3 Concurrencia

`TransactionConcurrencyTest` levanta Spring con una H2 propia y lanza **8 hilos simultáneos** sobre
la misma cuenta para comprobar el bloqueo pesimista:

| Escenario | Saldo inicial | Operaciones | Resultado verificado |
|---|---|---|---|
| Extracciones concurrentes | 500,00 | 8 extracciones de 100,00 | 5 exitosas, 3 rechazadas por saldo insuficiente, saldo final **0,00** (nunca negativo) |
| Depósitos concurrentes | 100,00 | 8 depósitos de 25,00 | Saldo final **300,00** (no se pierde ninguna actualización) |
| Mixto | 1000,00 | 4 depósitos de 100,00 y 4 extracciones de 50,00 | Saldo final **1200,00** |

## 3. Pruebas de punta a punta con la consola

Procedimiento:

1. Compilar ambos proyectos (`mvn clean package`).
2. Levantar el servicio con `java -jar target/atm-service-0.0.1-SNAPSHOT.jar`. Arrancó en 6 s:
   `Tomcat started on port(s): 8080` y `/actuator/health` → `{"status":"UP"}`.
3. Ejecutar cada escenario con el wrapper `atm.bat` (en las tablas, `atm` abrevia esa invocación) y registrar la salida (stdout + stderr) y el código de
   salida.

Los escenarios se ejecutaron en este orden sobre los [datos iniciales](../README.md#datos-de-prueba),
así que los saldos de cada paso reflejan las operaciones anteriores.

Tarjetas usadas: Ana `4000111122223333` (activa), Bruno `4000555566668888` (activa), Carla
`4000999988887777` (**inactiva**) y `4000000000000000` (inexistente).

### 3.1 Uso de la consola

| # | Comando | Salida | Exit | Resultado |
|---|---|---|---|---|
| E01 | `atm` | `Uso: atm <comando> [argumentos]` + lista de comandos | 1 | ✅ |
| E02 | `atm consultar 4000111122223333` | `Comando desconocido: consultar` + lista de comandos | 1 | ✅ |
| E03 | `atm login` | `Uso: atm login <tarjeta>` | 1 | ✅ |

Salida completa de E01:

```
Uso: atm <comando> [argumentos]
Comandos disponibles:
  atm login <tarjeta>
  atm extraer <tarjeta> <cuenta> <importe>
  atm depositar <tarjeta> <cbu> <importe>
  atm saldo <tarjeta> <cuenta>
```

### 3.2 Identificarse

| # | Comando | Esperado | Salida | Exit | Resultado |
|---|---|---|---|---|---|
| E04 | `atm login 4000111122223333` | Tarjeta activa | `Ingreso exitoso` | 0 | ✅ |
| E05 | `atm login 4000999988887777` | Tarjeta inactiva | `Ingreso no exitoso` | 1 | ✅ |
| E06 | `atm login 4000000000000000` | Tarjeta inexistente | `Ingreso no exitoso` | 1 | ✅ |

### 3.3 Consultar saldo

| # | Comando | Esperado | Salida | Exit | Resultado |
|---|---|---|---|---|---|
| E07 | `atm saldo 4000111122223333 0000000001` | Saldo inicial de la cuenta 1 | `Su saldo es $ 1000,00` | 0 | ✅ |
| E08 | `atm saldo 4000999988887777 0000000003` | Tarjeta inactiva: consulta permitida | `Su saldo es $ 0,00` | 0 | ✅ |
| E09 | `atm saldo 4000111122223333 0000000002` | Cuenta de otro titular | `Cuenta no encontrada para la tarjeta indicada` | 1 | ✅ |
| E10 | `atm saldo 4000000000000000 0000000001` | Tarjeta inexistente | `Cuenta no encontrada para la tarjeta indicada` | 1 | ✅ |

### 3.4 Extraer

| # | Comando | Esperado | Salida | Exit | Resultado |
|---|---|---|---|---|---|
| E11 | `atm extraer 4000111122223333 0000000001 300` | Extracción OK | `Retire su dinero` | 0 | ✅ |
| E12 | `atm saldo 4000111122223333 0000000001` | 1000 − 300 | `Su saldo es $ 700,00` | 0 | ✅ |
| E13 | `atm extraer 4000111122223333 0000000001 5000` | Saldo insuficiente | `Saldo insuficiente` | 1 | ✅ |
| E14 | `atm extraer 4000555566668888 0000000008 10` | Extraer el saldo exacto | `Retire su dinero` | 0 | ✅ |
| E15 | `atm saldo 4000555566668888 0000000008` | Queda en cero, no negativo | `Su saldo es $ 0,00` | 0 | ✅ |
| E16 | `atm extraer 4000999988887777 0000000003 10` | Tarjeta inactiva | `La tarjeta no esta activa` | 1 | ✅ |
| E17 | `atm extraer 4000000000000000 0000000001 10` | Tarjeta inexistente | `Cuenta no encontrada para la tarjeta indicada` | 1 | ✅ |
| E18 | `atm extraer 4000111122223333 0000000001 0` | Importe cero | `El importe debe ser mayor que cero` | 1 | ✅ |
| E19 | `atm extraer 4000111122223333 0000000001 -50` | Importe negativo | `Importe invalido: '-50'. Use un numero positivo, por ejemplo 1500.50` | 1 | ✅ |
| E20 | `atm extraer 4000111122223333 0000000001 10.505` | Más de 2 decimales | `El importe admite como maximo 2 decimales` | 1 | ✅ |
| E21 | `atm extraer 4000111122223333 0000000001 abc` | No numérico | `Importe invalido: 'abc'. Use un numero positivo, por ejemplo 1500.50` | 1 | ✅ |
| E22 | `atm extraer 4000111122223333 0000000004 1,25` | Coma como separador decimal | `Retire su dinero` | 0 | ✅ |

### 3.5 Depositar

| # | Comando | Esperado | Salida | Exit | Resultado |
|---|---|---|---|---|---|
| E23 | `atm depositar 4000111122223333 0170000000000000000004 250.50` | Depósito en cuenta propia | `Depósito exitoso` | 0 | ✅ |
| E24 | `atm saldo 4000111122223333 0000000004` | 25000,75 − 1,25 (E22) + 250,50 | `Su saldo es $ 25250,00` | 0 | ✅ |
| E25 | `atm depositar 4000111122223333 0170000000000000000002 100` | Depósito en cuenta de **otro titular** (Bruno) | `Depósito exitoso` | 0 | ✅ |
| E26 | `atm saldo 4000555566668888 0000000002` | Bruno: 50 + 100 | `Su saldo es $ 150,00` | 0 | ✅ |
| E27 | `atm depositar 4000999988887777 0170000000000000000001 100` | Tarjeta inactiva | `La tarjeta no esta activa` | 1 | ✅ |
| E28 | `atm depositar 4000000000000000 0170000000000000000001 100` | Tarjeta inexistente | `Tarjeta no encontrada` | 1 | ✅ |
| E29 | `atm depositar 4000111122223333 0170000000000000000099 100` | CBU inexistente | `Cuenta no encontrada para la tarjeta indicada` | 1 | ✅ |

### 3.6 Los rechazos no modifican saldos

| # | Comando | Esperado | Salida | Exit | Resultado |
|---|---|---|---|---|---|
| E30 | `atm saldo 4000111122223333 0000000001` | Sigue en 700 tras E13, E17–E21, E27 y E28 | `Su saldo es $ 700,00` | 0 | ✅ |
| E31 | `atm saldo 4000999988887777 0000000003` | Sigue en 0 tras E16 | `Su saldo es $ 0,00` | 0 | ✅ |

### 3.7 Servicio no disponible

| # | Comando | Esperado | Salida | Exit | Resultado |
|---|---|---|---|---|---|
| E32 | `atm login 4000111122223333` con `ATM_BASE_URL=http://localhost:9999` | Mensaje claro, sin traza | `Error: No se pudo conectar con el servicio ATM en http://localhost:9999. Verifique que el servicio este en ejecucion.` | 1 | ✅ |

**Resultado: 32 de 32 escenarios con el comportamiento esperado.**

### 3.8 Acentos en distintas terminales de Windows

Se ejecutó `atm depositar 4000111122223333 0170000000000000000007 0.01` (cuenta de Elena) en
distintas terminales para verificar que `Depósito exitoso` se vea correctamente (ver
[hallazgo H1](#hallazgos)):

| Terminal | Página de códigos | Cómo se recibe la salida | Antes de la corrección | Después |
|---|---|---|---|---|
| `cmd` | 850 | Consola | — | `Depósito exitoso` ✅ |
| PowerShell | 850 | Consola | — | `Depósito exitoso` ✅ |
| `cmd` | 65001 (UTF-8) | Consola | — | `Depósito exitoso` ✅ |
| PowerShell | 65001 (UTF-8) | Consola | — | `Depósito exitoso` ✅ |
| PowerShell (`OutputEncoding` UTF-8) | — | Capturada en una variable | `Dep�sito exitoso` | `Depósito exitoso` ✅ |
| `cmd` | — | Redirigida a archivo, leído como UTF-8 | — | `Depósito exitoso` ✅ |
| Bash (Git for Windows) | — | Pipe | `Dep�sito exitoso` | `Depósito exitoso` ✅ |

"—" indica que ese caso no se probó antes de la corrección. En una consola real, Java 8 informa la
página de códigos (`sun.stdout.encoding` = `cp850` o `cp65001`). Con 850 ya se veía bien; con 65001,
Java 8 no reconoce `cp65001` y usaba Cp1252.

Cuando la salida se captura o redirige, la consola no informa ninguna codificación y la aplicación
escribe en UTF-8. Si quien la captura decodifica con otra página de códigos (por ejemplo, PowerShell
con `[Console]::OutputEncoding` en 850), la "ó" se verá mal. Esa decodificación depende de quien
captura la salida, no de la aplicación.

## 4. Log transaccional generado

Contenido de `atm-service/logs/transacciones.log` al terminar la corrida, completo y sin editar:

```
2026-10-21 22:10:53.813 | tipo=LOGIN tarjeta=****3333 resultado=EXITOSO
2026-10-21 22:10:54.275 | tipo=LOGIN tarjeta=****7777 resultado=NO_EXITOSO
2026-10-21 22:10:54.978 | tipo=LOGIN tarjeta=****0000 resultado=NO_EXITOSO
2026-10-21 22:10:57.386 | tipo=EXTRACCION tarjeta=****3333 cuenta=0000000001 importe=300.00 resultado=OK
2026-10-21 22:10:58.267 | tipo=EXTRACCION tarjeta=****3333 cuenta=0000000001 importe=5000.00 resultado=RECHAZADA(SaldoInsuficienteException)
2026-10-21 22:10:58.688 | tipo=EXTRACCION tarjeta=****8888 cuenta=0000000008 importe=10.00 resultado=OK
2026-10-21 22:10:59.598 | tipo=EXTRACCION tarjeta=****7777 cuenta=0000000003 importe=10.00 resultado=RECHAZADA(TarjetaInactivaException)
2026-10-21 22:11:00.213 | tipo=EXTRACCION tarjeta=****0000 cuenta=0000000001 importe=10.00 resultado=RECHAZADA(CuentaNoEncontradaException)
2026-10-21 22:11:01.864 | tipo=EXTRACCION tarjeta=****3333 cuenta=0000000004 importe=1.25 resultado=OK
2026-10-21 22:11:02.307 | tipo=DEPOSITO tarjeta=****3333 cuenta=0170000000000000000004 importe=250.50 resultado=OK
2026-10-21 22:11:03.121 | tipo=DEPOSITO tarjeta=****3333 cuenta=0170000000000000000002 importe=100.00 resultado=OK
2026-10-21 22:11:03.931 | tipo=DEPOSITO tarjeta=****7777 cuenta=0170000000000000000001 importe=100.00 resultado=RECHAZADA(TarjetaInactivaException)
2026-10-21 22:11:04.347 | tipo=DEPOSITO tarjeta=****0000 cuenta=0170000000000000000001 importe=100.00 resultado=RECHAZADA(TarjetaNoEncontradaException)
2026-10-21 22:11:04.740 | tipo=DEPOSITO tarjeta=****3333 cuenta=0170000000000000000099 importe=100.00 resultado=RECHAZADA(CuentaNoEncontradaException)
2026-10-21 22:11:19.587 | tipo=DEPOSITO tarjeta=****3333 cuenta=0170000000000000000007 importe=0.01 resultado=OK
2026-10-21 22:11:19.978 | tipo=DEPOSITO tarjeta=****3333 cuenta=0170000000000000000007 importe=0.01 resultado=OK
2026-10-21 22:11:20.405 | tipo=DEPOSITO tarjeta=****3333 cuenta=0170000000000000000007 importe=0.01 resultado=OK
2026-10-21 22:11:20.773 | tipo=DEPOSITO tarjeta=****3333 cuenta=0170000000000000000007 importe=0.01 resultado=OK
2026-10-21 22:11:21.131 | tipo=DEPOSITO tarjeta=****3333 cuenta=0170000000000000000007 importe=0.01 resultado=OK
2026-10-21 22:11:29.408 | tipo=DEPOSITO tarjeta=****3333 cuenta=0170000000000000000007 importe=0.01 resultado=OK
2026-10-21 22:11:29.783 | tipo=DEPOSITO tarjeta=****3333 cuenta=0170000000000000000007 importe=0.01 resultado=OK
```

Qué se verifica:

- Cada login, extracción y depósito que llegó al servicio tiene su línea, con éxito o con la causa
  del rechazo.
- El número de tarjeta aparece siempre enmascarado (`****3333`).
- Los movimientos `OK` explican los saldos finales: la cuenta 1 pasa de 1000,00 a 700,00 por la
  única extracción OK de 300,00.
- Los importes inválidos (E18–E21) no aparecen, porque los rechaza la consola antes de llamar al
  servicio.
- La consulta de saldo no se registra (ver [concesiones](DISENO_TECNICO.md#6-concesiones-y-decisiones-ante-falta-de-especificación)).
- Las siete últimas líneas (depósitos de 0,01 a la cuenta 7) corresponden a las pruebas de
  [3.8](#38-acentos-en-distintas-terminales-de-windows). El saldo de esa cuenta, consultado al final
  por la API, es `1000000.06` = 999999,99 + 7 × 0,01.

## 5. Pruebas directas sobre la API

Como la consola filtra los importes inválidos, se llamó al servicio con `curl` para verificar que
también los valida por su cuenta:

| Prueba | Respuesta |
|---|---|
| `GET /swagger-ui.html` | HTTP 200 |
| `GET /v3/api-docs` | Expone `/api/login`, `/api/accounts/balance`, `/api/transactions/extract`, `/api/transactions/deposit` |
| `POST /api/transactions/extract` con `amount: 0` | HTTP 400 `{"message":"Datos de entrada invalidos","details":["amount: El importe debe ser mayor que cero"]}` |
| `POST /api/transactions/extract` con `amount: 10.555` | HTTP 400 `{"message":"Datos de entrada invalidos","details":["amount: Importe con formato invalido"]}` |
| `POST /api/login` con `Content-Type: text/plain` | HTTP 415 `{"message":"Content-Type no soportado, se espera application/json"}` |
| `GET /api/login` | HTTP 405 `{"message":"Metodo HTTP no permitido para este recurso"}` |

## Hallazgos

| Id | Descripción | Impacto | Estado |
|---|---|---|---|
| H1 | `Depósito exitoso` se mostraba como `Dep�sito exitoso` cuando la salida se capturaba o redirigía (PowerShell, Bash). Lo mismo ocurre en consolas con página de códigos UTF-8 (65001), porque Java 8 no reconoce `cp65001`. En esos casos la aplicación escribía con la codificación por defecto del sistema (Cp1252). | Solo visual: el depósito se realizaba y el código de salida era 0. | **Corregido.** `ConsolePrinter.forSystemStreams()` usa la codificación de la consola cuando Java la soporta y UTF-8 en los demás casos. Cubierto por `ConsolePrinterTest` y verificado en [3.8](#38-acentos-en-distintas-terminales-de-windows); la corrida E2E completa se repitió después de la corrección. |
| H2 | Con tarjeta inexistente, `depositar` responde `Tarjeta no encontrada` y `extraer` / `saldo` responden `Cuenta no encontrada para la tarjeta indicada`. El CBU inexistente también responde con el mensaje de "tarjeta". | Mensajes inconsistentes; el comportamiento es correcto (rechaza y no modifica saldos). | Documentado como concesión y mejora propuesta. |
