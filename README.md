# account-service

Microservicio reactivo para la **gestión de cuentas bancarias** (ahorro, corriente, plazo fijo), depósitos, retiros, transferencias, comisiones y tarjetas de débito.

## Tecnologías
Java 17 · Spring Boot 3.4.2 · WebFlux (Netty) · RxJava 3 · MongoDB Reactive · Eureka Client · Config Client · Resilience4j · Spring Kafka (productor) · MapStruct · Lombok · Springdoc OpenAPI.

## Prerrequisitos
- JDK 17.
- MongoDB en `localhost:27017` (base `account_db`).
- Eureka en `localhost:8761`.
- Kafka en `localhost:9092` (para publicar eventos de débito).

```bash
export JAVA_HOME=$HOME/.jdks/temurin-17.0.20
export PATH=$JAVA_HOME/bin:$PATH
```

## Configuración
- Puerto: **8082**
- Variables de entorno:
  - `SPRING_DATA_MONGODB_URI` (default `mongodb://localhost:27017/account_db`)
  - `EUREKA_SERVER_URL`, `CONFIG_SERVER_URL`
  - `KAFKA_BOOTSTRAP_SERVERS` (default `localhost:9092`)

## Ejecutar
```bash
cd account-service
./mvnw spring-boot:run
```
Verificar: http://localhost:8082/webjars/swagger-ui/index.html

## Endpoints
### `/api/v1/accounts`
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/` | Crear cuenta |
| GET | `/{id}` | Obtener por ID |
| GET | `/number/{accountNumber}` | Obtener por número |
| GET | `/customer/{customerId}` | Cuentas de un cliente |
| GET | `/customer/{customerId}/report?startDate=&endDate=` | Reporte por rango de fechas |
| GET | `/` | Listar todas |
| PUT | `/{id}` | Actualizar |
| DELETE | `/{id}` | Eliminar |
| POST | `/{id}/deposits` | Depositar |
| POST | `/{id}/withdrawals` | Retirar |
| POST | `/{id}/transfers` | Transferir entre cuentas |

### `/api/v1/debit-cards`
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/` | Emitir tarjeta de débito |
| POST | `/{id}/payments` | Pagar con tarjeta (debita la cuenta y publica evento) |

## Reglas de negocio
- Límites de tenencia: personal máx. 1 ahorro + 1 corriente; empresarial sin ahorro/plazo fijo.
- Comisión automática al superar el límite mensual de transacciones sin costo.
- Pago con débito es **event-driven** (publica `DebitCardPaymentEvent` en Kafka).

## Tests
```bash
./mvnw test     # 87 tests
./mvnw verify   # tests + cobertura (≥80%)
```

## Docker
```bash
docker build -t account-service .
docker run -p 8082:8082 -e EUREKA_SERVER_URL=http://host.docker.internal:8761/eureka account-service
```
