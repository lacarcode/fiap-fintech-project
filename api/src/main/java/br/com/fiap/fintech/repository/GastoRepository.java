package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findAllByOrderByDtGastoDescIdDesc();

    List<Gasto> findByUsuarioIdOrderByDtGastoDescIdDesc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);

    boolean existsByContaId(Long contaId);

    boolean existsByCategoriaId(Long categoriaId);
}
