# AGENTS.md — Projeto Fintech (FIAP)

Guia rápido para pessoas e agentes de IA que trabalham neste repositório.

## Sobre o projeto

Aplicação de **gestão financeira pessoal** ("Sistema Fintech", prefixo `SF`),
desenvolvida como projeto acadêmico da FIAP (Turma 1TDSOA — Larissa Gomes de Carvalho, RM 571266).

Épico: *Gerenciamento do fluxo de caixa e patrimônio.* O usuário cadastra perfil e
contas bancárias, registra receitas e gastos (fixos/variáveis), controla dívidas
(parcelas, juros, atrasos) e acompanha metas financeiras (aportes e progresso).

Prioridade do MVP (definida na especificação de requisitos):
1. Cadastro de perfil → 2. Receitas → 3. Gastos → 4. Metas → 5. Dívidas

Estado atual: documentação de análise concluída, camada de dados Oracle validada
(local e na instância da FIAP) e, desde a **Fase 8**, uma **API REST Spring Boot + JPA**
(`api/`) integrada a uma **SPA React** (`web/`) com CRUD completo de Usuario, Conta bancária,
Categoria, Receita, Gasto e Investimento, login e página inicial com resumo do mês.
`backend/` (JDBC/DAO, Fase 7) e `frontend/` (protótipo HTML estático) ficam como histórico das
fases anteriores. Dívidas, parcelas, metas e aportes ainda não têm API nem tela.
Documento de entrega da Fase 8: `README.md` (raiz); plano e evidências em `tasks/fase8/`
e `docs/07-api-spring-react/`.

## Estrutura

```
projeto-fintech/
├── AGENTS.md                  # este arquivo
├── .gitignore
├── docs/                      # artefatos de análise e design (fonte da verdade dos requisitos)
│   ├── 01-requisitos/
│   │   └── especificacao-de-requisitos-com-user-stories.docx   # épico, 5 user stories, critérios de aceitação, INVEST, priorização
│   ├── 02-casos-de-uso/
│   │   └── modelagem-casos-de-uso.pdf      # diagrama de contexto, casos de uso UC01–UC06, detalhamento do UC03
│   ├── 03-modelagem-de-dados/
│   │   ├── modelagem-de-dados.pdf          # modelo original da Fase 3 (preservado)
│   │   ├── modelo-relacional.drawio        # fonte editável do modelo v2 (Fase 6)
│   │   ├── modelo-logico-v2.png            # exportação do modelo lógico v2
│   │   ├── modelo-fisico-v2.png            # exportação do modelo físico v2
│   │   └── modelagem-de-dados-v2.pdf       # modelos lógico e físico v2
│   ├── 04-prototipos-interface/
│   │   └── interfaces.pdf                  # 5 telas do protótipo (dashboard, receitas, gastos, dívidas, metas)
│   └── 06-java-jdbc/
│       └── evidencias/execucao-teste-fiap.md   # saída do Teste na instância Oracle da FIAP (Fase 7)
├── database/                               # camada de dados Oracle (Fase 6)
│   ├── ddl/00-drop-tables.sql              # limpeza idempotente do schema
│   ├── ddl/01-create-tables.sql            # 10 tabelas, constraints, índices e comentários
│   ├── dml/02-seed.sql                     # massa de teste determinística
│   ├── comandos/fintech-comandos.sql       # entregável: 15 comandos com máscaras
│   ├── testes/                             # verificações estáticas e suíte Oracle (50 casos)
│   ├── scripts/aguardar-oracle.sh          # espera o Oracle Free estar disponível
│   └── README.md                           # execução, ambiente e armadilhas conhecidas
├── tasks/                                  # plano e evidências de execução da Fase 6
│   ├── README.md                           # painel, decisões e checklist
│   ├── T01-…T16-*.md                       # especificação e aceite de cada tarefa
│   └── fase7/README.md                     # plano e andamento da Fase 7 (DAO + JDBC, J01–J13)
├── frontend/                  # protótipo estático, sem build
│   ├── index.html             # Dashboard (saldo, fluxo de caixa, categorias, últimas transações)
│   ├── receitas.html          # Cadastro de receita
│   ├── gastos.html            # Cadastro de gasto
│   ├── dividas.html           # Gestão de dívidas
│   ├── metas.html             # Metas financeiras
│   └── css/styles.css         # CSS customizado (fonte Inter, scrollbar, foco, reduced-motion)
└── backend/                   # Java 21 + JDBC (projeto IntelliJ "metodos-em-java")
    ├── metodos-em-java.iml
    ├── .idea/                 # config do IntelliJ (workspace.xml é local e ignorado pelo git)
    ├── lib/ojdbc11.jar        # driver JDBC Oracle (versionado de propósito; ver lib/README.md)
    ├── db.properties.example  # modelo da conexão; o db.properties real é ignorado pelo git
    └── src/                   # pacote default
        ├── Main.java          # demonstração: instancia cada entidade e chama seus métodos
        ├── Teste.java         # teste dos DAOs: 5 inserts por entidade + getAll() + cenários de erro
        ├── Usuario.java       # cadastrarUsuario, validarEmail, atualizarPerfil
        ├── ContaBancaria.java # vincularConta, consultarSaldo, desativarConta
        ├── Receita.java       # registrarReceita, editarReceita, calcularTotalReceitas
        ├── Gasto.java         # registrarGasto, categorizarGasto, calcularImpactoNoSaldo
        ├── Investimento.java  # registrarInvestimento, calcularRendimentoEstimado, resgatarInvestimento
        ├── Categoria.java     # entidade de apoio (T_SF_CATEGORIA)
        ├── Divida.java        # registrarParcelaAtraso, calcularJuros, exibirAlertaVencimento
        ├── Meta.java          # criarMeta, registrarAporte, calcularProgresso, validarOrcamentoDisponivel
        ├── UsuarioDAO.java, GastoDAO.java, InvestimentoDAO.java   # insert() e getAll() (Fase 7)
        ├── ContaBancariaDAO.java, CategoriaDAO.java               # DAOs de apoio às chaves estrangeiras
        ├── ConnectionFactory.java  # abre a conexão (DB_URL/DB_USER/DB_PASSWORD ou db.properties)
        ├── DaoException.java       # traduz erros ORA-xxxxx em mensagens claras
        ├── Dominios.java           # normaliza domínios ("Variável" -> VARIAVEL, D-04)
        └── JdbcUtil.java           # LocalDate <-> java.sql.Date
```

