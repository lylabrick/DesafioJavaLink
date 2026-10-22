# Desafío Java – Producto ATM

Cajero automático simplificado: una aplicación de consola que consume por HTTP un microservicio
para identificarse, extraer, depositar y consultar saldo.

| Proyecto | Descripción | Tecnología |
|---|---|---|
| [`atm-service`](atm-service) | Microservicio REST con las operaciones y el log transaccional | Java 8, Spring Boot 2.7.18, H2 en memoria |
| [`atm-console`](atm-console) | Aplicación de consola (`login`, `extraer`, `depositar`, `saldo`) | Java 8, `HttpURLConnection` + Jackson |

Documentación:

- [Documento técnico](docs/DISENO_TECNICO.md): decisiones de diseño, cumplimiento de requisitos y concesiones.
- [Evidencias de pruebas](docs/EVIDENCIAS_PRUEBAS.md): tests automatizados y pruebas de punta a punta.

**Tiempo invertido:** 8 horas.

---

## Requisitos

- JDK 8 (probado con Amazon Corretto 1.8.0_504). `java` debe estar en el `PATH`.
- Maven 3.6 o superior (probado con 3.9.11). Los proyectos no incluyen Maven Wrapper.
- Puerto `8080` libre.

## 1. Instalar (compilar)

Cada proyecto se compila por separado. `mvn clean package` ejecuta también los tests.

```bash
cd atm-service
mvn clean package
```

```bash
cd atm-console
mvn clean package
```

Se generan:

- `atm-service/target/atm-service-0.0.1-SNAPSHOT.jar`
- `atm-console/target/atm-console.jar` (JAR ejecutable con sus dependencias)

## 2. Ejecutar el servicio

Desde la carpeta `atm-service`:

```bash
java -jar target/atm-service-0.0.1-SNAPSHOT.jar
```

o, sin empaquetar, `mvn spring-boot:run`.

| Recurso | URL |
|---|---|
| API | `http://localhost:8080/api/...` |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Health check | http://localhost:8080/actuator/health |
| Consola H2 | http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:atmdb`, usuario `sa`, sin contraseña) |

La base es H2 **en memoria** y se carga al iniciar desde
[`data.sql`](atm-service/src/main/resources/data.sql). Cada reinicio vuelve los saldos a sus valores
iniciales.

### Endpoints

Todos son `POST` con cuerpo JSON (`Content-Type: application/json`).

| Operación | Endpoint | Cuerpo | Respuesta OK |
|---|---|---|---|
| Login | `/api/login` | `{"cardNumber"}` | `{"success": true\|false}` |
| Saldo | `/api/accounts/balance` | `{"cardNumber", "accountNumber"}` | `{"balance"}` |
| Extracción | `/api/transactions/extract` | `{"cardNumber", "accountNumber", "amount"}` | `{"amount", "balance"}` |
| Depósito | `/api/transactions/deposit` | `{"cardNumber", "cbu", "amount"}` | `{"amount", "balance"}` |

Los errores devuelven `{"status", "error", "message", "details", "timestamp"}` con el código HTTP
correspondiente (400, 403, 404, 405, 415, 422, 503 o 500). El detalle está en el
[documento técnico](docs/DISENO_TECNICO.md#26-manejo-de-errores).

### Log transaccional

El servicio escribe una línea por operación en `atm-service/logs/transacciones.log`, relativo al
directorio desde donde se lanza el servicio. Rota diariamente a `transacciones.AAAA-MM-DD.log` y
conserva 90 días.

```
2026-10-22 01:06:15.049 | tipo=EXTRACCION tarjeta=****3333 cuenta=0000000001 importe=300.00 resultado=OK
2026-10-22 01:06:15.820 | tipo=EXTRACCION tarjeta=****3333 cuenta=0000000001 importe=5000.00 resultado=RECHAZADA(SaldoInsuficienteException)
```

## 3. Ejecutar la aplicación de consola

Con el servicio levantado, en `cmd` y desde la carpeta `atm-console` (el JAR debe estar generado, ver el
paso 1):

| Comando | Uso | Salida si la operación es exitosa |
|---|---|---|
| Identificarse | `atm login <tarjeta>` | `Ingreso exitoso` (si no: `Ingreso no exitoso`) |
| Extraer | `atm extraer <tarjeta> <cuenta> <importe>` | `Retire su dinero` |
| Depositar | `atm depositar <tarjeta> <cbu> <importe>` | `Depósito exitoso` |
| Consultar saldo | `atm saldo <tarjeta> <cuenta>` | `Su saldo es $ 99999,99` |

- El importe acepta `.` o `,` como separador decimal y hasta 2 decimales (`1500`, `1500.50`, `1500,5`).
- `depositar` acredita en la cuenta del CBU indicado, sea o no del titular de la tarjeta.
- Los errores se imprimen por stderr. El código de salida es `0` si la operación fue exitosa y `1` en
  cualquier otro caso.

### Datos de prueba

Cargados por [`data.sql`](atm-service/src/main/resources/data.sql):

| Titular | Tarjeta | Activa | Cuenta | CBU | Saldo inicial |
|---|---|---|---|---|---|
| Ana | `4000111122223333` | Sí | `0000000001` | `0170000000000000000001` | 1000,00 |
| Ana | `4000111122223333` | Sí | `0000000004` | `0170000000000000000004` | 25000,75 |
| Bruno | `4000555566668888` | Sí | `0000000002` | `0170000000000000000002` | 50,00 |
| Bruno | `4000555566668888` | Sí | `0000000008` | `0170000000000000000008` | 10,00 |
| Carla | `4000999988887777` | **No** | `0000000003` | `0170000000000000000003` | 0,00 |
| Diego | `4000222233334444` | Sí | `0000000005` | `0170000000000000000005` | 3500,00 |
| Diego | `4000222233334444` | Sí | `0000000006` | `0170000000000000000006` | 120,40 |
| Elena | `4000666677778888` | Sí | `0000000007` | `0170000000000000000007` | 999999,99 |

Ejemplos:

```
atm saldo 4000111122223333 0000000001
```

```
atm extraer 4000111122223333 0000000001 300
```

```
atm depositar 4000111122223333 0170000000000000000002 100
```

### Problemas frecuentes

| Mensaje | Causa y solución |
|---|---|
| `'java' is not recognized...` | `java` no está en el `PATH` de esa ventana. Agregá la carpeta `bin` del JDK 8 con `set PATH=C:\ruta\al\jdk8\bin;%PATH%` y verificá con `java -version`. |
| `Unable to access jarfile ...atm-console.jar` | Falta generar el JAR: `mvn clean package` en `atm-console`. |
| `No se pudo conectar con el servicio ATM en ...` | El servicio no está levantado. |

## 4. Probar

Tests automatizados de cada proyecto:

```bash
cd atm-service
mvn test
```

```bash
cd atm-console
mvn test
```

| Proyecto | Tests | Incluye |
|---|---|---|
| `atm-service` | 67 | Dominio, servicios (Mockito), controllers (`@WebMvcTest`), log transaccional y concurrencia con H2 real |
| `atm-console` | 45 | Comandos, despachador, cliente HTTP contra un servidor embebido y validación de importes |

Los reportes quedan en `target/surefire-reports/`. Los resultados y las pruebas de punta a punta
están en [EVIDENCIAS_PRUEBAS.md](docs/EVIDENCIAS_PRUEBAS.md).

## Estructura del repositorio

```
DesafioJavaLink/
├── atm-service/   microservicio Spring Boot
├── atm-console/   aplicación de consola
└── docs/          documento técnico y evidencias de pruebas
```
