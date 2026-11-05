package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.model.Receita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {

    List<Receita> findAllByOrderByDtRecebimentoDescIdDesc();

    List<Receita> findByUsuarioIdOrderByDtRecebimentoDescIdDesc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);

    boolean existsByContaId(Long contaId);

    boolean existsByCategoriaId(Long categoriaId);

    @Query("select sum(r.valor) from Receita r where r.usuario.id = :usuarioId and r.dtRecebimento between :inicio and :fim")
    BigDecimal somarNoPeriodo(@Param("usuarioId") Long usuarioId, @Param("inicio") LocalDate inicio,
                              @Param("fim") LocalDate fim);
}