### Fase 8 — API Spring Boot + SPA React

```
README.md                  # documento de entrega: inicialização, usuário de teste, endpoints, mapa controller → página
api/                       # Java 21 + Spring Boot 3.5.7 + Spring Data JPA (Maven Wrapper: ./mvnw)
├── application-local.properties.example   # modelo; o application-local.properties real é ignorado pelo git
└── src/main/java/br/com/fiap/fintech/
    ├── model/ repository/ service/ controller/ dto/ exception/ config/
    └── (DadosDemo cria o usuário de teste a cada subida)
web/                       # React 19 + Vite + React Router + Tailwind CSS (Vitest para testes)
└── src/{api,contexts,hooks,components,pages,utils}/ + router.jsx
tasks/fase8/               # painel, plano de implementação e revisão crítica da Fase 8
docs/07-api-spring-react/  # evidências de execução (FIAP e ponta a ponta)
```

## Rastreabilidade (requisito → dados → código → tela)

Fase 8 (vigente): cada recurso tem Entity + Repository + Service + Controller na `api/` e página(s) na `web/`.

| Recurso (tabela)                          | Controller                | Página(s) React                                         |
|-------------------------------------------|---------------------------|---------------------------------------------------------|
| Login                                     | `AuthController`          | `/login`                                                |
| Usuário (T_SF_USUARIO)                    | `UsuarioController`       | `/cadastro`, `/perfil`, `/` (resumo `GET /usuarios/{id}/resumo`) |
| Receita (T_SF_RECEITA)                    | `ReceitaController`       | `/receitas`, `/receitas/novo`, `/receitas/:id/editar`   |
| Gasto (T_SF_GASTO)                        | `GastoController`         | `/gastos`, `/gastos/novo`, `/gastos/:id/editar`         |
| Investimento (T_SF_INVESTIMENTO)          | `InvestimentoController`  | `/investimentos`, `/investimentos/novo`, `/investimentos/:id/editar` |
| Conta bancária (T_SF_CONTA_BANCARIA)      | `ContaBancariaController` | `/contas`                                               |
| Categoria (T_SF_CATEGORIA)                | `CategoriaController`     | `/categorias`                                           |

