package br.com.fiap.fintech.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumoResponse(String mes, BigDecimal totalReceitas, BigDecimal totalGastos, BigDecimal saldo,
                             BigDecimal totalInvestido, BigDecimal rendimentoEstimadoAnual,
                             List<TotalPorCategoria> gastosPorCategoria) {
}
