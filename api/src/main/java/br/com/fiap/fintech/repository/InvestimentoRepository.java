package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.model.Investimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestimentoRepository extends JpaRepository<Investimento, Long> {

    List<Investimento> findAllByOrderByDtAplicacaoDescIdDesc();

    List<Investimento> findByUsuarioIdOrderByDtAplicacaoDescIdDesc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);

    boolean existsByContaId(Long contaId);
}
