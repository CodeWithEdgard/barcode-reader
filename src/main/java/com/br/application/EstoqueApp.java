package com.br.application;

import com.br.domain.Product;
import com.br.repository.ProductRepositoryImpl;
import com.br.service.ProductService;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public class EstoqueApp extends Application {

    private ProductService service;

    private final ObservableList<Product> estoqueGeral = FXCollections.observableArrayList();
    private final ObservableList<ItemCarrinho> carrinhoSaida = FXCollections.observableArrayList();
    private final ObservableList<ItemCarrinho> carrinhoEntrada =
            FXCollections.observableArrayList();

    private final StringBuilder barcodeBuffer = new StringBuilder();
    private long lastKeyTime = 0;
    private static final long MAX_DELAY_MS = 50;

    private Label lblStatus;

    public static class ItemCarrinho {
        private final Product produto;
        private int quantidade = 0;

        public ItemCarrinho(Product produto, int qtdInicial) {
            this.produto = produto;
            this.quantidade = qtdInicial;
        }

        public void adicionar(int qtd) {
            this.quantidade += qtd;
        }

        public Product getProduto() {
            return produto;
        }

        public int getQuantidade() {
            return quantidade;
        }

        public String getCodigo() {
            return produto.getCodigo();
        }

        public String getNome() {
            return produto.getNome();
        }

        public String getDescricao() {
            return produto.getDescricao();
        }

        public BigDecimal getSaldoAtual() {
            return produto.getSaldo();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        service = new ProductService(new ProductRepositoryImpl());
        estoqueGeral.setAll(service.listarTodos());

        primaryStage.addEventFilter(KeyEvent.KEY_TYPED, this::handleScannerInput);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-tab-min-width: 140; -fx-tab-max-width: 140;");

        tabPane.getTabs().addAll(
                criarTabMovimentacao("Saída em Lote", carrinhoSaida, false, "#F44336"),
                criarTabMovimentacao("Entrada em Lote", carrinhoEntrada, true, "#2196F3"),
                new Tab("Cadastro de Produtos", criarPainelCadastro()),
                new Tab("Estoque Atual", criarPainelEstoqueAtual()));

        VBox root = new VBox(tabPane);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f5f7fa;");

        Scene scene = new Scene(root, 1050, 780);
        primaryStage.setTitle("Controle de Estoque - Suzano");
        primaryStage.setScene(scene);
        primaryStage.show();

        atualizarStatus("Sistema pronto ✓ Escaneie ou digite manualmente", false);
    }

    private Tab criarTabMovimentacao(String titulo, ObservableList<ItemCarrinho> carrinho,
            boolean isEntrada, String corBotao) {
        Tab tab = new Tab(titulo);

        VBox painel = new VBox(20);
        painel.setPadding(new Insets(20));

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #333;");

        TableView<ItemCarrinho> tabela = new TableView<>();
        tabela.setItems(carrinho);
        tabela.setPlaceholder(new Label("Carrinho vazio"));
        tabela.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");

        TableColumn<ItemCarrinho, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCodigo()));
        colCodigo.setPrefWidth(120);

        TableColumn<ItemCarrinho, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNome()));
        colNome.setPrefWidth(250);

        TableColumn<ItemCarrinho, String> colDesc = new TableColumn<>("Descrição");
        colDesc.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescricao()));
        colDesc.setPrefWidth(280);

        TableColumn<ItemCarrinho, Integer> colQtd = new TableColumn<>("Quantidade");
        colQtd.setCellValueFactory(
                c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQuantidade())
                        .asObject());
        colQtd.setPrefWidth(140);

        TableColumn<ItemCarrinho, String> colSaldo = new TableColumn<>("Saldo Atual");
        colSaldo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getSaldoAtual().toPlainString()));
        colSaldo.setPrefWidth(120);

        tabela.getColumns().addAll(colCodigo, colNome, colDesc, colQtd, colSaldo);

        VBox painelManual = new VBox(12);
        painelManual.setPadding(new Insets(15));
        painelManual.setStyle(
                "-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label lblManual = new Label("Adicionar manualmente");
        lblManual.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");

        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Código do produto");
        txtCodigo.setPrefWidth(220);

        TextField txtQtd = new TextField("1");
        txtQtd.setPromptText("Quantidade");
        txtQtd.setPrefWidth(100);

        Button btnAdd = new Button("Adicionar");
        btnAdd.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white;");
        btnAdd.setOnAction(e -> adicionarItemAoCarrinho(carrinho, txtCodigo, txtQtd));

        painelManual.getChildren().addAll(lblManual, new HBox(12, new Label("Código:"), txtCodigo),
                new HBox(12, new Label("Quantidade:"), txtQtd, btnAdd));

        Button btnFinalizar = new Button(isEntrada ? "CONFIRMAR ENTRADA" : "CONFIRMAR SAÍDA");
        btnFinalizar.setStyle("-fx-background-color: " + corBotao
                + "; -fx-text-fill: white; -fx-font-size: 16; -fx-padding: 12 30;");

        btnFinalizar.setOnAction(e -> finalizarMovimentacao(carrinho, isEntrada));

        Button btnLimpar = new Button("Limpar Carrinho");
        btnLimpar.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");

        btnLimpar.setOnAction(e -> carrinho.clear());

        // Adição segura dos componentes
        painel.getChildren().addAll(lblTitulo, painelManual, tabela,
                new HBox(20, btnFinalizar, btnLimpar), lblStatus = new Label(""));

        tab.setContent(painel);
        return tab;
    }

    private void adicionarItemAoCarrinho(ObservableList<ItemCarrinho> carrinho, TextField txtCodigo,
            TextField txtQtd) {
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            atualizarStatus("Informe o código do produto!", true);
            return;
        }

        try {
            int qtd = Integer.parseInt(txtQtd.getText().trim());
            if (qtd <= 0)
                throw new NumberFormatException();

            service.encontrarPeloCodigo(codigo).ifPresentOrElse(produto -> {
                Optional<ItemCarrinho> existente = carrinho.stream()
                        .filter(item -> item.getCodigo().equals(codigo)).findFirst();

                if (existente.isPresent()) {
                    existente.get().adicionar(qtd);
                    carrinho.set(carrinho.indexOf(existente.get()), existente.get());
                } else {
                    carrinho.add(new ItemCarrinho(produto, qtd));
                }

                atualizarStatus("Adicionado com sucesso ✓ " + codigo + " × " + qtd, false);
                txtCodigo.clear();
                txtQtd.setText("1");
            }, () -> atualizarStatus("Produto não encontrado ❌", true));
        } catch (NumberFormatException e) {
            atualizarStatus("Quantidade inválida! Digite número inteiro positivo", true);
        }
    }

    private void finalizarMovimentacao(ObservableList<ItemCarrinho> carrinho, boolean isEntrada) {
        if (carrinho.isEmpty()) {
            atualizarStatus("O carrinho está vazio no momento", true);
            return;
        }

        try {
            for (ItemCarrinho item : carrinho) {
                Product p = item.getProduto();
                BigDecimal qtd = BigDecimal.valueOf(item.getQuantidade());

                if (isEntrada) {
                    p.entrada(qtd);
                } else {
                    p.saida(qtd);
                }

                service.salvarProduto(p);
            }

            estoqueGeral.setAll(service.listarTodos());
            carrinho.clear();

            atualizarStatus(isEntrada ? "Entrada registrada com sucesso! ✓"
                    : "Saída registrada com sucesso! ✓", false);

        } catch (Exception ex) {
            atualizarStatus("Erro ao processar movimentação: " + ex.getMessage() + " ❌", true);
        }
    }

    // Aba Cadastro
    private VBox criarPainelCadastro() {
        Label lblTitulo = new Label("Adicionar ou Editar Produto");
        lblTitulo.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #333;");

        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Código (obrigatório para edição)");

        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome do produto");

        TextField txtDescricao = new TextField();
        txtDescricao.setPromptText("Descrição (opcional)");

        Button btnSalvar = new Button("Salvar Produto");
        btnSalvar.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 25;");

        btnSalvar.setOnAction(e -> {
            String codigo = txtCodigo.getText().trim();
            if (codigo.isEmpty()) {
                atualizarStatus("O código é obrigatório!", true);
                return;
            }

            try {
                Product p =
                        service.encontrarPeloCodigo(codigo).orElseGet(() -> new Product(codigo));

                p.alterarNomeDescricao(txtNome.getText().trim(), txtDescricao.getText().trim());
                service.salvarProduto(p);

                estoqueGeral.setAll(service.listarTodos());
                atualizarStatus("Produto salvo com sucesso! ✓", false);

                txtCodigo.clear();
                txtNome.clear();
                txtDescricao.clear();
            } catch (Exception ex) {
                atualizarStatus("Erro ao salvar: " + ex.getMessage(), true);
            }
        });

        Button btnLimpar = new Button("Limpar Formulário");
        btnLimpar.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");

        btnLimpar.setOnAction(e -> {
            txtCodigo.clear();
            txtNome.clear();
            txtDescricao.clear();
        });

        VBox painel = new VBox(20);
        painel.setPadding(new Insets(20));
        painel.getChildren().addAll(lblTitulo, new HBox(12, new Label("Código:"), txtCodigo),
                new HBox(12, new Label("Nome:"), txtNome),
                new HBox(12, new Label("Descrição:"), txtDescricao),
                new HBox(15, btnSalvar, btnLimpar));

        return painel;
    }

    // Aba Estoque Atual
    private VBox criarPainelEstoqueAtual() {
        TableView<Product> tabela = new TableView<>();
        tabela.setItems(estoqueGeral);
        tabela.setPlaceholder(new Label("Nenhum produto cadastrado"));

        // Configuração das colunas (mantida igual)
        TableColumn<Product, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCodigo()));
        colCodigo.setPrefWidth(120);

        TableColumn<Product, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNome()));
        colNome.setPrefWidth(250);

        TableColumn<Product, String> colDesc = new TableColumn<>("Descrição");
        colDesc.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescricao()));
        colDesc.setPrefWidth(300);

        TableColumn<Product, String> colSaldo = new TableColumn<>("Saldo");
        colSaldo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getSaldo().toPlainString()));
        colSaldo.setPrefWidth(120);

        tabela.getColumns().addAll(colCodigo, colNome, colDesc, colSaldo);

        Button btnAtualizar = new Button("Atualizar Estoque");
        btnAtualizar.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white;");

        btnAtualizar.setOnAction(e -> estoqueGeral.setAll(service.listarTodos()));

        // Label separada para aplicar estilo
        Label lblTitulo = new Label("Estoque Atual");
        lblTitulo.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        VBox painel = new VBox(15);
        painel.setPadding(new Insets(20));
        painel.getChildren().addAll(lblTitulo, tabela, btnAtualizar);

        return painel;
    }

    private void handleScannerInput(KeyEvent event) {
        long now = Instant.now().toEpochMilli();
        if (now - lastKeyTime > MAX_DELAY_MS) {
            barcodeBuffer.setLength(0);
        }
        lastKeyTime = now;

        String ch = event.getCharacter();
        if (ch.equals("\r") || event.getCode() == KeyCode.ENTER) {
            String codigo = barcodeBuffer.toString().trim();
            if (codigo.length() >= 3) {
                adicionarItemAoCarrinho(carrinhoSaida, new TextField(codigo), new TextField("1"));
            }
            barcodeBuffer.setLength(0);
            event.consume();
        } else if (!ch.isBlank()) {
            barcodeBuffer.append(ch);
        }
    }

    private void atualizarStatus(String texto, boolean erro) {
        if (lblStatus != null) {
            lblStatus.setText(texto);
            lblStatus.setStyle(erro ? "-fx-font-weight: bold; -fx-text-fill: #D32F2F;"
                    : "-fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
