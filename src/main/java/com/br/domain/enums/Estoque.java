package com.br.domain.enums;

public enum Estoque {

    E1, E2;

    public static Estoque fromCodigo(String codigo) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código do estoque inválido");
        }

        for (Estoque e : values()) {
            if (e.name().equals(codigo)) {
                return e;
            }
        }

        throw new IllegalArgumentException("Estoque não encontrado: " + codigo);
    }
}
