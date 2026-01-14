package com.br.controller;

import com.br.domain.Localizacao;
import com.br.domain.Product;
import com.br.domain.enums.Almoxarifado;
import com.br.domain.enums.Estoque;
import com.br.repository.ProductRepositoryImpl;
import com.br.service.ProductService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class EstoqueController {

    @FXML
    private TextField campoCodigo;

    @FXML
    private Button campoEntrada;

    @FXML
    private Button campoSaida;

    @FXML
    private Label labelStatus;

    private ProductService productService;

    private final Localizacao localizacaoPadrao =
            new Localizacao(Almoxarifado.AL01, Estoque.E1, "A", "015");

    @FXML
    private void initialize() {
        productService = new ProductService(new ProductRepositoryImpl());
        campoCodigo.requestFocus();
    }

    @FXML
    void lerEntrada(ActionEvent event) {
        processarMovimento(true);
    }

    @FXML
    void lerSaida(ActionEvent event) {
        processarMovimento(false);
    }

    private void processarMovimento(boolean entrada) {

        String codigoLido = campoCodigo.getText();

        if (codigoLido == null || codigoLido.isBlank()) {
            mostrarErro("Código inválido");
            return;
        }

        try {
            Product product = productService.encontrarPeloCodigo(codigoLido)
                    .orElseGet(() -> new Product(codigoLido, localizacaoPadrao));

            if (entrada) {
                product.entrada(codigoLido);
                mostrarSucesso("Entrada registrada com sucesso");
            } else {
                product.saida(codigoLido);
                mostrarSucesso("Saída registrada com sucesso");
            }

            productService.salvarProduto(product);

            System.out.println("Saldo atual: " + product.getSaldo());

        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
        }

        campoCodigo.clear();
        campoCodigo.requestFocus();
    }

    private void mostrarSucesso(String mensagem) {
        labelStatus.setText(mensagem);
        labelStatus.getStyleClass().setAll("status-ok");
    }

    private void mostrarErro(String mensagem) {
        labelStatus.setText(mensagem);
        labelStatus.getStyleClass().setAll("status-erro");
    }

}
