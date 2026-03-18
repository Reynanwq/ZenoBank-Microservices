# ZenōBank — Distributed Financial Microservices Across 12 Universes


> **ZenōBank** é uma das maiores e mais complexas arquiteturas de microserviços já concebidas — um sistema financeiro distribuído que opera **12 bancos financeiros independentes**, um para cada universo, todos orquestrados por um sistema central de gestão chamado **God Panel**. Cada banco é um sistema financeiro completo e autônomo, com sua própria stack tecnológica, seus próprios bancos de dados, seus próprios domínios de negócio e seu próprio ciclo de deploy — análogos reais a instituições financeiras como Nubank, PicPay, Itaú, entre outros, mas operando em escala cósmica.

---

## Índice

1. [O Projeto](#1-o-projeto)
2. [Visão Macro da Arquitetura](#2-visão-macro-da-arquitetura)
3. [God Panel — Sistema Central](#3-god-panel--sistema-central)
4. [Os 12 Bancos Financeiros](#4-os-12-bancos-financeiros)
5. [Infraestrutura Global](#5-infraestrutura-global)
6. [Padrões e Contratos Globais](#6-padrões-e-contratos-globais)
7. [Arquitetura Multi-Módulo](#7-arquitetura-multi-módulo)
8. [Banco Universo 7 — Especificação Completa](#8-banco-universo-7--especificação-completa)
9. [Bancos U1–U6 e U8–U12](#9-bancos-u1u6-e-u8u12)
10. [Glossário Global](#10-glossário-global)

---

## 1. O Projeto

### O que é o ZenōBank

O ZenōBank é um **ecossistema de microserviços financeiros distribuídos** composto por:

- **12 bancos financeiros independentes** — um por universo
- **1 sistema central de orquestração** — o God Panel
- **1 barramento de eventos global** — Kafka compartilhado entre todos os bancos
- **Serviços de infraestrutura compartilhados** — autenticação global, observabilidade, compliance, notificações

No total, estamos falando de dezenas de microserviços, centenas de endpoints, múltiplos paradigmas tecnológicos convivendo em paralelo, e uma base de dados que cobre **todos os seres vivos e todos os planetas de 12 universos distintos**.

### Por que 12 bancos independentes e não um único sistema

A decisão arquitetural central do ZenōBank é que **cada universo é uma jurisdição financeira soberana**. Assim como no mundo real diferentes países têm diferentes sistemas bancários, regulações e moedas, cada universo do ZenōBank opera sob suas próprias regras, sua própria stack e seu próprio modelo de dados. As consequências técnicas dessa decisão são:

- **Falha isolada** — se o banco do Universo 9 cair, os outros 11 continuam operando
- **Deploy independente** — cada banco tem seu próprio pipeline de CI/CD
- **Escalabilidade independente** — o Universo 11 pode ter 10x mais carga que o Universo 3 e escalar de forma autônoma
- **Tecnologia adequada ao domínio** — cada universo escolhe sua stack no momento da sua especificação
- **Times independentes** — cada banco pode ser desenvolvido por um time dedicado sem afetar os demais

### Escala do projeto

| Dimensão | Estimativa |
|----------|-----------|
| Total de módulos raiz | 13 (12 bancos + God Panel) + módulos de infra compartilhada |
| Total de sub-módulos | 80–120 (média de 7–10 por banco) |
| Bancos de dados distintos | 30–50 instâncias isoladas |
| Tópicos Kafka | 100+ tópicos entre locais e globais |
| Repositórios | Monorepo com workspace por módulo |
| Times de desenvolvimento | 1 time dedicado por universo + 1 time de plataforma |

---

## 2. Visão Macro da Arquitetura

```
┌─────────────────────────────────────────────────────────────────────┐
│                        GOD PANEL                                    │
│           Sistema Central de Orquestração e Monitoramento           │
│              Stack: a definir no momento da especificação           │
└───────┬──────────┬──────────┬──────────┬──────────┬────────────────┘
        │          │          │          │          │
        ▼          ▼          ▼          ▼          ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐
│  Banco   │ │  Banco   │ │  Banco   │ │  Banco   │ │    Banco     │
│   U1     │ │   U2     │ │   U3     │ │   U4     │ │   U5 … U12  │
│[stack TBD]│ │[stack TBD]│ │[stack TBD]│ │[stack TBD]│ │  [stack TBD] │
└──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────────┘
        │          │          │          │          │
        └──────────┴──────────┴──────────┴──────────┘
                              │
              ┌───────────────▼───────────────┐
              │     MESSAGE BUS — KAFKA        │
              │   Barramento de eventos global │
              └───────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌──────────────┐   ┌─────────────────┐   ┌──────────────────┐
│ Auth Service │   │ Observability   │   │ Compliance Svc   │
│ Global IAM   │   │ Prometheus +    │   │ Auditoria global │
│              │   │ Grafana + Jaeger│   │                  │
└──────────────┘   └─────────────────┘   └──────────────────┘
```

### Princípios arquiteturais globais

**Autonomia de banco:** Nenhum banco acessa diretamente o banco de dados de outro banco. A única forma de comunicação entre bancos é via eventos no Kafka ou via APIs expostas pelo God Panel.

**Event-driven por padrão:** Toda operação relevante publica um evento no Kafka. Outros serviços — dentro ou fora do mesmo banco — reagem a eventos, nunca fazem chamadas síncronas para domínios externos.

**Schema-on-write no ledger:** Todo lançamento financeiro é gravado com o estado completo no momento da operação (snapshot). Alíquotas, taxas e configurações que mudam ao longo do tempo são sempre registradas junto ao lançamento — nunca recalculadas depois.

**Imutabilidade contábil:** Registros financeiros liquidados nunca são alterados ou deletados. Correções geram novos registros. Isso vale para todos os 12 bancos.

**Sem transferência entre universos:** Cada banco opera exclusivamente dentro de seu universo. Interoperabilidade entre universos, se necessária no futuro, é responsabilidade do God Panel — não dos bancos individuais.

---

## 3. God Panel — Sistema Central

O God Panel é o sistema que o **Zeno-sama** usa para ter visibilidade e controle sobre todos os 12 universos financeiros. Ele não é um banco — não processa transações, não mantém contas. É um sistema de **orquestração, monitoramento e governança**.

### Responsabilidades

- **Dashboard de saúde em tempo real** de todos os 12 bancos — latência, volume de TXs, taxa de falhas, saldo consolidado
- **Relatórios consolidados** entre universos — visão financeira agregada de todo o ecossistema
- **Gestão de identidade global** — administração de acesso para operadores e administradores do sistema
- **Compliance e auditoria centralizada** — trilha de auditoria global, relatórios regulatórios
- **Gerenciamento de configuração** — parâmetros globais que afetam todos os bancos
- **Alertas e incidentes** — detecção de anomalias, acionamento de SRE

### Stack

> A stack do God Panel será definida no momento da sua especificação, assim como ocorre com cada banco universo.

### O que o God Panel NÃO faz

- Não processa transações financeiras
- Não acessa diretamente os bancos de dados dos 12 bancos
- Não interfere em operações em andamento
- Não é um ponto único de falha para os bancos — se o God Panel cair, os 12 bancos continuam operando normalmente

---

## 4. Os 12 Bancos Financeiros

Cada banco é um **sistema financeiro completo e autônomo**, com todos os componentes que uma instituição financeira real possui: contas, transações, cartões, crédito, investimentos, notificações, compliance.

### Stacks — decisão intencional e tardia

**As stacks dos bancos ainda não foram definidas.** Essa é uma decisão intencional do ZenōBank: cada universo escolherá sua stack no momento da sua especificação formal, respeitando as seguintes regras:

- **Uma stack por universo** — quando um universo escolhe sua linguagem e banco de dados, **tudo dentro dele** usa essa mesma stack. Se o Universo 7 for implementado em Java, todos os seus sub-módulos (being-service, account-service, clearing-service, etc.) serão Java. Não existe mescla de linguagens dentro de um mesmo universo.
- **Stacks diferentes entre universos são encorajadas** — reflete a filosofia de que cada universo tem sua própria identidade tecnológica.
- **O U7 (GalactiBank) é o único já especificado** — sua stack está definida na seção 8. Os demais estão com stack `[A DEFINIR]`.

### Tabela de bancos

| Universo | Banco | Stack | Status |
|----------|-------|-------|--------|
| U1 | NovaPay | `[A DEFINIR]` | `[A DEFINIR]` |
| U2 | PicBank | `[A DEFINIR]` | `[A DEFINIR]` |
| U3 | CyberBank | `[A DEFINIR]` | `[A DEFINIR]` |
| U4 | AstutoBank | `[A DEFINIR]` | `[A DEFINIR]` |
| U5 | IronVault | `[A DEFINIR]` | `[A DEFINIR]` |
| U6 | TwinBank | `[A DEFINIR]` | `[A DEFINIR]` |
| **U7** | **GalactiBank** | **Ver seção 8** | **Especificado ✓** |
| U8 | SwiftBank | `[A DEFINIR]` | `[A DEFINIR]` |
| U9 | BasicBank | `[A DEFINIR]` | `[A DEFINIR]` |
| U10 | ElephantBank | `[A DEFINIR]` | `[A DEFINIR]` |
| U11 | HeroBank | `[A DEFINIR]` | `[A DEFINIR]` |
| U12 | ZenBank | `[A DEFINIR]` | `[A DEFINIR]` |

### O que todo banco tem em comum

Independente da stack, todos os 12 bancos implementam os mesmos **domínios de negócio obrigatórios**:

```
Domínios obrigatórios por banco
├── Identity Domain      — seres vivos, planetas, autenticação local
├── Account Domain       — abertura, gestão e encerramento de contas
├── Transaction Domain   — movimentações intraplanetárias
├── Clearing Domain      — movimentações interplanetárias + impostos
├── Credit Domain        — crédito, análise de risco, cobrança
├── Card Domain          — emissão e gestão de cartões
├── Ledger Domain        — livro contábil imutável
└── Notification Domain  — comunicação com clientes
```

E todos publicam e consomem eventos seguindo o **contrato global de eventos** (ver seção 6).

---

## 5. Infraestrutura Global

### Kubernetes

Toda a infraestrutura do ZenōBank roda em Kubernetes. Cada banco tem seu próprio **namespace isolado**:

```
Namespaces Kubernetes
├── omnibank-god-panel
├── omnibank-u1
├── omnibank-u2
├── omnibank-u3
├── ...
├── omnibank-u7
├── ...
├── omnibank-u12
├── omnibank-kafka          ← compartilhado
├── omnibank-auth           ← compartilhado
├── omnibank-observability  ← compartilhado
└── omnibank-compliance     ← compartilhado
```

Cada namespace tem seus próprios `ResourceQuota` e `LimitRange` — um banco não pode consumir recursos de outro.

### Kafka — Barramento global

O Kafka é o único canal de comunicação **entre** bancos diferentes. Tópicos são particionados por universo:

```
Convenção de nomenclatura de tópicos
├── global.*          → eventos consumidos pelo God Panel (todos os bancos publicam)
├── u7.*              → eventos internos do Universo 7
├── u7.transaction.*  → eventos de transações do U7
├── u7.account.*      → eventos de contas do U7
└── ...               → mesma convenção para os outros universos
```

### Observabilidade global

| Camada | Ferramenta | Escopo |
|--------|-----------|--------|
| Métricas | Prometheus + Grafana | Por banco e global |
| Tracing distribuído | Jaeger | Rastreia TX entre microserviços |
| Logs | Loki + Grafana | Centralizado, separado por namespace |
| Alertas | Alertmanager | SLOs por banco + alertas globais |

### CI/CD

Cada banco tem seu próprio pipeline de CI/CD — totalmente independente. Um deploy no U7 não afeta nenhum outro banco. O God Panel tem seu próprio pipeline separado.

### Secrets e configuração

- **Vault** — gerenciamento de segredos (credenciais de banco, chaves de API, certificados)
- **ConfigMaps** — configurações não-sensíveis por ambiente
- **Helm Charts** — um chart por banco, versionado e parametrizado por ambiente

---

## 6. Padrões e Contratos Globais

Para que 12 bancos independentes coexistam no mesmo ecossistema, alguns contratos precisam ser globais e imutáveis.

### Contrato de eventos Kafka

Todo evento publicado por qualquer banco segue o envelope:

```json
{
  "event_id": "uuid-v4",
  "event_type": "transaction.settled",
  "universe": 7,
  "source_service": "clearing-service",
  "timestamp": "2024-01-01T00:00:00Z",
  "schema_version": "1.0",
  "payload": { }
}
```

### Eventos globais obrigatórios

Todo banco **deve** publicar estes eventos no tópico `global.*`:

| Evento | Quando |
|--------|--------|
| `global.transaction.settled` | Toda TX liquidada |
| `global.transaction.failed` | Toda TX com falha |
| `global.account.opened` | Toda nova conta aberta |
| `global.account.frozen` | Toda conta congelada |
| `global.health.heartbeat` | A cada 30 segundos |

### Moeda

Cada universo define sua própria política monetária. O Universo 7, por exemplo, opera com moeda única universal (UC). Outros universos podem ter moedas locais por planeta. Esse contrato é definido individualmente por cada banco — não há imposição global de modelo monetário.

### Identificadores

Todos os IDs em todos os bancos seguem o padrão **UUID v4**. Isso garante unicidade global sem coordenação central.

---

## 7. Arquitetura Multi-Módulo

O ZenōBank é construído com **uso intenso e pesado de multi-módulo**. Toda a organização de código reflete essa decisão arquitetural — desde o nível mais alto (o monorepo) até o nível mais interno de cada banco.

### 7.1 Visão geral — os 13 módulos raiz

O projeto inteiro vive em um **monorepo com workspace**. Cada banco é um módulo completamente autônomo, com seu próprio arquivo de dependências, seu próprio `Dockerfile` e seu próprio pipeline de CI/CD. Um `install` dentro de `u7-galactibank` não puxa nada de `u3-cyberbank`.

```
@zenobank/ (monorepo raiz)
│
├── god-panel/                  ← módulo 1
│
├── u1-novapay/                 ← módulo 2
├── u2-picbank/                 ← módulo 3
├── u3-cyberbank/               ← módulo 4
├── u4-astutobank/              ← módulo 5
├── u5-ironvault/               ← módulo 6
├── u6-twinbank/                ← módulo 7
├── u7-galactibank/             ← módulo 8 (especificado ✓)
├── u8-swiftbank/               ← módulo 9
├── u9-basicbank/               ← módulo 10
├── u10-elephantbank/           ← módulo 11
├── u11-herobank/               ← módulo 12
├── u12-zenbank/                ← módulo 13
│
└── infra/                      ← módulos de infraestrutura compartilhada
    ├── kafka-bus/
    ├── auth-global/
    ├── observability/
    └── compliance/
```

> Os módulos de infra compartilhada não contam como "bancos" — são pacotes utilitários consumidos pelos 13 módulos raiz.

### 7.2 Sub-módulos dentro de cada banco

Dentro de cada banco, cada domínio de negócio é também um **módulo separado**. O mecanismo exato de modularização depende da linguagem escolhida pelo universo (módulo NestJS, pacote Maven, pacote Go, etc.), mas o princípio é o mesmo em todos: **cada microserviço é um sub-módulo com fronteiras bem definidas**.

A estrutura abaixo usa o U7 como exemplo de referência — os outros bancos seguirão o mesmo padrão de organização, adaptado à sua stack:

```
u7-galactibank/
│
├── apps/
│   └── api-gateway/            ← entry point do banco (módulo raiz interno)
│
└── modules/
    ├── being/                  ← @u7/being
    ├── planet/                 ← @u7/planet
    ├── account/                ← @u7/account
    ├── transaction/            ← @u7/transaction
    ├── clearing/               ← @u7/clearing  ← módulo mais crítico
    ├── credit/                 ← @u7/credit
    ├── card/                   ← @u7/card
    ├── ledger/                 ← @u7/ledger
    ├── notification/           ← @u7/notification
    ├── wealth/                 ← @u7/wealth
    └── auth/                   ← @u7/auth (proxy do auth-global)
```

Cada sub-módulo possui seus próprios controllers, services, repositórios, DTOs e entidades — sem acesso direto às entidades de outro sub-módulo.

### 7.3 Regra de stack por universo

> **Uma stack por universo — sem exceções.**

Quando um universo define sua stack (linguagem + banco de dados), **todos os seus sub-módulos obrigatoriamente usam essa mesma stack**. Não existe mescla de linguagens dentro de um mesmo universo.

| Exemplo | Consequência |
|---------|-------------|
| Universo X define Java | being-service, account-service, clearing-service... todos em Java |
| Universo Y define Go | being-service, account-service, clearing-service... todos em Go |
| Universo Z define Python | being-service, account-service, clearing-service... todos em Python |

Isso garante coerência tecnológica interna, facilita onboarding de devs no time do universo e elimina complexidade de interoperabilidade dentro do mesmo banco.

### 7.4 Regras de comunicação entre módulos

| Nível | Comunicação permitida | Comunicação proibida |
|-------|----------------------|----------------------|
| Entre bancos diferentes | Kafka (eventos) | Import direto de código, chamada direta de DB |
| Entre sub-módulos do mesmo banco | DI do framework (service exportado) | Acesso direto ao repositório de outro módulo |
| Sub-módulo → infra compartilhada | Import do pacote `@zenobank/auth-global` etc. | Reimplementar o que já existe na infra |

A comunicação entre bancos **sempre passa pelo Kafka** — nunca por chamada HTTP direta entre módulos de universos distintos.

### 7.5 O que ainda será detalhado

A especificação interna de cada sub-módulo (controllers, services, DTOs, entidades, testes, contratos de API) será feita banco por banco, à medida que cada universo for especificado. O U7 serve de referência de processo para os demais.

---

## 8. Banco Universo 7 — Especificação Completa

> O Universo 7 é o universo do Goku. Seu banco financeiro, o **GalactiBank**, é o primeiro a ser completamente especificado e serve de referência de implementação para os demais universos.

### 8.1 Contexto do Universo 7

O GalactiBank é responsável por coordenar as informações financeiras de **todos os seres vivos e todos os planetas** do Universo 7. Opera como um banco financeiro real — contas, transações, crédito, cartões, investimentos — com a particularidade de que seus clientes incluem tanto seres individuais quanto planetas inteiros como entidades financeiras.

**Decisões que definem o U7:**

- **Moeda única universal (UC)** — sem câmbio, sem conversão entre planetas
- **Seres e planetas como clientes** — ambos têm conta, ambos podem transacionar
- **Imposto interplanetário** — cada planeta define sua alíquota; cobrado automaticamente no clearing
- **Crédito sempre local** — crédito aprovado em um planeta não pode ser usado em outro

### 8.2 Stack

| Camada | Tecnologia |
|--------|-----------|
| Runtime | Node.js |
| Framework | NestJS |
| Banco principal | PostgreSQL |
| Banco de produtos | MongoDB |
| Cache / Hold | Redis |
| Mensageria | Kafka |
| Containerização | Kubernetes + Helm |
| Observabilidade | Prometheus + Grafana + Jaeger |
| Secrets | Vault |

> Todos os sub-módulos do U7 (being-service, account-service, clearing-service, etc.) são implementados em Node.js + NestJS. Nenhum sub-módulo do U7 usa linguagem ou framework diferente.

### 8.3 Arquitetura interna

```
┌──────────────────────────────────────────────────────────────────┐
│                    Canais de Acesso                               │
│        App Mobile · Portal Web · API Pública · Painel Admin      │
└─────────────────────────┬────────────────────────────────────────┘
                          │
┌─────────────────────────▼────────────────────────────────────────┐
│               API Gateway  +  Auth / UID Service                  │
│     Rate limiting · JWT validation · UID lookup · Proxy check     │
└────┬──────────┬──────────┬──────────┬──────────┬─────────────────┘
     │          │          │          │          │
     ▼          ▼          ▼          ▼          ▼
┌─────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌──────────┐
│ Being   │ │Planet  │ │Account │ │Transact│ │Clearing  │
│ Module  │ │Module  │ │Module  │ │ Module │ │ Module   │
└─────────┘ └────────┘ └────────┘ └────────┘ └──────────┘
     │          │          │          │          │
     ▼          ▼          ▼          ▼          ▼
┌─────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌──────────┐
│ Credit  │ │  Card  │ │Wealth  │ │Notific.│ │ Ledger   │
│ Module  │ │Module  │ │Module  │ │ Module │ │ Module   │
└─────────┘ └────────┘ └────────┘ └────────┘ └──────────┘
                          │
        ┌─────────────────▼──────────────────────┐
        │           Event Bus — Kafka             │
        │        Tópicos: u7.* e global.*         │
        └─────────────────┬──────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        ▼                 ▼                 ▼
┌──────────────┐  ┌──────────────┐  ┌────────────┐
│  PostgreSQL  │  │   MongoDB    │  │   Redis    │
│ Seres·Contas │  │Crédito·Cards │  │Cache·Holds │
│ TXs · Ledger │  │              │  │            │
└──────────────┘  └──────────────┘  └────────────┘
```

### 8.4 Modelo de Dados

#### `BEING` — Ser Vivo

Todo ser vivo registrado no banco. A espécie é apenas um atributo descritivo — sem impacto em nenhuma lógica de negócio.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `uid` | UUID PK | Identificador único universal (CPF cósmico) — imutável |
| `full_name` | VARCHAR | Nome completo |
| `birth_date` | DATE | Data de nascimento |
| `origin_planet_id` | UUID FK | Planeta de origem |
| `species_label` | VARCHAR | Espécie (ex: "Saiyajin") — apenas descritivo |
| `status` | ENUM | `ACTIVE`, `INACTIVE` |
| `meta` | JSONB | Atributos extras flexíveis |
| `created_at` | TIMESTAMP | Data de registro |

---

#### `PLANET` — Planeta

Planeta é uma entidade financeira de primeira classe — tem conta, pode transacionar, define seus próprios impostos.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `planet_id` | UUID PK | Identificador único |
| `name` | VARCHAR | Nome do planeta |
| `galaxy` | VARCHAR | Galáxia onde está localizado |
| `population` | BIGINT | População registrada |
| `status` | ENUM | `ACTIVE`, `INACTIVE`, `DESTROYED` |
| `created_at` | TIMESTAMP | Data de registro |

> Planeta destruído nunca é deletado. Status muda para `DESTROYED` e todas as contas vinculadas são automaticamente congeladas (`FROZEN`). Nenhuma movimentação é permitida em contas de planetas destruídos.

---

#### `ACCOUNT` — Conta

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `account_id` | UUID PK | Identificador único |
| `owner_uid` | UUID | UID do ser vivo ou `planet_id` do planeta |
| `owner_type` | ENUM | `BEING` ou `PLANET` |
| `planet_id` | UUID FK | Planeta onde a conta está vinculada |
| `account_type` | ENUM | `CHECKING` · `FISCAL` |
| `balance` | NUMERIC | Saldo atual — nunca negativo |
| `status` | ENUM | `ACTIVE` · `FROZEN` · `CLOSED` |
| `opened_at` | TIMESTAMP | Data de abertura |

> `FISCAL` é o tipo de conta de um planeta destinada a receber impostos interplanetários.
> Um ser vivo pode ter no máximo **uma conta por planeta**.

---

#### `ACCOUNT_PROXY` — Procurador

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `proxy_id` | UUID PK | Identificador único |
| `account_id` | UUID FK | Conta representada |
| `proxy_uid` | UUID FK | UID do procurador |
| `permissions` | JSONB | Permissões (ex: `["transfer", "view", "card"]`) |
| `valid_until` | TIMESTAMP | Validade (null = indefinida) |
| `status` | ENUM | `ACTIVE` · `REVOKED` |
| `created_at` | TIMESTAMP | Data de criação |

---

#### `PLANET_TAX_CONFIG` — Configuração de Imposto

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `config_id` | UUID PK | Identificador único |
| `planet_id` | UUID FK | Planeta que define a alíquota |
| `tax_rate` | NUMERIC | Percentual (ex: `0.03` = 3%) |
| `tax_type` | ENUM | `OUTBOUND` · `INBOUND` |
| `valid_from` | TIMESTAMP | Início da vigência |
| `valid_until` | TIMESTAMP | Fim da vigência (null = vigente agora) |

> O histórico de alíquotas é preservado para auditoria. O Clearing Module sempre usa a alíquota vigente **no momento exato da transação**, nunca a atual.

---

#### `TRANSACTION` — Transação

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `tx_id` | UUID PK | Identificador único |
| `from_account_id` | UUID FK | Conta de origem |
| `to_account_id` | UUID FK | Conta de destino |
| `amount` | NUMERIC | Valor bruto enviado |
| `net_amount` | NUMERIC | Valor líquido recebido pelo destino |
| `tx_type` | ENUM | Tipo da transação |
| `is_interplanetary` | BOOLEAN | Se cruza planetas |
| `origin_tax_rate` | NUMERIC | Snapshot da alíquota de origem |
| `origin_tax_amount` | NUMERIC | Valor do imposto cobrado na origem |
| `dest_tax_rate` | NUMERIC | Snapshot da alíquota de destino |
| `dest_tax_amount` | NUMERIC | Valor do imposto cobrado no destino |
| `status` | ENUM | `INITIATED` · `PENDING` · `CLEARING` · `SETTLED` · `FAILED` · `REVERSED` |
| `created_at` | TIMESTAMP | Data/hora de criação |
| `settled_at` | TIMESTAMP | Data/hora de liquidação |

**Valores de `tx_type`:**

| Valor | Descrição |
|-------|-----------|
| `TRANSFER` | Transferência intraplanetária entre seres |
| `DIRECT_DEBIT` | Débito autorizado por terceiro |
| `BILL_PAYMENT` | Pagamento de fatura ou boleto |
| `CREDIT_DISBURSEMENT` | Banco libera valor de crédito aprovado |
| `CREDIT_REPAYMENT` | Ser paga parcela de crédito |
| `INTERPLANETARY_TRANSFER` | Transferência entre planetas |
| `CROSS_PLANET_PAYMENT` | Pagamento de serviço em outro planeta |
| `REVERSAL` | Estorno de transação anterior |

---

#### `UNIVERSE_LEDGER` — Livro Contábil

Registro imutável de todos os lançamentos. Nenhum registro pode ser alterado ou deletado — jamais.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `entry_id` | UUID PK | Identificador único do lançamento |
| `tx_id` | UUID FK | Transação que originou o lançamento |
| `entry_type` | ENUM | `DEBIT` · `CREDIT` · `TAX_ORIGIN` · `TAX_DEST` |
| `account_id` | UUID FK | Conta afetada |
| `amount` | NUMERIC | Valor do lançamento |
| `created_at` | TIMESTAMP | Data/hora — gravada uma vez, nunca alterada |

---

#### `TX_REVERSAL` — Estorno

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `reversal_id` | UUID PK | Identificador único |
| `original_tx_id` | UUID FK | TX original estornada |
| `reversal_tx_id` | UUID FK | Nova TX de estorno gerada |
| `reason` | TEXT | Motivo do estorno |
| `requested_by` | UUID | UID de quem solicitou |
| `created_at` | TIMESTAMP | Data do estorno |

---

#### `CREDIT` — Crédito

Crédito é sempre local ao planeta. Não existe crédito interplanetário.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `credit_id` | UUID PK | Identificador único |
| `account_id` | UUID FK | Conta vinculada |
| `credit_type` | ENUM | `PERSONAL_LOAN` · `FINANCING` · `OVERDRAFT_PROTECTION` |
| `total_amount` | NUMERIC | Valor total aprovado |
| `disbursed_amount` | NUMERIC | Valor já liberado |
| `remaining` | NUMERIC | Saldo devedor |
| `interest_rate` | NUMERIC | Taxa de juros |
| `due_date` | DATE | Data de vencimento |
| `status` | ENUM | `ACTIVE` · `PAID` · `DEFAULTED` · `CANCELLED` |
| `created_at` | TIMESTAMP | Data de aprovação |

---

#### `CARD` — Cartão

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `card_id` | UUID PK | Identificador único |
| `account_id` | UUID FK | Conta vinculada |
| `card_type` | ENUM | `DEBIT` · `CREDIT` |
| `status` | ENUM | `ACTIVE` · `BLOCKED` · `CANCELLED` |
| `created_at` | TIMESTAMP | Data de emissão |

---

### 8.5 Regras de Negócio

#### Seres vivos

- Todo ser recebe um **UID único e imutável** no momento do cadastro — independente de planeta, espécie ou nome
- Espécie é **apenas atributo descritivo** — sem impacto em nenhuma lógica de negócio
- Um ser pode ter **uma conta por planeta** — múltiplos planetas permitidos simultaneamente
- Um ser pode nomear **procuradores** com permissões específicas via `ACCOUNT_PROXY`
- Não existe limite de movimentação — qualquer valor pode ser transacionado
- Morte de seres não é escopo da versão atual

#### Planetas

- Planeta é entidade financeira: tem conta própria (`owner_type = PLANET`), pode enviar e receber transferências
- Planetas são **isentos de imposto interplanetário** — apenas seres vivos pagam
- Cada planeta define sua própria alíquota via `PLANET_TAX_CONFIG`
- Planeta destruído tem status `DESTROYED` — todas as contas vinculadas ficam `FROZEN` automaticamente
- Planetas **nunca são deletados** do sistema

#### Contas e saldo

- Saldo em conta corrente **nunca pode ser negativo** — transação bloqueada se saldo insuficiente
- Dívida existe apenas como produto de **crédito** (`CREDIT`) — nunca como saldo negativo em conta corrente
- Conta `FROZEN` não permite nenhuma movimentação — nem entrada nem saída
- Um ser não pode abrir mais de uma conta no mesmo planeta

#### Crédito

- Crédito é **sempre vinculado a uma conta específica em um planeta específico**
- Crédito aprovado no Planeta A **não pode ser usado** no Planeta B
- Análise, aprovação e cobrança são responsabilidade exclusiva do `Credit Module`

#### Transações

- TX liquidada (`SETTLED`) é **imutável** — nunca alterada, nunca deletada
- Estorno gera uma **nova TX** com `tx_type = REVERSAL` vinculada via `TX_REVERSAL`
- Se o ser destino **não tiver conta** no planeta de destino, TX falha com `FAILED`
- Não existe transferência entre universos — apenas dentro do Universo 7
- Não existe limite de valor por transação

---

### 8.6 Tipos de Movimentação

#### Intraplanetária (mesmo planeta) — 2 lançamentos no ledger

| Tipo | Descrição |
|------|-----------|
| `TRANSFER` | Ser A envia para Ser B no mesmo planeta |
| `DIRECT_DEBIT` | Ser B autorizado a debitar de Ser A |
| `BILL_PAYMENT` | Pagamento de fatura ou boleto |
| `CREDIT_DISBURSEMENT` | Banco libera crédito na conta do ser |
| `CREDIT_REPAYMENT` | Ser paga parcela de crédito |

Fluxo: `INITIATED → SETTLED` — operação atômica, 2 lançamentos, sem estado intermediário.

#### Interplanetária (planetas diferentes) — 4 lançamentos no ledger

Cada lado paga o imposto do **seu próprio planeta**:
- Quem envia: paga o imposto do planeta de **origem** (descontado do seu saldo)
- Quem recebe: paga o imposto do planeta de **destino** (descontado do valor recebido)

**Exemplo numérico:** A1 envia 100 UC · Planeta A cobra 3% · Planeta B cobra 5%

| Lançamento | Tipo | Conta afetada | Valor |
|-----------|------|--------------|-------|
| 1 | `DEBIT` | Conta de A1 | −103 UC |
| 2 | `TAX_ORIGIN` | Conta fiscal do Planeta A | +3 UC |
| 3 | `TAX_DEST` | Conta fiscal do Planeta B | +5 UC |
| 4 | `CREDIT` | Conta de B1 | +95 UC |

**Ciclo de estados:**
```
INITIATED → PENDING (hold aplicado no saldo de A1)
          → CLEARING (Clearing Module roteando)
          → SETTLED (4 lançamentos gravados atomicamente)
          → FAILED (saldo insuficiente / conta destino inexistente / timeout)
          → REVERSED (estorno solicitado após liquidação)
```

---

### 8.7 Clearing Module

O Clearing Module é o componente mais crítico do GalactiBank — responsável exclusivo pelo processamento de transações interplanetárias.

#### Responsabilidades (em ordem de execução)

1. Verificar existência de conta destino no planeta destino — se não existir, `FAILED` imediato
2. Verificar saldo disponível via `Account Module` — se insuficiente, `FAILED` imediato
3. Aplicar **hold** no saldo de origem — TX entra em `PENDING`
4. Consultar alíquotas vigentes de ambos os planetas em `PLANET_TAX_CONFIG`
5. Calcular os 4 valores:
   - `origin_tax = amount × origin_tax_rate`
   - `total_debit = amount + origin_tax`
   - `dest_tax = amount × dest_tax_rate`
   - `net_amount = amount − dest_tax`
6. Executar os 4 lançamentos **atomicamente** no ledger — tudo ou nada, sem estado parcial
7. Gravar snapshot das alíquotas diretamente na `TRANSACTION`
8. Publicar evento `u7.transaction.settled` no Kafka
9. Liberar hold após confirmação de liquidação

#### Tratamento de falhas

| Situação | Comportamento |
|----------|---------------|
| Conta destino inexistente | TX → `FAILED`, hold não aplicado |
| Saldo insuficiente | TX → `FAILED`, hold não aplicado |
| Timeout no clearing | Hold liberado, TX → `FAILED`, rollback total |
| Erro parcial nos lançamentos | Rollback de todos os lançamentos, TX → `FAILED` |

#### O que o Clearing Module NÃO faz

- Não define alíquotas — responsabilidade do `Planet Module`
- Não aprova crédito — responsabilidade do `Credit Module`
- Não notifica seres — responsabilidade do `Notification Module`
- Não processa transações intraplanetárias — responsabilidade do `Transaction Module`

---

### 8.8 Sub-módulos internos

| Módulo | Responsabilidade |
|--------|----------------|
| `being` | Cadastro de seres, emissão de UID, gestão de procuradores |
| `planet` | Cadastro de planetas, alíquotas, mudanças de status |
| `auth` | Autenticação, JWT, validação de UID, verificação de proxy |
| `account` | Abertura/encerramento de contas, saldo, holds, congelamento |
| `transaction` | Transações intraplanetárias — 2 lançamentos atômicos |
| `clearing` | Transações interplanetárias — 4 lançamentos atômicos + impostos |
| `credit` | Análise, aprovação, liberação e cobrança de crédito |
| `card` | Emissão, bloqueio, fatura de cartões |
| `wealth` | Investimentos e produtos de renda |
| `ledger` | Leitura e auditoria do livro contábil imutável |
| `notification` | Notificações via Kafka — push, mensagem, comunicados |

### 8.9 Tópicos Kafka do U7

| Tópico | Publicado por | Consumido por |
|--------|--------------|---------------|
| `u7.being.registered` | being | auth, notification |
| `u7.account.opened` | account | notification |
| `u7.account.frozen` | account | card, credit |
| `u7.transaction.initiated` | transaction / clearing | notification |
| `u7.transaction.settled` | transaction / clearing | ledger, notification, God Panel |
| `u7.transaction.failed` | transaction / clearing | notification |
| `u7.planet.destroyed` | planet | account |
| `u7.planet.tax.updated` | planet | clearing |
| `u7.credit.approved` | credit | notification |
| `global.transaction.settled` | clearing | God Panel |
| `global.health.heartbeat` | api-gateway | God Panel |

---

### 8.10 Decisões de Design do U7

| # | Decisão | Justificativa |
|---|---------|---------------|
| 1 | UID como identidade primária | Nome e espécie podem mudar ou se repetir. UID é imutável e cósmico. |
| 2 | Espécie como atributo | Sem lógica de negócio diferenciada por espécie. Simplifica o modelo. |
| 3 | Uma conta por planeta por ser | Evita fragmentação de saldo e complexidade de consolidação. |
| 4 | Saldo nunca negativo | Integridade financeira. Dívida existe apenas em produtos de crédito. |
| 5 | Crédito sempre local ao planeta | Isola risco de crédito por jurisdição planetária. |
| 6 | Moeda única universal | Elimina câmbio e simplifica radicalmente o modelo transacional. |
| 7 | Imposto como percentual puro | Sem taxa fixa — modelo simples e justo em escala cósmica. |
| 8 | Planetas isentos de imposto | Planetas são soberanos — tributá-los conflitaria com suas próprias alíquotas. |
| 9 | 4 lançamentos atômicos no clearing | Garante consistência contábil em transações interplanetárias. |
| 10 | Snapshot de alíquota na TX | Alíquotas mudam. O registro deve refletir o momento exato da operação. |
| 11 | Ledger imutável | Auditoria e compliance exigem trilha contábil permanente. |
| 12 | Estorno via nova TX | Preserva integridade do ledger. Estorno é evento financeiro, não correção técnica. |
| 13 | Status para planeta destruído | Planetas têm histórico financeiro. Deletar apagaria registros contábeis permanentes. |
| 14 | Sem transferência entre universos | Cada universo é jurisdição financeira independente. |
| 15 | Sem limites de movimentação | U7 não impõe restrições de valor. Controle de risco é do Credit Module. |
| 16 | Stack única (Node.js + NestJS) para todo o U7 | Coerência tecnológica interna. Todos os sub-módulos falam a mesma linguagem. |

---

## 9. Bancos U1–U6 e U8–U12

> Os demais 11 bancos ainda não foram especificados. Cada um seguirá o mesmo processo de design utilizado no Universo 7 — definição de stack, mapeamento de regras de negócio, modelo de dados, sub-módulos e decisões de arquitetura — respeitando as particularidades de cada universo.

Todos os bancos devem:

- Implementar os **domínios obrigatórios** listados na seção 4
- Seguir o **contrato global de eventos** da seção 6
- Publicar os **eventos globais obrigatórios** para o God Panel
- Respeitar os **padrões de nomenclatura** de tópicos Kafka
- Adotar **uma única stack** para todo o universo (linguagem + banco de dados)
- Organizar seus microserviços como **sub-módulos** seguindo o padrão da seção 7
- Operar com **isolamento total** de dados em relação aos outros bancos

A especificação de cada banco será adicionada a este README à medida que for concluída.

| Banco | Stack | Status da especificação |
|-------|-------|------------------------|
| U1 — NovaPay | `[A DEFINIR]` | `[A DEFINIR]` |
| U2 — PicBank | `[A DEFINIR]` | `[A DEFINIR]` |
| U3 — CyberBank | `[A DEFINIR]` | `[A DEFINIR]` |
| U4 — AstutoBank | `[A DEFINIR]` | `[A DEFINIR]` |
| U5 — IronVault | `[A DEFINIR]` | `[A DEFINIR]` |
| U6 — TwinBank | `[A DEFINIR]` | `[A DEFINIR]` |
| **U7 — GalactiBank** | **Node.js + NestJS + PostgreSQL + MongoDB + Redis** | **Especificado ✓** |
| U8 — SwiftBank | `[A DEFINIR]` | `[A DEFINIR]` |
| U9 — BasicBank | `[A DEFINIR]` | `[A DEFINIR]` |
| U10 — ElephantBank | `[A DEFINIR]` | `[A DEFINIR]` |
| U11 — HeroBank | `[A DEFINIR]` | `[A DEFINIR]` |
| U12 — ZenBank | `[A DEFINIR]` | `[A DEFINIR]` |

---

## 10. Glossário Global

| Termo | Definição |
|-------|-----------|
| **UID** | Universal ID — identificador único e imutável de um ser vivo |
| **UC** | Universal Currency — moeda única do Universo 7 |
| **Hold** | Reserva temporária de saldo durante o processamento de uma TX interplanetária |
| **Clearing** | Processo de roteamento e liquidação de transação interplanetária |
| **Ledger** | Livro contábil imutável com todos os lançamentos financeiros |
| **Conta fiscal** | Conta de um planeta destinada a receber impostos interplanetários |
| **Snapshot de alíquota** | Cópia da alíquota vigente gravada diretamente na transação no momento da liquidação |
| **Procurador** | Ser autorizado a operar a conta de outro ser com permissões específicas |
| **God Panel** | Sistema central de orquestração, monitoramento e governança dos 12 universos |
| **Owner type** | Tipo do titular de uma conta: `BEING` (ser vivo) ou `PLANET` (planeta) |
| **Namespace** | Isolamento lógico no Kubernetes — cada banco tem o seu |
| **Evento global** | Evento publicado no Kafka e consumido pelo God Panel para monitoramento centralizado |
| **TX** | Abreviação de Transaction — transação financeira |
| **Jurisdição financeira** | Conjunto de regras, moeda e políticas de um universo — análogo a um país no mundo real |
| **Módulo raiz** | Um dos 13 módulos de primeiro nível do monorepo (12 bancos + God Panel) |
| **Sub-módulo** | Módulo interno de um banco universo — representa um domínio de negócio (being, account, clearing, etc.) |
| **Stack por universo** | Regra que obriga todos os sub-módulos de um universo a usarem a mesma linguagem e banco de dados |
| **Multi-módulo** | Padrão arquitetural do ZenōBank onde cada banco e cada domínio interno são módulos independentes e isolados |

---

*Este documento é o contrato de especificação do ZenōBank.*
*Toda alteração em regras de negócio, modelo de dados, decisões de arquitetura ou estrutura de módulos deve ser refletida aqui antes de qualquer implementação.*
*Universos não especificados devem seguir o U7 como referência de processo.*
