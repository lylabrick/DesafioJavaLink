# atm-service

Microservicio REST del cajero automático: login, extracción, depósito y consulta de saldo, con log
transaccional en archivo.

Las instrucciones de compilación, ejecución y prueba, junto con los endpoints y los datos de prueba,
están en el [README del repositorio](../README.md). Las decisiones de diseño están en el
[documento técnico](../docs/DISENO_TECNICO.md).

## Uso rápido

```bash
mvn clean package
java -jar target/atm-service-0.0.1-SNAPSHOT.jar
```

- API en `http://localhost:8080/api/...`
- Swagger UI en http://localhost:8080/swagger-ui.html
- Log transaccional en `logs/transacciones.log`

## Estructura

```
com.servicio.atm
├── app/service/           casos de uso (interfaces) y sus implementaciones en impl/
├── domain/
│   ├── entity/            Card, Account, CardAccount
│   ├── exception/         excepciones de negocio
│   └── model/in, out/     requests y responses de la API
└── infra/
    ├── config/            OpenAPI
    ├── in/                controllers REST y GlobalExceptionHandler
    └── out/               repositorios JPA y TransactionLogger
```
