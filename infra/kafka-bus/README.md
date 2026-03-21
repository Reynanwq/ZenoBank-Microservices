# @zenobank/kafka-bus — Tópicos Globais

Documentação de todos os tópicos `global.*` do ZenōBank.

Tópicos de universo específico (`u1.*`, `u2.*`, ...) **não ficam aqui** — cada universo
define e documenta os seus próprios tópicos dentro do seu módulo.

---

## Configuração padrão (tópicos globais)

| Parâmetro | Valor | Motivo |
|-----------|-------|--------|
| Partições | 3 | Paralelismo básico — God Panel tem 1 consumer group |
| Replication factor | 1 | Single broker em dev |
| Retenção | 7 dias | Suficiente para auditoria e replay |
| Auto-create | desabilitado | Tópicos criados explicitamente pelo init |

---

## Contrato do envelope (todos os eventos)

Todo evento publicado em qualquer tópico — global ou específico de universo — segue este envelope:

```json
{
  "event_id": "uuid-v4",
  "event_type": "global.transaction.settled",
  "universe": 7,
  "source_service": "clearing",
  "timestamp": "2024-01-01T00:00:00Z",
  "schema_version": "1.0",
  "payload": {}
}
```

---

## Tópicos globais

### Grupo 1 — Saúde e monitoramento

| Tópico | Publicado por | Consumido por | Obrigatório |
|--------|--------------|---------------|-------------|
| `global.health.heartbeat` | Todos os bancos | God Panel | ✓ a cada 30s |
| `global.health.degraded` | Todos os bancos | God Panel | quando aplicável |
| `global.health.recovered` | Todos os bancos | God Panel | quando aplicável |

**`global.health.heartbeat`**
```json
{
  "payload": {
    "universe": 7,
    "bank_name": "GalactiBank",
    "status": "HEALTHY",
    "uptime_seconds": 3600,
    "active_connections": 42
  }
}
```

**`global.health.degraded`**
```json
{
  "payload": {
    "universe": 7,
    "affected_services": ["clearing", "ledger"],
    "reason": "High latency on PostgreSQL",
    "degraded_at": "2024-01-01T00:00:00Z"
  }
}
```

---

### Grupo 2 — Ciclo de vida de transações

| Tópico | Publicado por | Consumido por | Obrigatório |
|--------|--------------|---------------|-------------|
| `global.transaction.settled` | Todos os bancos | God Panel | ✓ toda TX liquidada |
| `global.transaction.failed` | Todos os bancos | God Panel | ✓ toda TX com falha |

**`global.transaction.settled`**
```json
{
  "payload": {
    "tx_id": "uuid",
    "universe": 7,
    "amount": 100.00,
    "net_amount": 95.00,
    "tx_type": "INTERPLANETARY_TRANSFER",
    "is_interplanetary": true,
    "settled_at": "2024-01-01T00:00:00Z"
  }
}
```

**`global.transaction.failed`**
```json
{
  "payload": {
    "tx_id": "uuid",
    "universe": 7,
    "tx_type": "TRANSFER",
    "failure_reason": "INSUFFICIENT_BALANCE",
    "failed_at": "2024-01-01T00:00:00Z"
  }
}
```

---

### Grupo 3 — Ciclo de vida de contas

| Tópico | Publicado por | Consumido por | Obrigatório |
|--------|--------------|---------------|-------------|
| `global.account.opened` | Todos os bancos | God Panel | ✓ toda conta aberta |
| `global.account.frozen` | Todos os bancos | God Panel | ✓ toda conta congelada |
| `global.account.closed` | Todos os bancos | God Panel | toda conta encerrada |

**`global.account.opened`**
```json
{
  "payload": {
    "account_id": "uuid",
    "universe": 7,
    "owner_type": "BEING",
    "account_type": "CHECKING",
    "opened_at": "2024-01-01T00:00:00Z"
  }
}
```

**`global.account.frozen`**
```json
{
  "payload": {
    "account_id": "uuid",
    "universe": 7,
    "reason": "PLANET_DESTROYED",
    "frozen_at": "2024-01-01T00:00:00Z"
  }
}
```

---

### Grupo 4 — Risco e crédito

| Tópico | Publicado por | Consumido por | Obrigatório |
|--------|--------------|---------------|-------------|
| `global.credit.defaulted` | Todos os bancos | God Panel | toda inadimplência |

**`global.credit.defaulted`**
```json
{
  "payload": {
    "credit_id": "uuid",
    "universe": 7,
    "total_amount": 5000.00,
    "remaining": 3200.00,
    "defaulted_at": "2024-01-01T00:00:00Z"
  }
}
```

---

### Grupo 5 — Segurança e compliance

| Tópico | Publicado por | Consumido por | Obrigatório |
|--------|--------------|---------------|-------------|
| `global.fraud.alert` | Todos os bancos | God Panel + Compliance | quando detectado |
| `global.compliance.audit.required` | Todos os bancos | God Panel + Compliance | quando aplicável |

**`global.fraud.alert`**
```json
{
  "payload": {
    "universe": 7,
    "account_id": "uuid",
    "alert_type": "UNUSUAL_VOLUME",
    "description": "50 transactions in 60 seconds",
    "detected_at": "2024-01-01T00:00:00Z"
  }
}
```

---

### Grupo 6 — Lifecycle de universo

| Tópico | Publicado por | Consumido por | Obrigatório |
|--------|--------------|---------------|-------------|
| `global.universe.degraded` | Todos os bancos | God Panel | quando aplicável |
| `global.universe.recovered` | Todos os bancos | God Panel | quando aplicável |

---

## Resumo — todos os tópicos globais

```
global.health.heartbeat
global.health.degraded
global.health.recovered

global.transaction.settled
global.transaction.failed

global.account.opened
global.account.frozen
global.account.closed

global.credit.defaulted

global.fraud.alert
global.compliance.audit.required

global.universe.degraded
global.universe.recovered
```

**Total: 13 tópicos globais**

---

## O que NÃO fica aqui

Tópicos específicos de universo (`u7.being.registered`, `u7.transaction.settled`, etc.)
são definidos e documentados dentro do módulo de cada universo — nunca neste arquivo.