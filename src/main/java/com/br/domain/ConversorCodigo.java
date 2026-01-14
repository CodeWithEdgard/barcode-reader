package com.br.domain;

import java.math.BigDecimal;
import com.br.domain.enums.Almoxarifado;
import com.br.domain.enums.Estoque;

public class ConversorCodigo {

    public static BigDecimal converterCodigoParaSaldo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        String s = codigo.trim();
        int n = s.length();

        int i = n - 1;

        // 1. Pula zeros à direita (finais)
        while (i >= 0 && s.charAt(i) == '0') {
            i--;
        }

        // Se chegou no início e era tudo zero → quantidade 0
        if (i < 0) {
            return BigDecimal.ZERO;
        }

        // Agora estamos no primeiro dígito > 0 vindo de trás
        // Vamos para a esquerda até achar um '0' ou o início
        int inicioQuantidade = i;
        while (inicioQuantidade > 0 && s.charAt(inicioQuantidade - 1) == '0') {
            inicioQuantidade--;
        }

        // Se o caractere à esquerda não é '0', mas ainda estamos dentro de dígitos,
        // a quantidade começa no primeiro dígito não-zero que achamos
        String quantidadeStr = s.substring(inicioQuantidade, n);

        try {
            return new BigDecimal(quantidadeStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Não foi possível converter quantidade: " + quantidadeStr);
        }
    }

    public static Localizacao converterParaLocalizacao(String codigoLocalizacao) {

        if (codigoLocalizacao == null || codigoLocalizacao.length() != 10) {
            throw new IllegalArgumentException("Código de localização inválido");
        }

        String almox = codigoLocalizacao.substring(0, 4); // AL01
        String estoque = codigoLocalizacao.substring(4, 6); // E1
        String corredor = codigoLocalizacao.substring(6, 7); // A
        String prateleira = codigoLocalizacao.substring(7, 10); // 015

        return new Localizacao(Almoxarifado.fromCodigo(almox), Estoque.fromCodigo(estoque),
                corredor, prateleira);
    }
}