Fora da Fase 8: `T_SF_DIVIDA`, `T_SF_PARCELA`, `T_SF_META` e `T_SF_APORTE_META` (sem API nem tela).

Histórico (fases 6 e 7, JDBC e protótipo estático):

| User Story            | Tabela(s)                          | Classe Java     | Tela             |
|-----------------------|------------------------------------|-----------------|------------------|
| 1. Cadastro de perfil | T_SF_USUARIO, T_SF_CONTA_BANCARIA  | Usuario + UsuarioDAO, ContaBancaria + ContaBancariaDAO | — |
| 2. Receitas           | T_SF_RECEITA, T_SF_CATEGORIA       | Receita, Categoria + CategoriaDAO | receitas.html |
| 3. Gastos             | T_SF_GASTO, T_SF_CATEGORIA         | Gasto + GastoDAO | gastos.html     |
| 4. Dívidas            | T_SF_DIVIDA, T_SF_PARCELA          | Divida          | dividas.html     |
| 5. Metas              | T_SF_META, T_SF_APORTE_META        | Meta            | metas.html       |
| Investimentos (Fase 6)| T_SF_INVESTIMENTO                  | Investimento + InvestimentoDAO | — |
| (visão consolidada)   | todas                              | —               | index.html       |

Lacunas conhecidas do histórico (`backend/` JDBC): `T_SF_PARCELA` e `T_SF_APORTE_META` não têm classe
Java; `Receita`, `Divida` e `Meta` não têm DAO; os DAOs só têm `insert()` e `getAll()`. A Fase 8
substitui esse caminho por JPA; permanecem sem API/tela apenas dívidas, parcelas, metas e aportes.

## Como executar

API + SPA (Fase 8; JDK 21 em `JAVA_HOME` e Node 20.19+). Detalhes e usuário de teste no `README.md`:
```bash
# terminal 1 — API em http://localhost:8080/api (senha da FIAP em api/application-local.properties
# ou na variável SPRING_DATASOURCE_PASSWORD; URL e usuário já estão no application.properties)
cd api
./mvnw spring-boot:run            # Windows: .\mvnw.cmd spring-boot:run

# terminal 2 — SPA em http://localhost:5173
cd web
npm install
npm run dev
```
Login de teste: `demo@fintech.com` / `Fintech@2026` (recriado a cada subida da API; esperar a linha
`DadosDemo : Usuário de teste ...` no log). Testes: `cd api && ./mvnw test` (`-Dit.oracle=true` valida as
entidades contra o Oracle configurado) e `cd web && npm test`. Para desenvolver no Oracle local, sobrescreva
`spring.datasource.url`/`username`/`password` no `api/application-local.properties`.
Testes manuais com acento via `curl` no Windows: enviar o corpo com `--data-binary @arquivo.json` (UTF-8).

Histórico — front-end estático: abrir `frontend/index.html` no navegador (Tailwind via CDN — requer internet).

Back-end (Java 17 ou 21, exigido pela Fase 7; o projeto IntelliJ usa language level 21):
```bash
cd backend
javac -encoding UTF-8 -cp "lib/*" -d out src/*.java
java -cp "out;lib/ojdbc11.jar" Main      # demonstração sem banco (no Linux/macOS use ':' no lugar de ';')
java -cp "out;lib/ojdbc11.jar" Teste     # DAOs contra o Oracle (precisa da conexão configurada)
```
Conexão: copie `backend/db.properties.example` para `backend/db.properties` e preencha
`url`, `user` e `password` (Oracle FIAP: `jdbc:oracle:thin:@ORACLE.FIAP.COM.BR:1521:ORCL`,
usuário = RM). Alternativa: variáveis `DB_URL`, `DB_USER`, `DB_PASSWORD`. O `Teste` grava
registros reais (e-mails únicos por horário) e termina com código 0 se todas as
verificações passarem. Ou abrir a pasta `backend/` no IntelliJ e executar `Main` ou `Teste`.

