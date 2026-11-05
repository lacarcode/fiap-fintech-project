package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.ResumoResponse;
import br.com.fiap.fintech.dto.UsuarioCreateRequest;
import br.com.fiap.fintech.dto.UsuarioResponse;
import br.com.fiap.fintech.dto.UsuarioUpdateRequest;
import br.com.fiap.fintech.service.ResumoFinanceiroService;
import br.com.fiap.fintech.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final ResumoFinanceiroService resumoService;

    public UsuarioController(UsuarioService service, ResumoFinanceiroService resumoService) {
        this.service = service;
        this.resumoService = resumoService;
    }

    /** Página inicial do front: saldo do mês, total investido e gastos por categoria. */
    @GetMapping("/{id}/resumo")
    public ResumoResponse resumo(@PathVariable Long id,
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return resumoService.resumir(id, mes);
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioCreateRequest req, UriComponentsBuilder uri) {
        UsuarioResponse criado = service.criar(req);
        return ResponseEntity.created(uri.path("/api/usuarios/{id}").buildAndExpand(criado.id()).toUri()).body(criado);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
