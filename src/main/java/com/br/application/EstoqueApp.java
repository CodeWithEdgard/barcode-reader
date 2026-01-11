package com.br.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class EstoqueApp extends Application {

    // stage janela
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/estoque-view.fxml"));

        Scene scene = new Scene(loader.load(), 900, 600);

        stage.setTitle("Sistema de Estoque - Leitor de Código");
        stage.setScene(scene);
        stage.show();

    }


    public static void main(String[] args) {

        // Inicia o JavaFX
        launch(args);
    }

}
