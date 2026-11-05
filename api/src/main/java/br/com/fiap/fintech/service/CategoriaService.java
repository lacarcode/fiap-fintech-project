package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.CategoriaRequest;
import br.com.fiap.fintech.dto.CategoriaResponse;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.Categoria;
import br.com.fiap.fintech.model.Dominios;
import br.com.fiap.fintech.repository.CategoriaRepository;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/** Categorias são compartilhadas por todos os usuários (T_SF_CATEGORIA não tem id_usuario). */
@Service
public class CategoriaService {

    private static final Set<String> TIPOS = Set.of("RECEITA", "GASTO");

    private final CategoriaRepository categoriaRepository;
    private final ReceitaRepository receitaRepository;
    private final GastoRepository gastoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ReceitaRepository receitaRepository,
                            GastoRepository gastoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.receitaRepository = receitaRepository;
        this.gastoRepository = gastoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar(String tipo) {
        var categorias = tipo == null || tipo.isBlank()
                ? categoriaRepository.findAllByOrderByTipoAscNomeAsc()
                : categoriaRepository.findByTipoOrderByNomeAsc(Dominios.normalizar(tipo));
        return categorias.stream().map(CategoriaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscar(Long id) {
        return CategoriaResponse.de(obter(id));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest req) {
        String nome = req.nome().trim();
        String tipo = tipoValido(req.tipo());
        if (categoriaRepository.existsByNomeIgnoreCaseAndTipo(nome, tipo)) {
            throw new ConflitoException("Já existe a categoria '" + nome + "' do tipo " + tipo);
        }
        return CategoriaResponse.de(categoriaRepository.save(new Categoria(nome, tipo)));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest req) {
        Categoria categoria = obter(id);
        String nome = req.nome().trim();
        String tipo = tipoValido(req.tipo());
        if (categoriaRepository.existsByNomeIgnoreCaseAndTipoAndIdNot(nome, tipo, id)) {
            throw new ConflitoException("Já existe a categoria '" + nome + "' do tipo " + tipo);
        }
        if (!tipo.equals(categoria.getTipo()) && emUso(id)) {
            throw new RegraNegocioException("Não é possível mudar o tipo de uma categoria em uso");
        }
        categoria.setNome(nome);
        categoria.setTipo(tipo);
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = obter(id);
        if (emUso(id)) {
            throw new ConflitoException("A categoria está em uso por lançamentos e não pode ser excluída");
        }
        categoriaRepository.delete(categoria);
    }

    private boolean emUso(Long id) {
        return receitaRepository.existsByCategoriaId(id) || gastoRepository.existsByCategoriaId(id);
    }

    private Categoria obter(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria " + id + " não encontrada"));
    }

    private static String tipoValido(String valor) {
        String tipo = Dominios.normalizar(valor);
        if (!TIPOS.contains(tipo)) {
            throw new RegraNegocioException("Tipo de categoria inválido: use RECEITA ou GASTO");
        }
        return tipo;
    }
}
