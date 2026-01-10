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
import java.util.HashMap;
import java.util.Map;

public class EstoqueApp extends Application {

    private ProductService service;

    private final ObservableList<Product> estoqueGeral = FXCollections.observableArrayList();

    // Carrinhos (apenas produtos únicos)
    private final ObservableList<Product> carrinhoSaida = FXCollections.observableArrayList();
    private final ObservableList<Product> carrinhoEntrada = FXCollections.observableArrayList();

    // Mapas para controlar quantidade
    private final Map<String, Integer> quantidadesSaida = new HashMap<>();
    private final Map<String, Integer> quantidadesEntrada = new HashMap<>();

    // Buffer para leitor HID
    private final StringBuilder barcodeBuffer = new StringBuilder();
    private long lastKeyTime = 0;
    private static final long MAX_DELAY_MS = 50;

    private Label lblStatus;

    @Override
    public void start(Stage primaryStage) {
        service = new ProductService(new ProductRepositoryImpl());
        estoqueGeral.setAll(service.listarTodos());

        primaryStage.addEventFilter(KeyEvent.KEY_TYPED, this::handleScannerInput);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
                criarTabMovimentacao("Saída em Lote", carrinhoSaida, quantidadesSaida, false,
                        "#F44336"),
                criarTabMovimentacao("Entrada em Lote", carrinhoEntrada, quantidadesEntrada, true,
                        "#2196F3"),
                new Tab("Cadastro de Produtos", criarPainelCadastro()),
                new Tab("Estoque Atual", criarPainelEstoqueAtual()));

        VBox root = new VBox(tabPane);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f5f7fa;");

        Scene scene = new Scene(root, 1050, 780);
        primaryStage.setTitle("Controle de Estoque");
        primaryStage.setScene(scene);
        primaryStage.show();

