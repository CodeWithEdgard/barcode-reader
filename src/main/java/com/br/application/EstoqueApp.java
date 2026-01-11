package com.br.application;

import com.br.service.ProductService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class EstoqueApp extends Application {

    private ProductService service;

    Button salvar = new Button("Salvar");
    TextField campo = new TextField();


    // Layout vertical box
    VBox layout = new VBox(10, campo, salvar);

    // conteudo da janela
    Scene scene = new Scene(layout, 300, 200);



    // stage janela
    @Override
    public void start(Stage stage) throws Exception {

        campo.setPromptText("Nome do Produto");

        salvar.setOnAction(e -> {
            String nome = campo.getText();
            System.out.println("Produto salvo: " + nome);
        });

        stage.setTitle("Cadastro Simples");
        stage.setScene(scene);
        stage.show();

    }


    public static void main(String[] args) {

        // Inicia o JavaFX
        launch(args);
    }

}
