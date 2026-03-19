# @zenobank/u7-galactibank — GalactiBank

> Banco financeiro do Universo 7 — o universo do Goku.
> Primeiro banco completamente especificado — referência de processo para os demais.

## Stack

| Camada | Tecnologia |
|--------|-----------|
| Runtime | Node.js 20 |
| Framework | NestJS |
| Banco principal | PostgreSQL |
| Banco de produtos | MongoDB |
| Cache / Hold | Redis |
| Mensageria | Kafka |

## Sub-módulos

```
u7-galactibank/
├── apps/
│   └── api-gateway/
└── modules/
    ├── being/
    ├── planet/
    ├── account/
    ├── transaction/
    ├── clearing/        ← módulo mais crítico
    ├── credit/
    ├── card/
    ├── ledger/
    ├── notification/
    ├── wealth/
    └── auth/
```

Especificação completa: README raiz — seção 8.
