package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.GastoRequest;
import br.com.fiap.fintech.dto.GastoResponse;
import br.com.fiap.fintech.service.GastoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    private final GastoService service;

    public GastoController(GastoService service) {
        this.service = service;
    }

    @GetMapping
    public List<GastoResponse> listar(@RequestParam(required = false) Long usuarioId) {
        return service.listar(usuarioId);
    }

    @GetMapping("/{id}")
    public GastoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<GastoResponse> criar(@Valid @RequestBody GastoRequest req, UriComponentsBuilder uri) {
        GastoResponse criado = service.criar(req);
        return ResponseEntity.created(uri.path("/api/gastos/{id}").buildAndExpand(criado.id()).toUri()).body(criado);
    }

    @PutMapping("/{id}")
    public GastoResponse atualizar(@PathVariable Long id, @Valid @RequestBody GastoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
