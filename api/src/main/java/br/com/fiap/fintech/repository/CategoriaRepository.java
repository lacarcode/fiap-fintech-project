package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByTipoAscNomeAsc();

    List<Categoria> findByTipoOrderByNomeAsc(String tipo);

    Optional<Categoria> findByNomeAndTipo(String nome, String tipo);

    boolean existsByNomeIgnoreCaseAndTipo(String nome, String tipo);

    boolean existsByNomeIgnoreCaseAndTipoAndIdNot(String nome, String tipo, Long id);
}
