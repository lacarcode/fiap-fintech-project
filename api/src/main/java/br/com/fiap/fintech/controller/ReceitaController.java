package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.ReceitaRequest;
import br.com.fiap.fintech.dto.ReceitaResponse;
import br.com.fiap.fintech.service.ReceitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/receitas")
public class ReceitaController {

    private final ReceitaService service;

    public ReceitaController(ReceitaService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReceitaResponse> listar(@RequestParam(required = false) Long usuarioId) {
        return service.listar(usuarioId);
    }

    @GetMapping("/{id}")
    public ReceitaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ReceitaResponse> criar(@Valid @RequestBody ReceitaRequest req, UriComponentsBuilder uri) {
        ReceitaResponse criada = service.criar(req);
        return ResponseEntity.created(uri.path("/api/receitas/{id}").buildAndExpand(criada.id()).toUri()).body(criada);
    }

    @PutMapping("/{id}")
    public ReceitaResponse atualizar(@PathVariable Long id, @Valid @RequestBody ReceitaRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
