package com.br.domain;

import java.math.BigDecimal;

public class Product {

    private final String codigo;

    private String nome;

    private String descricao;

    private BigDecimal saldo;

    public Product(String codigo) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Codigo do Produto é Obrigatorio");
        }

        this.codigo = codigo;
        this.nome = "";
        this.descricao = "";
        this.saldo = BigDecimal.ZERO;
    }

    public void entrada(BigDecimal quantidade) {

        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa ou zero");
        }

        saldo = saldo.add(quantidade);
    }

    public void saida(BigDecimal quantidade) {

        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantidade de retirada deve ser maior que zero");
        }

        if (quantidade.compareTo(saldo) > 0) {
            throw new IllegalArgumentException("Estoque insuficiente");
        }

        this.saldo = saldo.subtract(quantidade);
    }

    public void alterarNomeDescricao(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return this.codigo;
    }

    public BigDecimal getSaldo() {
        return this.saldo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((codigo == null) ? 0 : codigo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Product other = (Product) obj;
        if (codigo == null) {
            if (other.codigo != null)
                return false;
        } else if (!codigo.equals(other.codigo))
            return false;
        return true;
    }

}
