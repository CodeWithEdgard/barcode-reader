package com.br.application;

import com.br.domain.ConversorCodigo;
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

import java.time.Instant;

public class EstoqueApp extends Application {

    private ProductService service;

    private final ObservableList<Product> estoqueGeral = FXCollections.observableArrayList();

    private final ObservableList<Product> carrinhoEntrada = FXCollections.observableArrayList();
    private final ObservableList<Product> carrinhoSaida = FXCollections.observableArrayList();

    private final StringBuilder barcodeBuffer = new StringBuilder();
    private long lastKeyTime = 0;
    private static final long MAX_DELAY_MS = 100; // Aumentado para digitação manual + leitor HID

    private Label lblStatus;
    private TabPane tabPane;
    private Product ultimoProduto; // Para associar localização

    @Override
    public void start(Stage stage) {
        service = new ProductService(new ProductRepositoryImpl());
        estoqueGeral.setAll(service.listarTodos());

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(criarTabMovimentacao("Entrada", carrinhoEntrada, true, "#2196F3"),
                criarTabMovimentacao("Saída", carrinhoSaida, false, "#F44336"),
                new Tab("Cadastro", criarPainelCadastro()),
                new Tab("Estoque Atual", criarPainelEstoqueAtual()));

        VBox root = new VBox(tabPane);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f5f7fa;");

        // Listener corrigido: no ROOT com ANY (captura mesmo com foco em TextField)
        root.addEventHandler(KeyEvent.ANY, this::handleScanner);

        Scene scene = new Scene(root, 1050, 780);
        stage.setTitle("Controle de Estoque");
        stage.setScene(scene);
        stage.show();

        atualizarStatus("Sistema pronto ✓ Digite ou escaneie", false);
    }