Banco de dados (na raiz; Docker Engine no WSL2 e Oracle Free em execução):
```bash
# primeira inicialização do schema: 00 → 01 → 02
uv run --with oracledb python database/testes/executar_testes.py tudo

# verificações posteriores
uv run --with sqlglot python database/testes/verificar_estatico.py
uv run --with oracledb python database/testes/executar_testes.py testes
```
O executor aplica `00-drop-tables.sql` → `01-create-tables.sql` → `02-seed.sql`
quando chamado com `tudo`. Detalhes de conexão, subida do container e credenciais
locais estão em `database/README.md`.

## Convenções

- Idioma do domínio, código e documentação: **português (pt-BR)**.
- Java: classes em PascalCase, atributos/métodos em camelCase; nomes de atributos
  espelham as colunas do modelo de dados (`id_usuario` → `idUsuario`).
- Banco: tabelas `T_SF_<ENTIDADE>`, PK `id_<entidade>`, prefixos `vl_` (valor),
  `dt_` (data), `nr_` (número), `tx_` (taxa); booleanos como `CHAR(1)`.
- Banco (Fase 6): PKs usam `NUMBER(10) GENERATED BY DEFAULT ON NULL AS IDENTITY`;
  domínios textuais usam maiúsculas e `CHECK`; datas dinâmicas usam
  `TO_DATE('[DATA]', 'DD/MM/YYYY')`.
- SQL entregue: texto usa máscara `'[CAMPO]'`, número usa `[CAMPO]`, não há
  `COMMIT`, `SELECT *` nem retorno de `senha`. Na aplicação JDBC, as máscaras
  devem virar parâmetros `?` de `PreparedStatement`.
- Front-end: layout via classes Tailwind no HTML; `css/styles.css` só para
  melhorias globais. Cores da marca: `brand #15454e`, `emerald2 #10b981`.
- Java (DAO): um `<Entidade>DAO` por tabela, SQL com `?` em `PreparedStatement` e
  colunas explícitas (sem `SELECT *`; `senha` nunca é lida), try-with-resources, erros
  de `SQLException` convertidos em `DaoException` via `DaoException.de(...)`, domínios
  normalizados por `Dominios.normalizar` e dinheiro em `BigDecimal`.
- Credenciais do banco nunca vão para o git (`backend/db.properties`, `api/application-local.properties`
  e `database/.env.local`); a URL e o usuário da FIAP ficam no `api/.../application.properties`.
- API (Fase 8): pacotes por camada (`model`, `repository`, `service`, `controller`, `dto`, `exception`, `config`);
  `ddl-auto=validate` (o DDL de `database/ddl/` é a fonte da verdade, o Hibernate não altera tabelas); DTOs de
  entrada/saída (nunca devolver `senha`); regras de negócio no `service` com exceções de domínio
  (`RecursoNaoEncontrada` 404, `Conflito` 409, `RegraNegocio` 422, `CredenciaisInvalidas` 401) traduzidas pelo
  `GlobalExceptionHandler`; POST responde 201 + `Location`, DELETE 204; testes de service com Mockito e de
  controller com MockMvc (`@WebMvcTest`).
- SPA (Fase 8): componentes funcionais com hooks; uma função por endpoint em `web/src/api/`; sessão em
  `AuthContext`; páginas de listagem/formulário por recurso; Tailwind com as cores da marca; testes com Vitest.
- Artefatos gerados (`backend/out/`, `api/target/`, `web/node_modules/`, `web/dist/`, `*.class`) não são versionados.
- Novos documentos de análise vão em `docs/NN-<tema>/`, mantendo a numeração por fase.
