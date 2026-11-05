package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.ReceitaRequest;
import br.com.fiap.fintech.dto.ReceitaResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.Categoria;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Dominios;
import br.com.fiap.fintech.model.Receita;
import br.com.fiap.fintech.repository.CategoriaRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ReceitaService {

    private static final Set<String> ORIGENS = Set.of("SALARIO", "FREELANCE", "INVESTIMENTO", "OUTROS");
    private static final Set<String> RECORRENCIAS = Set.of("UNICA", "MENSAL", "ANUAL");

    private final ReceitaRepository receitaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ContaBancariaService contaService;

    public ReceitaService(ReceitaRepository receitaRepository, CategoriaRepository categoriaRepository,
                          ContaBancariaService contaService) {
        this.receitaRepository = receitaRepository;
        this.categoriaRepository = categoriaRepository;
        this.contaService = contaService;
    }

    /** usuarioId nulo lista todas as receitas. */
    @Transactional(readOnly = true)
    public List<ReceitaResponse> listar(Long usuarioId) {
        var receitas = usuarioId == null
                ? receitaRepository.findAllByOrderByDtRecebimentoDescIdDesc()
                : receitaRepository.findByUsuarioIdOrderByDtRecebimentoDescIdDesc(usuarioId);
        return receitas.stream().map(ReceitaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ReceitaResponse buscar(Long id) {
        return ReceitaResponse.de(obter(id));
    }

    @Transactional
    public ReceitaResponse criar(ReceitaRequest req) {
        Receita receita = new Receita();
        preencher(receita, req);
        return ReceitaResponse.de(receitaRepository.save(receita));
    }

    @Transactional
    public ReceitaResponse atualizar(Long id, ReceitaRequest req) {
        Receita receita = obter(id);
        Posse.garantirMesmoDono(receita.getUsuario(), req.usuarioId());
        preencher(receita, req);
        return ReceitaResponse.de(receita);
    }

    @Transactional
    public void excluir(Long id) {
        receitaRepository.delete(obter(id));
    }

    private void preencher(Receita receita, ReceitaRequest req) {
        ContaBancaria conta = contaService.obterContaAtivaDoUsuario(req.contaId(), req.usuarioId());
        Categoria categoria = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new RegraNegocioException("Categoria " + req.categoriaId() + " não encontrada"));
        if (!"RECEITA".equals(categoria.getTipo())) {
            throw new RegraNegocioException("A categoria informada não é de receita");
        }
        String origem = vazioComoNulo(req.origem());
        if (origem != null && !ORIGENS.contains(origem)) {
            throw new RegraNegocioException("Origem inválida: use SALARIO, FREELANCE, INVESTIMENTO ou OUTROS");
        }
        String recorrencia = vazioComoNulo(req.recorrencia());
        recorrencia = recorrencia == null ? "UNICA" : recorrencia;
        if (!RECORRENCIAS.contains(recorrencia)) {
            throw new RegraNegocioException("Recorrência inválida: use UNICA, MENSAL ou ANUAL");
        }
        receita.setUsuario(conta.getUsuario());
        receita.setConta(conta);
        receita.setCategoria(categoria);
        receita.setDescricao(req.descricao().trim());
        receita.setValor(req.valor());
        receita.setDtRecebimento(req.dtRecebimento());
        receita.setOrigem(origem);
        receita.setRecorrencia(recorrencia);
        receita.setComprovante(req.comprovante());
    }

    private static String vazioComoNulo(String valor) {
        return valor == null || valor.isBlank() ? null : Dominios.normalizar(valor);
    }

    private Receita obter(Long id) {
        return receitaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Receita " + id + " não encontrada"));
    }
}
