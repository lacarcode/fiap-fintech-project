package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.GastoRequest;
import br.com.fiap.fintech.dto.GastoResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.Categoria;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Dominios;
import br.com.fiap.fintech.model.Gasto;
import br.com.fiap.fintech.repository.CategoriaRepository;
import br.com.fiap.fintech.repository.GastoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class GastoService {

    private static final Set<String> TIPOS = Set.of("FIXO", "VARIAVEL");

    private final GastoRepository gastoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ContaBancariaService contaService;

    public GastoService(GastoRepository gastoRepository, CategoriaRepository categoriaRepository,
                        ContaBancariaService contaService) {
        this.gastoRepository = gastoRepository;
        this.categoriaRepository = categoriaRepository;
        this.contaService = contaService;
    }

    /** usuarioId nulo lista todos os gastos. */
    @Transactional(readOnly = true)
    public List<GastoResponse> listar(Long usuarioId) {
        var gastos = usuarioId == null
                ? gastoRepository.findAllByOrderByDtGastoDescIdDesc()
                : gastoRepository.findByUsuarioIdOrderByDtGastoDescIdDesc(usuarioId);
        return gastos.stream().map(GastoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public GastoResponse buscar(Long id) {
        return GastoResponse.de(obter(id));
    }

    @Transactional
    public GastoResponse criar(GastoRequest req) {
        Gasto gasto = new Gasto();
        preencher(gasto, req);
        return GastoResponse.de(gastoRepository.save(gasto));
    }

    @Transactional
    public GastoResponse atualizar(Long id, GastoRequest req) {
        Gasto gasto = obter(id);
        Posse.garantirMesmoDono(gasto.getUsuario(), req.usuarioId());
        preencher(gasto, req);
        return GastoResponse.de(gasto);
    }

    @Transactional
    public void excluir(Long id) {
        gastoRepository.delete(obter(id));
    }

    private void preencher(Gasto gasto, GastoRequest req) {
        ContaBancaria conta = contaService.obterContaAtivaDoUsuario(req.contaId(), req.usuarioId());
        Categoria categoria = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new RegraNegocioException("Categoria " + req.categoriaId() + " não encontrada"));
        if (!"GASTO".equals(categoria.getTipo())) {
            throw new RegraNegocioException("A categoria informada não é de gasto");
        }
        String tipo = Dominios.normalizar(req.tipo());
        if (!TIPOS.contains(tipo)) {
            throw new RegraNegocioException("Tipo de gasto inválido: use FIXO ou VARIAVEL");
        }
        gasto.setUsuario(conta.getUsuario());
        gasto.setConta(conta);
        gasto.setCategoria(categoria);
        gasto.setDescricao(req.descricao().trim());
        gasto.setValor(req.valor());
        gasto.setDtGasto(req.dtGasto());
        gasto.setTipo(tipo);
        gasto.setComprovante(req.comprovante());
    }

    private Gasto obter(Long id) {
        return gastoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Gasto " + id + " não encontrado"));
    }
}
