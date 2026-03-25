# Planet Service

Microserviço para gerenciamento de planetas e configurações fiscais no sistema Galactibank.

---

## Tecnologias

* Java 21
* Kotlin 1.9.24
* Quarkus 3.15.1
* MongoDB
* Panache
* Kafka (opcional)

---

## Pré-requisitos

* Docker Desktop
* JDK 21+
* Maven 3.9+

---

## Subindo as dependências

```bash
# MongoDB
docker run -d -p 27017:27017 --name mongodb-galactibank mongo:latest

# Kafka (opcional)
docker run -d --name kafka-galactibank -p 9092:9092 apache/kafka:latest
```

---

## Configuração

Arquivo `src/main/resources/application.yml`:

```yaml
quarkus:
  mongodb:
    connection-string: mongodb://localhost:27017/?retryWrites=false&uuidRepresentation=standard
    database: galactibank
  http:
    port: 8082
  kafka:
    devservices:
      enabled: false
```

---

## Executando

```bash
# Modo desenvolvimento
mvn clean compile quarkus:dev

# Build
mvn clean package

# Executar JAR
java -jar target/quarkus-app/quarkus-run.jar
```

---

## Endpoints

### Planetas

| Método | Endpoint                        | Descrição              |
| ------ | ------------------------------- | ---------------------- |
| POST   | /api/planets                    | Criar planeta          |
| GET    | /api/planets/{planetId}         | Buscar planeta         |
| PUT    | /api/planets/{planetId}         | Atualizar planeta      |
| DELETE | /api/planets/{planetId}         | Soft delete (INACTIVE) |
| POST   | /api/planets/{planetId}/destroy | Destruir (DESTROYED)   |

### Configurações Fiscais

| Método | Endpoint                                   | Descrição          |
| ------ | ------------------------------------------ | ------------------ |
| POST   | /api/planets/tax-config                    | Criar configuração |
| GET    | /api/planets/{planetId}/tax-rate/{taxType} | Buscar taxa atual  |

---

## Exemplos

### Criar planeta

```bash
curl -X POST http://localhost:8082/api/planets \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tatooine",
    "galaxy": "Andromeda",
    "population": 200000
  }'
```

**Response:**

```json
{
  "planet_id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Tatooine",
  "galaxy": "Andromeda",
  "population": 200000,
  "status": "ACTIVE",
  "created_at": "2026-03-25T10:00:00"
}
```

---

### Buscar planeta

```bash
curl http://localhost:8082/api/planets/550e8400-e29b-41d4-a716-446655440000
```

---

### Criar configuração de imposto

```bash
curl -X POST http://localhost:8082/api/planets/tax-config \
  -H "Content-Type: application/json" \
  -d '{
    "planet_id": "550e8400-e29b-41d4-a716-446655440000",
    "tax_rate": 0.05,
    "tax_type": "OUTBOUND"
  }'
```

**Response:**

```json
{
  "config_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "planet_id": "550e8400-e29b-41d4-a716-446655440000",
  "tax_rate": 0.05,
  "tax_type": "OUTBOUND",
  "valid_from": "2026-03-25T10:00:00",
  "valid_until": null
}
```

---

### Buscar taxa atual

```bash
curl http://localhost:8082/api/planets/550e8400-e29b-41d4-a716-446655440000/tax-rate/OUTBOUND
```

**Response:**

```json
{
  "planet_id": "550e8400-e29b-41d4-a716-446655440000",
  "tax_type": "OUTBOUND",
  "tax_rate": 0.05
}
```

---

### Atualizar planeta

```bash
curl -X PUT http://localhost:8082/api/planets/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tatooine Updated",
    "galaxy": "Andromeda",
    "population": 250000
  }'
```

---

### Soft delete

```bash
curl -X DELETE http://localhost:8082/api/planets/550e8400-e29b-41d4-a716-446655440000
```

---

### Destruir planeta

```bash
curl -X POST http://localhost:8082/api/planets/550e8400-e29b-41d4-a716-446655440000/destroy
```

---

## Estrutura do Banco

### Collection: planets

| Campo      | Tipo     | Descrição                   |
| ---------- | -------- | --------------------------- |
| planetId   | UUID     | Identificador único         |
| name       | String   | Nome do planeta             |
| galaxy     | String   | Galáxia                     |
| population | Long     | População                   |
| status     | Enum     | ACTIVE, INACTIVE, DESTROYED |
| createdAt  | DateTime | Data de criação             |

---

### Collection: planet_tax_configs

| Campo      | Tipo       | Descrição                        |
| ---------- | ---------- | -------------------------------- |
| configId   | UUID       | Identificador da configuração    |
| planetId   | UUID       | Referência ao planeta            |
| taxRate    | BigDecimal | Percentual do imposto            |
| taxType    | Enum       | OUTBOUND, INBOUND                |
| validFrom  | DateTime   | Início da vigência               |
| validUntil | DateTime   | Fim da vigência (null = vigente) |

---

## Queries MongoDB

```javascript
// Listar planetas ativos
db.planets.find({ status: "ACTIVE" })

// Buscar taxa atual
db.planet_tax_configs.find({
    planetId: UUID("550e8400-e29b-41d4-a716-446655440000"),
    taxType: "OUTBOUND",
    validFrom: { $lte: new Date() },
    validUntil: null
})

// Histórico de taxas
db.planet_tax_configs.find({
    planetId: UUID("550e8400-e29b-41d4-a716-446655440000")
}).sort({ validFrom: -1 })

// Estatísticas por status
db.planets.aggregate([
    { $group: { _id: "$status", count: { $sum: 1 } } }
])
```

---

## Swagger UI

```
http://localhost:8082/swagger-ui
```

---

## Estrutura do Projeto

```
src/main/kotlin/com/zenobank/u7/planet/
├── dto/
│   ├── PlanetRequest.kt
│   ├── PlanetResponse.kt
│   ├── TaxConfigRequest.kt
│   └── TaxConfigResponse.kt
├── entity/
│   ├── Planet.kt
│   ├── PlanetTaxConfig.kt
│   ├── PlanetStatus.kt
│   └── TaxType.kt
├── repository/
│   ├── PlanetRepository.kt
│   └── PlanetTaxConfigRepository.kt
├── resource/
│   └── PlanetResource.kt
└── service/
    └── PlanetService.kt
```
