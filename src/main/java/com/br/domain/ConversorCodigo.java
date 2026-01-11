package com.br.domain;

import java.math.BigDecimal;
import com.br.domain.enums.Almoxarifado;
import com.br.domain.enums.Estoque;

public class ConversorCodigo {

    /*
     * ======================= PRODUTO + QUANTIDADE =======================
     */

    public static BigDecimal converterCodigoParaSaldo(String codigo) {

        int i = codigo.length() - 1;

        while (Character.isDigit(codigo.charAt(i))) {
            i--;
        }

        String quantidadeStr = codigo.substring(i + 1);
        return BigDecimal.valueOf(Integer.parseInt(quantidadeStr));
    }

    public static String converterParaCodigoDeProduto(String codigo) {

        int i = codigo.length() - 1;

        while (Character.isDigit(codigo.charAt(i))) {
            i--;
        }

        return codigo.substring(0, i);
    }

    /*
     * ======================= LOCALIZAÇÃO =======================
     */

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
