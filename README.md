# Sistema de Controle de Estoque com Leitor de Código de Barras

---

## 📌 Contexto

Este projeto simula um **sistema de controle de estoque** para um ambiente real (comércio, almoxarifado ou indústria), utilizando um **leitor de código de barras USB**, tratado como um dispositivo de entrada padrão (teclado).

O sistema foi desenvolvido em **Java puro**, com foco em **modelagem correta de domínio**, **boas práticas de arquitetura** e **clareza das regras de negócio**, sem dependência de frameworks ou banco de dados.

O objetivo é demonstrar **capacidade técnica, organização e pensamento de software orientado ao domínio**, e não apenas funcionamento.

---

## 🎯 Objetivo do Projeto

Construir uma aplicação console capaz de:

- Controlar entrada e saída de produtos por código de barras
- Manter o saldo de estoque consistente
- Registrar o histórico completo de movimentações
- Garantir regras de negócio no **domínio**, não no fluxo de UI

---

## 🧠 Princípios e Decisões de Design

- **Domínio rico**: entidades protegem seu próprio estado
- **Produto identificado unicamente pelo código de barras**
- **BigDecimal** utilizado para saldo, permitindo:

  - produtos fracionados (metros, peso, volume)
  - precisão em operações

- **Separação clara de responsabilidades**:

  - UI → interação com usuário
  - Service → orquestração do fluxo
  - Domain → regras de negócio
  - Repository → persistência (in-memory)

- **Sem frameworks**, para evidenciar domínio e arquitetura

---

## 📋 Requisitos Funcionais

### RF01 – Registro de Produtos via Código de Barras

- O sistema recebe códigos de barras como `String`
- O código identifica unicamente um produto
- Caso o produto não exista, ele é criado automaticamente

---

### RF02 – Entrada de Produto

- Incrementa o saldo do produto
- Quantidade pode ser **fracionada** (`BigDecimal`)
- Quantidade deve ser **maior que zero**
- Operação inválida gera erro de domínio

---

### RF03 – Saída de Produto

- Decrementa o saldo do produto
- Impede saída se:

  - quantidade ≤ 0
  - quantidade maior que o saldo disponível

- Nunca permite saldo negativo

---

### RF04 – Saldo de Estoque

- O saldo pertence ao **Produto**
- Não existe setter direto para saldo
- Toda alteração ocorre via métodos de domínio

---

### RF05 – Histórico de Movimentações

- Toda entrada ou saída válida gera uma movimentação
- Cada movimentação contém:

  - código do produto
  - tipo (ENTRADA / SAÍDA)
  - quantidade movimentada
  - data e hora

- Histórico pode ser listado posteriormente

---

## 🧩 Regras de Negócio (Invariantes)

- Estoque nunca pode ser negativo
- Quantidades devem ser maiores que zero
- Produto não pode existir sem código
- Código de barras não pode ser nulo ou em branco
- Movimentações só são registradas se a operação for válida

---

## 🗂️ Estrutura de Pacotes

```
src/
 ├── domain/
 │    ├── Product.java
 │    ├── StockMovement.java
 │    └── MovementType.java
 │
 ├── repository/
 │    ├── ProductRepository.java
 │    ├── ProductRepositoryImpl.java
 │    ├── MovementRepository.java
 │    └── MovementRepositoryImpl.java
 │
 ├── service/
 │    └── StockService.java
 │
 ├── ui/
 │    └── ConsoleUI.java
 │
 └── Main.java
```

---

## 🧪 Testes

- Testes unitários focados na **camada de serviço**
- Validação das regras de negócio
- Uso de JUnit 5
- Repositórios em memória para isolamento dos testes

---

## 📈 Diferenciais Técnicos

- Domínio rico e encapsulado
- Uso consciente de `BigDecimal`
- Repositórios desacoplados (facilmente substituíveis)
- Código preparado para evolução (arquivo, banco, API)
- Commits pequenos e bem descritos

---

## 🏁 Considerações Finais

Este projeto foi desenvolvido com foco em **clareza, robustez e boas práticas**, simulando um cenário real de controle de estoque, mesmo utilizando apenas Java puro e aplicação console.

O objetivo não é apenas funcionar, mas **mostrar maturidade técnica, organização e capacidade de modelar regras de negócio reais**.

---