        atualizarStatus("Sistema pronto ✓ Escaneie ou digite manualmente", false);
    }

    private Tab criarTabMovimentacao(String titulo, ObservableList<Product> carrinho,
            Map<String, Integer> quantidades, boolean isEntrada, String corBotao) {
        Tab tab = new Tab(titulo);

        VBox painel = new VBox(20);
        painel.setPadding(new Insets(20));

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #333;");

        TableView<Product> tabela = new TableView<>();
        tabela.setItems(carrinho);
        tabela.setPlaceholder(new Label("Carrinho vazio"));

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
        colDesc.setPrefWidth(280);

        TableColumn<Product, Integer> colQtd = new TableColumn<>("Quantidade");
        colQtd.setCellValueFactory(c -> {
            String codigo = c.getValue().getCodigo();
            int qtd = quantidades.getOrDefault(codigo, 0);
            return new javafx.beans.property.SimpleIntegerProperty(qtd).asObject();
        });
        colQtd.setPrefWidth(140);

        TableColumn<Product, String> colSaldo = new TableColumn<>("Saldo Atual");
        colSaldo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getSaldo().toPlainString()));
        colSaldo.setPrefWidth(120);

        tabela.getColumns().addAll(colCodigo, colNome, colDesc, colQtd, colSaldo);

        VBox painelManual = new VBox(12);
        painelManual.setPadding(new Insets(15));
        painelManual.setStyle(
                "-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 6;");

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
        btnAdd.setOnAction(e -> {
            String codigo = txtCodigo.getText().trim();
            try {
                int qtd = Integer.parseInt(txtQtd.getText().trim());
                adicionarAoCarrinho(carrinho, quantidades, codigo, qtd);
                txtCodigo.clear();
                txtQtd.setText("1");
            } catch (NumberFormatException ex) {
                atualizarStatus("Quantidade inválida!", true);
            }
        });

        painelManual.getChildren().addAll(lblManual, new HBox(12, new Label("Código:"), txtCodigo),
                new HBox(12, new Label("Quantidade:"), txtQtd, btnAdd));

        Button btnFinalizar = new Button(isEntrada ? "CONFIRMAR ENTRADA" : "CONFIRMAR SAÍDA");
        btnFinalizar.setStyle("-fx-background-color: " + corBotao
                + "; -fx-text-fill: white; -fx-font-size: 16; -fx-padding: 12 30;");

        btnFinalizar.setOnAction(e -> finalizarMovimentacao(carrinho, quantidades, isEntrada));

        Button btnLimpar = new Button("Limpar Carrinho");
        btnLimpar.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");

        btnLimpar.setOnAction(e -> {
            carrinho.clear();
            quantidades.clear();
        });

        painel.getChildren().addAll(lblTitulo, painelManual, tabela,
                new HBox(20, btnFinalizar, btnLimpar), lblStatus = new Label(""));

        tab.setContent(painel);
        return tab;
    }

    private void adicionarAoCarrinho(ObservableList<Product> carrinho,
            Map<String, Integer> quantidades, String codigo, int qtdAdicionar) {
        if (codigo.isEmpty()) {
            atualizarStatus("Informe o código do produto!", true);
            return;
        }

        if (qtdAdicionar <= 0) {
            atualizarStatus("Quantidade deve ser maior que zero!", true);
            return;
        }

        service.encontrarPeloCodigo(codigo).ifPresentOrElse(produto -> {
            // Atualiza quantidade no mapa
            quantidades.merge(codigo, qtdAdicionar, Integer::sum);

            // Adiciona o produto na lista apenas uma vez
            if (!carrinho.contains(produto)) {
                carrinho.add(produto);
            }

            atualizarStatus("Adicionado ✓ " + codigo + " × " + qtdAdicionar, false);
        }, () -> atualizarStatus("Produto não encontrado ❌", true));
    }

    private void finalizarMovimentacao(ObservableList<Product> carrinho,
            Map<String, Integer> quantidades, boolean isEntrada) {
        if (carrinho.isEmpty()) {
            atualizarStatus("Carrinho vazio!", true);
            return;
        }

        try {
            for (Product p : carrinho) {
                String codigo = p.getCodigo();
                int qtd = quantidades.getOrDefault(codigo, 0);

                if (qtd > 0) {
                    BigDecimal quantidade = BigDecimal.valueOf(qtd);
                    if (isEntrada) {
                        p.entrada(quantidade);
                    } else {
                        p.saida(quantidade);
                    }
                    service.salvarProduto(p);
                }
            }

            estoqueGeral.setAll(service.listarTodos());
            carrinho.clear();
            quantidades.clear();

            atualizarStatus(isEntrada ? "Entrada concluída ✓" : "Saída concluída ✓", false);
        } catch (Exception ex) {
            atualizarStatus("Erro: " + ex.getMessage(), true);
        }
    }

    private VBox criarPainelCadastro() {
        Label lblTitulo = new Label("Adicionar ou Editar Produto");
        lblTitulo.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Código");

        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome");

        TextField txtDescricao = new TextField();
        txtDescricao.setPromptText("Descrição");

        Button btnSalvar = new Button("Salvar");
        btnSalvar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        btnSalvar.setOnAction(e -> {
            String codigo = txtCodigo.getText().trim();
            if (codigo.isEmpty()) {
                atualizarStatus("Código obrigatório!", true);
                return;
            }

            try {
                Product p =
                        service.encontrarPeloCodigo(codigo).orElseGet(() -> new Product(codigo));

                p.alterarNomeDescricao(txtNome.getText().trim(), txtDescricao.getText().trim());
                service.salvarProduto(p);

                estoqueGeral.setAll(service.listarTodos());
                atualizarStatus("Salvo com sucesso!", false);

                txtCodigo.clear();
                txtNome.clear();
                txtDescricao.clear();
            } catch (Exception ex) {
                atualizarStatus("Erro: " + ex.getMessage(), true);
            }
        });

        Button btnLimpar = new Button("Limpar");
        btnLimpar.setOnAction(e -> {
            txtCodigo.clear();
            txtNome.clear();
            txtDescricao.clear();
        });

        VBox painel = new VBox(20);
        painel.getChildren().addAll(lblTitulo, new HBox(10, new Label("Código:"), txtCodigo),
                new HBox(10, new Label("Nome:"), txtNome),
                new HBox(10, new Label("Descrição:"), txtDescricao),
                new HBox(10, btnSalvar, btnLimpar));

        return painel;
    }

    private VBox criarPainelEstoqueAtual() {
        TableView<Product> tabela = new TableView<>();
        tabela.setItems(estoqueGeral);

        TableColumn<Product, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCodigo()));

        TableColumn<Product, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNome()));

        TableColumn<Product, String> colDesc = new TableColumn<>("Descrição");
        colDesc.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescricao()));

        TableColumn<Product, String> colSaldo = new TableColumn<>("Saldo");
        colSaldo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getSaldo().toPlainString()));

        tabela.getColumns().addAll(colCodigo, colNome, colDesc, colSaldo);

        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.setOnAction(e -> estoqueGeral.setAll(service.listarTodos()));

        VBox painel = new VBox(15);
        painel.getChildren().addAll(new Label("Estoque Atual"), tabela, btnAtualizar);

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
                adicionarAoCarrinho(carrinhoSaida, quantidadesSaida, codigo, 1);
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
            lblStatus.setStyle(erro ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
