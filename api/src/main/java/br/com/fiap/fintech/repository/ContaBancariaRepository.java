package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.model.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {

    List<ContaBancaria> findAllByOrderByIdAsc();

    List<ContaBancaria> findByUsuarioIdOrderByIdAsc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
