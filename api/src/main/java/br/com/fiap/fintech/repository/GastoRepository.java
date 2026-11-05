package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.dto.TotalPorCategoria;
import br.com.fiap.fintech.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findAllByOrderByDtGastoDescIdDesc();

    List<Gasto> findByUsuarioIdOrderByDtGastoDescIdDesc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);

    boolean existsByContaId(Long contaId);

    boolean existsByCategoriaId(Long categoriaId);

    @Query("select sum(g.valor) from Gasto g where g.usuario.id = :usuarioId and g.dtGasto between :inicio and :fim")
    BigDecimal somarNoPeriodo(@Param("usuarioId") Long usuarioId, @Param("inicio") LocalDate inicio,
                              @Param("fim") LocalDate fim);

    @Query("""
            select new br.com.fiap.fintech.dto.TotalPorCategoria(g.categoria.nome, sum(g.valor))
            from Gasto g
            where g.usuario.id = :usuarioId and g.dtGasto between :inicio and :fim
            group by g.categoria.nome
            order by sum(g.valor) desc""")
    List<TotalPorCategoria> totaisPorCategoria(@Param("usuarioId") Long usuarioId, @Param("inicio") LocalDate inicio,
                                               @Param("fim") LocalDate fim);
}
