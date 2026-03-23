#!/bin/bash

# =============================================================================
# ZenōBank — Criação dos tópicos Kafka globais
#
# Regras:
#   - Tópicos global.*  → consumidos pelo God Panel, publicados por todos os bancos
#   - 3 partições por tópico global (paralelismo básico, 1 consumer group no God Panel)
#   - Retenção: 7 dias (604800000 ms)
#   - Replication factor: 1 (single broker em dev)
#
# Tópicos de universo específico (u1.*, u2.*, ...) NÃO são criados aqui.
# Cada universo cria os seus próprios tópicos no seu próprio init.
# =============================================================================

KAFKA_BROKER="kafka:29092"
PARTITIONS=3
REPLICATION=1
RETENTION_MS=604800000  # 7 dias

echo "=================================================="
echo "  ZenōBank — Kafka Topic Init"
echo "  Broker: $KAFKA_BROKER"
echo "=================================================="

echo ""
echo "[wait] Aguardando Kafka ficar disponível..."
cub kafka-ready -b $KAFKA_BROKER 1 30
echo "[ok] Kafka disponível."
echo ""

create_topic() {
  local TOPIC=$1
  local DESCRIPTION=$2

  echo "[topic] Criando: $TOPIC"
  echo "        $DESCRIPTION"

  kafka-topics --bootstrap-server $KAFKA_BROKER \
    --create \
    --if-not-exists \
    --topic "$TOPIC" \
    --partitions $PARTITIONS \
    --replication-factor $REPLICATION \
    --config retention.ms=$RETENTION_MS

  echo ""
}

# =============================================================================
# GRUPO 1 — Saúde e monitoramento
# =============================================================================
create_topic "global.health.heartbeat"  "Heartbeat de cada banco — publicado a cada 30s."
create_topic "global.health.degraded"   "Banco reporta degradação parcial."
create_topic "global.health.recovered"  "Banco reporta recuperação após estado degradado."

# =============================================================================
# GRUPO 2 — Ciclo de vida de transações
# =============================================================================
create_topic "global.transaction.settled" "Toda transação liquidada (SETTLED)."
create_topic "global.transaction.failed"  "Toda transação que falhou (FAILED)."

# =============================================================================
# GRUPO 3 — Ciclo de vida de contas
# =============================================================================
create_topic "global.account.opened" "Toda nova conta aberta em qualquer universo."
create_topic "global.account.frozen" "Toda conta congelada (FROZEN) em qualquer universo."
create_topic "global.account.closed" "Toda conta encerrada (CLOSED) em qualquer universo."

# =============================================================================
# GRUPO 4 — Risco e crédito
# =============================================================================
create_topic "global.credit.defaulted" "Crédito entrou em inadimplência (DEFAULTED)."

# =============================================================================
# GRUPO 5 — Segurança e compliance
# =============================================================================
create_topic "global.fraud.alert"               "Banco detectou padrão suspeito ou possível fraude."
create_topic "global.compliance.audit.required" "Evento que aciona auditoria centralizada."

# =============================================================================
# GRUPO 6 — Lifecycle de universo
# =============================================================================
create_topic "global.universe.degraded"  "Banco inteiro reporta estado degradado."
create_topic "global.universe.recovered" "Banco reporta recuperação total."

# =============================================================================
# Listagem final
# =============================================================================
echo "=================================================="
echo "  Tópicos criados. Listagem final:"
echo "=================================================="
kafka-topics --bootstrap-server $KAFKA_BROKER --list | grep "^global\." | sort
echo "=================================================="
echo "  Init concluído."
echo "=================================================="