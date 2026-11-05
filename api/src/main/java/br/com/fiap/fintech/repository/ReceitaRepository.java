package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.model.Receita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {

    List<Receita> findAllByOrderByDtRecebimentoDescIdDesc();

    List<Receita> findByUsuarioIdOrderByDtRecebimentoDescIdDesc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);

    boolean existsByContaId(Long contaId);

    boolean existsByCategoriaId(Long categoriaId);
}
