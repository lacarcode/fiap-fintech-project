package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.InvestimentoRequest;
import br.com.fiap.fintech.dto.InvestimentoResponse;
import br.com.fiap.fintech.service.InvestimentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/investimentos")
public class InvestimentoController {

    private final InvestimentoService service;

    public InvestimentoController(InvestimentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<InvestimentoResponse> listar(@RequestParam(required = false) Long usuarioId) {
        return service.listar(usuarioId);
    }

    @GetMapping("/{id}")
    public InvestimentoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<InvestimentoResponse> criar(@Valid @RequestBody InvestimentoRequest req,
                                                      UriComponentsBuilder uri) {
        InvestimentoResponse criado = service.criar(req);
        return ResponseEntity.created(uri.path("/api/investimentos/{id}").buildAndExpand(criado.id()).toUri())
                .body(criado);
    }

    @PutMapping("/{id}")
    public InvestimentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody InvestimentoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
