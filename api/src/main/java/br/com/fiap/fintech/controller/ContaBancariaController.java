package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.ContaRequest;
import br.com.fiap.fintech.dto.ContaResponse;
import br.com.fiap.fintech.service.ContaBancariaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/contas")
public class ContaBancariaController {

    private final ContaBancariaService service;

    public ContaBancariaController(ContaBancariaService service) {
        this.service = service;
    }

    @GetMapping
    public List<ContaResponse> listar(@RequestParam(required = false) Long usuarioId) {
        return service.listar(usuarioId);
    }

    @GetMapping("/{id}")
    public ContaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaRequest req, UriComponentsBuilder uri) {
        ContaResponse criada = service.criar(req);
        return ResponseEntity.created(uri.path("/api/contas/{id}").buildAndExpand(criada.id()).toUri()).body(criada);
    }

    @PutMapping("/{id}")
    public ContaResponse atualizar(@PathVariable Long id, @Valid @RequestBody ContaRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
