package com.br.domain.enums;

public enum Almoxarifado {

    AL01, AL02;

    public static Almoxarifado fromCodigo(String codigo) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código do almoxarifado inválido");
        }

        for (Almoxarifado a : values()) {
            
            if (a.name().equals(codigo)) {
                return a;
            }
        }

        throw new IllegalArgumentException("Almoxarifado não encontrado: " + codigo);
    }
}
