package com.br.domain;

import com.br.domain.enums.Almoxarifado;
import com.br.domain.enums.Estoque;


public class Localizacao {

    private final Almoxarifado almoxarifado;
    private final Estoque estoque;
    private final String corredor;
    private final String prateleira;

    public Localizacao(Almoxarifado almoxarifado, Estoque estoque, String corredor,
            String prateleira) {
                
        if (almoxarifado == null || estoque == null)
            throw new IllegalArgumentException("Almoxarifado e estoque são obrigatórios");

        if (corredor == null || !corredor.matches("[A-Z]"))
            throw new IllegalArgumentException("Corredor inválido");

        if (prateleira == null || !prateleira.matches("\\d{3}"))
            throw new IllegalArgumentException("Prateleira inválida");

        this.almoxarifado = almoxarifado;
        this.estoque = estoque;
        this.corredor = corredor;
        this.prateleira = prateleira;
    }

    public Almoxarifado getAlmoxarifado() {
        return almoxarifado;
    }

    public Estoque getEstoque() {
        return estoque;
    }

    public String getCorredor() {
        return corredor;
    }

    public String getPrateleira() {
        return prateleira;
    }



}