    private Tab criarTabMovimentacao(String titulo, ObservableList<Product> carrinho,
            boolean isEntrada, String corBotao) {
        Tab tab = new Tab(titulo);

        VBox box = new VBox(15);
        box.setPadding(new Insets(15));

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        TableView<Product> tabela = new TableView<>(carrinho);

        TableColumn<Product, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCodigo()));

        TableColumn<Product, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNome()));

        TableColumn<Product, String> colSaldo = new TableColumn<>("Saldo");
        colSaldo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getSaldo().toPlainString()));

        tabela.getColumns().addAll(colCodigo, colNome, colSaldo);

        Button btnConfirmar = new Button(isEntrada ? "CONFIRMAR ENTRADA" : "CONFIRMAR SAÍDA");
        btnConfirmar.setStyle("-fx-background-color: " + corBotao + "; -fx-text-fill: white;");
        btnConfirmar.setOnAction(e -> finalizarMovimentacao(carrinho, isEntrada));

        box.getChildren().addAll(lblTitulo, tabela, btnConfirmar, lblStatus = new Label(""));

        tab.setContent(box);
        return tab;
    }

    private void finalizarMovimentacao(ObservableList<Product> carrinho, boolean isEntrada) {
        if (carrinho.isEmpty()) {
            atualizarStatus("Carrinho vazio", true);
            return;
        }

        try {
            for (Product p : carrinho) {
                // Para teste: usa um código fictício com quantidade 1 (ajuste conforme sua regra
                // real)
                String codigoLido = p.getCodigo() + "0001"; // ex: ABC1230001

                if (isEntrada) {
                    p.entrada(codigoLido);
                } else {
                    p.saida(codigoLido);
                }
                service.salvarProduto(p);
            }

            carrinho.clear();
            estoqueGeral.setAll(service.listarTodos());
            atualizarStatus("Movimentação concluída ✓", false);

        } catch (Exception e) {
            atualizarStatus("Erro ao finalizar: " + e.getMessage(), true);
        }
    }

    private void handleScanner(KeyEvent event) {
        // Debug: veja no console o que está sendo capturado
        System.out.println("Tecla: char='" + event.getCharacter() + "' | code=" + event.getCode()
                + " | tipo=" + event.getEventType());

        long now = Instant.now().toEpochMilli();
        if (now - lastKeyTime > MAX_DELAY_MS) {
            barcodeBuffer.setLength(0);
            System.out.println("Buffer resetado (timeout)");
        }
        lastKeyTime = now;

        if (event.getEventType() == KeyEvent.KEY_TYPED) {
            String ch = event.getCharacter();
            if (ch != null && !ch.isBlank() && !ch.equals("\r") && !ch.equals("\n")) {
                barcodeBuffer.append(ch);
                System.out.println(
                        "Adicionado ao buffer: '" + ch + "' | Buffer atual: " + barcodeBuffer);
            }
        }

        if (event.getCode() == KeyCode.ENTER) {
            String codigoLido = barcodeBuffer.toString().trim();
            System.out.println("ENTER detectado! Código completo: [" + codigoLido + "]");

            if (codigoLido.length() >= 3) {
                Tab atual = tabPane.getSelectionModel().getSelectedItem();
                if (atual == null) {
                    atualizarStatus("Nenhuma aba ativa", true);
                    return;
                }

                String tituloAba = atual.getText();
                System.out.println("Aba ativa: " + tituloAba);

                if (tituloAba.contains("Entrada")) {
                    adicionarViaScanner(carrinhoEntrada, codigoLido);
                } else if (tituloAba.contains("Saída")) {
                    adicionarViaScanner(carrinhoSaida, codigoLido);
                }
            }

            barcodeBuffer.setLength(0);
            event.consume();
        }
    }

    private void adicionarViaScanner(ObservableList<Product> carrinho, String codigoLido) {
        try {
            String codigoProduto = ConversorCodigo.converterParaCodigoDeProduto(codigoLido);

            service.encontrarPeloCodigo(codigoProduto).ifPresentOrElse(produto -> {
                carrinho.add(produto);
                if (carrinho == carrinhoEntrada) {
                    produto.entrada(codigoLido);
                } else {
                    produto.saida(codigoLido);
                }
                service.salvarProduto(produto);
                ultimoProduto = produto;
                atualizarStatus("Adicionado ✓ " + codigoProduto, false);
            }, () -> atualizarStatus("Produto não encontrado", true));

        } catch (Exception e) {
            atualizarStatus("Erro no scan: " + e.getMessage(), true);
        }
    }

    private VBox criarPainelCadastro() {
        Label lblTitulo = new Label("Cadastro de Produto");
        lblTitulo.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Código");

        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome");

        TextField txtDescricao = new TextField();
        txtDescricao.setPromptText("Descrição");

        Button btnSalvar = new Button("Salvar");
        btnSalvar.setOnAction(e -> {
            String codigo = txtCodigo.getText().trim();
            if (codigo.isEmpty()) {
                atualizarStatus("Código obrigatório!", true);
                return;
            }

            try {
                Product p = service.encontrarPeloCodigo(codigo)
                        .orElseGet(() -> new Product(codigo, null));

                p.alterarNomeDescricao(txtNome.getText().trim(), txtDescricao.getText().trim());
                service.salvarProduto(p);

                estoqueGeral.setAll(service.listarTodos());
                atualizarStatus("Produto salvo ✓", false);

                txtCodigo.clear();
                txtNome.clear();
                txtDescricao.clear();
            } catch (Exception ex) {
                atualizarStatus("Erro: " + ex.getMessage(), true);
            }
        });

        VBox box = new VBox(15, lblTitulo, new HBox(10, new Label("Código:"), txtCodigo),
                new HBox(10, new Label("Nome:"), txtNome),
                new HBox(10, new Label("Descrição:"), txtDescricao), btnSalvar);

        return box;
    }

    private VBox criarPainelEstoqueAtual() {
        TableView<Product> tabela = new TableView<>(estoqueGeral);

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

        VBox box = new VBox(15, new Label("Estoque Atual"), tabela);

        return box;
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
