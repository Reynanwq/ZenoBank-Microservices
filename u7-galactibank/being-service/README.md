# being-service — Cadastro de Seres Vivos

## Responsabilidade

Serviço responsável pelo cadastro e gestão de todos os seres vivos do Universo 7.

## Domínio

- Emissão de **UID** (Universal ID) — identificador único e imutável de cada ser
- Cadastro de seres vivos com nome, espécie, planeta de origem
- Gestão de procuradores (account proxies)
- Consulta pública de dados básicos de seres

## Contexto

Todo ser vivo no Universo 7 precisa estar registrado no sistema antes de:
- Abrir conta em qualquer planeta
- Realizar transações financeiras
- Solicitar crédito ou cartão

## Regras de negócio

- UID é gerado automaticamente no cadastro e nunca muda
- Espécie é apenas atributo descritivo — não impacta lógica financeira
- Um ser pode estar registrado sem ter conta (conta é opcional)
- Nome pode ser alterado, UID não

## Banco de dados

**MongoDB**

Coleções:
- `beings` — dados dos seres vivos
- `proxies` — procuradores autorizados

## Endpoints principais

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/beings` | Cadastrar novo ser |
| GET | `/api/beings/{uid}` | Buscar ser por UID |
| PUT | `/api/beings/{uid}` | Atualizar dados |
| POST | `/api/beings/{uid}/proxies` | Adicionar procurador |
| GET | `/api/beings/{uid}/proxies` | Listar procuradores |

## Eventos publicados

| Tópico | Quando |
|--------|--------|
| `u7.being.registered` | Ser cadastrado com sucesso |
| `u7.being.updated` | Dados atualizados |
| `u7.proxy.added` | Procurador adicionado |
| `u7.proxy.revoked` | Procurador revogado |

## Dependências

- MongoDB
- Kafka (para eventos)
- Quarkus REST

## Stack

- Java 21
- Quarkus 3.32.4
- MongoDB Panache
- Kafka Messaging