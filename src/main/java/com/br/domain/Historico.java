package com.br.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.br.domain.enums.TipoMovimento;

public class Historico {

    private TipoMovimento movimento;

    private BigDecimal quantidade;

    private LocalDateTime dataHora;

}
