# Sistema de Controle de Estoque com Leitor de Código de Barras

## O que é isso?

Basicamente é um sistema de estoque com interface gráfica. A ideia era fazer algo que pudesse ser usado de verdade num comércio ou almoxarifado, usando aqueles leitores de código de barras USB (que o computador lê como se fosse um teclado mesmo).

Usei JavaFX com Scene Builder pra interface. Separei o FXML e o CSS em arquivos diferentes pra ficar mais organizado. O foco era deixar o código bem estruturado e com as regras de negócio no lugar certo.

## O que dá pra fazer

- Registrar entrada e saída de produtos pelo código de barras
- Ver o saldo atual de cada produto
- Consultar o histórico de todas as movimentações
- O sistema não deixa você tirar mais produto do que tem em estoque

## Por que fiz algumas coisas de um jeito específico

**BigDecimal pro saldo:** Porque tem produto que você vende por peso, metro, litro... então o estoque não é sempre um número inteiro. E BigDecimal evita aqueles erros doidos de arredondamento.

**Produto se identifica pelo código de barras:** Na vida real é assim mesmo. Um código = um produto.

**Domínio rico:** Tentei fazer as classes do domínio (Product, StockMovement) se protegerem sozinhas. Por exemplo, você não consegue deixar o estoque negativo porque a própria classe Product não permite.

**Separação em camadas:**

- `domain` = as regras do negócio (Product, Historico, Localizacao, enums...)
- `service` = organiza o fluxo das operações
- `repository` = guarda os dados (por enquanto só em memória)
- `controller` = faz a ponte entre a interface e o service
- `application` = inicia o JavaFX
- `resources` = arquivos FXML e CSS separados

## Funcionalidades

### Cadastro de produto

Quando você escaneia um código de barras novo, o sistema cria o produto automaticamente.

### Entrada de produto

Adiciona quantidade no estoque. Pode ser número quebrado tipo 2.5kg, 10.75m, etc.

### Saída de produto

Tira do estoque. O sistema checa se tem quantidade suficiente antes de deixar você fazer a saída.

### Histórico

Toda entrada e saída fica registrada com data/hora, tipo de movimento e quantidade.

## Regras que o sistema garante

- Estoque nunca fica negativo
- Não dá pra movimentar quantidade zero ou negativa
- Todo produto precisa ter um código de barras válido
- Só registra no histórico se a operação for válida

## Como tá organizado

```
.
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── br
│   │   │           ├── application
│   │   │           │   └── EstoqueApp.java
│   │   │           ├── controller
│   │   │           │   └── EstoqueController.java
│   │   │           ├── domain
│   │   │           │   ├── ConversorCodigo.java
│   │   │           │   ├── enums
│   │   │           │   │   ├── Almoxarifado.java
│   │   │           │   │   ├── Corredor.java
│   │   │           │   │   ├── Estoque.java
│   │   │           │   │   ├── Prateleira.java
│   │   │           │   │   └── TipoMovimento.java
│   │   │           │   ├── Historico.java
│   │   │           │   ├── Localizacao.java
│   │   │           │   └── Product.java
│   │   │           ├── repository
│   │   │           │   ├── ProductRepositoryImpl.java
│   │   │           │   └── ProductRepository.java
│   │   │           └── service
│   │   │               └── ProductService.java
│   │   └── resources
│   │       ├── css
│   │       │   └── estilo.css
│   │       └── view
│   │           └── estoque-view.fxml
│   └── test
│       └── java
│           └── com
│               └── br
│                   └── Service
│                       └── ProductServiceTest.java
```

## Testes

Fiz testes unitários pra camada de serviço usando JUnit 5. A ideia é testar se as regras de negócio tão funcionando direitinho.

## Próximos passos (se eu for continuar isso)

- Salvar os dados em arquivo ou banco de dados
- Melhorar a interface (talvez adicionar gráficos, dashboards)
- Adicionar mais campos pros produtos (fornecedor, validade, etc)
- Fazer relatórios de estoque em PDF
- Sistema de permissões/usuários

## Como rodar

```bash
mvn javafx:run
```

Só isso. O Maven baixa as dependências do JavaFX e roda a aplicação.

## Observações

Usei JavaFX com Scene Builder pra fazer a interface. Separei bem o FXML (estrutura da tela) do CSS (visual) e do Controller (lógica). Achei importante aprender a organizar um projeto JavaFX direito antes de sair fazendo tudo misturado.

A parte do Maven (pom.xml) tá configurada pra rodar o JavaFX certinho. Foi meio chato configurar no começo mas agora roda tranquilo.

![alt text](<Captura de tela de 2026-01-14 20-25-16.png>)
