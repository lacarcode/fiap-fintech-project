# Fase 8 — API Spring Boot + Front-end React — Plano de Implementação

> **Para agentes:** SUB-SKILL OBRIGATÓRIA: use `superpowers:subagent-driven-development` (recomendado) ou `superpowers:executing-plans` para executar este plano tarefa a tarefa. Os passos usam checkbox (`- [ ]`) para acompanhamento. Atualize o painel em `tasks/fase8/README.md` ao concluir cada tarefa.

**Objetivo:** entregar uma API REST (Spring Boot + JPA + Oracle FIAP) e uma SPA React que fazem CRUD de Usuario, Receita, Gasto, Investimento, ContaBancaria e Categoria, com login, página inicial (resumo financeiro) e página de erro.

> **Revisão 1** (ver `revisao-critica.md`): correções R1–R11 já incorporadas. Receita é a 4ª entidade com CRUD; Conta e Categoria têm CRUD completo e página; resumo calculado na API; FIAP no `application.properties`; DDL no ZIP; `mvnw` portável; `strictPort` no Vite.

**Arquitetura:** `api/` é um projeto Maven Spring Boot com pacotes por camada (`model` → `repository` → `service` → `controller`, DTOs em `dto`). As entidades JPA mapeiam o DDL existente e o Hibernate só valida o schema. `web/` é uma SPA Vite + React com React Router, estado via hooks/Context e consumo da API por `fetch`.

**Stack:** Java 21, Spring Boot 3.5.7 (Web, Data JPA, Validation), `spring-security-crypto` (BCrypt), Oracle JDBC `ojdbc11`, JUnit 5 + Mockito + MockMvc; React + Vite, React Router, Tailwind CSS v4, Vitest + Testing Library.

## Restrições globais

- Java **21** (Temurin em `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot`; o `java` do PATH é o 26 — sempre definir `JAVA_HOME`).
- Não há Maven global: usar o **Maven Wrapper** (`./mvnw` no Git Bash, `.\mvnw.cmd` no PowerShell).
- Banco: Oracle FIAP `jdbc:oracle:thin:@ORACLE.FIAP.COM.BR:1521:ORCL`, usuário = RM. Tabelas já existem (Fase 7). **`spring.jpa.hibernate.ddl-auto=validate`** — nada de `create`/`update`.
- Desenvolvimento/testes de integração podem usar o Oracle local (Docker, `jdbc:oracle:thin:@//localhost:1521/FREEPDB1`, ver `database/README.md`), mas a demonstração final roda na FIAP.
- URL e usuário da FIAP ficam no `application.properties` (o avaliador precisa ver a conexão). **A senha nunca vai para o git**: fica em `api/application-local.properties` (ignorado) ou na variável `SPRING_DATASOURCE_PASSWORD`.
- Todo `@RestController` expõe GET (lista e por id), POST, PUT e DELETE, e tem página correspondente no front (D-F8-07).
- Lançamentos (receita, gasto, investimento) só usam conta **ativa do próprio usuário** e não podem mudar de dono num PUT (422).
- Pacote raiz `br.com.fiap.fintech`. Domínio, código, mensagens e docs em **pt-BR**.
- Dinheiro em `BigDecimal`, datas em `LocalDate`, domínios textuais em MAIÚSCULAS sem acento (normalizados por `Dominios.normalizar`).
- `senha` nunca sai da API (respostas são DTOs sem senha); senha gravada como hash BCrypt.
- API em `http://localhost:8080/api`; front em `http://localhost:5173`; CORS liberado só para essa origem.
- Front: componentes funcionais, hooks, `react-router-dom`, Tailwind com as cores da marca `brand #15454e` e `emerald2 #10b981`; `package-lock.json` versionado; `node_modules/` e `dist/` nunca entram no git nem no ZIP.
- Commits pequenos, mensagem no padrão `feat:`/`test:`/`docs:` em pt-BR, terminando com `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.

## Contrato REST (compartilhado entre back e front)

| Método | Rota | Corpo | Sucesso | Erros |
|---|---|---|---|---|
| POST | `/api/auth/login` | `{email, senha}` | 200 `Usuario` | 400, 401 |
| GET | `/api/usuarios` | — | 200 `Usuario[]` | — |
| GET | `/api/usuarios/{id}` | — | 200 `Usuario` | 404 |
| POST | `/api/usuarios` | `{nome, email, senha, avatar?}` | **201** + `Location` | 400, 409 |
| PUT | `/api/usuarios/{id}` | `{nome, email, avatar?, senha?, ativo?}` | 200 `Usuario` | 400, 404, 409 |
| DELETE | `/api/usuarios/{id}` | — | **204** | 404, 409 |
| GET | `/api/gastos?usuarioId=` | — | 200 `Gasto[]` | 400 |
| GET | `/api/gastos/{id}` | — | 200 `Gasto` | 404 |
| POST | `/api/gastos` | `GastoRequest` | **201** + `Location` | 400, 422 |
| PUT | `/api/gastos/{id}` | `GastoRequest` | 200 `Gasto` | 400, 404, 422 |
| DELETE | `/api/gastos/{id}` | — | **204** | 404 |
| GET | `/api/investimentos?usuarioId=` | — | 200 `Investimento[]` | 400 |
| GET | `/api/investimentos/{id}` | — | 200 `Investimento` | 404 |
| POST | `/api/investimentos` | `InvestimentoRequest` | **201** + `Location` | 400, 422 |
| PUT | `/api/investimentos/{id}` | `InvestimentoRequest` | 200 `Investimento` | 400, 404, 422 |
| DELETE | `/api/investimentos/{id}` | — | **204** | 404 |
| GET | `/api/usuarios/{id}/resumo?mes=AAAA-MM` | — | 200 `Resumo` | 400, 404 |
| GET | `/api/receitas?usuarioId=` | — | 200 `Receita[]` | 400 |
| GET | `/api/receitas/{id}` | — | 200 `Receita` | 404 |
| POST | `/api/receitas` | `ReceitaRequest` | **201** + `Location` | 400, 422 |
| PUT | `/api/receitas/{id}` | `ReceitaRequest` | 200 `Receita` | 400, 404, 422 |
| DELETE | `/api/receitas/{id}` | — | **204** | 404 |
| GET | `/api/contas?usuarioId=` | — | 200 `Conta[]` | 400 |
| GET | `/api/contas/{id}` | — | 200 `Conta` | 404 |
| POST | `/api/contas` | `ContaRequest` | **201** + `Location` | 400, 409, 422 |
| PUT | `/api/contas/{id}` | `ContaRequest` | 200 `Conta` | 400, 404, 409, 422 |
| DELETE | `/api/contas/{id}` | — | **204** | 404, 409 (tem lançamentos) |
| GET | `/api/categorias?tipo=GASTO` | — | 200 `Categoria[]` | — |
| GET | `/api/categorias/{id}` | — | 200 `Categoria` | 404 |
| POST | `/api/categorias` | `{nome, tipo}` | **201** + `Location` | 400, 409, 422 |
| PUT | `/api/categorias/{id}` | `{nome, tipo}` | 200 `Categoria` | 400, 404, 409, 422 |
| DELETE | `/api/categorias/{id}` | — | **204** | 404, 409 (em uso) |

Corpo de erro (todas as rotas): `{"status": 422, "mensagem": "A conta informada não pertence ao usuário", "detalhes": []}`.
Mapeamento: não encontrado → 404 · validação de campos/JSON inválido → 400 · e-mail duplicado, registro em uso ou violação de constraint → 409 · regra de negócio → 422 · login inválido → 401.

JSON de resposta:
- `Usuario`: `{id, nome, email, avatar, dtCadastro, ativo}`
- `Receita`: `{id, usuarioId, contaId, conta, categoriaId, categoria, descricao, valor, dtRecebimento, origem, recorrencia, comprovante}`
- `Gasto`: `{id, usuarioId, contaId, conta, categoriaId, categoria, descricao, valor, dtGasto, tipo, comprovante}`
- `Investimento`: `{id, usuarioId, contaId, conta, nomeInvestimento, tipo, instituicao, valorAplicado, taxaRentabilidade, dtAplicacao, dtVencimento, status, rendimentoEstimadoAnual}`
- `Categoria`: `{id, nome, tipo}` · `Conta`: `{id, usuarioId, nmBanco, tipoConta, nrConta, ativa}`
- `Resumo`: `{mes, totalReceitas, totalGastos, saldo, totalInvestido, rendimentoEstimadoAnual, gastosPorCategoria: [{categoria, total}]}`

`ReceitaRequest`: `{usuarioId, contaId, categoriaId, descricao, valor, dtRecebimento, origem?, recorrencia?, comprovante?}`. `origem` ∈ SALARIO, FREELANCE, INVESTIMENTO, OUTROS; `recorrencia` ∈ UNICA, MENSAL, ANUAL (padrão UNICA).
`GastoRequest`: `{usuarioId, contaId, categoriaId, descricao, valor, dtGasto, tipo, comprovante?}`.
`InvestimentoRequest`: `{usuarioId, contaId, nomeInvestimento, tipo, instituicao?, valorAplicado, taxaRentabilidade?, dtAplicacao, dtVencimento?, status?}`. `taxaRentabilidade` é **fração anual** (0.1250 = 12,5% a.a., até 4 casas).
`ContaRequest`: `{usuarioId, nmBanco, tipoConta, nrConta, ativa?}` (`ativa` nulo = true na criação e mantém o valor na edição).

## Regras de negócio por service (vão para o README de entrega)

| Service | Regras |
|---|---|
| UsuarioService | e-mail único (409) e normalizado em minúsculas; senha guardada só como hash BCrypt e nunca devolvida; PUT sem senha mantém o hash; exclusão remove receitas, gastos, investimentos e contas do usuário (409 se houver metas/dívidas) |
| AuthService | login só para usuário ativo; a mesma mensagem para e-mail ou senha inválidos (401) |
| ContaBancariaService | tipo de conta do domínio (422); conta não muda de dono (422); não exclui conta com lançamentos (409, sugere desativar); lançamento só usa conta ativa do próprio usuário (422) |
| CategoriaService | tipo RECEITA ou GASTO (422); nome único por tipo (409); não exclui categoria em uso (409) |
| ReceitaService | categoria do tipo RECEITA; origem e recorrência do domínio; recorrência padrão UNICA; não muda de dono (422) |
| GastoService | categoria do tipo GASTO; tipo FIXO ou VARIAVEL (aceita "Variável"); não muda de dono (422) |
| InvestimentoService | tipo do domínio; vencimento ≥ aplicação; status padrão ATIVO; resgate via PUT com status RESGATADO; rendimento anual estimado = valor × taxa |
| ResumoFinanceiroService | saldo do mês = receitas − gastos; total investido e rendimento só de investimentos ATIVO; gastos agrupados por categoria |

## Estrutura de arquivos

```
api/
├── pom.xml, mvnw, mvnw.cmd, .mvn/wrapper/
├── application-local.properties.example
└── src/
    ├── main/java/br/com/fiap/fintech/
    │   ├── FintechApplication.java
    │   ├── config/      CorsConfig, PasswordConfig, DadosDemo
    │   ├── exception/   ApiErro, GlobalExceptionHandler, RecursoNaoEncontradoException,
    │   │                RegraNegocioException, ConflitoException, CredenciaisInvalidasException
    │   ├── model/       Usuario, ContaBancaria, Categoria, Receita, Gasto, Investimento,
    │   │                SimNaoConverter, Dominios
    │   ├── repository/  UsuarioRepository, ContaBancariaRepository, CategoriaRepository,
    │   │                ReceitaRepository, GastoRepository, InvestimentoRepository
    │   ├── dto/         UsuarioCreateRequest, UsuarioUpdateRequest, UsuarioResponse, LoginRequest,
    │   │                ContaRequest, ContaResponse, CategoriaRequest, CategoriaResponse,
    │   │                ReceitaRequest, ReceitaResponse, GastoRequest, GastoResponse,
    │   │                InvestimentoRequest, InvestimentoResponse, ResumoResponse, TotalPorCategoria
    │   ├── service/     UsuarioService, AuthService, ContaBancariaService, CategoriaService,
    │   │                ReceitaService, GastoService, InvestimentoService, ResumoFinanceiroService
    │   └── controller/  UsuarioController, AuthController, ContaBancariaController,
    │                    CategoriaController, ReceitaController, GastoController, InvestimentoController
    ├── main/resources/application.properties
    └── test/java/br/com/fiap/fintech/
        ├── repository/SchemaOracleTest.java          (integração, só com -Dit.oracle=true)
        ├── model/InvestimentoTest.java
        ├── service/  UsuarioServiceTest, AuthServiceTest, ContaBancariaServiceTest, CategoriaServiceTest,
        │             ReceitaServiceTest, GastoServiceTest, InvestimentoServiceTest, ResumoFinanceiroServiceTest
        └── controller/ UsuarioControllerTest, AuthControllerTest, ContaBancariaControllerTest,
                        CategoriaControllerTest, ReceitaControllerTest, GastoControllerTest,
                        InvestimentoControllerTest
web/
├── package.json, package-lock.json, vite.config.js, index.html, .env.example
└── src/
    ├── main.jsx, router.jsx, index.css, setupTests.js
    ├── api/        client.js, auth.js, usuarios.js, receitas.js, gastos.js, investimentos.js,
    │               contas.js, categorias.js
    ├── contexts/   AuthContext.jsx
    ├── hooks/      useDados.js
    ├── utils/      formatar.js
    ├── components/ Layout, RotaProtegida, PageHeader, Campo, Botao, Alerta, Carregando,
    │               CardResumo, ConfirmarExclusao
    │   ├── receitas/      TabelaReceitas, ReceitaForm
    │   ├── gastos/        TabelaGastos, GastoForm
    │   ├── investimentos/ TabelaInvestimentos, InvestimentoForm
    │   ├── contas/        ContaForm
    │   ├── categorias/    CategoriaForm
    │   └── perfil/        PerfilForm
    └── pages/      LoginPage, CadastroPage, DashboardPage, ErroPage, PerfilPage,
                    ReceitasPage, ReceitaFormPage, GastosPage, GastoFormPage,
                    InvestimentosPage, InvestimentoFormPage, ContasPage, CategoriasPage
README.md (raiz)            → reescrito em E01 como documento de entrega
database/ddl/               → vai no ZIP (R6): o avaliador vê e pode recriar as tabelas
docs/07-api-spring-react/   → evidências (B08, E01)

### Mapa controller → página (requisito "páginas correspondentes a cada controller")

| Controller | Página(s) React | Verbos usados pela tela |
|---|---|---|
| AuthController | `/login` | POST |
| UsuarioController | `/cadastro` (inserir), `/perfil` (consultar, atualizar, remover), `/` (resumo) | POST, GET, PUT, DELETE |
| ReceitaController | `/receitas`, `/receitas/novo`, `/receitas/:id/editar` | GET, POST, PUT, DELETE |
| GastoController | `/gastos`, `/gastos/novo`, `/gastos/:id/editar` | GET, POST, PUT, DELETE |
| InvestimentoController | `/investimentos`, `/investimentos/novo`, `/investimentos/:id/editar` | GET, POST, PUT, DELETE |
| ContaBancariaController | `/contas` | GET, POST, PUT, DELETE |
| CategoriaController | `/categorias` | GET, POST, PUT, DELETE |
```

Comando padrão de testes do back (Git Bash, a partir de `api/`):
```bash
JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot" ./mvnw -q test
```
Com integração Oracle (precisa de `api/application-local.properties` ou das variáveis `SPRING_DATASOURCE_*`):
```bash
JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot" ./mvnw -q test -Dit.oracle=true
```

---

### Task P00: Integrar a Fase 7 e preparar a branch

**Arquivos:**
- Modificar: `.gitignore`

- [ ] **Passo 1: integrar `feat/dao-jdbc` na `main`.** Ação externa (abre PR): **pedir confirmação da Larissa antes**.
```bash
gh pr create --base main --head feat/dao-jdbc --title "feat: acesso ao Oracle via JDBC (Fase 7)" --body "Integra a Fase 7 (DAO + JDBC)."
```
Depois do merge feito pela Larissa:
```bash
git switch main && git pull && git switch -c feat/spring-react
```

- [ ] **Passo 2: acrescentar ao final do `.gitignore`:**
```gitignore

# Fase 8 — API Spring Boot (api/) e SPA React (web/)
api/target/
api/application-local.properties
web/node_modules/
web/dist/
web/.env.local
```

- [ ] **Passo 3: acrescentar ao final do `.gitattributes`** (R8: `mvnw` com CRLF quebra no Mac/Linux com `/bin/sh^M`):
```gitattributes

# Maven Wrapper: script sh sempre LF; scripts Windows sempre CRLF
mvnw  text eol=lf
*.cmd text eol=crlf
```

- [ ] **Passo 4: commit**
```bash
git add .gitignore .gitattributes tasks/fase8/
git commit -m "chore: preparar Fase 8 (plano, .gitignore e .gitattributes)"
```

---

### Task B01: Projeto Spring Boot + entidades JPA + repositories

**Arquivos:**
- Criar: `api/pom.xml`, `api/mvnw`, `api/mvnw.cmd`, `api/.mvn/wrapper/maven-wrapper.properties`
- Criar: `api/src/main/java/br/com/fiap/fintech/FintechApplication.java`
- Criar: `api/src/main/java/br/com/fiap/fintech/config/PasswordConfig.java`, `config/CorsConfig.java`
- Criar: `api/src/main/java/br/com/fiap/fintech/model/{SimNaoConverter,Dominios,Usuario,Categoria,ContaBancaria,Receita,Gasto,Investimento}.java`
- Criar: `api/src/main/java/br/com/fiap/fintech/repository/*.java` (6 interfaces)
- Criar: `api/src/main/resources/application.properties`, `api/application-local.properties.example`
- Testes: `api/src/test/java/br/com/fiap/fintech/repository/SchemaOracleTest.java`, `api/src/test/java/br/com/fiap/fintech/model/InvestimentoTest.java`

**Interfaces:**
- Produz (usado por B02–B08): entidades com construtores e getters/setters descritos abaixo; repositories:
  - `UsuarioRepository`: `Optional<Usuario> findByEmailIgnoreCase(String)`, `boolean existsByEmailIgnoreCase(String)`, `boolean existsByEmailIgnoreCaseAndIdNot(String, Long)`
  - `CategoriaRepository`: `List<Categoria> findAllByOrderByTipoAscNomeAsc()`, `List<Categoria> findByTipoOrderByNomeAsc(String)`, `Optional<Categoria> findByNomeAndTipo(String, String)`, `boolean existsByNomeIgnoreCaseAndTipo(String, String)`, `boolean existsByNomeIgnoreCaseAndTipoAndIdNot(String, String, Long)`
  - `ContaBancariaRepository`: `List<ContaBancaria> findAllByOrderByIdAsc()`, `List<ContaBancaria> findByUsuarioIdOrderByIdAsc(Long)`, `void deleteByUsuarioId(Long)`
  - `ReceitaRepository`: `List<Receita> findAllByOrderByDtRecebimentoDescIdDesc()`, `List<Receita> findByUsuarioIdOrderByDtRecebimentoDescIdDesc(Long)`, `void deleteByUsuarioId(Long)`, `boolean existsByContaId(Long)`, `boolean existsByCategoriaId(Long)`
  - `GastoRepository`: `List<Gasto> findAllByOrderByDtGastoDescIdDesc()`, `List<Gasto> findByUsuarioIdOrderByDtGastoDescIdDesc(Long)`, `void deleteByUsuarioId(Long)`, `boolean existsByContaId(Long)`, `boolean existsByCategoriaId(Long)`
  - `InvestimentoRepository`: `List<Investimento> findAllByOrderByDtAplicacaoDescIdDesc()`, `List<Investimento> findByUsuarioIdOrderByDtAplicacaoDescIdDesc(Long)`, `void deleteByUsuarioId(Long)`, `boolean existsByContaId(Long)`
  - As consultas de soma usadas pelo resumo são acrescentadas em B07.
  - `Investimento.calcularRendimentoEstimado(): BigDecimal`

- [ ] **Passo 1: gerar o esqueleto só para obter o Maven Wrapper** (o `pom.xml` é substituído no passo 2). Downloads em pasta própria do scratchpad:
```bash
mkdir -p "$TMPDIR/initializr" && cd "$TMPDIR/initializr" \
 && curl -s https://start.spring.io/starter.zip -d type=maven-project -d language=java -d javaVersion=21 \
    -d groupId=br.com.fiap -d artifactId=fintech-api -d packageName=br.com.fiap.fintech -d baseDir=api -o api.zip \
 && unzip -q api.zip
```
Copiar para o repositório **apenas** `api/mvnw`, `api/mvnw.cmd` e `api/.mvn/` (não copiar `src/` nem `pom.xml` gerados).

- [ ] **Passo 2: criar `api/pom.xml`**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.7</version>
        <relativePath/>
    </parent>
    <groupId>br.com.fiap</groupId>
    <artifactId>fintech-api</artifactId>
    <version>1.0.0</version>
    <name>fintech-api</name>
    <description>API REST do Sistema Fintech (FIAP) — Spring Boot + JPA + Oracle</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <!-- Só o BCrypt; sem o filtro do Spring Security (D-F8-03) -->
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-crypto</artifactId>
        </dependency>
        <dependency>
            <groupId>com.oracle.database.jdbc</groupId>
            <artifactId>ojdbc11</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Passo 3: configuração**

`api/src/main/resources/application.properties`:
```properties
spring.application.name=fintech-api

# Conexão obrigatória: instância Oracle da FIAP (usuário = RM).
# A senha NÃO fica aqui: application-local.properties (ignorado pelo git) ou variável SPRING_DATASOURCE_PASSWORD.
# Para desenvolver no Oracle local, sobrescreva url/username no application-local.properties.
spring.datasource.url=jdbc:oracle:thin:@ORACLE.FIAP.COM.BR:1521:ORCL
spring.datasource.username=RM571266
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.config.import=optional:file:./application-local.properties
# O DDL é a fonte da verdade (D-F8-01): o Hibernate só confere o mapeamento
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false

server.port=8080
fintech.cors.origens=http://localhost:5173

# Usuário de teste criado/garantido na subida (D-F8-05)
fintech.dados-demo.habilitado=true
fintech.dados-demo.email=demo@fintech.com
fintech.dados-demo.senha=Fintech@2026
```

`api/application-local.properties.example`:
```properties
# Copie para application-local.properties (mesma pasta, ignorado pelo git) e preencha a senha.
# URL e usuário da FIAP já estão no application.properties.
spring.datasource.password=
# Só para desenvolvimento no Oracle local (Docker, ver database/README.md):
# spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1
# spring.datasource.username=

```

`FintechApplication.java`:
```java
package br.com.fiap.fintech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FintechApplication {

    public static void main(String[] args) {
        SpringApplication.run(FintechApplication.class, args);
    }
}
```

`config/PasswordConfig.java`:
```java
package br.com.fiap.fintech.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

`config/CorsConfig.java`:
```java
package br.com.fiap.fintech.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Libera o front-end React (Vite) a chamar a API. */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origens;

    public CorsConfig(@Value("${fintech.cors.origens}") String[] origens) {
        this.origens = origens;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origens)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .exposedHeaders("Location");
    }
}
```

- [ ] **Passo 4: escrever os testes (vão falhar: as classes não existem)**

`model/InvestimentoTest.java` (unitário, roda sempre):
```java
package br.com.fiap.fintech.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InvestimentoTest {

    @Test
    void rendimentoEstimadoEhValorAplicadoVezesTaxaAnual() {
        Investimento cdb = new Investimento();
        cdb.setValorAplicado(new BigDecimal("5000.00"));
        cdb.setTaxaRentabilidade(new BigDecimal("0.1250"));

        assertThat(cdb.calcularRendimentoEstimado()).isEqualByComparingTo("625.00");
    }

    @Test
    void semTaxaNaoHaRendimentoEstimado() {
        Investimento acao = new Investimento();
        acao.setValorAplicado(new BigDecimal("1000.00"));

        assertThat(acao.calcularRendimentoEstimado()).isNull();
    }
}
```

`repository/SchemaOracleTest.java` (integração; só roda com `-Dit.oracle=true`; faz rollback no fim):
```java
package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.model.Categoria;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Gasto;
import br.com.fiap.fintech.model.Investimento;
import br.com.fiap.fintech.model.Receita;
import br.com.fiap.fintech.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Sobe o contexto com ddl-auto=validate contra o Oracle real e grava um grafo completo (rollback no fim). */
@SpringBootTest(properties = "fintech.dados-demo.habilitado=false")
@EnabledIfSystemProperty(named = "it.oracle", matches = "true")
@Transactional
class SchemaOracleTest {

    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired ContaBancariaRepository contas;
    @Autowired ReceitaRepository receitas;
    @Autowired GastoRepository gastos;
    @Autowired InvestimentoRepository investimentos;

    @Test
    void mapeamentoBateComODdlEPersisteTodasAsEntidades() {
        Usuario usuario = new Usuario();
        usuario.setNome("Teste de Integração");
        usuario.setEmail("it-" + System.nanoTime() + "@teste.com");
        usuario.setSenha("$2a$10$hashDeTesteNaoUsadoParaLogin.................");
        usuarios.saveAndFlush(usuario);

        assertThat(usuario.getId()).isNotNull();
        assertThat(usuario.getAtivo()).isTrue();
        assertThat(usuario.getDtCadastro()).isEqualTo(LocalDate.now());

        Categoria categoria = categorias.findByNomeAndTipo("Teste IT", "GASTO")
                .orElseGet(() -> categorias.saveAndFlush(new Categoria("Teste IT", "GASTO")));
        ContaBancaria conta = contas.saveAndFlush(new ContaBancaria(usuario, "Banco IT", "CORRENTE", "IT-001"));

        Gasto gasto = new Gasto();
        gasto.setUsuario(usuario);
        gasto.setConta(conta);
        gasto.setCategoria(categoria);
        gasto.setDescricao("Gasto de integração");
        gasto.setValor(new BigDecimal("10.50"));
        gasto.setDtGasto(LocalDate.now());
        gasto.setTipo("VARIAVEL");
        gastos.saveAndFlush(gasto);

        Categoria salario = categorias.findByNomeAndTipo("Teste IT", "RECEITA")
                .orElseGet(() -> categorias.saveAndFlush(new Categoria("Teste IT", "RECEITA")));
        Receita receita = new Receita();
        receita.setUsuario(usuario);
        receita.setConta(conta);
        receita.setCategoria(salario);
        receita.setDescricao("Receita de integração");
        receita.setValor(new BigDecimal("3000.00"));
        receita.setDtRecebimento(LocalDate.now());
        receita.setOrigem("SALARIO");
        receita.setRecorrencia("MENSAL");
        receitas.saveAndFlush(receita);
        assertThat(receitas.existsByContaId(conta.getId())).isTrue();

        Investimento investimento = new Investimento();
        investimento.setUsuario(usuario);
        investimento.setConta(conta);
        investimento.setNomeInvestimento("CDB IT");
        investimento.setTipo("CDB");
        investimento.setValorAplicado(new BigDecimal("1000.00"));
        investimento.setTaxaRentabilidade(new BigDecimal("0.1250"));
        investimento.setDtAplicacao(LocalDate.now());
        investimentos.saveAndFlush(investimento);

        assertThat(gastos.findByUsuarioIdOrderByDtGastoDescIdDesc(usuario.getId())).hasSize(1);
        assertThat(investimentos.findByUsuarioIdOrderByDtAplicacaoDescIdDesc(usuario.getId()))
                .singleElement()
                .satisfies(i -> assertThat(i.getStatus()).isEqualTo("ATIVO"));
        assertThat(usuarios.existsByEmailIgnoreCase(usuario.getEmail().toUpperCase())).isTrue();
    }
}
```

- [ ] **Passo 5: rodar e confirmar que falha na compilação**
Run: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot" ./mvnw -q test` (em `api/`)
Esperado: `COMPILATION ERROR` (`cannot find symbol: class Investimento`).

- [ ] **Passo 6: criar o modelo.** Toda entidade tem construtor público sem argumentos e **getters e setters públicos para todos os campos** (inclusive `setId`, usado nos testes); gerar no IntelliJ com Alt+Insert.

`model/SimNaoConverter.java`:
```java
package br.com.fiap.fintech.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Booleano Java <-> CHAR(1) 'S'/'N' do banco. */
@Converter
public class SimNaoConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean valor) {
        return valor == null ? null : (valor ? "S" : "N");
    }

    @Override
    public Boolean convertToEntityAttribute(String valor) {
        return valor == null ? null : "S".equals(valor.trim());
    }
}
```

`model/Dominios.java` (mesma regra da Fase 7):
```java
package br.com.fiap.fintech.model;

import java.text.Normalizer;
import java.util.Locale;

/** Normaliza valores de domínio para o formato aceito pelos CHECKs do banco (D-04). */
public final class Dominios {

    private Dominios() {
    }

    /** "Variável" -> "VARIAVEL"; "Cheque especial" -> "CHEQUE_ESPECIAL"; null -> null. */
    public static String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String semAcento = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toUpperCase(Locale.ROOT).replace(' ', '_');
    }
}
```

`model/Usuario.java`:
```java
package br.com.fiap.fintech.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "T_SF_USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "senha", nullable = false, length = 255)
    private String senha; // hash BCrypt; nunca sai da API

    @Column(name = "avatar", length = 500)
    private String avatar;

    @Column(name = "dt_cadastro", nullable = false)
    private LocalDate dtCadastro;

    @Convert(converter = SimNaoConverter.class)
    @Column(name = "ativo", nullable = false, columnDefinition = "char(1)")
    private Boolean ativo;

    // O DEFAULT do banco não vale quando o Hibernate envia NULL explícito
    @PrePersist
    void aoCriar() {
        if (dtCadastro == null) {
            dtCadastro = LocalDate.now();
        }
        if (ativo == null) {
            ativo = true;
        }
    }

    // getters e setters de todos os campos
}
```

`model/Categoria.java`:
```java
package br.com.fiap.fintech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "T_SF_CATEGORIA")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;

    @Column(name = "nome", nullable = false, length = 80)
    private String nome;

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo; // RECEITA ou GASTO

    public Categoria() {
    }

    public Categoria(String nome, String tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    // getters e setters de todos os campos
}
```

`model/ContaBancaria.java`:
```java
package br.com.fiap.fintech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "T_SF_CONTA_BANCARIA")
public class ContaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "nm_banco", nullable = false, length = 100)
    private String nmBanco;

    @Column(name = "tipo_conta", nullable = false, length = 20)
    private String tipoConta; // CORRENTE, POUPANCA, PAGAMENTO, SALARIO, INVESTIMENTO

    @Column(name = "nr_conta", nullable = false, length = 20)
    private String nrConta;

    @Convert(converter = SimNaoConverter.class)
    @Column(name = "ativa", nullable = false, columnDefinition = "char(1)")
    private Boolean ativa;

    public ContaBancaria() {
    }

    public ContaBancaria(Usuario usuario, String nmBanco, String tipoConta, String nrConta) {
        this.usuario = usuario;
        this.nmBanco = nmBanco;
        this.tipoConta = tipoConta;
        this.nrConta = nrConta;
        this.ativa = true;
    }

    @PrePersist
    void aoCriar() {
        if (ativa == null) {
            ativa = true;
        }
    }

    /** Rótulo exibido nas telas: "Banco Demo · 0001-9". */
    public String getRotulo() {
        return nmBanco + " · " + nrConta;
    }

    // getters e setters de todos os campos
}
```

`model/Receita.java`:
```java
package br.com.fiap.fintech.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "T_SF_RECEITA")
public class Receita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receita")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // FK composta (id_conta, id_usuario) existe no banco (D-14); o service garante que a conta é do usuário
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conta", nullable = false)
    private ContaBancaria conta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(name = "descricao", nullable = false, length = 300)
    private String descricao;

    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "dt_recebimento", nullable = false)
    private LocalDate dtRecebimento;

    @Column(name = "origem", length = 20)
    private String origem; // SALARIO, FREELANCE, INVESTIMENTO, OUTROS

    @Column(name = "recorrencia", length = 10)
    private String recorrencia; // UNICA, MENSAL, ANUAL

    @Column(name = "comprovante", length = 500)
    private String comprovante;

    // getters e setters de todos os campos
}
```

`model/Gasto.java`:
```java
package br.com.fiap.fintech.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "T_SF_GASTO")
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gasto")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // FK composta (id_conta, id_usuario) existe no banco (D-14); o service garante que a conta é do usuário
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conta", nullable = false)
    private ContaBancaria conta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(name = "descricao", nullable = false, length = 300)
    private String descricao;

    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "dt_gasto", nullable = false)
    private LocalDate dtGasto;

    @Column(name = "tipo", length = 10)
    private String tipo; // FIXO ou VARIAVEL

    @Column(name = "comprovante", length = 500)
    private String comprovante;

    // getters e setters de todos os campos
}
```

`model/Investimento.java`:
```java
package br.com.fiap.fintech.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "T_SF_INVESTIMENTO")
public class Investimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_investimento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conta", nullable = false)
    private ContaBancaria conta;

    @Column(name = "nm_investimento", nullable = false, length = 150)
    private String nomeInvestimento;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo; // CDB, LCI, LCA, TESOURO, ACOES, FII, FUNDO, POUPANCA, CRIPTO, OUTROS

    @Column(name = "instituicao", length = 100)
    private String instituicao;

    @Column(name = "vl_aplicado", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAplicado;

    @Column(name = "tx_rentabilidade", precision = 7, scale = 4)
    private BigDecimal taxaRentabilidade; // fração anual: 0.1250 = 12,5% a.a. (D-17)

    @Column(name = "dt_aplicacao", nullable = false)
    private LocalDate dtAplicacao;

    @Column(name = "dt_vencimento")
    private LocalDate dtVencimento;

    @Column(name = "status", nullable = false, length = 15)
    private String status; // ATIVO, RESGATADO, VENCIDO

    @PrePersist
    void aoCriar() {
        if (status == null) {
            status = "ATIVO";
        }
    }

    /** Rendimento anual estimado (juros simples); null quando não há taxa informada. */
    public BigDecimal calcularRendimentoEstimado() {
        if (taxaRentabilidade == null || valorAplicado == null) {
            return null;
        }
        return valorAplicado.multiply(taxaRentabilidade).setScale(2, RoundingMode.HALF_EVEN);
    }

    // getters e setters de todos os campos
}
```

- [ ] **Passo 7: criar os repositories**

```java
package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
```
```java
package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByTipoAscNomeAsc();

    List<Categoria> findByTipoOrderByNomeAsc(String tipo);

    Optional<Categoria> findByNomeAndTipo(String nome, String tipo);

    boolean existsByNomeIgnoreCaseAndTipo(String nome, String tipo);

    boolean existsByNomeIgnoreCaseAndTipoAndIdNot(String nome, String tipo, Long id);
}
```
```java
package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.model.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {

    List<ContaBancaria> findAllByOrderByIdAsc();

    List<ContaBancaria> findByUsuarioIdOrderByIdAsc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
```
```java
package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.model.Receita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {

    List<Receita> findAllByOrderByDtRecebimentoDescIdDesc();

    List<Receita> findByUsuarioIdOrderByDtRecebimentoDescIdDesc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);

    boolean existsByContaId(Long contaId);

    boolean existsByCategoriaId(Long categoriaId);
}
```
```java
package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findAllByOrderByDtGastoDescIdDesc();

    List<Gasto> findByUsuarioIdOrderByDtGastoDescIdDesc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);

    boolean existsByContaId(Long contaId);

    boolean existsByCategoriaId(Long categoriaId);
}
```
```java
package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.model.Investimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestimentoRepository extends JpaRepository<Investimento, Long> {

    List<Investimento> findAllByOrderByDtAplicacaoDescIdDesc();

    List<Investimento> findByUsuarioIdOrderByDtAplicacaoDescIdDesc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);

    boolean existsByContaId(Long contaId);
}
```

- [ ] **Passo 8: testes unitários passam (o de integração é ignorado)**
Run: `JAVA_HOME=... ./mvnw -q test`
Esperado: `BUILD SUCCESS`; `InvestimentoTest` 2 testes OK; `SchemaOracleTest` *skipped*.

- [ ] **Passo 9: integração contra o Oracle local.** Subir o container (`wsl -e bash -c "docker start oracle-fintech"`, manter um `wsl sleep` ativo) e criar `api/application-local.properties` apontando para `FREEPDB1` (credenciais em `database/.env.local`).
Run: `JAVA_HOME=... ./mvnw -q test -Dit.oracle=true`
Esperado: `SchemaOracleTest` PASS.
**Se falhar com** `Schema-validation: wrong column type encountered in column [id_xxx] ... found [number (Types#NUMERIC)], but expecting [number(19,0) (Types#BIGINT)]`: acrescentar `columnDefinition = "number(10)"` no `@Column` de cada `id` e no `@JoinColumn` de cada FK. Regra geral: alinhar `columnDefinition` ao tipo do DDL; **nunca** trocar `validate` por `update`. Registrar o ajuste nas "Notas de execução" do painel.

- [ ] **Passo 10: commit** (o `chmod` garante que `./mvnw` rode no Mac/Linux do avaliador — R8)
```bash
git add api/ && git update-index --chmod=+x api/mvnw
git commit -m "feat(api): projeto Spring Boot com entidades JPA e repositories validados contra o DDL"
git ls-files --eol api/mvnw    # esperado: i/lf ... attr/text eol=lf
```

---

### Task B02: Tratamento de erros + Usuario (service, login, controller)

**Arquivos:**
- Criar: `exception/{ApiErro,GlobalExceptionHandler,RecursoNaoEncontradoException,RegraNegocioException,ConflitoException,CredenciaisInvalidasException}.java`
- Criar: `dto/{UsuarioCreateRequest,UsuarioUpdateRequest,UsuarioResponse,LoginRequest}.java`
- Criar: `service/{UsuarioService,AuthService}.java`, `controller/{UsuarioController,AuthController}.java`
- Testes: `service/UsuarioServiceTest.java`, `service/AuthServiceTest.java`, `controller/UsuarioControllerTest.java`, `controller/AuthControllerTest.java`

**Interfaces:**
- Consome (B01): `UsuarioRepository`, `ReceitaRepository.deleteByUsuarioId`, `GastoRepository.deleteByUsuarioId`, `InvestimentoRepository.deleteByUsuarioId`, `ContaBancariaRepository.deleteByUsuarioId`, `PasswordEncoder`.
- Produz: exceções (todas `RuntimeException` com construtor `(String mensagem)`), `UsuarioResponse.de(Usuario)`, `UsuarioService.{listar(), buscar(Long), criar(UsuarioCreateRequest), atualizar(Long, UsuarioUpdateRequest), excluir(Long)}`, `AuthService.login(LoginRequest): UsuarioResponse`.

- [ ] **Passo 1: exceções e DTOs (sem lógica; necessários para os testes compilarem)**

```java
package br.com.fiap.fintech.exception;

/** 404 — recurso pedido na URL não existe. */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
```
Criar do mesmo jeito (mesmo corpo, só muda nome e comentário):
`RegraNegocioException` (`/** 422 — dados válidos no formato, mas que violam regra do Fintech. */`),
`ConflitoException` (`/** 409 — conflito com dado existente (ex.: e-mail já cadastrado). */`),
`CredenciaisInvalidasException` (`/** 401 — e-mail ou senha inválidos. */`).

```java
package br.com.fiap.fintech.exception;

import java.util.List;

/** Corpo padrão de erro da API. */
public record ApiErro(int status, String mensagem, List<String> detalhes) {
}
```

```java
package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioCreateRequest(
        @NotBlank @Size(max = 150) String nome,
        // @Pattern espelha o CHECK do banco (CK_SF_USUARIO_EMAIL exige ponto no domínio)
        @NotBlank @Email @Pattern(regexp = ".+@.+\\..+", message = "deve ter domínio com ponto") @Size(max = 255) String email,
        @NotBlank @Size(min = 6, max = 72) String senha,
        @Size(max = 500) String avatar) {
}
```
```java
package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** senha e ativo são opcionais: null mantém o valor atual. */
public record UsuarioUpdateRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Email @Pattern(regexp = ".+@.+\\..+", message = "deve ter domínio com ponto") @Size(max = 255) String email,
        @Size(min = 6, max = 72) String senha,
        @Size(max = 500) String avatar,
        Boolean ativo) {
}
```
```java
package br.com.fiap.fintech.dto;

import br.com.fiap.fintech.model.Usuario;

import java.time.LocalDate;

/** Nunca inclui a senha. */
public record UsuarioResponse(Long id, String nome, String email, String avatar, LocalDate dtCadastro, boolean ativo) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getAvatar(), u.getDtCadastro(),
                Boolean.TRUE.equals(u.getAtivo()));
    }
}
```
```java
package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String email, @NotBlank String senha) {
}
```

- [ ] **Passo 2: testes do service (falham)**

`service/UsuarioServiceTest.java`:
```java
package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.UsuarioCreateRequest;
import br.com.fiap.fintech.dto.UsuarioResponse;
import br.com.fiap.fintech.dto.UsuarioUpdateRequest;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.ContaBancariaRepository;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock ReceitaRepository receitaRepository;
    @Mock GastoRepository gastoRepository;
    @Mock InvestimentoRepository investimentoRepository;
    @Mock ContaBancariaRepository contaRepository;
    PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarioRepository, receitaRepository, gastoRepository, investimentoRepository,
                contaRepository, encoder);
    }

    private Usuario ana() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("Ana");
        u.setEmail("ana@email.com");
        u.setSenha(encoder.encode("senhaAntiga"));
        u.setAtivo(true);
        return u;
    }

    @Test
    void criarNormalizaEmailEGuardaSoOHashDaSenha() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@email.com")).thenReturn(false);
        when(usuarioRepository.save(any())).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponse resposta = service.criar(new UsuarioCreateRequest(" Ana ", " Ana@Email.com ", "segredo1", null));

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(salvo.capture());
        assertThat(salvo.getValue().getEmail()).isEqualTo("ana@email.com");
        assertThat(salvo.getValue().getNome()).isEqualTo("Ana");
        assertThat(salvo.getValue().getSenha()).isNotEqualTo("segredo1");
        assertThat(encoder.matches("segredo1", salvo.getValue().getSenha())).isTrue();
        assertThat(resposta.id()).isEqualTo(1L);
    }

    @Test
    void criarComEmailJaCadastradoLancaConflito() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@email.com")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new UsuarioCreateRequest("Ana", "ana@email.com", "segredo1", null)))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("E-mail já cadastrado");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void buscarInexistenteLancaNaoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário 99 não encontrado");
    }

    @Test
    void atualizarSemSenhaMantemOHashAtual() {
        Usuario ana = ana();
        String hashAntigo = ana.getSenha();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("ana.nova@email.com", 1L)).thenReturn(false);

        UsuarioResponse resposta = service.atualizar(1L,
                new UsuarioUpdateRequest("Ana Maria", "ana.nova@email.com", null, null, null));

        assertThat(resposta.nome()).isEqualTo("Ana Maria");
        assertThat(ana.getEmail()).isEqualTo("ana.nova@email.com");
        assertThat(ana.getSenha()).isEqualTo(hashAntigo);
    }

    @Test
    void atualizarComEmailDeOutroUsuarioLancaConflito() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana()));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("bia@email.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, new UsuarioUpdateRequest("Ana", "bia@email.com", null, null, null)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void excluirRemoveLancamentosEContasAntesDoUsuario() {
        Usuario ana = ana();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));

        service.excluir(1L);

        InOrder ordem = inOrder(receitaRepository, gastoRepository, investimentoRepository, contaRepository,
                usuarioRepository);
        ordem.verify(receitaRepository).deleteByUsuarioId(1L);
        ordem.verify(gastoRepository).deleteByUsuarioId(1L);
        ordem.verify(investimentoRepository).deleteByUsuarioId(1L);
        ordem.verify(contaRepository).deleteByUsuarioId(1L);
        ordem.verify(usuarioRepository).delete(ana);
        ordem.verify(usuarioRepository).flush();
    }
}
```

`service/AuthServiceTest.java`:
```java
package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.LoginRequest;
import br.com.fiap.fintech.exception.CredenciaisInvalidasException;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    AuthService service;
    Usuario demo;

    @BeforeEach
    void setUp() {
        service = new AuthService(usuarioRepository, encoder);
        demo = new Usuario();
        demo.setId(7L);
        demo.setNome("Usuária Demo");
        demo.setEmail("demo@fintech.com");
        demo.setSenha(encoder.encode("segredo1"));
        demo.setAtivo(true);
    }

    @Test
    void loginComSenhaCorretaDevolveUsuario() {
        when(usuarioRepository.findByEmailIgnoreCase("demo@fintech.com")).thenReturn(Optional.of(demo));

        assertThat(service.login(new LoginRequest(" demo@fintech.com ", "segredo1")).id()).isEqualTo(7L);
    }

    @Test
    void loginComSenhaErradaLanca401() {
        when(usuarioRepository.findByEmailIgnoreCase("demo@fintech.com")).thenReturn(Optional.of(demo));

        assertThatThrownBy(() -> service.login(new LoginRequest("demo@fintech.com", "errada")))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("E-mail ou senha inválidos");
    }

    @Test
    void usuarioInativoNaoEntra() {
        demo.setAtivo(false);
        when(usuarioRepository.findByEmailIgnoreCase("demo@fintech.com")).thenReturn(Optional.of(demo));

        assertThatThrownBy(() -> service.login(new LoginRequest("demo@fintech.com", "segredo1")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }
}
```

- [ ] **Passo 3: testes dos controllers (falham)**

`controller/UsuarioControllerTest.java`:
```java
package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.UsuarioResponse;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean UsuarioService service;

    private final UsuarioResponse ana =
            new UsuarioResponse(1L, "Ana", "ana@email.com", null, LocalDate.of(2026, 1, 10), true);

    @Test
    void listarRetorna200() throws Exception {
        when(service.listar()).thenReturn(List.of(ana));
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ana@email.com"));
    }

    @Test
    void buscarRetorna200SemSenha() throws Exception {
        when(service.buscar(1L)).thenReturn(ana);
        mvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void buscarInexistenteRetorna404ComMensagem() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Usuário 99 não encontrado"));
        mvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Usuário 99 não encontrado"));
    }

    @Test
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any())).thenReturn(ana);
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","email":"ana@email.com","senha":"segredo1"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/usuarios/1")))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void criarInvalidoRetorna400ComDetalhes() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","email":"sem-arroba","senha":"1"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes").isNotEmpty());
        verifyNoInteractions(service);
    }

    @Test
    void criarComEmailDuplicadoRetorna409() throws Exception {
        when(service.criar(any())).thenThrow(new ConflitoException("E-mail já cadastrado: ana@email.com"));
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","email":"ana@email.com","senha":"segredo1"}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(service.atualizar(eq(1L), any())).thenReturn(ana);
        mvc.perform(put("/api/usuarios/1").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","email":"ana@email.com"}"""))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mvc.perform(delete("/api/usuarios/1")).andExpect(status().isNoContent());
        verify(service).excluir(1L);
    }

    @Test
    void jsonMalformadoRetorna400() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content("{nome"))
                .andExpect(status().isBadRequest());
    }
}
```

`controller/AuthControllerTest.java`:
```java
package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.UsuarioResponse;
import br.com.fiap.fintech.exception.CredenciaisInvalidasException;
import br.com.fiap.fintech.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean AuthService service;

    private static final String CORPO = """
            {"email":"demo@fintech.com","senha":"segredo1"}""";

    @Test
    void loginValidoRetorna200() throws Exception {
        when(service.login(any())).thenReturn(
                new UsuarioResponse(7L, "Usuária Demo", "demo@fintech.com", null, LocalDate.now(), true));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void loginInvalidoRetorna401() throws Exception {
        when(service.login(any())).thenThrow(new CredenciaisInvalidasException("E-mail ou senha inválidos"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos"));
    }
}
```

- [ ] **Passo 4: rodar e confirmar falha** — `./mvnw -q test` → `COMPILATION ERROR` (faltam `UsuarioService`, `AuthService`, controllers).

- [ ] **Passo 5: implementar**

`exception/GlobalExceptionHandler.java`:
```java
package br.com.fiap.fintech.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/** Converte exceções em respostas HTTP com corpo ApiErro. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<ApiErro> naoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler(ConflitoException.class)
    ResponseEntity<ApiErro> conflito(ConflitoException e) {
        return resposta(HttpStatus.CONFLICT, e.getMessage(), List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    ResponseEntity<ApiErro> regraNegocio(RegraNegocioException e) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), List.of());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    ResponseEntity<ApiErro> credenciais(CredenciaisInvalidasException e) {
        return resposta(HttpStatus.UNAUTHORIZED, e.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErro> validacao(MethodArgumentNotValidException e) {
        List<String> detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .sorted()
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", detalhes);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    ResponseEntity<ApiErro> requisicaoInvalida(Exception e) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida: verifique o formato dos dados enviados", List.of());
    }

    // Constraints do banco (UNIQUE, FK, CHECK) que escaparam das validações do service
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErro> integridade(DataIntegrityViolationException e) {
        return resposta(HttpStatus.CONFLICT,
                "A operação viola uma restrição do banco de dados (registro duplicado ou em uso)", List.of());
    }

    private ResponseEntity<ApiErro> resposta(HttpStatus status, String mensagem, List<String> detalhes) {
        return ResponseEntity.status(status).body(new ApiErro(status.value(), mensagem, detalhes));
    }
}
```

`service/UsuarioService.java`:
```java
package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.UsuarioCreateRequest;
import br.com.fiap.fintech.dto.UsuarioResponse;
import br.com.fiap.fintech.dto.UsuarioUpdateRequest;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.ContaBancariaRepository;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ReceitaRepository receitaRepository;
    private final GastoRepository gastoRepository;
    private final InvestimentoRepository investimentoRepository;
    private final ContaBancariaRepository contaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, ReceitaRepository receitaRepository,
                          GastoRepository gastoRepository, InvestimentoRepository investimentoRepository,
                          ContaBancariaRepository contaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.receitaRepository = receitaRepository;
        this.gastoRepository = gastoRepository;
        this.investimentoRepository = investimentoRepository;
        this.contaRepository = contaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll(Sort.by("id")).stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        return UsuarioResponse.de(obter(id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioCreateRequest req) {
        String email = normalizarEmail(req.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("E-mail já cadastrado: " + email);
        }
        Usuario usuario = new Usuario();
        usuario.setNome(req.nome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(req.senha()));
        usuario.setAvatar(req.avatar());
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioUpdateRequest req) {
        Usuario usuario = obter(id);
        String email = normalizarEmail(req.email());
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflitoException("E-mail já cadastrado: " + email);
        }
        usuario.setNome(req.nome().trim());
        usuario.setEmail(email);
        usuario.setAvatar(req.avatar());
        if (req.ativo() != null) {
            usuario.setAtivo(req.ativo());
        }
        if (req.senha() != null && !req.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(req.senha()));
        }
        return UsuarioResponse.de(usuario);
    }

    /** Encerra o cadastro: apaga receitas, gastos, investimentos e contas do usuário (D-F8-06). */
    @Transactional
    public void excluir(Long id) {
        Usuario usuario = obter(id);
        receitaRepository.deleteByUsuarioId(id);
        gastoRepository.deleteByUsuarioId(id);
        investimentoRepository.deleteByUsuarioId(id);
        contaRepository.deleteByUsuarioId(id);
        usuarioRepository.delete(usuario);
        // Força o DELETE aqui: se houver receitas/metas/dívidas, a FK vira 409 no handler
        usuarioRepository.flush();
    }

    private Usuario obter(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado"));
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
```

`service/AuthService.java`:
```java
package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.LoginRequest;
import br.com.fiap.fintech.dto.UsuarioResponse;
import br.com.fiap.fintech.exception.CredenciaisInvalidasException;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Mesma mensagem para e-mail inexistente, senha errada ou usuário inativo (não revela qual falhou). */
    @Transactional(readOnly = true)
    public UsuarioResponse login(LoginRequest req) {
        return usuarioRepository.findByEmailIgnoreCase(req.email().trim())
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .filter(u -> passwordEncoder.matches(req.senha(), u.getSenha()))
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha inválidos"));
    }
}
```

`controller/UsuarioController.java`:
```java
package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.UsuarioCreateRequest;
import br.com.fiap.fintech.dto.UsuarioResponse;
import br.com.fiap.fintech.dto.UsuarioUpdateRequest;
import br.com.fiap.fintech.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
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
```

`controller/AuthController.java`:
```java
package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.LoginRequest;
import br.com.fiap.fintech.dto.UsuarioResponse;
import br.com.fiap.fintech.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }
}
```

- [ ] **Passo 6: rodar** — `./mvnw -q test` → `BUILD SUCCESS`.
- [ ] **Passo 7: commit** — `git add api/ && git commit -m "feat(api): CRUD de usuários, login com BCrypt e tratamento de erros"`

---

### Task B03: ContaBancaria e Categoria — CRUD completo com regras

> Revisão 1 (R2): todo controller precisa de GET/POST/PUT/DELETE e de regra de negócio no service.

**Arquivos:**
- Criar: `dto/{ContaRequest,ContaResponse,CategoriaRequest,CategoriaResponse}.java`
- Criar: `service/{Posse,ContaBancariaService,CategoriaService}.java`, `controller/{ContaBancariaController,CategoriaController}.java`
- Testes: `service/ContaBancariaServiceTest.java`, `service/CategoriaServiceTest.java`, `controller/ContaBancariaControllerTest.java`, `controller/CategoriaControllerTest.java`

**Interfaces:**
- Consome (B01): `ContaBancariaRepository`, `CategoriaRepository`, `UsuarioRepository`, `ReceitaRepository.existsByContaId/existsByCategoriaId`, `GastoRepository.existsByContaId/existsByCategoriaId`, `InvestimentoRepository.existsByContaId`.
- Produz (usado por B04–B06):
  - `Posse.garantirMesmoDono(Usuario donoAtual, Long usuarioIdPedido)` (package-private, `service`): lança `RegraNegocioException("Não é permitido transferir o registro para outro usuário")`; aceita `donoAtual == null` (registro novo).
  - `ContaBancariaService.obterContaAtivaDoUsuario(Long contaId, Long usuarioId): ContaBancaria`: lança `RegraNegocioException` com `"Conta {id} não encontrada"`, `"A conta informada não pertence ao usuário"` ou `"A conta informada está inativa"`.
  - `ContaBancariaService.{listar(Long usuarioIdOuNull), buscar(Long), criar(ContaRequest), atualizar(Long, ContaRequest), excluir(Long)}`.
  - `CategoriaService.{listar(String tipoOuNull), buscar(Long), criar(CategoriaRequest), atualizar(Long, CategoriaRequest), excluir(Long)}`.

- [ ] **Passo 1: DTOs e helper de posse**
```java
package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** ativa nula = true na criação; na edição mantém o valor atual. */
public record ContaRequest(
        @NotNull Long usuarioId,
        @NotBlank @Size(max = 100) String nmBanco,
        @NotBlank String tipoConta,
        @NotBlank @Size(max = 20) String nrConta,
        Boolean ativa) {
}
```
```java
package br.com.fiap.fintech.dto;

import br.com.fiap.fintech.model.ContaBancaria;

public record ContaResponse(Long id, Long usuarioId, String nmBanco, String tipoConta, String nrConta, boolean ativa) {

    public static ContaResponse de(ContaBancaria c) {
        return new ContaResponse(c.getId(), c.getUsuario().getId(), c.getNmBanco(), c.getTipoConta(), c.getNrConta(),
                Boolean.TRUE.equals(c.getAtiva()));
    }
}
```
```java
package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(@NotBlank @Size(max = 80) String nome, @NotBlank String tipo) {
}
```
```java
package br.com.fiap.fintech.dto;

import br.com.fiap.fintech.model.Categoria;

public record CategoriaResponse(Long id, String nome, String tipo) {

    public static CategoriaResponse de(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome(), c.getTipo());
    }
}
```
```java
package br.com.fiap.fintech.service;

import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.Usuario;

/** Regra comum: um registro (conta ou lançamento) não muda de dono num PUT. */
final class Posse {

    private Posse() {
    }

    static void garantirMesmoDono(Usuario donoAtual, Long usuarioIdPedido) {
        if (donoAtual != null && !donoAtual.getId().equals(usuarioIdPedido)) {
            throw new RegraNegocioException("Não é permitido transferir o registro para outro usuário");
        }
    }
}
```

- [ ] **Passo 2: testes dos services (falham)**

`service/ContaBancariaServiceTest.java`:
```java
package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.ContaRequest;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.ContaBancariaRepository;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaBancariaServiceTest {

    @Mock ContaBancariaRepository contaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ReceitaRepository receitaRepository;
    @Mock GastoRepository gastoRepository;
    @Mock InvestimentoRepository investimentoRepository;
    @InjectMocks ContaBancariaService service;
    Usuario ana;
    ContaBancaria conta;

    @BeforeEach
    void setUp() {
        ana = new Usuario();
        ana.setId(1L);
        conta = new ContaBancaria(ana, "Banco Demo", "CORRENTE", "0001-9");
        conta.setId(5L);
    }

    @Test
    void contaDoProprioUsuarioEhAceita() {
        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));
        assertThat(service.obterContaAtivaDoUsuario(5L, 1L)).isSameAs(conta);
    }

    @Test
    void contaDeOutroUsuarioEhRecusada() {
        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));
        assertThatThrownBy(() -> service.obterContaAtivaDoUsuario(5L, 2L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A conta informada não pertence ao usuário");
    }

    @Test
    void contaInativaEhRecusada() {
        conta.setAtiva(false);
        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));
        assertThatThrownBy(() -> service.obterContaAtivaDoUsuario(5L, 1L))
                .hasMessage("A conta informada está inativa");
    }

    @Test
    void contaInexistenteEhRecusada() {
        when(contaRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obterContaAtivaDoUsuario(9L, 1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Conta 9 não encontrada");
    }

    @Test
    void criarNormalizaTipoDeConta() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(contaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.criar(new ContaRequest(1L, "Nubank", "Poupança", "123", null)).tipoConta())
                .isEqualTo("POUPANCA");
    }

    @Test
    void criarComTipoInvalidoEhRecusado() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        assertThatThrownBy(() -> service.criar(new ContaRequest(1L, "Nubank", "Digital", "123", null)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void atualizarPodeDesativarAConta() {
        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));

        assertThat(service.atualizar(5L, new ContaRequest(1L, "Banco Demo", "CORRENTE", "0001-9", false)).ativa())
                .isFalse();
    }

    @Test
    void atualizarNaoTransfereContaParaOutroUsuario() {
        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.atualizar(5L, new ContaRequest(2L, "Banco Demo", "CORRENTE", "0001-9", null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é permitido transferir o registro para outro usuário");
    }

    @Test
    void excluirContaComLancamentosLancaConflito() {
        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));
        when(receitaRepository.existsByContaId(5L)).thenReturn(false);
        when(gastoRepository.existsByContaId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(5L))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("A conta possui lançamentos vinculados; desative-a em vez de excluir");
        verify(contaRepository, never()).delete(any());
    }

    @Test
    void excluirContaSemLancamentosRemove() {
        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));

        service.excluir(5L);

        verify(contaRepository).delete(conta);
    }
}
```

`service/CategoriaServiceTest.java`:
```java
package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.CategoriaRequest;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.Categoria;
import br.com.fiap.fintech.repository.CategoriaRepository;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock CategoriaRepository categoriaRepository;
    @Mock ReceitaRepository receitaRepository;
    @Mock GastoRepository gastoRepository;
    @InjectMocks CategoriaService service;
    Categoria moradia;

    @BeforeEach
    void setUp() {
        moradia = new Categoria("Moradia", "GASTO");
        moradia.setId(3L);
    }

    @Test
    void criarNormalizaTipo() {
        when(categoriaRepository.existsByNomeIgnoreCaseAndTipo("Pets", "GASTO")).thenReturn(false);
        when(categoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.criar(new CategoriaRequest(" Pets ", "gasto")).tipo()).isEqualTo("GASTO");
    }

    @Test
    void criarDuplicadaLancaConflito() {
        when(categoriaRepository.existsByNomeIgnoreCaseAndTipo("Moradia", "GASTO")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new CategoriaRequest("Moradia", "GASTO")))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe a categoria 'Moradia' do tipo GASTO");
    }

    @Test
    void tipoForaDoDominioEhRecusado() {
        assertThatThrownBy(() -> service.criar(new CategoriaRequest("Pets", "DESPESA")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Tipo de categoria inválido: use RECEITA ou GASTO");
    }

    @Test
    void naoMudaOTipoDeCategoriaEmUso() {
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(moradia));
        when(categoriaRepository.existsByNomeIgnoreCaseAndTipoAndIdNot("Moradia", "RECEITA", 3L)).thenReturn(false);
        when(receitaRepository.existsByCategoriaId(3L)).thenReturn(false);
        when(gastoRepository.existsByCategoriaId(3L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(3L, new CategoriaRequest("Moradia", "RECEITA")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é possível mudar o tipo de uma categoria em uso");
    }

    @Test
    void excluirCategoriaEmUsoLancaConflito() {
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(moradia));
        when(receitaRepository.existsByCategoriaId(3L)).thenReturn(false);
        when(gastoRepository.existsByCategoriaId(3L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(3L))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("A categoria está em uso por lançamentos e não pode ser excluída");
        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void buscarInexistenteLancaNaoEncontrado() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Categoria 99 não encontrada");
    }
}
```

- [ ] **Passo 3: testes dos controllers (falham)**

`controller/ContaBancariaControllerTest.java`:
```java
package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.ContaResponse;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.service.ContaBancariaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContaBancariaController.class)
class ContaBancariaControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean ContaBancariaService service;

    private static final String CORPO = """
            {"usuarioId":1,"nmBanco":"Banco Demo","tipoConta":"CORRENTE","nrConta":"0001-9"}""";
    private final ContaResponse conta = new ContaResponse(5L, 1L, "Banco Demo", "CORRENTE", "0001-9", true);

    @Test
    void listarPorUsuarioRetorna200() throws Exception {
        when(service.listar(1L)).thenReturn(List.of(conta));
        mvc.perform(get("/api/contas").param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nmBanco").value("Banco Demo"));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Conta 99 não encontrada"));
        mvc.perform(get("/api/contas/99")).andExpect(status().isNotFound());
    }

    @Test
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any())).thenReturn(conta);
        mvc.perform(post("/api/contas").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/contas/5")));
    }

    @Test
    void criarSemBancoRetorna400() throws Exception {
        mvc.perform(post("/api/contas").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO.replace("Banco Demo", "")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void criarComTipoInvalidoRetorna422() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("Tipo de conta inválido: DIGITAL"));
        mvc.perform(post("/api/contas").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(service.atualizar(eq(5L), any())).thenReturn(conta);
        mvc.perform(put("/api/contas/5").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mvc.perform(delete("/api/contas/5")).andExpect(status().isNoContent());
        verify(service).excluir(5L);
    }

    @Test
    void excluirContaComLancamentosRetorna409() throws Exception {
        doThrow(new ConflitoException("A conta possui lançamentos vinculados; desative-a em vez de excluir"))
                .when(service).excluir(5L);
        mvc.perform(delete("/api/contas/5"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("A conta possui lançamentos vinculados; desative-a em vez de excluir"));
    }
}
```

`controller/CategoriaControllerTest.java`:
```java
package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.CategoriaResponse;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean CategoriaService service;

    private static final String CORPO = """
            {"nome":"Moradia","tipo":"GASTO"}""";
    private final CategoriaResponse moradia = new CategoriaResponse(3L, "Moradia", "GASTO");

    @Test
    void listarPorTipoRetorna200() throws Exception {
        when(service.listar("GASTO")).thenReturn(List.of(moradia));
        mvc.perform(get("/api/categorias").param("tipo", "GASTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Moradia"));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Categoria 99 não encontrada"));
        mvc.perform(get("/api/categorias/99")).andExpect(status().isNotFound());
    }

    @Test
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any())).thenReturn(moradia);
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/categorias/3")));
    }

    @Test
    void criarDuplicadaRetorna409() throws Exception {
        when(service.criar(any())).thenThrow(new ConflitoException("Já existe a categoria 'Moradia' do tipo GASTO"));
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isConflict());
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(service.atualizar(eq(3L), any())).thenReturn(moradia);
        mvc.perform(put("/api/categorias/3").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mvc.perform(delete("/api/categorias/3")).andExpect(status().isNoContent());
        verify(service).excluir(3L);
    }
}
```

- [ ] **Passo 4: rodar e confirmar falha** — `./mvnw -q test` → `COMPILATION ERROR`.

- [ ] **Passo 5: implementar os services**

`service/ContaBancariaService.java`:
```java
package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.ContaRequest;
import br.com.fiap.fintech.dto.ContaResponse;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Dominios;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.ContaBancariaRepository;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ContaBancariaService {

    private static final Set<String> TIPOS = Set.of("CORRENTE", "POUPANCA", "PAGAMENTO", "SALARIO", "INVESTIMENTO");

    private final ContaBancariaRepository contaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReceitaRepository receitaRepository;
    private final GastoRepository gastoRepository;
    private final InvestimentoRepository investimentoRepository;

    public ContaBancariaService(ContaBancariaRepository contaRepository, UsuarioRepository usuarioRepository,
                                ReceitaRepository receitaRepository, GastoRepository gastoRepository,
                                InvestimentoRepository investimentoRepository) {
        this.contaRepository = contaRepository;
        this.usuarioRepository = usuarioRepository;
        this.receitaRepository = receitaRepository;
        this.gastoRepository = gastoRepository;
        this.investimentoRepository = investimentoRepository;
    }

    /** usuarioId nulo lista todas as contas. */
    @Transactional(readOnly = true)
    public List<ContaResponse> listar(Long usuarioId) {
        var contas = usuarioId == null
                ? contaRepository.findAllByOrderByIdAsc()
                : contaRepository.findByUsuarioIdOrderByIdAsc(usuarioId);
        return contas.stream().map(ContaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ContaResponse buscar(Long id) {
        return ContaResponse.de(obter(id));
    }

    @Transactional
    public ContaResponse criar(ContaRequest req) {
        Usuario usuario = usuarioRepository.findById(req.usuarioId())
                .orElseThrow(() -> new RegraNegocioException("Usuário " + req.usuarioId() + " não encontrado"));
        ContaBancaria conta = new ContaBancaria(usuario, req.nmBanco().trim(), tipoValido(req.tipoConta()),
                req.nrConta().trim());
        if (req.ativa() != null) {
            conta.setAtiva(req.ativa());
        }
        return ContaResponse.de(contaRepository.save(conta));
    }

    /** Com ativa=false a conta é desativada (antigo desativarConta). */
    @Transactional
    public ContaResponse atualizar(Long id, ContaRequest req) {
        ContaBancaria conta = obter(id);
        Posse.garantirMesmoDono(conta.getUsuario(), req.usuarioId());
        conta.setNmBanco(req.nmBanco().trim());
        conta.setTipoConta(tipoValido(req.tipoConta()));
        conta.setNrConta(req.nrConta().trim());
        if (req.ativa() != null) {
            conta.setAtiva(req.ativa());
        }
        return ContaResponse.de(conta);
    }

    @Transactional
    public void excluir(Long id) {
        ContaBancaria conta = obter(id);
        if (receitaRepository.existsByContaId(id) || gastoRepository.existsByContaId(id)
                || investimentoRepository.existsByContaId(id)) {
            throw new ConflitoException("A conta possui lançamentos vinculados; desative-a em vez de excluir");
        }
        contaRepository.delete(conta);
    }

    /** Regra usada por receitas, gastos e investimentos: só conta ativa do próprio usuário (D-14). */
    @Transactional(readOnly = true)
    public ContaBancaria obterContaAtivaDoUsuario(Long contaId, Long usuarioId) {
        ContaBancaria conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new RegraNegocioException("Conta " + contaId + " não encontrada"));
        if (!conta.getUsuario().getId().equals(usuarioId)) {
            throw new RegraNegocioException("A conta informada não pertence ao usuário");
        }
        if (!Boolean.TRUE.equals(conta.getAtiva())) {
            throw new RegraNegocioException("A conta informada está inativa");
        }
        return conta;
    }

    private ContaBancaria obter(Long id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta " + id + " não encontrada"));
    }

    private static String tipoValido(String tipoConta) {
        String tipo = Dominios.normalizar(tipoConta);
        if (!TIPOS.contains(tipo)) {
            throw new RegraNegocioException("Tipo de conta inválido: " + tipoConta);
        }
        return tipo;
    }
}
```

`service/CategoriaService.java`:
```java
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
```

- [ ] **Passo 6: implementar os controllers**

`controller/ContaBancariaController.java`:
```java
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
```

`controller/CategoriaController.java`:
```java
package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.CategoriaRequest;
import br.com.fiap.fintech.dto.CategoriaResponse;
import br.com.fiap.fintech.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaResponse> listar(@RequestParam(required = false) String tipo) {
        return service.listar(tipo);
    }

    @GetMapping("/{id}")
    public CategoriaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest req, UriComponentsBuilder uri) {
        CategoriaResponse criada = service.criar(req);
        return ResponseEntity.created(uri.path("/api/categorias/{id}").buildAndExpand(criada.id()).toUri())
                .body(criada);
    }

    @PutMapping("/{id}")
    public CategoriaResponse atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
```

- [ ] **Passo 7: rodar** — `./mvnw -q test` → `BUILD SUCCESS`.
- [ ] **Passo 8: commit** — `git add api/ && git commit -m "feat(api): CRUD de contas bancárias e categorias com regras de uso e posse"`

---

### Task B04: Gasto

**Arquivos:**
- Criar: `dto/{GastoRequest,GastoResponse}.java`, `service/GastoService.java`, `controller/GastoController.java`
- Testes: `service/GastoServiceTest.java`, `controller/GastoControllerTest.java`

**Interfaces:**
- Consome: `ContaBancariaService.obterContaAtivaDoUsuario(Long, Long)`, `Posse.garantirMesmoDono`, `CategoriaRepository.findById`, `GastoRepository`.
- Produz: `GastoService.{listar(Long usuarioIdOuNull), buscar(Long), criar(GastoRequest), atualizar(Long, GastoRequest), excluir(Long)}`.

- [ ] **Passo 1: DTOs**
```java
package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoRequest(
        @NotNull Long usuarioId,
        @NotNull Long contaId,
        @NotNull Long categoriaId,
        @NotBlank @Size(max = 300) String descricao,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valor,
        @NotNull LocalDate dtGasto,
        @NotBlank String tipo,
        @Size(max = 500) String comprovante) {
}
```
```java
package br.com.fiap.fintech.dto;

import br.com.fiap.fintech.model.Gasto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoResponse(Long id, Long usuarioId, Long contaId, String conta, Long categoriaId, String categoria,
                            String descricao, BigDecimal valor, LocalDate dtGasto, String tipo, String comprovante) {

    public static GastoResponse de(Gasto g) {
        return new GastoResponse(g.getId(), g.getUsuario().getId(), g.getConta().getId(), g.getConta().getRotulo(),
                g.getCategoria().getId(), g.getCategoria().getNome(), g.getDescricao(), g.getValor(), g.getDtGasto(),
                g.getTipo(), g.getComprovante());
    }
}
```

- [ ] **Passo 2: testes (falham)**

`service/GastoServiceTest.java`:
```java
package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.GastoRequest;
import br.com.fiap.fintech.dto.GastoResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.Categoria;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Gasto;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.CategoriaRepository;
import br.com.fiap.fintech.repository.GastoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GastoServiceTest {

    @Mock GastoRepository gastoRepository;
    @Mock CategoriaRepository categoriaRepository;
    @Mock ContaBancariaService contaService;
    @InjectMocks GastoService service;
    ContaBancaria conta;
    Categoria moradia;

    @BeforeEach
    void setUp() {
        Usuario ana = new Usuario();
        ana.setId(1L);
        conta = new ContaBancaria(ana, "Banco Demo", "CORRENTE", "0001-9");
        conta.setId(5L);
        moradia = new Categoria("Moradia", "GASTO");
        moradia.setId(3L);
    }

    private GastoRequest pedido(String tipo) {
        return new GastoRequest(1L, 5L, 3L, " Aluguel ", new BigDecimal("1500.00"), LocalDate.of(2026, 11, 1), tipo, null);
    }

    @Test
    void criarNormalizaTipoEUsaContaECategoria() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(moradia));
        when(gastoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GastoResponse r = service.criar(pedido("Variável"));

        assertThat(r.tipo()).isEqualTo("VARIAVEL");
        assertThat(r.descricao()).isEqualTo("Aluguel");
        assertThat(r.categoria()).isEqualTo("Moradia");
        assertThat(r.conta()).isEqualTo("Banco Demo · 0001-9");
        assertThat(r.usuarioId()).isEqualTo(1L);
    }

    @Test
    void categoriaDeReceitaEhRecusada() {
        Categoria salario = new Categoria("Salário", "RECEITA");
        salario.setId(1L);
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(salario));

        assertThatThrownBy(() -> service.criar(pedido("FIXO")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A categoria informada não é de gasto");
        verify(gastoRepository, never()).save(any());
    }

    @Test
    void tipoForaDoDominioEhRecusado() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(moradia));

        assertThatThrownBy(() -> service.criar(pedido("Eventual")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Tipo de gasto inválido: use FIXO ou VARIAVEL");
    }

    @Test
    void contaDeOutroUsuarioPropagaRegraDeNegocio() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L))
                .thenThrow(new RegraNegocioException("A conta informada não pertence ao usuário"));

        assertThatThrownBy(() -> service.criar(pedido("FIXO"))).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void atualizarAlteraOGastoExistente() {
        Gasto existente = new Gasto();
        existente.setId(10L);
        when(gastoRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(moradia));

        GastoResponse r = service.atualizar(10L, pedido("FIXO"));

        assertThat(r.id()).isEqualTo(10L);
        assertThat(existente.getValor()).isEqualByComparingTo("1500.00");
    }

    @Test
    void atualizarNaoTransfereParaOutroUsuario() {
        Usuario bia = new Usuario();
        bia.setId(2L);
        Gasto existente = new Gasto();
        existente.setId(10L);
        existente.setUsuario(bia);
        when(gastoRepository.findById(10L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.atualizar(10L, pedido("FIXO")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é permitido transferir o registro para outro usuário");
    }

    @Test
    void excluirInexistenteLancaNaoEncontrado() {
        when(gastoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Gasto 99 não encontrado");
    }
}
```

`controller/GastoControllerTest.java`:
```java
package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.GastoResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.service.GastoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GastoController.class)
class GastoControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean GastoService service;

    private static final String CORPO = """
            {"usuarioId":1,"contaId":5,"categoriaId":3,"descricao":"Aluguel","valor":1500.00,
             "dtGasto":"2026-11-01","tipo":"FIXO"}""";

    private final GastoResponse aluguel = new GastoResponse(10L, 1L, 5L, "Banco Demo · 0001-9", 3L, "Moradia",
            "Aluguel", new BigDecimal("1500.00"), LocalDate.of(2026, 11, 1), "FIXO", null);

    @Test
    void listarPorUsuarioRetorna200() throws Exception {
        when(service.listar(1L)).thenReturn(List.of(aluguel));
        mvc.perform(get("/api/gastos").param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("Moradia"))
                .andExpect(jsonPath("$[0].dtGasto").value("2026-11-01"));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Gasto 99 não encontrado"));
        mvc.perform(get("/api/gastos/99")).andExpect(status().isNotFound());
    }

    @Test
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any())).thenReturn(aluguel);
        mvc.perform(post("/api/gastos").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/gastos/10")));
    }

    @Test
    void criarComValorNegativoRetorna400() throws Exception {
        mvc.perform(post("/api/gastos").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO.replace("1500.00", "-5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value(org.hamcrest.Matchers.startsWith("valor:")));
        verifyNoInteractions(service);
    }

    @Test
    void criarComContaDeOutroUsuarioRetorna422() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("A conta informada não pertence ao usuário"));
        mvc.perform(post("/api/gastos").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("A conta informada não pertence ao usuário"));
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(service.atualizar(eq(10L), any())).thenReturn(aluguel);
        mvc.perform(put("/api/gastos/10").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mvc.perform(delete("/api/gastos/10")).andExpect(status().isNoContent());
        verify(service).excluir(10L);
    }
}
```

- [ ] **Passo 3: rodar e confirmar falha** — `COMPILATION ERROR`.

- [ ] **Passo 4: implementar**

`service/GastoService.java`:
```java
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
```

`controller/GastoController.java`:
```java
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
```

- [ ] **Passo 5: rodar** — `./mvnw -q test` → `BUILD SUCCESS`.
- [ ] **Passo 6: commit** — `git add api/ && git commit -m "feat(api): CRUD de gastos com regras de conta, categoria e tipo"`

---

### Task B05: Receita

> Revisão 1 (R1): user story nº 2 do MVP e tela `receitas.html` do protótipo. Fica com 3 entidades financeiras com CRUD completo.

**Arquivos:**
- Criar: `dto/{ReceitaRequest,ReceitaResponse}.java`, `service/ReceitaService.java`, `controller/ReceitaController.java`
- Testes: `service/ReceitaServiceTest.java`, `controller/ReceitaControllerTest.java`

**Interfaces:**
- Consome: `ContaBancariaService.obterContaAtivaDoUsuario(Long, Long)`, `Posse.garantirMesmoDono`, `CategoriaRepository.findById`, `ReceitaRepository`.
- Produz: `ReceitaService.{listar(Long usuarioIdOuNull), buscar(Long), criar(ReceitaRequest), atualizar(Long, ReceitaRequest), excluir(Long)}`.

- [ ] **Passo 1: DTOs**
```java
package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** origem e recorrencia são opcionais; recorrência nula vira UNICA. */
public record ReceitaRequest(
        @NotNull Long usuarioId,
        @NotNull Long contaId,
        @NotNull Long categoriaId,
        @NotBlank @Size(max = 300) String descricao,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valor,
        @NotNull LocalDate dtRecebimento,
        String origem,
        String recorrencia,
        @Size(max = 500) String comprovante) {
}
```
```java
package br.com.fiap.fintech.dto;

import br.com.fiap.fintech.model.Receita;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceitaResponse(Long id, Long usuarioId, Long contaId, String conta, Long categoriaId, String categoria,
                              String descricao, BigDecimal valor, LocalDate dtRecebimento, String origem,
                              String recorrencia, String comprovante) {

    public static ReceitaResponse de(Receita r) {
        return new ReceitaResponse(r.getId(), r.getUsuario().getId(), r.getConta().getId(), r.getConta().getRotulo(),
                r.getCategoria().getId(), r.getCategoria().getNome(), r.getDescricao(), r.getValor(),
                r.getDtRecebimento(), r.getOrigem(), r.getRecorrencia(), r.getComprovante());
    }
}
```

- [ ] **Passo 2: testes (falham)**

`service/ReceitaServiceTest.java`:
```java
package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.ReceitaRequest;
import br.com.fiap.fintech.dto.ReceitaResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.Categoria;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Receita;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.CategoriaRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceitaServiceTest {

    @Mock ReceitaRepository receitaRepository;
    @Mock CategoriaRepository categoriaRepository;
    @Mock ContaBancariaService contaService;
    @InjectMocks ReceitaService service;
    Usuario ana;
    ContaBancaria conta;
    Categoria salario;

    @BeforeEach
    void setUp() {
        ana = new Usuario();
        ana.setId(1L);
        conta = new ContaBancaria(ana, "Banco Demo", "CORRENTE", "0001-9");
        conta.setId(5L);
        salario = new Categoria("Salário", "RECEITA");
        salario.setId(1L);
    }

    private ReceitaRequest pedido(Long usuarioId, String origem, String recorrencia) {
        return new ReceitaRequest(usuarioId, 5L, 1L, " Salário de novembro ", new BigDecimal("5000.00"),
                LocalDate.of(2026, 11, 5), origem, recorrencia, null);
    }

    @Test
    void criarNormalizaDominiosEAssumeRecorrenciaUnica() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(salario));
        when(receitaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReceitaResponse r = service.criar(pedido(1L, "Salário", null));

        assertThat(r.origem()).isEqualTo("SALARIO");
        assertThat(r.recorrencia()).isEqualTo("UNICA");
        assertThat(r.descricao()).isEqualTo("Salário de novembro");
        assertThat(r.categoria()).isEqualTo("Salário");
    }

    @Test
    void categoriaDeGastoEhRecusada() {
        Categoria moradia = new Categoria("Moradia", "GASTO");
        moradia.setId(1L);
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(moradia));

        assertThatThrownBy(() -> service.criar(pedido(1L, null, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A categoria informada não é de receita");
        verify(receitaRepository, never()).save(any());
    }

    @Test
    void origemForaDoDominioEhRecusada() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(salario));

        assertThatThrownBy(() -> service.criar(pedido(1L, "Herança", null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Origem inválida: use SALARIO, FREELANCE, INVESTIMENTO ou OUTROS");
    }

    @Test
    void recorrenciaForaDoDominioEhRecusada() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(salario));

        assertThatThrownBy(() -> service.criar(pedido(1L, null, "Semanal")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Recorrência inválida: use UNICA, MENSAL ou ANUAL");
    }

    @Test
    void atualizarNaoTransfereParaOutroUsuario() {
        Receita existente = new Receita();
        existente.setId(30L);
        existente.setUsuario(ana);
        when(receitaRepository.findById(30L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.atualizar(30L, pedido(2L, null, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é permitido transferir o registro para outro usuário");
    }

    @Test
    void excluirInexistenteLancaNaoEncontrado() {
        when(receitaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Receita 99 não encontrada");
    }
}
```

`controller/ReceitaControllerTest.java`:
```java
package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.ReceitaResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.service.ReceitaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceitaController.class)
class ReceitaControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean ReceitaService service;

    private static final String CORPO = """
            {"usuarioId":1,"contaId":5,"categoriaId":1,"descricao":"Salário","valor":5000.00,
             "dtRecebimento":"2026-11-05","origem":"SALARIO","recorrencia":"MENSAL"}""";

    private final ReceitaResponse salario = new ReceitaResponse(30L, 1L, 5L, "Banco Demo · 0001-9", 1L, "Salário",
            "Salário", new BigDecimal("5000.00"), LocalDate.of(2026, 11, 5), "SALARIO", "MENSAL", null);

    @Test
    void listarPorUsuarioRetorna200() throws Exception {
        when(service.listar(1L)).thenReturn(List.of(salario));
        mvc.perform(get("/api/receitas").param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origem").value("SALARIO"));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Receita 99 não encontrada"));
        mvc.perform(get("/api/receitas/99")).andExpect(status().isNotFound());
    }

    @Test
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any())).thenReturn(salario);
        mvc.perform(post("/api/receitas").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/receitas/30")));
    }

    @Test
    void criarSemDataRetorna400() throws Exception {
        mvc.perform(post("/api/receitas").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO.replace("\"dtRecebimento\":\"2026-11-05\",", "")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void criarComCategoriaDeGastoRetorna422() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("A categoria informada não é de receita"));
        mvc.perform(post("/api/receitas").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(service.atualizar(eq(30L), any())).thenReturn(salario);
        mvc.perform(put("/api/receitas/30").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mvc.perform(delete("/api/receitas/30")).andExpect(status().isNoContent());
        verify(service).excluir(30L);
    }
}
```

- [ ] **Passo 3: rodar e confirmar falha** — `COMPILATION ERROR`.

- [ ] **Passo 4: implementar**

`service/ReceitaService.java`:
```java
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
```

`controller/ReceitaController.java`:
```java
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
```

- [ ] **Passo 5: rodar** — `./mvnw -q test` → `BUILD SUCCESS`.
- [ ] **Passo 6: commit** — `git add api/ && git commit -m "feat(api): CRUD de receitas com regras de categoria, origem e recorrência"`

---

### Task B06: Investimento

**Arquivos:**
- Criar: `dto/{InvestimentoRequest,InvestimentoResponse}.java`, `service/InvestimentoService.java`, `controller/InvestimentoController.java`
- Testes: `service/InvestimentoServiceTest.java`, `controller/InvestimentoControllerTest.java`

**Interfaces:**
- Consome: `ContaBancariaService.obterContaAtivaDoUsuario`, `Posse.garantirMesmoDono`, `InvestimentoRepository`, `Investimento.calcularRendimentoEstimado()`.
- Produz: `InvestimentoService.{listar(Long), buscar(Long), criar(InvestimentoRequest), atualizar(Long, InvestimentoRequest), excluir(Long)}`.

- [ ] **Passo 1: DTOs**
```java
package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** taxaRentabilidade em fração anual (0.1250 = 12,5% a.a.); status nulo = ATIVO na criação ou mantém na edição. */
public record InvestimentoRequest(
        @NotNull Long usuarioId,
        @NotNull Long contaId,
        @NotBlank @Size(max = 150) String nomeInvestimento,
        @NotBlank String tipo,
        @Size(max = 100) String instituicao,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valorAplicado,
        @PositiveOrZero @Digits(integer = 3, fraction = 4) BigDecimal taxaRentabilidade,
        @NotNull LocalDate dtAplicacao,
        LocalDate dtVencimento,
        String status) {
}
```
```java
package br.com.fiap.fintech.dto;

import br.com.fiap.fintech.model.Investimento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvestimentoResponse(Long id, Long usuarioId, Long contaId, String conta, String nomeInvestimento,
                                   String tipo, String instituicao, BigDecimal valorAplicado,
                                   BigDecimal taxaRentabilidade, LocalDate dtAplicacao, LocalDate dtVencimento,
                                   String status, BigDecimal rendimentoEstimadoAnual) {

    public static InvestimentoResponse de(Investimento i) {
        return new InvestimentoResponse(i.getId(), i.getUsuario().getId(), i.getConta().getId(),
                i.getConta().getRotulo(), i.getNomeInvestimento(), i.getTipo(), i.getInstituicao(),
                i.getValorAplicado(), i.getTaxaRentabilidade(), i.getDtAplicacao(), i.getDtVencimento(),
                i.getStatus(), i.calcularRendimentoEstimado());
    }
}
```

- [ ] **Passo 2: testes (falham)**

`service/InvestimentoServiceTest.java`:
```java
package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.InvestimentoRequest;
import br.com.fiap.fintech.dto.InvestimentoResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Investimento;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestimentoServiceTest {

    @Mock InvestimentoRepository investimentoRepository;
    @Mock ContaBancariaService contaService;
    @InjectMocks InvestimentoService service;
    ContaBancaria conta;

    @BeforeEach
    void setUp() {
        Usuario ana = new Usuario();
        ana.setId(1L);
        conta = new ContaBancaria(ana, "Banco Demo", "INVESTIMENTO", "0001-9");
        conta.setId(5L);
    }

    private InvestimentoRequest pedido(String tipo, LocalDate vencimento, String status) {
        return new InvestimentoRequest(1L, 5L, "CDB Banco Demo", tipo, "Banco Demo", new BigDecimal("5000.00"),
                new BigDecimal("0.1250"), LocalDate.of(2026, 1, 10), vencimento, status);
    }

    @Test
    void criarSemStatusAssumeAtivoECalculaRendimento() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(investimentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InvestimentoResponse r = service.criar(pedido("cdb", LocalDate.of(2028, 1, 10), null));

        assertThat(r.tipo()).isEqualTo("CDB");
        assertThat(r.status()).isEqualTo("ATIVO");
        assertThat(r.rendimentoEstimadoAnual()).isEqualByComparingTo("625.00");
    }

    @Test
    void vencimentoAntesDaAplicacaoEhRecusado() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);

        assertThatThrownBy(() -> service.criar(pedido("CDB", LocalDate.of(2025, 12, 31), null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A data de vencimento não pode ser anterior à data de aplicação");
        verify(investimentoRepository, never()).save(any());
    }

    @Test
    void tipoForaDoDominioEhRecusado() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);

        assertThatThrownBy(() -> service.criar(pedido("Imóvel", null, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageStartingWith("Tipo de investimento inválido");
    }

    @Test
    void atualizarComStatusResgatadoResgataOInvestimento() {
        Investimento existente = new Investimento();
        existente.setId(20L);
        existente.setStatus("ATIVO");
        when(investimentoRepository.findById(20L)).thenReturn(Optional.of(existente));
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);

        assertThat(service.atualizar(20L, pedido("CDB", null, "Resgatado")).status()).isEqualTo("RESGATADO");
    }

    @Test
    void atualizarSemStatusMantemOAtual() {
        Investimento existente = new Investimento();
        existente.setId(20L);
        existente.setStatus("VENCIDO");
        when(investimentoRepository.findById(20L)).thenReturn(Optional.of(existente));
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);

        assertThat(service.atualizar(20L, pedido("CDB", null, null)).status()).isEqualTo("VENCIDO");
    }

    @Test
    void buscarInexistenteLancaNaoEncontrado() {
        when(investimentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Investimento 99 não encontrado");
    }
}
```

`controller/InvestimentoControllerTest.java`:
```java
package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.InvestimentoResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.service.InvestimentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvestimentoController.class)
class InvestimentoControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean InvestimentoService service;

    private static final String CORPO = """
            {"usuarioId":1,"contaId":5,"nomeInvestimento":"CDB Banco Demo","tipo":"CDB","valorAplicado":5000.00,
             "taxaRentabilidade":0.1250,"dtAplicacao":"2026-01-10","dtVencimento":"2028-01-10"}""";

    private final InvestimentoResponse cdb = new InvestimentoResponse(20L, 1L, 5L, "Banco Demo · 0001-9",
            "CDB Banco Demo", "CDB", "Banco Demo", new BigDecimal("5000.00"), new BigDecimal("0.1250"),
            LocalDate.of(2026, 1, 10), LocalDate.of(2028, 1, 10), "ATIVO", new BigDecimal("625.00"));

    @Test
    void listarPorUsuarioRetorna200ComRendimento() throws Exception {
        when(service.listar(1L)).thenReturn(List.of(cdb));
        mvc.perform(get("/api/investimentos").param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rendimentoEstimadoAnual").value(625.00));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Investimento 99 não encontrado"));
        mvc.perform(get("/api/investimentos/99")).andExpect(status().isNotFound());
    }

    @Test
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any())).thenReturn(cdb);
        mvc.perform(post("/api/investimentos").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/investimentos/20")));
    }

    @Test
    void criarComTaxaNegativaRetorna400() throws Exception {
        mvc.perform(post("/api/investimentos").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO.replace("0.1250", "-0.01")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void criarComVencimentoInvalidoRetorna422() throws Exception {
        when(service.criar(any())).thenThrow(
                new RegraNegocioException("A data de vencimento não pode ser anterior à data de aplicação"));
        mvc.perform(post("/api/investimentos").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(service.atualizar(eq(20L), any())).thenReturn(cdb);
        mvc.perform(put("/api/investimentos/20").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mvc.perform(delete("/api/investimentos/20")).andExpect(status().isNoContent());
        verify(service).excluir(20L);
    }
}
```

- [ ] **Passo 3: rodar e confirmar falha** — `COMPILATION ERROR`.

- [ ] **Passo 4: implementar**

`service/InvestimentoService.java`:
```java
package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.InvestimentoRequest;
import br.com.fiap.fintech.dto.InvestimentoResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Dominios;
import br.com.fiap.fintech.model.Investimento;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class InvestimentoService {

    private static final Set<String> TIPOS =
            Set.of("CDB", "LCI", "LCA", "TESOURO", "ACOES", "FII", "FUNDO", "POUPANCA", "CRIPTO", "OUTROS");
    private static final Set<String> STATUS = Set.of("ATIVO", "RESGATADO", "VENCIDO");

    private final InvestimentoRepository investimentoRepository;
    private final ContaBancariaService contaService;

    public InvestimentoService(InvestimentoRepository investimentoRepository, ContaBancariaService contaService) {
        this.investimentoRepository = investimentoRepository;
        this.contaService = contaService;
    }

    /** usuarioId nulo lista todos os investimentos. */
    @Transactional(readOnly = true)
    public List<InvestimentoResponse> listar(Long usuarioId) {
        var investimentos = usuarioId == null
                ? investimentoRepository.findAllByOrderByDtAplicacaoDescIdDesc()
                : investimentoRepository.findByUsuarioIdOrderByDtAplicacaoDescIdDesc(usuarioId);
        return investimentos.stream().map(InvestimentoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public InvestimentoResponse buscar(Long id) {
        return InvestimentoResponse.de(obter(id));
    }

    @Transactional
    public InvestimentoResponse criar(InvestimentoRequest req) {
        Investimento investimento = new Investimento();
        preencher(investimento, req);
        return InvestimentoResponse.de(investimentoRepository.save(investimento));
    }

    @Transactional
    public InvestimentoResponse atualizar(Long id, InvestimentoRequest req) {
        Investimento investimento = obter(id);
        Posse.garantirMesmoDono(investimento.getUsuario(), req.usuarioId());
        preencher(investimento, req);
        return InvestimentoResponse.de(investimento);
    }

    @Transactional
    public void excluir(Long id) {
        investimentoRepository.delete(obter(id));
    }

    private void preencher(Investimento inv, InvestimentoRequest req) {
        ContaBancaria conta = contaService.obterContaAtivaDoUsuario(req.contaId(), req.usuarioId());
        String tipo = Dominios.normalizar(req.tipo());
        if (!TIPOS.contains(tipo)) {
            throw new RegraNegocioException("Tipo de investimento inválido: " + req.tipo());
        }
        if (req.dtVencimento() != null && req.dtVencimento().isBefore(req.dtAplicacao())) {
            throw new RegraNegocioException("A data de vencimento não pode ser anterior à data de aplicação");
        }
        String status = req.status() == null || req.status().isBlank()
                ? (inv.getStatus() == null ? "ATIVO" : inv.getStatus())
                : Dominios.normalizar(req.status());
        if (!STATUS.contains(status)) {
            throw new RegraNegocioException("Status inválido: use ATIVO, RESGATADO ou VENCIDO");
        }
        inv.setUsuario(conta.getUsuario());
        inv.setConta(conta);
        inv.setNomeInvestimento(req.nomeInvestimento().trim());
        inv.setTipo(tipo);
        inv.setInstituicao(req.instituicao());
        inv.setValorAplicado(req.valorAplicado());
        inv.setTaxaRentabilidade(req.taxaRentabilidade());
        inv.setDtAplicacao(req.dtAplicacao());
        inv.setDtVencimento(req.dtVencimento());
        inv.setStatus(status);
    }

    private Investimento obter(Long id) {
        return investimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Investimento " + id + " não encontrado"));
    }
}
```

`controller/InvestimentoController.java`:
```java
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
```

- [ ] **Passo 5: rodar** — `./mvnw -q test` → `BUILD SUCCESS`.
- [ ] **Passo 6: commit** — `git add api/ && git commit -m "feat(api): CRUD de investimentos com rendimento estimado e regras de vencimento/status"`

---

### Task B07: Resumo financeiro (`GET /api/usuarios/{id}/resumo`)

> Revisão 1 (R4): o saldo do mês e os totais da página inicial passam a ser regra de negócio da API (antigo `calcularImpactoNoSaldo`), e não mais soma feita no navegador.

**Arquivos:**
- Criar: `dto/{TotalPorCategoria,ResumoResponse}.java`, `service/ResumoFinanceiroService.java`
- Modificar: `repository/ReceitaRepository.java`, `repository/GastoRepository.java` (consultas de soma), `controller/UsuarioController.java` (endpoint), `controller/UsuarioControllerTest.java`
- Teste: `service/ResumoFinanceiroServiceTest.java`

**Interfaces:**
- Produz: `ReceitaRepository.somarNoPeriodo(Long usuarioId, LocalDate inicio, LocalDate fim): BigDecimal` (null quando não há registros), `GastoRepository.somarNoPeriodo(...)` (mesma assinatura), `GastoRepository.totaisPorCategoria(Long, LocalDate, LocalDate): List<TotalPorCategoria>`, `ResumoFinanceiroService.resumir(Long usuarioId, YearMonth mesOuNull): ResumoResponse`.

- [ ] **Passo 1: DTOs e consultas**
```java
package br.com.fiap.fintech.dto;

import java.math.BigDecimal;

public record TotalPorCategoria(String categoria, BigDecimal total) {
}
```
```java
package br.com.fiap.fintech.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumoResponse(String mes, BigDecimal totalReceitas, BigDecimal totalGastos, BigDecimal saldo,
                             BigDecimal totalInvestido, BigDecimal rendimentoEstimadoAnual,
                             List<TotalPorCategoria> gastosPorCategoria) {
}
```
Acrescentar em `ReceitaRepository` (imports: `org.springframework.data.jpa.repository.Query`, `org.springframework.data.repository.query.Param`, `java.math.BigDecimal`, `java.time.LocalDate`):
```java
    @Query("select sum(r.valor) from Receita r where r.usuario.id = :usuarioId and r.dtRecebimento between :inicio and :fim")
    BigDecimal somarNoPeriodo(@Param("usuarioId") Long usuarioId, @Param("inicio") LocalDate inicio,
                              @Param("fim") LocalDate fim);
```
Acrescentar em `GastoRepository` (mesmos imports + `br.com.fiap.fintech.dto.TotalPorCategoria`):
```java
    @Query("select sum(g.valor) from Gasto g where g.usuario.id = :usuarioId and g.dtGasto between :inicio and :fim")
    BigDecimal somarNoPeriodo(@Param("usuarioId") Long usuarioId, @Param("inicio") LocalDate inicio,
                              @Param("fim") LocalDate fim);

    @Query("""
            select new br.com.fiap.fintech.dto.TotalPorCategoria(g.categoria.nome, sum(g.valor))
            from Gasto g
            where g.usuario.id = :usuarioId and g.dtGasto between :inicio and :fim
            group by g.categoria.nome
            order by sum(g.valor) desc""")
    List<TotalPorCategoria> totaisPorCategoria(@Param("usuarioId") Long usuarioId, @Param("inicio") LocalDate inicio,
                                               @Param("fim") LocalDate fim);
```

- [ ] **Passo 2: teste do service (falha)** — `service/ResumoFinanceiroServiceTest.java`:
```java
package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.ResumoResponse;
import br.com.fiap.fintech.dto.TotalPorCategoria;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.model.Investimento;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumoFinanceiroServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock ReceitaRepository receitaRepository;
    @Mock GastoRepository gastoRepository;
    @Mock InvestimentoRepository investimentoRepository;
    @InjectMocks ResumoFinanceiroService service;

    private final YearMonth novembro = YearMonth.of(2026, 11);

    private static Investimento investimento(String valor, String taxa, String status) {
        Investimento i = new Investimento();
        i.setValorAplicado(new BigDecimal(valor));
        i.setTaxaRentabilidade(new BigDecimal(taxa));
        i.setStatus(status);
        return i;
    }

    @Test
    void saldoEhReceitasMenosGastosEInvestimentoSoContaSeAtivo() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(receitaRepository.somarNoPeriodo(1L, novembro.atDay(1), novembro.atEndOfMonth()))
                .thenReturn(new BigDecimal("5000.00"));
        when(gastoRepository.somarNoPeriodo(1L, novembro.atDay(1), novembro.atEndOfMonth()))
                .thenReturn(new BigDecimal("1979.25"));
        when(gastoRepository.totaisPorCategoria(1L, novembro.atDay(1), novembro.atEndOfMonth()))
                .thenReturn(List.of(new TotalPorCategoria("Moradia", new BigDecimal("1500.00"))));
        when(investimentoRepository.findByUsuarioIdOrderByDtAplicacaoDescIdDesc(1L)).thenReturn(List.of(
                investimento("5000.00", "0.1250", "ATIVO"),
                investimento("1000.00", "0.1000", "RESGATADO")));

        ResumoResponse r = service.resumir(1L, novembro);

        assertThat(r.mes()).isEqualTo("2026-11");
        assertThat(r.saldo()).isEqualByComparingTo("3020.75");
        assertThat(r.totalInvestido()).isEqualByComparingTo("5000.00");
        assertThat(r.rendimentoEstimadoAnual()).isEqualByComparingTo("625.00");
        assertThat(r.gastosPorCategoria()).extracting(TotalPorCategoria::categoria).containsExactly("Moradia");
    }

    @Test
    void mesSemLancamentosResultaEmZeros() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        ResumoResponse r = service.resumir(1L, novembro);

        assertThat(r.totalReceitas()).isEqualByComparingTo("0");
        assertThat(r.totalGastos()).isEqualByComparingTo("0");
        assertThat(r.saldo()).isEqualByComparingTo("0");
        assertThat(r.gastosPorCategoria()).isEmpty();
    }

    @Test
    void usuarioInexistenteLancaNaoEncontrado() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.resumir(99L, novembro))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário 99 não encontrado");
    }
}
```

- [ ] **Passo 3: testes do endpoint (falham)** — em `UsuarioControllerTest`, acrescentar o mock (o controller passa a depender do resumo), os imports `br.com.fiap.fintech.dto.ResumoResponse`, `br.com.fiap.fintech.service.ResumoFinanceiroService`, `java.math.BigDecimal`, `java.time.YearMonth` e os testes:
```java
    @MockitoBean ResumoFinanceiroService resumoService;

    @Test
    void resumoDoMesRetorna200() throws Exception {
        when(resumoService.resumir(1L, YearMonth.of(2026, 11))).thenReturn(new ResumoResponse("2026-11",
                new BigDecimal("5000.00"), new BigDecimal("1979.25"), new BigDecimal("3020.75"),
                new BigDecimal("5000.00"), new BigDecimal("625.00"), List.of()));
        mvc.perform(get("/api/usuarios/1/resumo").param("mes", "2026-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(3020.75));
    }

    @Test
    void resumoComMesInvalidoRetorna400() throws Exception {
        mvc.perform(get("/api/usuarios/1/resumo").param("mes", "2026-13"))
                .andExpect(status().isBadRequest());
    }
```

- [ ] **Passo 4: rodar e confirmar falha** — `COMPILATION ERROR`.

- [ ] **Passo 5: implementar**

`service/ResumoFinanceiroService.java`:
```java
package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.ResumoResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.model.Investimento;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/** Visão consolidada do mês: fluxo de caixa (receitas − gastos) e carteira de investimentos ativos. */
@Service
public class ResumoFinanceiroService {

    private final UsuarioRepository usuarioRepository;
    private final ReceitaRepository receitaRepository;
    private final GastoRepository gastoRepository;
    private final InvestimentoRepository investimentoRepository;

    public ResumoFinanceiroService(UsuarioRepository usuarioRepository, ReceitaRepository receitaRepository,
                                   GastoRepository gastoRepository, InvestimentoRepository investimentoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.receitaRepository = receitaRepository;
        this.gastoRepository = gastoRepository;
        this.investimentoRepository = investimentoRepository;
    }

    /** mes nulo = mês atual. */
    @Transactional(readOnly = true)
    public ResumoResponse resumir(Long usuarioId, YearMonth mes) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário " + usuarioId + " não encontrado");
        }
        YearMonth referencia = mes == null ? YearMonth.now() : mes;
        LocalDate inicio = referencia.atDay(1);
        LocalDate fim = referencia.atEndOfMonth();

        BigDecimal receitas = zeroSeNulo(receitaRepository.somarNoPeriodo(usuarioId, inicio, fim));
        BigDecimal gastos = zeroSeNulo(gastoRepository.somarNoPeriodo(usuarioId, inicio, fim));

        List<Investimento> ativos = investimentoRepository.findByUsuarioIdOrderByDtAplicacaoDescIdDesc(usuarioId)
                .stream().filter(i -> "ATIVO".equals(i.getStatus())).toList();
        BigDecimal investido = ativos.stream().map(Investimento::getValorAplicado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rendimento = ativos.stream().map(Investimento::calcularRendimentoEstimado)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResumoResponse(referencia.toString(), receitas, gastos, receitas.subtract(gastos), investido,
                rendimento, gastoRepository.totaisPorCategoria(usuarioId, inicio, fim));
    }

    private static BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
```

Em `UsuarioController`: injetar `ResumoFinanceiroService resumoService` no construtor (segundo parâmetro) e acrescentar (imports `br.com.fiap.fintech.dto.ResumoResponse`, `br.com.fiap.fintech.service.ResumoFinanceiroService`, `org.springframework.format.annotation.DateTimeFormat`, `java.time.YearMonth`):
```java
    /** Página inicial do front: saldo do mês, total investido e gastos por categoria. */
    @GetMapping("/{id}/resumo")
    public ResumoResponse resumo(@PathVariable Long id,
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return resumoService.resumir(id, mes);
    }
```

- [ ] **Passo 6: rodar** — `./mvnw -q test` → `BUILD SUCCESS`; depois `./mvnw -q test -Dit.oracle=true` (a subida do contexto valida as consultas JPQL novas contra o Oracle).
- [ ] **Passo 7: commit** — `git add api/ && git commit -m "feat(api): resumo financeiro mensal (saldo, investimentos e gastos por categoria)"`

---

### Task B08: Dados de demonstração + execução na FIAP

**Arquivos:**
- Criar: `api/src/main/java/br/com/fiap/fintech/config/DadosDemo.java`
- Criar: `docs/07-api-spring-react/evidencias/execucao-api-fiap.md`

**Interfaces:**
- Consome: todos os repositories + `PasswordEncoder`; propriedades `fintech.dados-demo.*`.
- Produz: categorias de gasto e de receita; usuário de teste `demo@fintech.com` (senha em `fintech.dados-demo.senha`) com 1 conta, 2 receitas, 3 gastos e 2 investimentos.

- [ ] **Passo 1: implementar `DadosDemo`**
```java
package br.com.fiap.fintech.config;

import br.com.fiap.fintech.model.*;
import br.com.fiap.fintech.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Garante, a cada subida, as categorias de gasto e o usuário de teste do README (D-F8-05).
 * Se o usuário já existe, só reativa e redefine a senha configurada; os lançamentos de exemplo
 * só são criados na primeira vez.
 */
@Component
@ConditionalOnProperty(name = "fintech.dados-demo.habilitado", havingValue = "true")
public class DadosDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosDemo.class);
    private static final List<String> CATEGORIAS_GASTO =
            List.of("Moradia", "Alimentação", "Transporte", "Lazer", "Saúde", "Educação");
    private static final List<String> CATEGORIAS_RECEITA = List.of("Salário", "Freelance", "Rendimentos", "Outras receitas");

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ContaBancariaRepository contaRepository;
    private final ReceitaRepository receitaRepository;
    private final GastoRepository gastoRepository;
    private final InvestimentoRepository investimentoRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;

    public DadosDemo(UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository,
                     ContaBancariaRepository contaRepository, ReceitaRepository receitaRepository,
                     GastoRepository gastoRepository, InvestimentoRepository investimentoRepository,
                     PasswordEncoder passwordEncoder,
                     @Value("${fintech.dados-demo.email}") String email,
                     @Value("${fintech.dados-demo.senha}") String senha) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.contaRepository = contaRepository;
        this.receitaRepository = receitaRepository;
        this.gastoRepository = gastoRepository;
        this.investimentoRepository = investimentoRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, Categoria> categorias = garantirCategorias(CATEGORIAS_GASTO, "GASTO");
        Map<String, Categoria> categoriasReceita = garantirCategorias(CATEGORIAS_RECEITA, "RECEITA");

        var existente = usuarioRepository.findByEmailIgnoreCase(email);
        if (existente.isPresent()) {
            existente.get().setAtivo(true);
            existente.get().setSenha(passwordEncoder.encode(senha));
            log.info("Usuário de teste {} já existe: senha e status redefinidos.", email);
            return;
        }

        Usuario demo = new Usuario();
        demo.setNome("Usuária Demo");
        demo.setEmail(email);
        demo.setSenha(passwordEncoder.encode(senha));
        usuarioRepository.save(demo);

        ContaBancaria conta = contaRepository.save(new ContaBancaria(demo, "Banco Demo", "CORRENTE", "0001-9"));
        LocalDate hoje = LocalDate.now();

        receitaRepository.saveAll(List.of(
                receita(demo, conta, categoriasReceita.get("Salário"), "Salário do mês", "5000.00",
                        hoje.withDayOfMonth(1), "SALARIO", "MENSAL"),
                receita(demo, conta, categoriasReceita.get("Freelance"), "Projeto de site", "1200.00",
                        hoje.withDayOfMonth(Math.min(10, hoje.lengthOfMonth())), "FREELANCE", "UNICA")));

        gastoRepository.saveAll(List.of(
                gasto(demo, conta, categorias.get("Moradia"), "Aluguel", "1500.00", hoje.withDayOfMonth(1), "FIXO"),
                gasto(demo, conta, categorias.get("Alimentação"), "Mercado do mês", "420.35", hoje.minusDays(3), "VARIAVEL"),
                gasto(demo, conta, categorias.get("Transporte"), "Corridas de aplicativo", "58.90", hoje.minusDays(1), "VARIAVEL")));

        investimentoRepository.saveAll(List.of(
                investimento(demo, conta, "CDB Banco Demo", "CDB", "5000.00", "0.1250", hoje.minusMonths(2), hoje.plusYears(2)),
                investimento(demo, conta, "Tesouro Selic", "TESOURO", "2000.00", "0.1075", hoje.minusMonths(1), null)));

        log.info("Usuário de teste {} criado com conta, receitas, gastos e investimentos de exemplo.", email);
    }

    private Map<String, Categoria> garantirCategorias(List<String> nomes, String tipo) {
        return nomes.stream()
                .map(nome -> categoriaRepository.findByNomeAndTipo(nome, tipo)
                        .orElseGet(() -> categoriaRepository.save(new Categoria(nome, tipo))))
                .collect(Collectors.toMap(Categoria::getNome, Function.identity()));
    }

    private static Receita receita(Usuario u, ContaBancaria c, Categoria cat, String descricao, String valor,
                                   LocalDate data, String origem, String recorrencia) {
        Receita r = new Receita();
        r.setUsuario(u);
        r.setConta(c);
        r.setCategoria(cat);
        r.setDescricao(descricao);
        r.setValor(new BigDecimal(valor));
        r.setDtRecebimento(data);
        r.setOrigem(origem);
        r.setRecorrencia(recorrencia);
        return r;
    }

    private static Gasto gasto(Usuario u, ContaBancaria c, Categoria cat, String descricao, String valor,
                               LocalDate data, String tipo) {
        Gasto g = new Gasto();
        g.setUsuario(u);
        g.setConta(c);
        g.setCategoria(cat);
        g.setDescricao(descricao);
        g.setValor(new BigDecimal(valor));
        g.setDtGasto(data);
        g.setTipo(tipo);
        return g;
    }

    private static Investimento investimento(Usuario u, ContaBancaria c, String nome, String tipo, String valor,
                                             String taxa, LocalDate aplicacao, LocalDate vencimento) {
        Investimento i = new Investimento();
        i.setUsuario(u);
        i.setConta(c);
        i.setNomeInvestimento(nome);
        i.setTipo(tipo);
        i.setInstituicao(c.getNmBanco());
        i.setValorAplicado(new BigDecimal(valor));
        i.setTaxaRentabilidade(new BigDecimal(taxa));
        i.setDtAplicacao(aplicacao);
        i.setDtVencimento(vencimento);
        return i;
    }
}
```

- [ ] **Passo 2: subir contra o Oracle local** e verificar o log `Usuário de teste demo@fintech.com criado...`; reiniciar e verificar `... já existe: senha e status redefinidos.`
```bash
JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot" ./mvnw spring-boot:run
```
(rodar em background; parar depois de checar o log).

- [ ] **Passo 3: deixar no `api/application-local.properties` só a senha da FIAP** (sem sobrescrever URL e usuário), subir a API e executar a bateria abaixo (Git Bash), anotando o status de cada chamada:
```bash
API=http://localhost:8080/api
curl -s -o /dev/null -w "login 200? %{http_code}\n" -X POST $API/auth/login -H "Content-Type: application/json" -d '{"email":"demo@fintech.com","senha":"Fintech@2026"}'
curl -s -o /dev/null -w "login 401? %{http_code}\n" -X POST $API/auth/login -H "Content-Type: application/json" -d '{"email":"demo@fintech.com","senha":"errada"}'
curl -s -i -X POST $API/usuarios -H "Content-Type: application/json" -d "{\"nome\":\"Teste API\",\"email\":\"api-$(date +%s)@teste.com\",\"senha\":\"segredo1\"}" | head -1
curl -s -o /dev/null -w "usuario 404? %{http_code}\n" $API/usuarios/999999
curl -s "$API/gastos?usuarioId=<ID_DEMO>" | head -c 300; echo
curl -s -o /dev/null -w "gasto 400? %{http_code}\n" -X POST $API/gastos -H "Content-Type: application/json" -d '{"valor":-1}'
curl -s -o /dev/null -w "preflight CORS %{http_code}\n" -X OPTIONS $API/gastos -H "Origin: http://localhost:5173" -H "Access-Control-Request-Method: POST"
curl -s "$API/usuarios/<ID_DEMO>/resumo"; echo
curl -s -o /dev/null -w "resumo mes invalido 400? %{http_code}\n" "$API/usuarios/<ID_DEMO>/resumo?mes=2026-13"
curl -s -o /dev/null -w "conta com lancamentos 409? %{http_code}\n" -X DELETE "$API/contas/<ID_CONTA_DEMO>"
curl -s -o /dev/null -w "categoria em uso 409? %{http_code}\n" -X DELETE "$API/categorias/<ID_MORADIA>"
```
Esperado: 200, 401, `HTTP/1.1 201`, 404, lista JSON, 400, 200, JSON com `saldo`, 400, 409, 409.
Fazer também o ciclo **POST → GET por id → PUT → DELETE → GET (404)** para **cada um dos 6 controllers** (usuários, receitas, gastos, investimentos, contas e categorias), anotando 201, 200, 200, 204 e 404.

- [ ] **Passo 4: registrar evidência** em `docs/07-api-spring-react/evidencias/execucao-api-fiap.md` (R6), sem senhas:
  - data, versão do Java, URL e usuário da conexão;
  - trecho do log de subida (`HikariPool-1 - Start completed`, nenhum erro de *schema validation*): prova de que as entidades batem com as tabelas da FIAP;
  - saída de `SELECT table_name FROM user_tables WHERE table_name LIKE 'T_SF_%' ORDER BY 1` na FIAP (10 tabelas), que prova que as tabelas existem lá;
  - tabela "chamada → status esperado → status obtido".

- [ ] **Passo 5: commit** — `git add api/ docs/07-api-spring-react/ && git commit -m "feat(api): usuário de teste automático e evidência de execução na FIAP"`

---

### Task F01: Projeto React + cliente da API + AuthContext

**Arquivos:**
- Criar (Vite): `web/package.json`, `web/package-lock.json`, `web/index.html`, `web/vite.config.js`, `web/.env.example`
- Criar: `web/src/{main.jsx,index.css,setupTests.js}`, `web/src/api/{client,auth,usuarios,receitas,gastos,investimentos,contas,categorias}.js`, `web/src/contexts/AuthContext.jsx`, `web/src/hooks/useDados.js`, `web/src/utils/formatar.js`
- Remover do template: `web/src/App.jsx`, `web/src/App.css`, `web/src/assets/`, `web/public/vite.svg`
- Teste: `web/src/api/client.test.js`

**Interfaces:**
- Produz: `request(caminho, {method, body})`, `ApiError {status, message, detalhes}`; funções de API: `login(email, senha)`, `buscarUsuario(id)`, `criarUsuario(dados)`, `atualizarUsuario(id, dados)`, `excluirUsuario(id)`, `buscarResumo(usuarioId, mes?)`, `listarGastos(usuarioId)`, `buscarGasto(id)`, `criarGasto(dados)`, `atualizarGasto(id, dados)`, `excluirGasto(id)`, idem `...Receita(s)` e `...Investimento(s)`, `listarContas(usuarioId)`, `criarConta(dados)`, `atualizarConta(id, dados)`, `excluirConta(id)`, `listarCategorias(tipo?)`, `criarCategoria(dados)`, `atualizarCategoria(id, dados)`, `excluirCategoria(id)`; `AuthProvider`, `useAuth() → {usuario, entrar(email, senha), sair(), atualizarSessao(usuario)}`; `useDados(buscar) → {dados, erro, carregando, recarregar, setDados}`; `formatarMoeda`, `formatarData`, `formatarPercentual`, `hojeISO`.

- [ ] **Passo 1: criar o projeto e instalar dependências** (na raiz do repo):
```bash
npm create vite@latest web -- --template react
cd web && npm install
npm install react-router-dom
npm install -D tailwindcss @tailwindcss/vite vitest jsdom @testing-library/react @testing-library/user-event @testing-library/jest-dom
```
Apagar `src/App.jsx`, `src/App.css`, `src/assets/`, `public/vite.svg`.

- [ ] **Passo 2: configuração**

`web/vite.config.js`:
```js
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  plugins: [react(), tailwindcss()],
  // strictPort: se a 5173 estiver ocupada, falha em vez de trocar de porta (o CORS da API só libera a 5173)
  server: { port: 5173, strictPort: true },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/setupTests.js',
  },
});
```
Em `web/package.json`, acrescentar ao bloco `scripts`: `"test": "vitest run"`.

`web/.env.example`:
```
# Copie para .env.local se a API não estiver em http://localhost:8080/api
VITE_API_URL=http://localhost:8080/api
```

`web/index.html` (substituir):
```html
<!doctype html>
<html lang="pt-BR">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet" />
    <title>Fintech</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.jsx"></script>
  </body>
</html>
```

`web/src/index.css` (substituir):
```css
@import "tailwindcss";

@theme {
  --color-brand: #15454e;
  --color-brand-claro: #1f5f6b;
  --color-emerald2: #10b981;
  --font-sans: "Inter", ui-sans-serif, system-ui, sans-serif;
}

body {
  @apply bg-slate-50 text-slate-900 antialiased;
}
```

`web/src/setupTests.js`:
```js
import '@testing-library/jest-dom/vitest';
```

- [ ] **Passo 3: teste do cliente (falha)** — `web/src/api/client.test.js`:
```js
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, request } from './client';

afterEach(() => vi.unstubAllGlobals());

describe('request', () => {
  it('devolve null quando a API responde 204', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(null, { status: 204 })));
    await expect(request('/gastos/1', { method: 'DELETE' })).resolves.toBeNull();
  });

  it('envia JSON e devolve o corpo da resposta', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ id: 1 }), { status: 201, headers: { 'Content-Type': 'application/json' } }),
    );
    vi.stubGlobal('fetch', fetchMock);

    await expect(request('/gastos', { method: 'POST', body: { valor: 10 } })).resolves.toEqual({ id: 1 });
    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/gastos', expect.objectContaining({
      method: 'POST',
      body: '{"valor":10}',
      headers: { 'Content-Type': 'application/json' },
    }));
  });

  it('converte o corpo de erro da API em ApiError', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(
      JSON.stringify({ status: 422, mensagem: 'A conta informada não pertence ao usuário', detalhes: ['x'] }),
      { status: 422, headers: { 'Content-Type': 'application/json' } },
    )));

    const erro = await request('/gastos', { method: 'POST', body: {} }).catch((e) => e);
    expect(erro).toBeInstanceOf(ApiError);
    expect(erro.status).toBe(422);
    expect(erro.message).toBe('A conta informada não pertence ao usuário');
    expect(erro.detalhes).toEqual(['x']);
  });

  it('avisa quando a API está fora do ar', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')));

    const erro = await request('/gastos').catch((e) => e);
    expect(erro.status).toBe(0);
    expect(erro.message).toMatch(/Não foi possível conectar à API/);
  });
});
```
Run: `npm test` → FAIL (`Failed to resolve import "./client"`).

- [ ] **Passo 4: implementar**

`web/src/api/client.js`:
```js
const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

/** Erro devolvido pela API (ou de conexão, com status 0). */
export class ApiError extends Error {
  constructor(status, mensagem, detalhes = []) {
    super(mensagem);
    this.name = 'ApiError';
    this.status = status;
    this.detalhes = detalhes;
  }
}

export async function request(caminho, { method = 'GET', body } = {}) {
  let resposta;
  try {
    resposta = await fetch(`${BASE_URL}${caminho}`, {
      method,
      headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, 'Não foi possível conectar à API. Verifique se o backend está em execução.');
  }
  if (resposta.status === 204) return null;
  const dados = await resposta.json().catch(() => null);
  if (!resposta.ok) {
    throw new ApiError(resposta.status, dados?.mensagem ?? `Erro ${resposta.status}`, dados?.detalhes ?? []);
  }
  return dados;
}
```

`web/src/api/auth.js`:
```js
import { request } from './client';

export const login = (email, senha) => request('/auth/login', { method: 'POST', body: { email, senha } });
```

`web/src/api/usuarios.js`:
```js
import { request } from './client';

export const buscarUsuario = (id) => request(`/usuarios/${id}`);
export const criarUsuario = (dados) => request('/usuarios', { method: 'POST', body: dados });
export const atualizarUsuario = (id, dados) => request(`/usuarios/${id}`, { method: 'PUT', body: dados });
export const excluirUsuario = (id) => request(`/usuarios/${id}`, { method: 'DELETE' });
/** mes no formato AAAA-MM; sem mes = mês atual */
export const buscarResumo = (id, mes) => request(`/usuarios/${id}/resumo${mes ? `?mes=${mes}` : ''}`);
```

`web/src/api/receitas.js`:
```js
import { request } from './client';

export const listarReceitas = (usuarioId) => request(`/receitas?usuarioId=${usuarioId}`);
export const buscarReceita = (id) => request(`/receitas/${id}`);
export const criarReceita = (dados) => request('/receitas', { method: 'POST', body: dados });
export const atualizarReceita = (id, dados) => request(`/receitas/${id}`, { method: 'PUT', body: dados });
export const excluirReceita = (id) => request(`/receitas/${id}`, { method: 'DELETE' });
```

`web/src/api/gastos.js`:
```js
import { request } from './client';

export const listarGastos = (usuarioId) => request(`/gastos?usuarioId=${usuarioId}`);
export const buscarGasto = (id) => request(`/gastos/${id}`);
export const criarGasto = (dados) => request('/gastos', { method: 'POST', body: dados });
export const atualizarGasto = (id, dados) => request(`/gastos/${id}`, { method: 'PUT', body: dados });
export const excluirGasto = (id) => request(`/gastos/${id}`, { method: 'DELETE' });
```

`web/src/api/investimentos.js`:
```js
import { request } from './client';

export const listarInvestimentos = (usuarioId) => request(`/investimentos?usuarioId=${usuarioId}`);
export const buscarInvestimento = (id) => request(`/investimentos/${id}`);
export const criarInvestimento = (dados) => request('/investimentos', { method: 'POST', body: dados });
export const atualizarInvestimento = (id, dados) => request(`/investimentos/${id}`, { method: 'PUT', body: dados });
export const excluirInvestimento = (id) => request(`/investimentos/${id}`, { method: 'DELETE' });
```

`web/src/api/contas.js`:
```js
import { request } from './client';

export const listarContas = (usuarioId) => request(`/contas?usuarioId=${usuarioId}`);
export const buscarConta = (id) => request(`/contas/${id}`);
export const criarConta = (dados) => request('/contas', { method: 'POST', body: dados });
export const atualizarConta = (id, dados) => request(`/contas/${id}`, { method: 'PUT', body: dados });
export const excluirConta = (id) => request(`/contas/${id}`, { method: 'DELETE' });
```

`web/src/api/categorias.js`:
```js
import { request } from './client';

export const listarCategorias = (tipo) => request(`/categorias${tipo ? `?tipo=${tipo}` : ''}`);
export const buscarCategoria = (id) => request(`/categorias/${id}`);
export const criarCategoria = (dados) => request('/categorias', { method: 'POST', body: dados });
export const atualizarCategoria = (id, dados) => request(`/categorias/${id}`, { method: 'PUT', body: dados });
export const excluirCategoria = (id) => request(`/categorias/${id}`, { method: 'DELETE' });
```

`web/src/contexts/AuthContext.jsx`:
```jsx
import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { login } from '../api/auth';

const AuthContext = createContext(null);
const CHAVE = 'fintech.usuario';

function lerUsuarioSalvo() {
  try {
    return JSON.parse(localStorage.getItem(CHAVE));
  } catch {
    return null;
  }
}

function salvar(usuario) {
  try {
    if (usuario) localStorage.setItem(CHAVE, JSON.stringify(usuario));
    else localStorage.removeItem(CHAVE);
  } catch {
    // localStorage indisponível (aba anônima): a sessão vale só até recarregar a página
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(lerUsuarioSalvo);

  const atualizarSessao = useCallback((novo) => {
    setUsuario(novo);
    salvar(novo);
  }, []);

  const entrar = useCallback(async (email, senha) => {
    const autenticado = await login(email, senha);
    atualizarSessao(autenticado);
    return autenticado;
  }, [atualizarSessao]);

  const sair = useCallback(() => atualizarSessao(null), [atualizarSessao]);

  const valor = useMemo(() => ({ usuario, entrar, sair, atualizarSessao }), [usuario, entrar, sair, atualizarSessao]);
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const contexto = useContext(AuthContext);
  if (!contexto) throw new Error('useAuth precisa estar dentro de <AuthProvider>');
  return contexto;
}
```

`web/src/hooks/useDados.js`:
```js
import { useCallback, useEffect, useState } from 'react';

/** Carrega dados assíncronos. `buscar` deve ser estável (useCallback) para não recarregar em loop. */
export function useDados(buscar) {
  const [dados, setDados] = useState(null);
  const [erro, setErro] = useState(null);
  const [carregando, setCarregando] = useState(true);

  const recarregar = useCallback(async () => {
    setCarregando(true);
    setErro(null);
    try {
      setDados(await buscar());
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregando(false);
    }
  }, [buscar]);

  useEffect(() => {
    recarregar();
  }, [recarregar]);

  return { dados, erro, carregando, recarregar, setDados };
}
```

`web/src/utils/formatar.js`:
```js
const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

export const formatarMoeda = (valor) => moeda.format(Number(valor ?? 0));

export const formatarData = (iso) => (iso ? new Date(`${iso}T00:00:00`).toLocaleDateString('pt-BR') : '—');

/** 0.125 -> "12,5%" */
export const formatarPercentual = (fracao) =>
  fracao == null ? '—' : `${(Number(fracao) * 100).toLocaleString('pt-BR', { maximumFractionDigits: 2 })}%`;

/** Data local de hoje em YYYY-MM-DD (formato do <input type="date"> e da API). */
export const hojeISO = () => new Date().toLocaleDateString('en-CA');
```

`web/src/main.jsx` (provisório; F02 troca pelo RouterProvider):
```jsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <p className="p-8 text-brand">Fintech — em construção</p>
  </StrictMode>,
);
```

- [ ] **Passo 5: rodar** — `npm test` → 4 testes PASS; `npm run build` → sem erros.
- [ ] **Passo 6: commit** — `git add web/ && git commit -m "feat(web): projeto React com Vite, Tailwind, cliente da API e contexto de autenticação"` (confirmar com `git status` que `web/node_modules` **não** aparece).

---

### Task F02: Componentes base, layout, rotas, Login, Cadastro e página de erro

**Arquivos:**
- Criar: `web/src/components/{Campo,Botao,Alerta,Carregando,PageHeader,CardResumo,ConfirmarExclusao,Layout,RotaProtegida}.jsx`
- Criar: `web/src/pages/{LoginPage,CadastroPage,ErroPage}.jsx`, `web/src/router.jsx`
- Criar provisórios (substituídos em F03–F07): `web/src/pages/{DashboardPage,ReceitasPage,ReceitaFormPage,GastosPage,GastoFormPage,InvestimentosPage,InvestimentoFormPage,ContasPage,CategoriasPage,PerfilPage}.jsx`
- Modificar: `web/src/main.jsx`
- Testes: `web/src/pages/LoginPage.test.jsx`, `web/src/pages/ErroPage.test.jsx`

**Interfaces:**
- Produz (props): `Campo({label, name, as='input', erro, className, children, ...props})`, `Botao({variante='primario'|'secundario'|'perigo', type='button', className, ...props})`, `Alerta({tipo='erro'|'sucesso', detalhes=[], children})`, `PageHeader({titulo, descricao, acao})`, `CardResumo({titulo, valor, detalhe, destaque})`, `ConfirmarExclusao({aberto, titulo, mensagem, onConfirmar, onCancelar, processando})`, `RotaProtegida({children})`; `rotas` (array) e `router` exportados de `router.jsx`.

- [ ] **Passo 1: testes (falham)**

`web/src/pages/LoginPage.test.jsx`:
```jsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { beforeEach, expect, it, vi } from 'vitest';
import { login } from '../api/auth';
import { AuthProvider } from '../contexts/AuthContext';
import LoginPage from './LoginPage';

vi.mock('../api/auth', () => ({ login: vi.fn() }));

function renderizar() {
  const router = createMemoryRouter(
    [
      { path: '/login', element: <LoginPage /> },
      { path: '/', element: <p>Página inicial</p> },
    ],
    { initialEntries: ['/login'] },
  );
  render(<AuthProvider><RouterProvider router={router} /></AuthProvider>);
}

beforeEach(() => {
  localStorage.clear();
  vi.mocked(login).mockReset();
});

it('entra e redireciona para a página inicial', async () => {
  vi.mocked(login).mockResolvedValue({ id: 1, nome: 'Usuária Demo', email: 'demo@fintech.com' });
  renderizar();

  await userEvent.type(screen.getByLabelText('E-mail'), 'demo@fintech.com');
  await userEvent.type(screen.getByLabelText('Senha'), 'segredo1');
  await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));

  expect(await screen.findByText('Página inicial')).toBeInTheDocument();
  expect(login).toHaveBeenCalledWith('demo@fintech.com', 'segredo1');
});

it('mostra a mensagem da API quando as credenciais são inválidas', async () => {
  vi.mocked(login).mockRejectedValue(new Error('E-mail ou senha inválidos'));
  renderizar();

  await userEvent.type(screen.getByLabelText('E-mail'), 'demo@fintech.com');
  await userEvent.type(screen.getByLabelText('Senha'), 'errada');
  await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));

  expect(await screen.findByRole('alert')).toHaveTextContent('E-mail ou senha inválidos');
});
```

`web/src/pages/ErroPage.test.jsx`:
```jsx
import { render, screen } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { expect, it } from 'vitest';
import ErroPage from './ErroPage';

it('mostra a página 404 personalizada para rota inexistente', () => {
  const router = createMemoryRouter(
    [
      { path: '/', element: <p>Início</p> },
      { path: '*', element: <ErroPage /> },
    ],
    { initialEntries: ['/rota-que-nao-existe'] },
  );
  render(<RouterProvider router={router} />);

  expect(screen.getByRole('heading', { name: 'Página não encontrada' })).toBeInTheDocument();
  expect(screen.getByRole('link', { name: 'Voltar ao início' })).toHaveAttribute('href', '/');
});
```
Run: `npm test` → FAIL (módulos inexistentes).

- [ ] **Passo 2: componentes base**

`components/Campo.jsx`:
```jsx
/** Rótulo + input/select/textarea + mensagem de erro. */
export default function Campo({ label, name, as: Elemento = 'input', erro, className = '', children, ...props }) {
  return (
    <label htmlFor={name} className={`block text-sm ${className}`}>
      <span className="mb-1 block font-medium text-slate-700">{label}</span>
      <Elemento
        id={name}
        name={name}
        className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20"
        {...props}
      >
        {children}
      </Elemento>
      {erro && <span className="mt-1 block text-xs text-red-600">{erro}</span>}
    </label>
  );
}
```

`components/Botao.jsx`:
```jsx
const ESTILOS = {
  primario: 'bg-brand text-white hover:bg-brand-claro',
  secundario: 'bg-white text-slate-700 ring-1 ring-slate-300 hover:bg-slate-50',
  perigo: 'bg-red-600 text-white hover:bg-red-700',
};

export default function Botao({ variante = 'primario', type = 'button', className = '', ...props }) {
  return (
    <button
      type={type}
      className={`inline-flex items-center justify-center rounded-lg px-4 py-2 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-60 ${ESTILOS[variante]} ${className}`}
      {...props}
    />
  );
}
```

`components/Alerta.jsx`:
```jsx
const CORES = {
  erro: 'bg-red-50 text-red-700 ring-red-200',
  sucesso: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
};

export default function Alerta({ tipo = 'erro', detalhes = [], children }) {
  return (
    <div role={tipo === 'erro' ? 'alert' : 'status'} className={`rounded-lg px-4 py-3 text-sm ring-1 ${CORES[tipo]}`}>
      <p>{children}</p>
      {detalhes.length > 0 && (
        <ul className="mt-1 list-disc pl-5">
          {detalhes.map((d) => <li key={d}>{d}</li>)}
        </ul>
      )}
    </div>
  );
}
```

`components/Carregando.jsx`:
```jsx
export default function Carregando() {
  return <p role="status" className="text-sm text-slate-500">Carregando…</p>;
}
```

`components/PageHeader.jsx`:
```jsx
export default function PageHeader({ titulo, descricao, acao }) {
  return (
    <header className="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">{titulo}</h1>
        {descricao && <p className="text-sm text-slate-500">{descricao}</p>}
      </div>
      {acao}
    </header>
  );
}
```

`components/CardResumo.jsx`:
```jsx
export default function CardResumo({ titulo, valor, detalhe, destaque = false }) {
  return (
    <div className={`rounded-2xl p-5 ring-1 ${destaque ? 'bg-brand text-white ring-brand' : 'bg-white ring-slate-200'}`}>
      <p className={`text-sm ${destaque ? 'text-white/80' : 'text-slate-500'}`}>{titulo}</p>
      <p className="mt-1 text-2xl font-bold">{valor}</p>
      {detalhe && <p className={`mt-1 text-xs ${destaque ? 'text-white/70' : 'text-slate-400'}`}>{detalhe}</p>}
    </div>
  );
}
```

`components/ConfirmarExclusao.jsx`:
```jsx
import Botao from './Botao';

export default function ConfirmarExclusao({ aberto, titulo = 'Confirmar exclusão', mensagem, onConfirmar, onCancelar, processando = false }) {
  if (!aberto) return null;
  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-900/40 px-4" role="dialog" aria-modal="true" aria-labelledby="confirmar-titulo">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        <h2 id="confirmar-titulo" className="text-lg font-semibold text-slate-900">{titulo}</h2>
        <p className="mt-2 text-sm text-slate-600">{mensagem}</p>
        <div className="mt-6 flex justify-end gap-2">
          <Botao variante="secundario" onClick={onCancelar} disabled={processando}>Cancelar</Botao>
          <Botao variante="perigo" onClick={onConfirmar} disabled={processando}>
            {processando ? 'Excluindo…' : 'Excluir'}
          </Botao>
        </div>
      </div>
    </div>
  );
}
```

`components/RotaProtegida.jsx`:
```jsx
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function RotaProtegida({ children }) {
  const { usuario } = useAuth();
  const location = useLocation();
  if (!usuario) return <Navigate to="/login" replace state={{ de: location.pathname }} />;
  return children;
}
```

`components/Layout.jsx`:
```jsx
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const LINKS = [
  { to: '/', rotulo: 'Início', end: true },
  { to: '/receitas', rotulo: 'Receitas' },
  { to: '/gastos', rotulo: 'Gastos' },
  { to: '/investimentos', rotulo: 'Investimentos' },
  { to: '/contas', rotulo: 'Contas' },
  { to: '/categorias', rotulo: 'Categorias' },
  { to: '/perfil', rotulo: 'Perfil' },
];

const estiloLink = ({ isActive }) =>
  `whitespace-nowrap rounded-lg px-3 py-2 text-sm font-medium ${isActive ? 'bg-white/15 text-white' : 'text-white/80 hover:bg-white/10'}`;

export default function Layout() {
  const { usuario, sair } = useAuth();
  return (
    <div className="min-h-screen md:flex">
      <aside className="bg-brand text-white md:min-h-screen md:w-60 md:shrink-0">
        <div className="flex items-center justify-between px-5 py-4 md:block">
          <span className="text-xl font-bold">Fintech</span>
          <span className="text-xs text-white/70 md:mt-1 md:block">Olá, {usuario.nome.split(' ')[0]}</span>
        </div>
        <nav className="flex gap-1 overflow-x-auto px-3 pb-3 md:flex-col md:pb-0">
          {LINKS.map((l) => (
            <NavLink key={l.to} to={l.to} end={l.end} className={estiloLink}>{l.rotulo}</NavLink>
          ))}
          <button type="button" onClick={sair} className="whitespace-nowrap rounded-lg px-3 py-2 text-left text-sm font-medium text-white/80 hover:bg-white/10">
            Sair
          </button>
        </nav>
      </aside>
      <main className="flex-1 px-4 py-6 md:px-8">
        <Outlet />
      </main>
    </div>
  );
}
```

- [ ] **Passo 3: páginas de entrada e erro**

`pages/LoginPage.jsx`:
```jsx
import { useState } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import Alerta from '../components/Alerta';
import Botao from '../components/Botao';
import Campo from '../components/Campo';
import { useAuth } from '../contexts/AuthContext';

export default function LoginPage() {
  const { usuario, entrar } = useAuth();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', senha: '' });
  const [erro, setErro] = useState(null);
  const [enviando, setEnviando] = useState(false);

  // Ao autenticar, o estado muda e esta linha faz o redirecionamento
  if (usuario) return <Navigate to={location.state?.de ?? '/'} replace />;

  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  async function enviar(e) {
    e.preventDefault();
    setEnviando(true);
    setErro(null);
    try {
      await entrar(form.email, form.senha);
    } catch (err) {
      setErro(err.message);
      setEnviando(false);
    }
  }

  return (
    <main className="grid min-h-screen place-items-center px-4">
      <form onSubmit={enviar} className="w-full max-w-sm space-y-4 rounded-2xl bg-white p-8 shadow-sm ring-1 ring-slate-200">
        <div>
          <h1 className="text-2xl font-bold text-brand">Fintech</h1>
          <p className="text-sm text-slate-500">Entre para gerenciar suas finanças.</p>
        </div>
        {erro && <Alerta>{erro}</Alerta>}
        <Campo label="E-mail" name="email" type="email" value={form.email} onChange={alterar} required autoComplete="email" />
        <Campo label="Senha" name="senha" type="password" value={form.senha} onChange={alterar} required autoComplete="current-password" />
        <Botao type="submit" disabled={enviando} className="w-full">{enviando ? 'Entrando…' : 'Entrar'}</Botao>
        <p className="text-center text-sm text-slate-600">
          Não tem conta? <Link to="/cadastro" className="font-medium text-brand hover:underline">Cadastre-se</Link>
        </p>
      </form>
    </main>
  );
}
```

`pages/CadastroPage.jsx`:
```jsx
import { useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { criarUsuario } from '../api/usuarios';
import Alerta from '../components/Alerta';
import Botao from '../components/Botao';
import Campo from '../components/Campo';
import { useAuth } from '../contexts/AuthContext';

export default function CadastroPage() {
  const { usuario, entrar } = useAuth();
  const [form, setForm] = useState({ nome: '', email: '', senha: '', confirmacao: '' });
  const [erro, setErro] = useState(null);
  const [enviando, setEnviando] = useState(false);

  if (usuario) return <Navigate to="/" replace />;

  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  async function enviar(e) {
    e.preventDefault();
    if (form.senha !== form.confirmacao) {
      setErro({ message: 'As senhas não conferem.', detalhes: [] });
      return;
    }
    setEnviando(true);
    setErro(null);
    try {
      await criarUsuario({ nome: form.nome, email: form.email, senha: form.senha });
      await entrar(form.email, form.senha);
    } catch (err) {
      setErro(err);
      setEnviando(false);
    }
  }

  return (
    <main className="grid min-h-screen place-items-center px-4">
      <form onSubmit={enviar} className="w-full max-w-sm space-y-4 rounded-2xl bg-white p-8 shadow-sm ring-1 ring-slate-200">
        <div>
          <h1 className="text-2xl font-bold text-brand">Criar conta</h1>
          <p className="text-sm text-slate-500">Cadastre-se para começar.</p>
        </div>
        {erro && <Alerta detalhes={erro.detalhes}>{erro.message}</Alerta>}
        <Campo label="Nome" name="nome" value={form.nome} onChange={alterar} required maxLength={150} autoComplete="name" />
        <Campo label="E-mail" name="email" type="email" value={form.email} onChange={alterar} required autoComplete="email" />
        <Campo label="Senha" name="senha" type="password" value={form.senha} onChange={alterar} required minLength={6} autoComplete="new-password" />
        <Campo label="Confirmar senha" name="confirmacao" type="password" value={form.confirmacao} onChange={alterar} required minLength={6} autoComplete="new-password" />
        <Botao type="submit" disabled={enviando} className="w-full">{enviando ? 'Cadastrando…' : 'Cadastrar'}</Botao>
        <p className="text-center text-sm text-slate-600">
          Já tem conta? <Link to="/login" className="font-medium text-brand hover:underline">Entrar</Link>
        </p>
      </form>
    </main>
  );
}
```

`pages/ErroPage.jsx`:
```jsx
import { isRouteErrorResponse, Link, useRouteError } from 'react-router-dom';

/** Usada como rota "*" (404) e como errorElement (erro inesperado em qualquer rota). */
export default function ErroPage() {
  const erro = useRouteError();
  const naoEncontrada = !erro || (isRouteErrorResponse(erro) && erro.status === 404);
  return (
    <main className="grid min-h-screen place-items-center px-4 text-center">
      <div>
        <p className="text-6xl font-bold text-brand">{naoEncontrada ? '404' : 'Ops!'}</p>
        <h1 className="mt-4 text-2xl font-semibold text-slate-900">
          {naoEncontrada ? 'Página não encontrada' : 'Algo deu errado'}
        </h1>
        <p className="mt-2 text-sm text-slate-500">
          {naoEncontrada ? 'O endereço acessado não existe ou foi removido.' : 'Ocorreu um erro inesperado. Tente novamente em instantes.'}
        </p>
        <Link to="/" className="mt-6 inline-block rounded-lg bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand-claro">
          Voltar ao início
        </Link>
      </div>
    </main>
  );
}
```

- [ ] **Passo 4: rotas e páginas provisórias.** Cada página provisória (`DashboardPage`, `ReceitasPage`, `ReceitaFormPage`, `GastosPage`, `GastoFormPage`, `InvestimentosPage`, `InvestimentoFormPage`, `ContasPage`, `CategoriasPage`, `PerfilPage`) tem este formato, trocando o nome e o título:
```jsx
import PageHeader from '../components/PageHeader';

export default function GastosPage() {
  return <PageHeader titulo="Gastos" descricao="Em construção" />;
}
```

`web/src/router.jsx`:
```jsx
import { createBrowserRouter } from 'react-router-dom';
import Layout from './components/Layout';
import RotaProtegida from './components/RotaProtegida';
import CadastroPage from './pages/CadastroPage';
import CategoriasPage from './pages/CategoriasPage';
import ContasPage from './pages/ContasPage';
import DashboardPage from './pages/DashboardPage';
import ErroPage from './pages/ErroPage';
import GastoFormPage from './pages/GastoFormPage';
import GastosPage from './pages/GastosPage';
import InvestimentoFormPage from './pages/InvestimentoFormPage';
import InvestimentosPage from './pages/InvestimentosPage';
import LoginPage from './pages/LoginPage';
import PerfilPage from './pages/PerfilPage';
import ReceitaFormPage from './pages/ReceitaFormPage';
import ReceitasPage from './pages/ReceitasPage';

export const rotas = [
  { path: '/login', element: <LoginPage />, errorElement: <ErroPage /> },
  { path: '/cadastro', element: <CadastroPage />, errorElement: <ErroPage /> },
  {
    path: '/',
    element: <RotaProtegida><Layout /></RotaProtegida>,
    errorElement: <ErroPage />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'receitas', element: <ReceitasPage /> },
      { path: 'receitas/novo', element: <ReceitaFormPage /> },
      { path: 'receitas/:id/editar', element: <ReceitaFormPage /> },
      { path: 'gastos', element: <GastosPage /> },
      { path: 'gastos/novo', element: <GastoFormPage /> },
      { path: 'gastos/:id/editar', element: <GastoFormPage /> },
      { path: 'investimentos', element: <InvestimentosPage /> },
      { path: 'investimentos/novo', element: <InvestimentoFormPage /> },
      { path: 'investimentos/:id/editar', element: <InvestimentoFormPage /> },
      { path: 'contas', element: <ContasPage /> },
      { path: 'categorias', element: <CategoriasPage /> },
      { path: 'perfil', element: <PerfilPage /> },
    ],
  },
  { path: '*', element: <ErroPage /> },
];

export const router = createBrowserRouter(rotas);
```

`web/src/main.jsx` (substituir):
```jsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import './index.css';
import { router } from './router';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  </StrictMode>,
);
```

- [ ] **Passo 5: rodar** — `npm test` → 7 PASS; `npm run build` OK. Com a API rodando (`./mvnw spring-boot:run`) e `npm run dev`: entrar com o usuário demo, navegar pelo menu, acessar `/qualquer-coisa` (404 personalizado), clicar "Sair" (volta ao login), acessar `/gastos` deslogado (redireciona ao login e, após entrar, volta para `/gastos`).
- [ ] **Passo 6: commit** — `git add web/ && git commit -m "feat(web): rotas SPA, layout, login, cadastro e página de erro"`

---

### Task F03: Página inicial + páginas de Gastos

**Arquivos:**
- Criar: `web/src/components/gastos/{TabelaGastos,GastoForm}.jsx`
- Substituir: `web/src/pages/{DashboardPage,GastosPage,GastoFormPage}.jsx`
- Teste: `web/src/components/gastos/GastoForm.test.jsx`, `web/src/pages/DashboardPage.test.jsx`

**Interfaces:**
- Consome: `listarGastos`, `buscarGasto`, `criarGasto`, `atualizarGasto`, `excluirGasto`, `buscarResumo`, `listarReceitas`, `listarCategorias`, `listarContas`, `useDados`, `useAuth`, componentes base.
- Produz: `GastoForm({inicial, categorias, contas, onSubmit, enviando})` — chama `onSubmit({descricao, valor:number, dtGasto, tipo, categoriaId:number, contaId:number, comprovante|null})`; `TabelaGastos({gastos, onExcluir})`.

- [ ] **Passo 1: teste do formulário (falha)** — `components/gastos/GastoForm.test.jsx`:
```jsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { expect, it, vi } from 'vitest';
import GastoForm from './GastoForm';

const categorias = [{ id: 3, nome: 'Moradia' }];
const contas = [{ id: 7, nmBanco: 'Banco Demo', nrConta: '0001-9' }];

it('envia os valores no formato esperado pela API', async () => {
  const onSubmit = vi.fn();
  render(<MemoryRouter><GastoForm categorias={categorias} contas={contas} onSubmit={onSubmit} /></MemoryRouter>);

  await userEvent.type(screen.getByLabelText('Descrição'), '  Aluguel  ');
  await userEvent.type(screen.getByLabelText('Valor (R$)'), '1500.50');
  await userEvent.selectOptions(screen.getByLabelText('Categoria'), '3');
  await userEvent.selectOptions(screen.getByLabelText('Conta'), '7');
  await userEvent.selectOptions(screen.getByLabelText('Tipo'), 'FIXO');
  await userEvent.click(screen.getByRole('button', { name: 'Salvar' }));

  expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({
    descricao: 'Aluguel', valor: 1500.5, categoriaId: 3, contaId: 7, tipo: 'FIXO', comprovante: null,
  }));
});

it('preenche os campos ao editar um gasto existente', () => {
  render(
    <MemoryRouter>
      <GastoForm
        categorias={categorias}
        contas={contas}
        onSubmit={vi.fn()}
        inicial={{ descricao: 'Mercado', valor: '420.35', dtGasto: '2026-11-01', tipo: 'VARIAVEL', categoriaId: 3, contaId: 7, comprovante: '' }}
      />
    </MemoryRouter>,
  );
  expect(screen.getByLabelText('Descrição')).toHaveValue('Mercado');
  expect(screen.getByLabelText('Categoria')).toHaveValue('3');
});
```
Run: `npm test` → FAIL.

- [ ] **Passo 2: componentes de gastos**

`components/gastos/GastoForm.jsx`:
```jsx
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { hojeISO } from '../../utils/formatar';
import Botao from '../Botao';
import Campo from '../Campo';

const VAZIO = { descricao: '', valor: '', dtGasto: hojeISO(), tipo: 'VARIAVEL', categoriaId: '', contaId: '', comprovante: '' };

export default function GastoForm({ inicial, categorias, contas, onSubmit, enviando = false }) {
  const [form, setForm] = useState(() => ({ ...VAZIO, ...inicial }));
  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  function enviar(e) {
    e.preventDefault();
    onSubmit({
      descricao: form.descricao.trim(),
      valor: Number(form.valor),
      dtGasto: form.dtGasto,
      tipo: form.tipo,
      categoriaId: Number(form.categoriaId),
      contaId: Number(form.contaId),
      comprovante: form.comprovante.trim() || null,
    });
  }

  return (
    <form onSubmit={enviar} className="grid gap-4 rounded-2xl bg-white p-6 ring-1 ring-slate-200 sm:grid-cols-2">
      <Campo className="sm:col-span-2" label="Descrição" name="descricao" value={form.descricao} onChange={alterar} required maxLength={300} />
      <Campo label="Valor (R$)" name="valor" type="number" min="0.01" step="0.01" value={form.valor} onChange={alterar} required />
      <Campo label="Data" name="dtGasto" type="date" value={form.dtGasto} onChange={alterar} required />
      <Campo as="select" label="Categoria" name="categoriaId" value={form.categoriaId} onChange={alterar} required>
        <option value="">Selecione…</option>
        {categorias.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
      </Campo>
      <Campo as="select" label="Conta" name="contaId" value={form.contaId} onChange={alterar} required>
        <option value="">Selecione…</option>
        {contas.map((c) => <option key={c.id} value={c.id}>{c.nmBanco} · {c.nrConta}</option>)}
      </Campo>
      <Campo as="select" label="Tipo" name="tipo" value={form.tipo} onChange={alterar}>
        <option value="FIXO">Fixo</option>
        <option value="VARIAVEL">Variável</option>
      </Campo>
      <Campo label="Comprovante (URL, opcional)" name="comprovante" value={form.comprovante} onChange={alterar} maxLength={500} />
      <div className="flex justify-end gap-2 sm:col-span-2">
        <Link to="/gastos" className="rounded-lg px-4 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100">Cancelar</Link>
        <Botao type="submit" disabled={enviando}>{enviando ? 'Salvando…' : 'Salvar'}</Botao>
      </div>
    </form>
  );
}
```

`components/gastos/TabelaGastos.jsx`:
```jsx
import { Link } from 'react-router-dom';
import { formatarData, formatarMoeda } from '../../utils/formatar';

const COLUNAS = ['Data', 'Descrição', 'Categoria', 'Conta', 'Tipo', 'Valor', 'Ações'];

export default function TabelaGastos({ gastos, onExcluir }) {
  if (gastos.length === 0) {
    return <p className="rounded-2xl bg-white p-6 text-sm text-slate-500 ring-1 ring-slate-200">Nenhum gasto cadastrado ainda.</p>;
  }
  return (
    <div className="overflow-x-auto rounded-2xl bg-white ring-1 ring-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-slate-500">
          <tr>{COLUNAS.map((c) => <th key={c} className="px-4 py-3 font-medium">{c}</th>)}</tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {gastos.map((g) => (
            <tr key={g.id}>
              <td className="whitespace-nowrap px-4 py-3">{formatarData(g.dtGasto)}</td>
              <td className="px-4 py-3">{g.descricao}</td>
              <td className="px-4 py-3">{g.categoria}</td>
              <td className="px-4 py-3">{g.conta}</td>
              <td className="px-4 py-3">{g.tipo === 'FIXO' ? 'Fixo' : 'Variável'}</td>
              <td className="whitespace-nowrap px-4 py-3 font-medium">{formatarMoeda(g.valor)}</td>
              <td className="whitespace-nowrap px-4 py-3">
                <Link to={`/gastos/${g.id}/editar`} className="text-brand hover:underline">Editar</Link>
                <button type="button" onClick={() => onExcluir(g)} className="ml-3 text-red-600 hover:underline">Excluir</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

- [ ] **Passo 3: páginas**

`pages/GastosPage.jsx` (consultar + remover):
```jsx
import { useCallback, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { excluirGasto, listarGastos } from '../api/gastos';
import Alerta from '../components/Alerta';
import Carregando from '../components/Carregando';
import ConfirmarExclusao from '../components/ConfirmarExclusao';
import TabelaGastos from '../components/gastos/TabelaGastos';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';
import { formatarMoeda } from '../utils/formatar';

export default function GastosPage() {
  const { usuario } = useAuth();
  const location = useLocation();
  const buscar = useCallback(() => listarGastos(usuario.id), [usuario.id]);
  const { dados: gastos, erro, carregando, setDados } = useDados(buscar);
  const [paraExcluir, setParaExcluir] = useState(null);
  const [excluindo, setExcluindo] = useState(false);
  const [erroExclusao, setErroExclusao] = useState(null);
  const [mensagem, setMensagem] = useState(location.state?.mensagem ?? null);

  async function confirmarExclusao() {
    setExcluindo(true);
    setErroExclusao(null);
    setMensagem(null);
    try {
      await excluirGasto(paraExcluir.id);
      setDados(gastos.filter((g) => g.id !== paraExcluir.id));
      setMensagem('Gasto excluído com sucesso.');
    } catch (e) {
      setErroExclusao(e.message);
    } finally {
      setExcluindo(false);
      setParaExcluir(null);
    }
  }

  const total = (gastos ?? []).reduce((soma, g) => soma + Number(g.valor), 0);

  return (
    <div className="space-y-4">
      <PageHeader
        titulo="Gastos"
        descricao={gastos ? `${gastos.length} gasto(s) · total ${formatarMoeda(total)}` : 'Seus gastos'}
        acao={<Link to="/gastos/novo" className="rounded-lg bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand-claro">Novo gasto</Link>}
      />
      {mensagem && <Alerta tipo="sucesso">{mensagem}</Alerta>}
      {erroExclusao && <Alerta>{erroExclusao}</Alerta>}
      {carregando && <Carregando />}
      {erro && <Alerta>{erro}</Alerta>}
      {gastos && <TabelaGastos gastos={gastos} onExcluir={setParaExcluir} />}
      <ConfirmarExclusao
        aberto={Boolean(paraExcluir)}
        mensagem={`Excluir o gasto "${paraExcluir?.descricao}"? Esta ação não pode ser desfeita.`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setParaExcluir(null)}
        processando={excluindo}
      />
    </div>
  );
}
```

`pages/GastoFormPage.jsx` (inserir + atualizar):
```jsx
import { useCallback, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { listarCategorias } from '../api/categorias';
import { listarContas } from '../api/contas';
import { atualizarGasto, buscarGasto, criarGasto } from '../api/gastos';
import Alerta from '../components/Alerta';
import Carregando from '../components/Carregando';
import GastoForm from '../components/gastos/GastoForm';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';

export default function GastoFormPage() {
  const { id } = useParams();
  const editando = Boolean(id);
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const buscar = useCallback(
    () => Promise.all([listarCategorias('GASTO'), listarContas(usuario.id), id ? buscarGasto(id) : null]),
    [usuario.id, id],
  );
  const { dados, erro, carregando } = useDados(buscar);
  const [erroEnvio, setErroEnvio] = useState(null);
  const [enviando, setEnviando] = useState(false);

  async function salvar(valores) {
    setEnviando(true);
    setErroEnvio(null);
    try {
      const corpo = { ...valores, usuarioId: usuario.id };
      if (editando) await atualizarGasto(id, corpo);
      else await criarGasto(corpo);
      navigate('/gastos', { state: { mensagem: editando ? 'Gasto atualizado com sucesso.' : 'Gasto cadastrado com sucesso.' } });
    } catch (e) {
      setErroEnvio(e);
      setEnviando(false);
    }
  }

  if (carregando) return <Carregando />;
  if (erro) return <Alerta>{erro}</Alerta>;
  const [categorias, contas, gasto] = dados;
  const contasAtivas = contas.filter((c) => c.ativa);

  return (
    <div className="space-y-4">
      <PageHeader titulo={editando ? 'Editar gasto' : 'Novo gasto'} />
      {contasAtivas.length === 0 && (
        <Alerta>Cadastre uma conta bancária ativa em <Link to="/contas" className="underline">Contas</Link> antes de registrar gastos.</Alerta>
      )}
      {erroEnvio && <Alerta detalhes={erroEnvio.detalhes}>{erroEnvio.message}</Alerta>}
      <GastoForm
        inicial={gasto ? { ...gasto, valor: String(gasto.valor), comprovante: gasto.comprovante ?? '' } : undefined}
        categorias={categorias}
        contas={contasAtivas}
        onSubmit={salvar}
        enviando={enviando}
      />
    </div>
  );
}
```

`pages/DashboardPage.jsx` (página inicial — R4: números vêm do `GET /api/usuarios/{id}/resumo`, inspirado no dashboard do protótipo: saldo, fluxo do mês, gastos por categoria e últimas transações):
```jsx
import { useCallback } from 'react';
import { Link } from 'react-router-dom';
import { listarGastos } from '../api/gastos';
import { listarReceitas } from '../api/receitas';
import { buscarResumo } from '../api/usuarios';
import Alerta from '../components/Alerta';
import CardResumo from '../components/CardResumo';
import Carregando from '../components/Carregando';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';
import { formatarData, formatarMoeda } from '../utils/formatar';

export default function DashboardPage() {
  const { usuario } = useAuth();
  const buscar = useCallback(
    () => Promise.all([buscarResumo(usuario.id), listarReceitas(usuario.id), listarGastos(usuario.id)]),
    [usuario.id],
  );
  const { dados, erro, carregando } = useDados(buscar);

  if (carregando) return <Carregando />;
  if (erro) return <Alerta>{erro}</Alerta>;

  const [resumo, receitas, gastos] = dados;
  const [ano, mes] = resumo.mes.split('-');
  // Últimas transações: receitas e gastos juntos, mais recentes primeiro
  const transacoes = [
    ...receitas.map((r) => ({ id: `r${r.id}`, data: r.dtRecebimento, descricao: r.descricao, categoria: r.categoria, valor: r.valor, entrada: true })),
    ...gastos.map((g) => ({ id: `g${g.id}`, data: g.dtGasto, descricao: g.descricao, categoria: g.categoria, valor: g.valor, entrada: false })),
  ].sort((a, b) => b.data.localeCompare(a.data)).slice(0, 6);
  const maiorCategoria = Math.max(1, ...resumo.gastosPorCategoria.map((c) => Number(c.total)));

  return (
    <div className="space-y-8">
      <PageHeader titulo={`Olá, ${usuario.nome.split(' ')[0]}!`} descricao={`Resumo financeiro de ${mes}/${ano}`} />
      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <CardResumo destaque titulo="Saldo do mês" valor={formatarMoeda(resumo.saldo)} detalhe="Receitas − gastos" />
        <CardResumo titulo="Receitas no mês" valor={formatarMoeda(resumo.totalReceitas)} />
        <CardResumo titulo="Gastos no mês" valor={formatarMoeda(resumo.totalGastos)} />
        <CardResumo titulo="Total investido" valor={formatarMoeda(resumo.totalInvestido)} detalhe={`Rendimento estimado: ${formatarMoeda(resumo.rendimentoEstimadoAnual)}/ano`} />
      </section>
      <div className="grid gap-4 lg:grid-cols-2">
        <section className="rounded-2xl bg-white p-5 ring-1 ring-slate-200">
          <h2 className="mb-3 font-semibold">Gastos por categoria</h2>
          {resumo.gastosPorCategoria.length === 0 ? (
            <p className="text-sm text-slate-500">Nenhum gasto neste mês.</p>
          ) : (
            <ul className="space-y-3">
              {resumo.gastosPorCategoria.map((c) => (
                <li key={c.categoria}>
                  <div className="flex justify-between text-sm"><span>{c.categoria}</span><span className="font-medium">{formatarMoeda(c.total)}</span></div>
                  <div className="mt-1 h-2 rounded-full bg-slate-100">
                    <div className="h-2 rounded-full bg-brand" style={{ width: `${(Number(c.total) / maiorCategoria) * 100}%` }} />
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
        <section className="rounded-2xl bg-white p-5 ring-1 ring-slate-200">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="font-semibold">Últimas transações</h2>
            <span className="space-x-3 text-sm">
              <Link to="/receitas" className="text-brand hover:underline">Receitas</Link>
              <Link to="/gastos" className="text-brand hover:underline">Gastos</Link>
            </span>
          </div>
          {transacoes.length === 0 ? (
            <p className="text-sm text-slate-500">Nenhuma transação registrada.</p>
          ) : (
            <ul className="divide-y divide-slate-100">
              {transacoes.map((t) => (
                <li key={t.id} className="flex justify-between gap-4 py-2 text-sm">
                  <span>{t.descricao} <span className="text-slate-400">· {t.categoria} · {formatarData(t.data)}</span></span>
                  <span className={`whitespace-nowrap font-medium ${t.entrada ? 'text-emerald-700' : 'text-red-600'}`}>
                    {t.entrada ? '+' : '-'}{formatarMoeda(t.valor)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}
```

`pages/DashboardPage.test.jsx`:
```jsx
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, expect, it, vi } from 'vitest';
import { listarGastos } from '../api/gastos';
import { listarReceitas } from '../api/receitas';
import { buscarResumo } from '../api/usuarios';
import { AuthProvider } from '../contexts/AuthContext';
import DashboardPage from './DashboardPage';

vi.mock('../api/usuarios', () => ({ buscarResumo: vi.fn() }));
vi.mock('../api/receitas', () => ({ listarReceitas: vi.fn() }));
vi.mock('../api/gastos', () => ({ listarGastos: vi.fn() }));

beforeEach(() => {
  localStorage.setItem('fintech.usuario', JSON.stringify({ id: 1, nome: 'Usuária Demo', email: 'demo@fintech.com' }));
  vi.mocked(buscarResumo).mockResolvedValue({
    mes: '2026-11', totalReceitas: 5000, totalGastos: 1979.25, saldo: 3020.75, totalInvestido: 5000,
    rendimentoEstimadoAnual: 625, gastosPorCategoria: [{ categoria: 'Moradia', total: 1500 }],
  });
  vi.mocked(listarReceitas).mockResolvedValue([{ id: 1, dtRecebimento: '2026-11-01', descricao: 'Salário do mês', categoria: 'Salário', valor: 5000 }]);
  vi.mocked(listarGastos).mockResolvedValue([{ id: 2, dtGasto: '2026-11-03', descricao: 'Aluguel', categoria: 'Moradia', valor: 1500 }]);
});

it('mostra o saldo calculado pela API e as últimas transações', async () => {
  render(<AuthProvider><MemoryRouter><DashboardPage /></MemoryRouter></AuthProvider>);

  expect(await screen.findByText(/3\.020,75/)).toBeInTheDocument();
  expect(buscarResumo).toHaveBeenCalledWith(1);
  expect(screen.getByText('Salário do mês')).toBeInTheDocument();
  expect(screen.getByText('Aluguel')).toBeInTheDocument();
});
```

- [ ] **Passo 4: rodar** — `npm test` → todos PASS; `npm run build` OK. Manual com API: listar, criar, editar e excluir um gasto; tentar valor 0 (bloqueado pelo navegador); conferir mensagem de sucesso após salvar.
- [ ] **Passo 5: commit** — `git add web/ && git commit -m "feat(web): página inicial e CRUD de gastos"`

---

### Task F04: Páginas de Receitas

> Revisão 1 (R1): tela correspondente ao `ReceitaController` e ao protótipo `receitas.html` (origem, descrição, valor, data, recorrência, conta, categoria).

**Arquivos:**
- Criar: `web/src/components/receitas/{TabelaReceitas,ReceitaForm}.jsx`
- Substituir: `web/src/pages/{ReceitasPage,ReceitaFormPage}.jsx` (provisórias criadas em F02)
- Teste: `web/src/components/receitas/ReceitaForm.test.jsx`

**Interfaces:**
- Consome: `listarReceitas`, `buscarReceita`, `criarReceita`, `atualizarReceita`, `excluirReceita` (`api/receitas.js`), `listarCategorias('RECEITA')`, `listarContas(usuarioId)`.
- Produz: `ReceitaForm({inicial, categorias, contas, onSubmit, enviando})` → `onSubmit({descricao, valor:number, dtRecebimento, origem|null, recorrencia, categoriaId:number, contaId:number, comprovante|null})`; `TabelaReceitas({receitas, onExcluir})`.

- [ ] **Passo 1: teste (falha)** — `components/receitas/ReceitaForm.test.jsx`:
```jsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { expect, it, vi } from 'vitest';
import ReceitaForm from './ReceitaForm';

const categorias = [{ id: 1, nome: 'Salário' }];
const contas = [{ id: 7, nmBanco: 'Banco Demo', nrConta: '0001-9' }];

it('envia os valores no formato esperado pela API', async () => {
  const onSubmit = vi.fn();
  render(<MemoryRouter><ReceitaForm categorias={categorias} contas={contas} onSubmit={onSubmit} /></MemoryRouter>);

  await userEvent.selectOptions(screen.getByLabelText('Origem'), 'SALARIO');
  await userEvent.type(screen.getByLabelText('Descrição'), ' Salário de novembro ');
  await userEvent.type(screen.getByLabelText('Valor (R$)'), '5000');
  await userEvent.selectOptions(screen.getByLabelText('Recorrência'), 'MENSAL');
  await userEvent.selectOptions(screen.getByLabelText('Conta destino'), '7');
  await userEvent.selectOptions(screen.getByLabelText('Categoria'), '1');
  await userEvent.click(screen.getByRole('button', { name: 'Salvar' }));

  expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({
    descricao: 'Salário de novembro', valor: 5000, origem: 'SALARIO', recorrencia: 'MENSAL',
    contaId: 7, categoriaId: 1, comprovante: null,
  }));
});

it('envia origem nula quando não informada', async () => {
  const onSubmit = vi.fn();
  render(
    <MemoryRouter>
      <ReceitaForm
        categorias={categorias}
        contas={contas}
        onSubmit={onSubmit}
        inicial={{ descricao: 'Bônus', valor: '300', dtRecebimento: '2026-11-10', categoriaId: 1, contaId: 7 }}
      />
    </MemoryRouter>,
  );
  await userEvent.click(screen.getByRole('button', { name: 'Salvar' }));
  expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({ origem: null, recorrencia: 'UNICA' }));
});
```
Run: `npm test` → FAIL.

- [ ] **Passo 2: componentes**

`components/receitas/ReceitaForm.jsx`:
```jsx
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { hojeISO } from '../../utils/formatar';
import Botao from '../Botao';
import Campo from '../Campo';

export const ORIGENS = [['SALARIO', 'Salário'], ['FREELANCE', 'Freelance'], ['INVESTIMENTO', 'Investimento'], ['OUTROS', 'Outros']];
export const RECORRENCIAS = [['UNICA', 'Única'], ['MENSAL', 'Mensal'], ['ANUAL', 'Anual']];

const VAZIO = {
  origem: '', descricao: '', valor: '', dtRecebimento: hojeISO(), recorrencia: 'UNICA',
  contaId: '', categoriaId: '', comprovante: '',
};

export default function ReceitaForm({ inicial, categorias, contas, onSubmit, enviando = false }) {
  const [form, setForm] = useState(() => ({ ...VAZIO, ...inicial }));
  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  function enviar(e) {
    e.preventDefault();
    onSubmit({
      descricao: form.descricao.trim(),
      valor: Number(form.valor),
      dtRecebimento: form.dtRecebimento,
      origem: form.origem || null,
      recorrencia: form.recorrencia,
      categoriaId: Number(form.categoriaId),
      contaId: Number(form.contaId),
      comprovante: (form.comprovante ?? '').trim() || null,
    });
  }

  return (
    <form onSubmit={enviar} className="grid gap-4 rounded-2xl bg-white p-6 ring-1 ring-slate-200 sm:grid-cols-2">
      <Campo as="select" label="Origem" name="origem" value={form.origem} onChange={alterar}>
        <option value="">Não informada</option>
        {ORIGENS.map(([valor, rotulo]) => <option key={valor} value={valor}>{rotulo}</option>)}
      </Campo>
      <Campo label="Descrição" name="descricao" value={form.descricao} onChange={alterar} required maxLength={300} />
      <Campo label="Valor (R$)" name="valor" type="number" min="0.01" step="0.01" value={form.valor} onChange={alterar} required />
      <Campo label="Data de recebimento" name="dtRecebimento" type="date" value={form.dtRecebimento} onChange={alterar} required />
      <Campo as="select" label="Recorrência" name="recorrencia" value={form.recorrencia} onChange={alterar}>
        {RECORRENCIAS.map(([valor, rotulo]) => <option key={valor} value={valor}>{rotulo}</option>)}
      </Campo>
      <Campo as="select" label="Conta destino" name="contaId" value={form.contaId} onChange={alterar} required>
        <option value="">Selecione…</option>
        {contas.map((c) => <option key={c.id} value={c.id}>{c.nmBanco} · {c.nrConta}</option>)}
      </Campo>
      <Campo as="select" label="Categoria" name="categoriaId" value={form.categoriaId} onChange={alterar} required>
        <option value="">Selecione…</option>
        {categorias.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
      </Campo>
      <Campo label="Comprovante (URL, opcional)" name="comprovante" value={form.comprovante ?? ''} onChange={alterar} maxLength={500} />
      <div className="flex justify-end gap-2 sm:col-span-2">
        <Link to="/receitas" className="rounded-lg px-4 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100">Cancelar</Link>
        <Botao type="submit" disabled={enviando}>{enviando ? 'Salvando…' : 'Salvar'}</Botao>
      </div>
    </form>
  );
}
```

`components/receitas/TabelaReceitas.jsx`:
```jsx
import { Link } from 'react-router-dom';
import { formatarData, formatarMoeda } from '../../utils/formatar';
import { ORIGENS, RECORRENCIAS } from './ReceitaForm';

const COLUNAS = ['Data', 'Descrição', 'Origem', 'Categoria', 'Conta', 'Recorrência', 'Valor', 'Ações'];
const rotulo = (lista, valor) => lista.find(([v]) => v === valor)?.[1] ?? '—';

export default function TabelaReceitas({ receitas, onExcluir }) {
  if (receitas.length === 0) {
    return <p className="rounded-2xl bg-white p-6 text-sm text-slate-500 ring-1 ring-slate-200">Nenhuma receita cadastrada ainda.</p>;
  }
  return (
    <div className="overflow-x-auto rounded-2xl bg-white ring-1 ring-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-slate-500">
          <tr>{COLUNAS.map((c) => <th key={c} className="px-4 py-3 font-medium">{c}</th>)}</tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {receitas.map((r) => (
            <tr key={r.id}>
              <td className="whitespace-nowrap px-4 py-3">{formatarData(r.dtRecebimento)}</td>
              <td className="px-4 py-3">{r.descricao}</td>
              <td className="px-4 py-3">{rotulo(ORIGENS, r.origem)}</td>
              <td className="px-4 py-3">{r.categoria}</td>
              <td className="px-4 py-3">{r.conta}</td>
              <td className="px-4 py-3">{rotulo(RECORRENCIAS, r.recorrencia)}</td>
              <td className="whitespace-nowrap px-4 py-3 font-medium text-emerald-700">{formatarMoeda(r.valor)}</td>
              <td className="whitespace-nowrap px-4 py-3">
                <Link to={`/receitas/${r.id}/editar`} className="text-brand hover:underline">Editar</Link>
                <button type="button" onClick={() => onExcluir(r)} className="ml-3 text-red-600 hover:underline">Excluir</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

- [ ] **Passo 3: páginas**

`pages/ReceitasPage.jsx` (consultar + remover):
```jsx
import { useCallback, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { excluirReceita, listarReceitas } from '../api/receitas';
import Alerta from '../components/Alerta';
import Carregando from '../components/Carregando';
import ConfirmarExclusao from '../components/ConfirmarExclusao';
import PageHeader from '../components/PageHeader';
import TabelaReceitas from '../components/receitas/TabelaReceitas';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';
import { formatarMoeda } from '../utils/formatar';

export default function ReceitasPage() {
  const { usuario } = useAuth();
  const location = useLocation();
  const buscar = useCallback(() => listarReceitas(usuario.id), [usuario.id]);
  const { dados: receitas, erro, carregando, setDados } = useDados(buscar);
  const [paraExcluir, setParaExcluir] = useState(null);
  const [excluindo, setExcluindo] = useState(false);
  const [erroExclusao, setErroExclusao] = useState(null);
  const [mensagem, setMensagem] = useState(location.state?.mensagem ?? null);

  async function confirmarExclusao() {
    setExcluindo(true);
    setErroExclusao(null);
    setMensagem(null);
    try {
      await excluirReceita(paraExcluir.id);
      setDados(receitas.filter((r) => r.id !== paraExcluir.id));
      setMensagem('Receita excluída com sucesso.');
    } catch (e) {
      setErroExclusao(e.message);
    } finally {
      setExcluindo(false);
      setParaExcluir(null);
    }
  }

  const total = (receitas ?? []).reduce((soma, r) => soma + Number(r.valor), 0);

  return (
    <div className="space-y-4">
      <PageHeader
        titulo="Receitas"
        descricao={receitas ? `${receitas.length} receita(s) · total ${formatarMoeda(total)}` : 'Suas receitas'}
        acao={<Link to="/receitas/novo" className="rounded-lg bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand-claro">Nova receita</Link>}
      />
      {mensagem && <Alerta tipo="sucesso">{mensagem}</Alerta>}
      {erroExclusao && <Alerta>{erroExclusao}</Alerta>}
      {carregando && <Carregando />}
      {erro && <Alerta>{erro}</Alerta>}
      {receitas && <TabelaReceitas receitas={receitas} onExcluir={setParaExcluir} />}
      <ConfirmarExclusao
        aberto={Boolean(paraExcluir)}
        mensagem={`Excluir a receita "${paraExcluir?.descricao}"? Esta ação não pode ser desfeita.`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setParaExcluir(null)}
        processando={excluindo}
      />
    </div>
  );
}
```

`pages/ReceitaFormPage.jsx` (inserir + atualizar):
```jsx
import { useCallback, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { listarCategorias } from '../api/categorias';
import { listarContas } from '../api/contas';
import { atualizarReceita, buscarReceita, criarReceita } from '../api/receitas';
import Alerta from '../components/Alerta';
import Carregando from '../components/Carregando';
import PageHeader from '../components/PageHeader';
import ReceitaForm from '../components/receitas/ReceitaForm';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';

export default function ReceitaFormPage() {
  const { id } = useParams();
  const editando = Boolean(id);
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const buscar = useCallback(
    () => Promise.all([listarCategorias('RECEITA'), listarContas(usuario.id), id ? buscarReceita(id) : null]),
    [usuario.id, id],
  );
  const { dados, erro, carregando } = useDados(buscar);
  const [erroEnvio, setErroEnvio] = useState(null);
  const [enviando, setEnviando] = useState(false);

  async function salvar(valores) {
    setEnviando(true);
    setErroEnvio(null);
    try {
      const corpo = { ...valores, usuarioId: usuario.id };
      if (editando) await atualizarReceita(id, corpo);
      else await criarReceita(corpo);
      navigate('/receitas', { state: { mensagem: editando ? 'Receita atualizada com sucesso.' : 'Receita cadastrada com sucesso.' } });
    } catch (e) {
      setErroEnvio(e);
      setEnviando(false);
    }
  }

  if (carregando) return <Carregando />;
  if (erro) return <Alerta>{erro}</Alerta>;
  const [categorias, contas, receita] = dados;
  const contasAtivas = contas.filter((c) => c.ativa);

  return (
    <div className="space-y-4">
      <PageHeader titulo={editando ? 'Editar receita' : 'Cadastrar nova receita'} />
      {contasAtivas.length === 0 && (
        <Alerta>Cadastre uma conta bancária ativa em <Link to="/contas" className="underline">Contas</Link> antes de registrar receitas.</Alerta>
      )}
      {erroEnvio && <Alerta detalhes={erroEnvio.detalhes}>{erroEnvio.message}</Alerta>}
      <ReceitaForm
        inicial={receita ? { ...receita, valor: String(receita.valor), origem: receita.origem ?? '', comprovante: receita.comprovante ?? '' } : undefined}
        categorias={categorias}
        contas={contasAtivas}
        onSubmit={salvar}
        enviando={enviando}
      />
    </div>
  );
}
```

- [ ] **Passo 4: rodar** — `npm test` PASS; `npm run build` OK; manual com API: criar, editar e excluir uma receita; conferir que o saldo da página inicial muda.
- [ ] **Passo 5: commit** — `git add web/ && git commit -m "feat(web): CRUD de receitas"`

---

### Task F05: Páginas de Investimentos

**Arquivos:**
- Criar: `web/src/components/investimentos/{TabelaInvestimentos,InvestimentoForm}.jsx`
- Substituir: `web/src/pages/{InvestimentosPage,InvestimentoFormPage}.jsx`
- Teste: `web/src/components/investimentos/InvestimentoForm.test.jsx`

**Interfaces:**
- Produz: `InvestimentoForm({inicial, contas, editando, onSubmit, enviando})` — taxa digitada em **% a.a.** e enviada como fração com 4 casas (`12.5` → `0.125`); campo Status só aparece quando `editando`.

- [ ] **Passo 1: teste (falha)** — `components/investimentos/InvestimentoForm.test.jsx`:
```jsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { expect, it, vi } from 'vitest';
import InvestimentoForm from './InvestimentoForm';

const contas = [{ id: 7, nmBanco: 'Banco Demo', nrConta: '0001-9' }];

it('converte a taxa de % ao ano para fração e omite campos vazios', async () => {
  const onSubmit = vi.fn();
  render(<MemoryRouter><InvestimentoForm contas={contas} onSubmit={onSubmit} /></MemoryRouter>);

  await userEvent.type(screen.getByLabelText('Nome'), 'Tesouro Selic');
  await userEvent.selectOptions(screen.getByLabelText('Tipo'), 'TESOURO');
  await userEvent.type(screen.getByLabelText('Valor aplicado (R$)'), '2000');
  await userEvent.type(screen.getByLabelText('Rentabilidade (% a.a.)'), '10.75');
  await userEvent.selectOptions(screen.getByLabelText('Conta'), '7');
  await userEvent.click(screen.getByRole('button', { name: 'Salvar' }));

  expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({
    nomeInvestimento: 'Tesouro Selic', tipo: 'TESOURO', valorAplicado: 2000, taxaRentabilidade: 0.1075,
    contaId: 7, instituicao: null, dtVencimento: null, status: null,
  }));
  expect(screen.queryByLabelText('Status')).not.toBeInTheDocument();
});

it('mostra o status ao editar', () => {
  render(<MemoryRouter><InvestimentoForm contas={contas} onSubmit={vi.fn()} editando inicial={{ status: 'ATIVO' }} /></MemoryRouter>);
  expect(screen.getByLabelText('Status')).toHaveValue('ATIVO');
});
```

- [ ] **Passo 2: componentes**

`components/investimentos/InvestimentoForm.jsx`:
```jsx
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { hojeISO } from '../../utils/formatar';
import Botao from '../Botao';
import Campo from '../Campo';

export const TIPOS_INVESTIMENTO = [
  ['CDB', 'CDB'], ['LCI', 'LCI'], ['LCA', 'LCA'], ['TESOURO', 'Tesouro Direto'], ['ACOES', 'Ações'],
  ['FII', 'Fundo imobiliário'], ['FUNDO', 'Fundo de investimento'], ['POUPANCA', 'Poupança'], ['CRIPTO', 'Cripto'], ['OUTROS', 'Outros'],
];

const VAZIO = {
  nomeInvestimento: '', tipo: 'CDB', instituicao: '', valorAplicado: '', taxa: '',
  dtAplicacao: hojeISO(), dtVencimento: '', contaId: '', status: 'ATIVO',
};

export default function InvestimentoForm({ inicial, contas, editando = false, onSubmit, enviando = false }) {
  const [form, setForm] = useState(() => ({ ...VAZIO, ...inicial }));
  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  function enviar(e) {
    e.preventDefault();
    onSubmit({
      nomeInvestimento: form.nomeInvestimento.trim(),
      tipo: form.tipo,
      instituicao: form.instituicao.trim() || null,
      valorAplicado: Number(form.valorAplicado),
      // % a.a. -> fração com 4 casas (evita 0.10750000000000001)
      taxaRentabilidade: form.taxa === '' ? null : Number((Number(form.taxa) / 100).toFixed(4)),
      dtAplicacao: form.dtAplicacao,
      dtVencimento: form.dtVencimento || null,
      contaId: Number(form.contaId),
      status: editando ? form.status : null,
    });
  }

  return (
    <form onSubmit={enviar} className="grid gap-4 rounded-2xl bg-white p-6 ring-1 ring-slate-200 sm:grid-cols-2">
      <Campo label="Nome" name="nomeInvestimento" value={form.nomeInvestimento} onChange={alterar} required maxLength={150} />
      <Campo as="select" label="Tipo" name="tipo" value={form.tipo} onChange={alterar}>
        {TIPOS_INVESTIMENTO.map(([valor, rotulo]) => <option key={valor} value={valor}>{rotulo}</option>)}
      </Campo>
      <Campo label="Instituição (opcional)" name="instituicao" value={form.instituicao} onChange={alterar} maxLength={100} />
      <Campo as="select" label="Conta" name="contaId" value={form.contaId} onChange={alterar} required>
        <option value="">Selecione…</option>
        {contas.map((c) => <option key={c.id} value={c.id}>{c.nmBanco} · {c.nrConta}</option>)}
      </Campo>
      <Campo label="Valor aplicado (R$)" name="valorAplicado" type="number" min="0.01" step="0.01" value={form.valorAplicado} onChange={alterar} required />
      <Campo label="Rentabilidade (% a.a.)" name="taxa" type="number" min="0" step="0.01" value={form.taxa} onChange={alterar} />
      <Campo label="Data de aplicação" name="dtAplicacao" type="date" value={form.dtAplicacao} onChange={alterar} required />
      <Campo label="Vencimento (opcional)" name="dtVencimento" type="date" min={form.dtAplicacao} value={form.dtVencimento} onChange={alterar} />
      {editando && (
        <Campo as="select" label="Status" name="status" value={form.status} onChange={alterar}>
          <option value="ATIVO">Ativo</option>
          <option value="RESGATADO">Resgatado</option>
          <option value="VENCIDO">Vencido</option>
        </Campo>
      )}
      <div className="flex justify-end gap-2 sm:col-span-2">
        <Link to="/investimentos" className="rounded-lg px-4 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100">Cancelar</Link>
        <Botao type="submit" disabled={enviando}>{enviando ? 'Salvando…' : 'Salvar'}</Botao>
      </div>
    </form>
  );
}
```

`components/investimentos/TabelaInvestimentos.jsx`:
```jsx
import { Link } from 'react-router-dom';
import { formatarData, formatarMoeda, formatarPercentual } from '../../utils/formatar';

const COLUNAS = ['Nome', 'Tipo', 'Aplicado', 'Taxa a.a.', 'Rendimento est./ano', 'Vencimento', 'Status', 'Ações'];
const COR_STATUS = { ATIVO: 'bg-emerald-50 text-emerald-700', RESGATADO: 'bg-slate-100 text-slate-600', VENCIDO: 'bg-amber-50 text-amber-700' };

export default function TabelaInvestimentos({ investimentos, onExcluir }) {
  if (investimentos.length === 0) {
    return <p className="rounded-2xl bg-white p-6 text-sm text-slate-500 ring-1 ring-slate-200">Nenhum investimento cadastrado ainda.</p>;
  }
  return (
    <div className="overflow-x-auto rounded-2xl bg-white ring-1 ring-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-slate-500">
          <tr>{COLUNAS.map((c) => <th key={c} className="px-4 py-3 font-medium">{c}</th>)}</tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {investimentos.map((i) => (
            <tr key={i.id}>
              <td className="px-4 py-3">{i.nomeInvestimento}<span className="block text-xs text-slate-400">{i.instituicao ?? i.conta}</span></td>
              <td className="px-4 py-3">{i.tipo}</td>
              <td className="whitespace-nowrap px-4 py-3 font-medium">{formatarMoeda(i.valorAplicado)}</td>
              <td className="px-4 py-3">{formatarPercentual(i.taxaRentabilidade)}</td>
              <td className="whitespace-nowrap px-4 py-3 text-emerald-700">{i.rendimentoEstimadoAnual == null ? '—' : formatarMoeda(i.rendimentoEstimadoAnual)}</td>
              <td className="whitespace-nowrap px-4 py-3">{formatarData(i.dtVencimento)}</td>
              <td className="px-4 py-3"><span className={`rounded-full px-2 py-1 text-xs font-medium ${COR_STATUS[i.status]}`}>{i.status}</span></td>
              <td className="whitespace-nowrap px-4 py-3">
                <Link to={`/investimentos/${i.id}/editar`} className="text-brand hover:underline">Editar</Link>
                <button type="button" onClick={() => onExcluir(i)} className="ml-3 text-red-600 hover:underline">Excluir</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

- [ ] **Passo 3: páginas**

`pages/InvestimentosPage.jsx`:
```jsx
import { useCallback, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { excluirInvestimento, listarInvestimentos } from '../api/investimentos';
import Alerta from '../components/Alerta';
import Carregando from '../components/Carregando';
import ConfirmarExclusao from '../components/ConfirmarExclusao';
import TabelaInvestimentos from '../components/investimentos/TabelaInvestimentos';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';

export default function InvestimentosPage() {
  const { usuario } = useAuth();
  const location = useLocation();
  const buscar = useCallback(() => listarInvestimentos(usuario.id), [usuario.id]);
  const { dados: investimentos, erro, carregando, setDados } = useDados(buscar);
  const [paraExcluir, setParaExcluir] = useState(null);
  const [excluindo, setExcluindo] = useState(false);
  const [erroExclusao, setErroExclusao] = useState(null);
  const [mensagem, setMensagem] = useState(location.state?.mensagem ?? null);

  async function confirmarExclusao() {
    setExcluindo(true);
    setErroExclusao(null);
    setMensagem(null);
    try {
      await excluirInvestimento(paraExcluir.id);
      setDados(investimentos.filter((i) => i.id !== paraExcluir.id));
      setMensagem('Investimento excluído com sucesso.');
    } catch (e) {
      setErroExclusao(e.message);
    } finally {
      setExcluindo(false);
      setParaExcluir(null);
    }
  }

  return (
    <div className="space-y-4">
      <PageHeader
        titulo="Investimentos"
        descricao="Consulte, cadastre, edite e exclua suas aplicações"
        acao={<Link to="/investimentos/novo" className="rounded-lg bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand-claro">Novo investimento</Link>}
      />
      {mensagem && <Alerta tipo="sucesso">{mensagem}</Alerta>}
      {erroExclusao && <Alerta>{erroExclusao}</Alerta>}
      {carregando && <Carregando />}
      {erro && <Alerta>{erro}</Alerta>}
      {investimentos && <TabelaInvestimentos investimentos={investimentos} onExcluir={setParaExcluir} />}
      <ConfirmarExclusao
        aberto={Boolean(paraExcluir)}
        mensagem={`Excluir o investimento "${paraExcluir?.nomeInvestimento}"? Esta ação não pode ser desfeita.`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setParaExcluir(null)}
        processando={excluindo}
      />
    </div>
  );
}
```

`pages/InvestimentoFormPage.jsx`:
```jsx
import { useCallback, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { listarContas } from '../api/contas';
import { atualizarInvestimento, buscarInvestimento, criarInvestimento } from '../api/investimentos';
import Alerta from '../components/Alerta';
import Carregando from '../components/Carregando';
import InvestimentoForm from '../components/investimentos/InvestimentoForm';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';

/** Resposta da API -> valores do formulário (taxa em fração vira % a.a.). */
function paraFormulario(i) {
  return {
    ...i,
    instituicao: i.instituicao ?? '',
    valorAplicado: String(i.valorAplicado),
    taxa: i.taxaRentabilidade == null ? '' : String(Number((Number(i.taxaRentabilidade) * 100).toFixed(2))),
    dtVencimento: i.dtVencimento ?? '',
  };
}

export default function InvestimentoFormPage() {
  const { id } = useParams();
  const editando = Boolean(id);
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const buscar = useCallback(
    () => Promise.all([listarContas(usuario.id), id ? buscarInvestimento(id) : null]),
    [usuario.id, id],
  );
  const { dados, erro, carregando } = useDados(buscar);
  const [erroEnvio, setErroEnvio] = useState(null);
  const [enviando, setEnviando] = useState(false);

  async function salvar(valores) {
    setEnviando(true);
    setErroEnvio(null);
    try {
      const corpo = { ...valores, usuarioId: usuario.id };
      if (editando) await atualizarInvestimento(id, corpo);
      else await criarInvestimento(corpo);
      navigate('/investimentos', { state: { mensagem: editando ? 'Investimento atualizado com sucesso.' : 'Investimento cadastrado com sucesso.' } });
    } catch (e) {
      setErroEnvio(e);
      setEnviando(false);
    }
  }

  if (carregando) return <Carregando />;
  if (erro) return <Alerta>{erro}</Alerta>;
  const [contas, investimento] = dados;
  const contasAtivas = contas.filter((c) => c.ativa);

  return (
    <div className="space-y-4">
      <PageHeader titulo={editando ? 'Editar investimento' : 'Novo investimento'} />
      {contasAtivas.length === 0 && (
        <Alerta>Cadastre uma conta bancária ativa em <Link to="/contas" className="underline">Contas</Link> antes de registrar investimentos.</Alerta>
      )}
      {erroEnvio && <Alerta detalhes={erroEnvio.detalhes}>{erroEnvio.message}</Alerta>}
      <InvestimentoForm
        inicial={investimento ? paraFormulario(investimento) : undefined}
        contas={contasAtivas}
        editando={editando}
        onSubmit={salvar}
        enviando={enviando}
      />
    </div>
  );
}
```

- [ ] **Passo 4: rodar** — `npm test` PASS; `npm run build` OK; manual: criar com taxa 12,5, conferir "12,5%" e rendimento na tabela, editar para RESGATADO (sai do total investido do dashboard), excluir.
- [ ] **Passo 5: commit** — `git add web/ && git commit -m "feat(web): CRUD de investimentos com taxa em % a.a. e rendimento estimado"`

---

### Task F06: Páginas de Contas bancárias e Categorias

> Revisão 1 (R3): cada controller do backend tem página. Cada tela faz as 4 operações: lista (GET), formulário de criar/editar (POST/PUT) e exclusão com confirmação (DELETE).

**Arquivos:**
- Criar: `web/src/components/contas/ContaForm.jsx`, `web/src/components/categorias/CategoriaForm.jsx`
- Substituir: `web/src/pages/{ContasPage,CategoriasPage}.jsx` (provisórias criadas em F02)
- Teste: `web/src/components/contas/ContaForm.test.jsx`, `web/src/components/categorias/CategoriaForm.test.jsx`

**Interfaces:**
- Consome: `listarContas`, `criarConta`, `atualizarConta`, `excluirConta` (`api/contas.js`); `listarCategorias`, `criarCategoria`, `atualizarCategoria`, `excluirCategoria` (`api/categorias.js`).
- Produz: `ContaForm({inicial, onSubmit, onCancelar, enviando})` → `onSubmit({nmBanco, tipoConta, nrConta, ativa:boolean})`; `CategoriaForm({inicial, onSubmit, onCancelar, enviando})` → `onSubmit({nome, tipo})`. Os dois usam `key` no pai para reiniciar o estado ao trocar o registro em edição.

- [ ] **Passo 1: testes (falham)**

`components/contas/ContaForm.test.jsx`:
```jsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { expect, it, vi } from 'vitest';
import ContaForm from './ContaForm';

it('cria conta ativa por padrão', async () => {
  const onSubmit = vi.fn();
  render(<ContaForm onSubmit={onSubmit} />);

  await userEvent.type(screen.getByLabelText('Banco'), ' Nubank ');
  await userEvent.selectOptions(screen.getByLabelText('Tipo de conta'), 'POUPANCA');
  await userEvent.type(screen.getByLabelText('Número da conta'), '1234-5');
  await userEvent.click(screen.getByRole('button', { name: 'Adicionar conta' }));

  expect(onSubmit).toHaveBeenCalledWith({ nmBanco: 'Nubank', tipoConta: 'POUPANCA', nrConta: '1234-5', ativa: true });
});

it('permite desativar uma conta em edição', async () => {
  const onSubmit = vi.fn();
  render(<ContaForm inicial={{ nmBanco: 'Banco Demo', tipoConta: 'CORRENTE', nrConta: '0001-9', ativa: true }} onSubmit={onSubmit} onCancelar={vi.fn()} />);

  await userEvent.click(screen.getByLabelText('Conta ativa'));
  await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }));

  expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({ ativa: false }));
});
```

`components/categorias/CategoriaForm.test.jsx`:
```jsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { expect, it, vi } from 'vitest';
import CategoriaForm from './CategoriaForm';

it('envia nome sem espaços e o tipo escolhido', async () => {
  const onSubmit = vi.fn();
  render(<CategoriaForm onSubmit={onSubmit} />);

  await userEvent.type(screen.getByLabelText('Nome'), '  Pets  ');
  await userEvent.selectOptions(screen.getByLabelText('Tipo'), 'GASTO');
  await userEvent.click(screen.getByRole('button', { name: 'Adicionar categoria' }));

  expect(onSubmit).toHaveBeenCalledWith({ nome: 'Pets', tipo: 'GASTO' });
});
```
Run: `npm test` → FAIL.

- [ ] **Passo 2: formulários**

`components/contas/ContaForm.jsx`:
```jsx
import { useState } from 'react';
import Botao from '../Botao';
import Campo from '../Campo';

export const TIPOS_CONTA = [['CORRENTE', 'Corrente'], ['POUPANCA', 'Poupança'], ['PAGAMENTO', 'Pagamento'], ['SALARIO', 'Salário'], ['INVESTIMENTO', 'Investimento']];
const VAZIO = { nmBanco: '', tipoConta: 'CORRENTE', nrConta: '', ativa: true };

/** Sem `inicial` cria; com `inicial` edita (e mostra "Conta ativa" e "Cancelar"). */
export default function ContaForm({ inicial, onSubmit, onCancelar, enviando = false }) {
  const editando = Boolean(inicial);
  const [form, setForm] = useState(() => ({ ...VAZIO, ...inicial }));
  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.type === 'checkbox' ? e.target.checked : e.target.value });

  function enviar(e) {
    e.preventDefault();
    onSubmit({ nmBanco: form.nmBanco.trim(), tipoConta: form.tipoConta, nrConta: form.nrConta.trim(), ativa: form.ativa });
  }

  return (
    <form onSubmit={enviar} className="grid gap-4 rounded-2xl bg-white p-6 ring-1 ring-slate-200 sm:grid-cols-3">
      <Campo label="Banco" name="nmBanco" value={form.nmBanco} onChange={alterar} required maxLength={100} />
      <Campo as="select" label="Tipo de conta" name="tipoConta" value={form.tipoConta} onChange={alterar}>
        {TIPOS_CONTA.map(([valor, rotulo]) => <option key={valor} value={valor}>{rotulo}</option>)}
      </Campo>
      <Campo label="Número da conta" name="nrConta" value={form.nrConta} onChange={alterar} required maxLength={20} />
      {editando && (
        <label htmlFor="ativa" className="flex items-center gap-2 text-sm text-slate-700">
          <input id="ativa" name="ativa" type="checkbox" checked={form.ativa} onChange={alterar} className="h-4 w-4 accent-brand" />
          Conta ativa
        </label>
      )}
      <div className="flex justify-end gap-2 sm:col-span-3">
        {editando && <Botao variante="secundario" onClick={onCancelar}>Cancelar</Botao>}
        <Botao type="submit" disabled={enviando}>
          {enviando ? 'Salvando…' : editando ? 'Salvar alterações' : 'Adicionar conta'}
        </Botao>
      </div>
    </form>
  );
}
```

`components/categorias/CategoriaForm.jsx`:
```jsx
import { useState } from 'react';
import Botao from '../Botao';
import Campo from '../Campo';

const VAZIO = { nome: '', tipo: 'GASTO' };

export default function CategoriaForm({ inicial, onSubmit, onCancelar, enviando = false }) {
  const editando = Boolean(inicial);
  const [form, setForm] = useState(() => ({ ...VAZIO, ...inicial }));
  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  function enviar(e) {
    e.preventDefault();
    onSubmit({ nome: form.nome.trim(), tipo: form.tipo });
  }

  return (
    <form onSubmit={enviar} className="grid gap-4 rounded-2xl bg-white p-6 ring-1 ring-slate-200 sm:grid-cols-3 sm:items-end">
      <Campo label="Nome" name="nome" value={form.nome} onChange={alterar} required maxLength={80} />
      <Campo as="select" label="Tipo" name="tipo" value={form.tipo} onChange={alterar}>
        <option value="GASTO">Gasto</option>
        <option value="RECEITA">Receita</option>
      </Campo>
      <div className="flex justify-end gap-2">
        {editando && <Botao variante="secundario" onClick={onCancelar}>Cancelar</Botao>}
        <Botao type="submit" disabled={enviando}>
          {enviando ? 'Salvando…' : editando ? 'Salvar alterações' : 'Adicionar categoria'}
        </Botao>
      </div>
    </form>
  );
}
```

- [ ] **Passo 3: páginas**

`pages/ContasPage.jsx`:
```jsx
import { useCallback, useState } from 'react';
import { atualizarConta, criarConta, excluirConta, listarContas } from '../api/contas';
import Alerta from '../components/Alerta';
import Carregando from '../components/Carregando';
import ConfirmarExclusao from '../components/ConfirmarExclusao';
import ContaForm, { TIPOS_CONTA } from '../components/contas/ContaForm';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';

const rotuloTipo = (valor) => TIPOS_CONTA.find(([v]) => v === valor)?.[1] ?? valor;

export default function ContasPage() {
  const { usuario } = useAuth();
  const buscar = useCallback(() => listarContas(usuario.id), [usuario.id]);
  const { dados: contas, erro, carregando, setDados } = useDados(buscar);
  const [emEdicao, setEmEdicao] = useState(null);
  const [paraExcluir, setParaExcluir] = useState(null);
  const [processando, setProcessando] = useState(false);
  const [mensagem, setMensagem] = useState(null);
  const [erroAcao, setErroAcao] = useState(null);

  async function salvar(valores) {
    setProcessando(true);
    setErroAcao(null);
    setMensagem(null);
    try {
      const corpo = { ...valores, usuarioId: usuario.id };
      if (emEdicao) {
        const atualizada = await atualizarConta(emEdicao.id, corpo);
        setDados(contas.map((c) => (c.id === atualizada.id ? atualizada : c)));
        setMensagem('Conta atualizada com sucesso.');
      } else {
        setDados([...contas, await criarConta(corpo)]);
        setMensagem('Conta cadastrada com sucesso.');
      }
      setEmEdicao(null);
    } catch (e) {
      setErroAcao(e);
    } finally {
      setProcessando(false);
    }
  }

  async function confirmarExclusao() {
    setProcessando(true);
    setErroAcao(null);
    try {
      await excluirConta(paraExcluir.id);
      setDados(contas.filter((c) => c.id !== paraExcluir.id));
      setMensagem('Conta excluída.');
    } catch (e) {
      setErroAcao(e); // 409: conta com lançamentos -> a mensagem da API sugere desativar
    } finally {
      setProcessando(false);
      setParaExcluir(null);
    }
  }

  return (
    <div className="space-y-4">
      <PageHeader titulo="Contas bancárias" descricao="Contas usadas nas receitas, gastos e investimentos" />
      {mensagem && <Alerta tipo="sucesso">{mensagem}</Alerta>}
      {erroAcao && <Alerta detalhes={erroAcao.detalhes}>{erroAcao.message}</Alerta>}
      <ContaForm key={emEdicao?.id ?? 'nova'} inicial={emEdicao ?? undefined} onSubmit={salvar} onCancelar={() => setEmEdicao(null)} enviando={processando} />
      {carregando && <Carregando />}
      {erro && <Alerta>{erro}</Alerta>}
      {contas && (contas.length === 0 ? (
        <p className="rounded-2xl bg-white p-6 text-sm text-slate-500 ring-1 ring-slate-200">Nenhuma conta cadastrada.</p>
      ) : (
        <div className="overflow-x-auto rounded-2xl bg-white ring-1 ring-slate-200">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-slate-500">
              <tr>{['Banco', 'Número', 'Tipo', 'Situação', 'Ações'].map((c) => <th key={c} className="px-4 py-3 font-medium">{c}</th>)}</tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {contas.map((c) => (
                <tr key={c.id}>
                  <td className="px-4 py-3">{c.nmBanco}</td>
                  <td className="px-4 py-3">{c.nrConta}</td>
                  <td className="px-4 py-3">{rotuloTipo(c.tipoConta)}</td>
                  <td className="px-4 py-3">{c.ativa ? 'Ativa' : 'Inativa'}</td>
                  <td className="whitespace-nowrap px-4 py-3">
                    <button type="button" onClick={() => setEmEdicao(c)} className="text-brand hover:underline">Editar</button>
                    <button type="button" onClick={() => setParaExcluir(c)} className="ml-3 text-red-600 hover:underline">Excluir</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ))}
      <ConfirmarExclusao
        aberto={Boolean(paraExcluir)}
        mensagem={`Excluir a conta ${paraExcluir?.nmBanco} · ${paraExcluir?.nrConta}?`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setParaExcluir(null)}
        processando={processando}
      />
    </div>
  );
}
```

`pages/CategoriasPage.jsx`:
```jsx
import { useState } from 'react';
import { atualizarCategoria, criarCategoria, excluirCategoria, listarCategorias } from '../api/categorias';
import Alerta from '../components/Alerta';
import CategoriaForm from '../components/categorias/CategoriaForm';
import Carregando from '../components/Carregando';
import ConfirmarExclusao from '../components/ConfirmarExclusao';
import PageHeader from '../components/PageHeader';
import { useDados } from '../hooks/useDados';

const buscarTodas = () => listarCategorias();

export default function CategoriasPage() {
  const { dados: categorias, erro, carregando, setDados } = useDados(buscarTodas);
  const [emEdicao, setEmEdicao] = useState(null);
  const [paraExcluir, setParaExcluir] = useState(null);
  const [processando, setProcessando] = useState(false);
  const [mensagem, setMensagem] = useState(null);
  const [erroAcao, setErroAcao] = useState(null);

  async function salvar(valores) {
    setProcessando(true);
    setErroAcao(null);
    setMensagem(null);
    try {
      if (emEdicao) {
        const atualizada = await atualizarCategoria(emEdicao.id, valores);
        setDados(categorias.map((c) => (c.id === atualizada.id ? atualizada : c)));
        setMensagem('Categoria atualizada com sucesso.');
      } else {
        setDados([...categorias, await criarCategoria(valores)]);
        setMensagem('Categoria cadastrada com sucesso.');
      }
      setEmEdicao(null);
    } catch (e) {
      setErroAcao(e);
    } finally {
      setProcessando(false);
    }
  }

  async function confirmarExclusao() {
    setProcessando(true);
    setErroAcao(null);
    try {
      await excluirCategoria(paraExcluir.id);
      setDados(categorias.filter((c) => c.id !== paraExcluir.id));
      setMensagem('Categoria excluída.');
    } catch (e) {
      setErroAcao(e); // 409: categoria em uso
    } finally {
      setProcessando(false);
      setParaExcluir(null);
    }
  }

  const grupos = [['RECEITA', 'Categorias de receita'], ['GASTO', 'Categorias de gasto']];

  return (
    <div className="space-y-4">
      <PageHeader titulo="Categorias" descricao="Compartilhadas por todos os usuários" />
      {mensagem && <Alerta tipo="sucesso">{mensagem}</Alerta>}
      {erroAcao && <Alerta detalhes={erroAcao.detalhes}>{erroAcao.message}</Alerta>}
      <CategoriaForm key={emEdicao?.id ?? 'nova'} inicial={emEdicao ?? undefined} onSubmit={salvar} onCancelar={() => setEmEdicao(null)} enviando={processando} />
      {carregando && <Carregando />}
      {erro && <Alerta>{erro}</Alerta>}
      {categorias && (
        <div className="grid gap-4 md:grid-cols-2">
          {grupos.map(([tipo, titulo]) => (
            <section key={tipo} className="rounded-2xl bg-white p-5 ring-1 ring-slate-200">
              <h2 className="mb-2 font-semibold">{titulo}</h2>
              <ul className="divide-y divide-slate-100 text-sm">
                {categorias.filter((c) => c.tipo === tipo).map((c) => (
                  <li key={c.id} className="flex items-center justify-between py-2">
                    <span>{c.nome}</span>
                    <span className="whitespace-nowrap">
                      <button type="button" onClick={() => setEmEdicao(c)} className="text-brand hover:underline">Editar</button>
                      <button type="button" onClick={() => setParaExcluir(c)} className="ml-3 text-red-600 hover:underline">Excluir</button>
                    </span>
                  </li>
                ))}
              </ul>
            </section>
          ))}
        </div>
      )}
      <ConfirmarExclusao
        aberto={Boolean(paraExcluir)}
        mensagem={`Excluir a categoria "${paraExcluir?.nome}"?`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setParaExcluir(null)}
        processando={processando}
      />
    </div>
  );
}
```

- [ ] **Passo 4: rodar** — `npm test` PASS; `npm run build` OK; manual: criar, editar, desativar e excluir uma conta sem lançamentos; tentar excluir a conta do usuário demo (deve aparecer a mensagem 409 sugerindo desativar); criar, renomear e excluir uma categoria; tentar excluir "Moradia" (409, em uso).
- [ ] **Passo 5: commit** — `git add web/ && git commit -m "feat(web): CRUD de contas bancárias e categorias"`

---

### Task F07: Perfil (consultar, atualizar e remover usuário)

**Arquivos:**
- Criar: `web/src/components/perfil/PerfilForm.jsx` (contas bancárias saíram do Perfil e ganharam página própria em F06)
- Substituir: `web/src/pages/PerfilPage.jsx`
- Teste: `web/src/components/perfil/PerfilForm.test.jsx`

**Interfaces:**
- Produz: `PerfilForm({inicial, onSubmit, enviando})` → `onSubmit({nome, email, avatar|null, senha|null, ativo:true})`.

- [ ] **Passo 1: teste (falha)** — `components/perfil/PerfilForm.test.jsx`:
```jsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { expect, it, vi } from 'vitest';
import PerfilForm from './PerfilForm';

const ana = { nome: 'Ana', email: 'ana@email.com', avatar: null };

it('envia senha nula quando o campo fica em branco', async () => {
  const onSubmit = vi.fn();
  render(<PerfilForm inicial={ana} onSubmit={onSubmit} />);

  await userEvent.clear(screen.getByLabelText('Nome'));
  await userEvent.type(screen.getByLabelText('Nome'), 'Ana Maria');
  await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }));

  expect(onSubmit).toHaveBeenCalledWith({ nome: 'Ana Maria', email: 'ana@email.com', avatar: null, senha: null, ativo: true });
});

it('envia a nova senha quando preenchida', async () => {
  const onSubmit = vi.fn();
  render(<PerfilForm inicial={ana} onSubmit={onSubmit} />);

  await userEvent.type(screen.getByLabelText('Nova senha (opcional)'), 'novaSenha1');
  await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }));

  expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({ senha: 'novaSenha1' }));
});
```

- [ ] **Passo 2: componentes**

`components/perfil/PerfilForm.jsx`:
```jsx
import { useState } from 'react';
import Botao from '../Botao';
import Campo from '../Campo';

export default function PerfilForm({ inicial, onSubmit, enviando = false }) {
  const [form, setForm] = useState({ nome: inicial.nome, email: inicial.email, avatar: inicial.avatar ?? '', senha: '' });
  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  function enviar(e) {
    e.preventDefault();
    onSubmit({
      nome: form.nome.trim(),
      email: form.email.trim(),
      avatar: form.avatar.trim() || null,
      senha: form.senha || null,
      ativo: true,
    });
  }

  return (
    <form onSubmit={enviar} className="grid gap-4 rounded-2xl bg-white p-6 ring-1 ring-slate-200 sm:grid-cols-2">
      <Campo label="Nome" name="nome" value={form.nome} onChange={alterar} required maxLength={150} />
      <Campo label="E-mail" name="email" type="email" value={form.email} onChange={alterar} required />
      <Campo label="Avatar (URL, opcional)" name="avatar" value={form.avatar} onChange={alterar} maxLength={500} />
      <Campo label="Nova senha (opcional)" name="senha" type="password" value={form.senha} onChange={alterar} minLength={6} autoComplete="new-password" />
      <div className="flex justify-end sm:col-span-2">
        <Botao type="submit" disabled={enviando}>{enviando ? 'Salvando…' : 'Salvar alterações'}</Botao>
      </div>
    </form>
  );
}
```

- [ ] **Passo 3: página**

`pages/PerfilPage.jsx`:
```jsx
import { useCallback, useState } from 'react';
import { atualizarUsuario, buscarUsuario, excluirUsuario } from '../api/usuarios';
import Alerta from '../components/Alerta';
import Botao from '../components/Botao';
import Carregando from '../components/Carregando';
import ConfirmarExclusao from '../components/ConfirmarExclusao';
import PageHeader from '../components/PageHeader';
import PerfilForm from '../components/perfil/PerfilForm';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';
import { formatarData } from '../utils/formatar';

export default function PerfilPage() {
  const { usuario, atualizarSessao, sair } = useAuth();
  const buscar = useCallback(() => buscarUsuario(usuario.id), [usuario.id]);
  const { dados: perfil, erro, carregando, setDados } = useDados(buscar);
  const [mensagem, setMensagem] = useState(null);
  const [erroAcao, setErroAcao] = useState(null);
  const [salvando, setSalvando] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [excluindo, setExcluindo] = useState(false);

  async function salvar(valores) {
    setSalvando(true);
    setMensagem(null);
    setErroAcao(null);
    try {
      const atualizado = await atualizarUsuario(usuario.id, valores);
      setDados(atualizado);
      atualizarSessao(atualizado);
      setMensagem('Perfil atualizado com sucesso.');
    } catch (e) {
      setErroAcao(e);
    } finally {
      setSalvando(false);
    }
  }

  async function excluirConta() {
    setExcluindo(true);
    try {
      await excluirUsuario(usuario.id);
      sair(); // RotaProtegida leva para /login
    } catch (e) {
      setErroAcao(e);
      setExcluindo(false);
      setConfirmando(false);
    }
  }

  if (carregando) return <Carregando />;
  if (erro) return <Alerta>{erro}</Alerta>;

  return (
    <div className="space-y-6">
      <PageHeader titulo="Meu perfil" descricao={`Cliente desde ${formatarData(perfil.dtCadastro)}`} />
      {mensagem && <Alerta tipo="sucesso">{mensagem}</Alerta>}
      {erroAcao && <Alerta detalhes={erroAcao.detalhes}>{erroAcao.message}</Alerta>}
      <PerfilForm key={perfil.email} inicial={perfil} onSubmit={salvar} enviando={salvando} />
      <section className="rounded-2xl bg-white p-6 ring-1 ring-red-200">
        <h2 className="font-semibold text-red-700">Excluir conta</h2>
        <p className="mt-1 text-sm text-slate-600">Remove seu cadastro, contas bancárias, receitas, gastos e investimentos.</p>
        <Botao variante="perigo" className="mt-4" onClick={() => setConfirmando(true)}>Excluir minha conta</Botao>
      </section>
      <ConfirmarExclusao
        aberto={confirmando}
        titulo="Excluir sua conta?"
        mensagem="Todos os seus dados serão apagados permanentemente."
        onConfirmar={excluirConta}
        onCancelar={() => setConfirmando(false)}
        processando={excluindo}
      />
    </div>
  );
}
```

- [ ] **Passo 4: rodar** — `npm test` PASS; `npm run build` OK; manual: alterar nome (o menu mostra o novo nome); cadastrar um usuário novo pela tela de cadastro, criar uma conta em `/contas` e uma receita e um gasto para ele, e excluir a conta dele pelo perfil (volta ao login; login com ele agora dá 401).
- [ ] **Passo 5: commit** — `git add web/ && git commit -m "feat(web): perfil com consulta, edição e exclusão do usuário"`

---

### Task E01: Documentação de entrega, verificação ponta a ponta e ZIP

**Arquivos:**
- Substituir: `README.md` (raiz) — documento de entrega
- Modificar: `AGENTS.md` (estrutura, rastreabilidade, como executar)
- Criar: `docs/07-api-spring-react/evidencias/teste-ponta-a-ponta.md`
- Modificar: `tasks/fase8/README.md` (status e checklist)

- [ ] **Passo 1: resolver as pendências do painel** (credenciais no ZIP, nome do ZIP e nome da fase) **com a Larissa** antes de gerar o ZIP.

- [ ] **Passo 2: reescrever `README.md`.** O avaliador lê este arquivo primeiro, então cada requisito do enunciado precisa ser encontrável nele. Seções:
  1. **Sistema Fintech** — uma frase + autora/RM/turma.
  2. **Estrutura da entrega** — `api/` (Spring Boot), `web/` (React), `database/ddl/` (scripts das tabelas).
  3. **Pré-requisitos** — JDK 21, Node **20.19+** (exigência do Vite atual) e npm, acesso ao Oracle FIAP. Não precisa instalar Maven (o projeto traz o Maven Wrapper).
  4. **Tabelas na instância Oracle da FIAP** (R6) — as 10 tabelas `T_SF_*` foram criadas na FIAP com `database/ddl/01-create-tables.sql` (para recriar num schema vazio: executar esse script no SQL Developer). A tabela abaixo mapeia cada entidade JPA para sua tabela; a API sobe com `ddl-auto=validate`, ou seja, **só inicia se cada entidade bater com a tabela da FIAP**.
     | Entidade | Tabela |
     |---|---|
     | Usuario | T_SF_USUARIO |
     | ContaBancaria | T_SF_CONTA_BANCARIA |
     | Categoria | T_SF_CATEGORIA |
     | Receita | T_SF_RECEITA |
     | Gasto | T_SF_GASTO |
     | Investimento | T_SF_INVESTIMENTO |
  5. **Backend** — a URL e o usuário da FIAP já estão em `api/src/main/resources/application.properties`. Informar a senha em `api/application-local.properties` (modelo: `.example`) ou na variável `SPRING_DATASOURCE_PASSWORD`. Se o ZIP já trouxer o arquivo (pendência 1), basta executar:
     ```bash
     cd api
     ./mvnw spring-boot:run
     ```
     No Windows: `.\mvnw.cmd spring-boot:run`, com `JAVA_HOME` apontando para o JDK 21. A API fica em `http://localhost:8080/api`. Também abre no IntelliJ como projeto Maven (executar `FintechApplication`).
  6. **Frontend**:
     ```bash
     cd web
     npm install
     npm run dev
     ```
     Abre em `http://localhost:5173` (a porta é fixa por causa do CORS).
  7. **Usuário de teste (login)** — e-mail `demo@fintech.com`, senha `Fintech@2026`. Criado na primeira subida da API e redefinido nas seguintes; já vem com conta, receitas, gastos e investimentos.
  8. **Arquitetura do backend** — camadas `model` → `repository` → `service` → `controller` (+ `dto`, `exception`, `config`) e a tabela "Regras de negócio por service" deste plano.
  9. **Endpoints e códigos HTTP** — a tabela "Contrato REST" + o resumo verbo → status: GET 200/404 · POST 201 + `Location` · PUT 200 · DELETE 204 · 400 validação · 401 login · 409 conflito · 422 regra de negócio.
  10. **Frontend: páginas e rotas** — a tabela "Mapa controller → página" + rotas públicas (`/login`, `/cadastro`), página inicial (`/`) e página de erro (qualquer rota inexistente).
  11. **Componentização, props e estado** — onde cada coisa aparece: componentes reutilizáveis (`Campo`, `Botao`, `Alerta`, `ConfirmarExclusao`, `CardResumo`); props pai → filho (`categorias`, `contas`, `inicial`) e filho → pai (`onSubmit`, `onExcluir`); estado com `useState`, efeito com `useEffect` (`useDados`), contexto global de sessão (`AuthContext` + `useAuth`).
  12. **Testes** — `./mvnw test` (unitários + MockMvc) e `npm test`; `./mvnw test -Dit.oracle=true` roda também a integração contra o Oracle.
  13. **Decisões e limitações** — D-F8-03 (sem token), D-F8-06 (exclusão de usuário), categorias compartilhadas, Metas/Dívidas fora do escopo desta entrega.

- [ ] **Passo 3: atualizar `AGENTS.md`** — árvore com `api/` e `web/`, tabela de rastreabilidade (colunas novas "Controller/API" e "Página React"), seção "Como executar" e convenções Spring/React. Marcar `backend/` e `frontend/` como fases anteriores e remover a lacuna "Receita não tem DAO" (agora tem repository).

- [ ] **Passo 4: verificação ponta a ponta na FIAP** (API apontando para a FIAP + `npm run dev`), registrando em `docs/07-api-spring-react/evidencias/teste-ponta-a-ponta.md` (tabela "passo → resultado", com prints das telas principais):
  1. login com senha errada → "E-mail ou senha inválidos"; login com o usuário demo → página inicial com saldo, gastos por categoria e últimas transações;
  2. **para cada página de controller** (receitas, gastos, investimentos, contas, categorias): consultar a lista → inserir → editar → excluir;
  3. investimentos: editar para "Resgatado" e confirmar que sai do "Total investido";
  4. contas: excluir a conta com lançamentos (mensagem 409) → desativar → ela some dos formulários de lançamento;
  5. categorias: excluir uma categoria em uso (mensagem 409);
  6. cadastro: criar usuário novo → perfil: editar nome → excluir conta → volta ao login;
  7. `/rota-inexistente` → página 404 personalizada; sair → `/gastos` redireciona ao login.

- [ ] **Passo 5: rodar todas as suítes**
```bash
cd api && JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot" ./mvnw -q test -Dit.oracle=true
cd ../web && npm test && npm run build
```
Esperado: tudo verde. Registrar as contagens de testes nas notas do painel.

- [ ] **Passo 6: montar o ZIP** a partir de uma cópia limpa do que está no git (garante que não vão `node_modules`, `target`, `dist`), **incluindo o DDL** (R6):
```bash
rm -rf "$TMPDIR/entrega" && mkdir -p "$TMPDIR/entrega"
git archive --format=tar HEAD api web database/ddl README.md | tar -x -C "$TMPDIR/entrega"
# Só se a Larissa decidir incluir as credenciais (pendência do painel):
# cp api/application-local.properties "$TMPDIR/entrega/api/"
cd "$TMPDIR/entrega" && powershell.exe -Command "Compress-Archive -Path api,web,database,README.md -DestinationPath RM571266.zip -Force"
```
Conferir:
- `unzip -l RM571266.zip | grep -c node_modules` → `0`;
- presentes: `web/package.json`, `web/package-lock.json`, `api/mvnw`, `api/mvnw.cmd`, `api/.mvn/`, `database/ddl/01-create-tables.sql`;
- `api/mvnw` com LF: `file "$TMPDIR/entrega/api/mvnw"` não pode mencionar `CRLF` (R8);
- nenhuma senha em `application.properties`: `grep -i password "$TMPDIR/entrega/api/src/main/resources/application.properties"` → vazio.

- [ ] **Passo 7: validar o ZIP em pasta limpa** — extrair em pasta nova do scratchpad, seguir **apenas** o README (`./mvnw spring-boot:run`, `npm install && npm run dev`) e entrar com o usuário de teste. Só entregar se funcionar sem nenhum passo fora do README.

- [ ] **Passo 8: checklist final do painel** (copiar para `tasks/fase8/README.md` e marcar):
  - [ ] B1 — Entidades JPA: Usuario, ContaBancaria, Categoria, Receita, Gasto, Investimento
  - [ ] B2 — Repository JPA para cada entidade (6)
  - [ ] B3 — Service com regras de negócio para cada repository (6 + resumo)
  - [ ] B4 — Todo RestController com GET, POST, PUT e DELETE
  - [ ] B5 — Status HTTP por verbo (200/201/204/400/401/404/409/422) cobertos por testes MockMvc
  - [ ] B6 — Tabelas na instância FIAP (DDL no ZIP + consulta `USER_TABLES` na evidência)
  - [ ] B7 — API conectada à FIAP (`application.properties` + log de subida na evidência)
  - [ ] B8 — ≥ 3 entidades com todas as camadas (4 com CRUD de lançamento: Receita, Gasto, Investimento + Usuario)
  - [ ] F1 — Componentização
  - [ ] F2 — Rotas SPA
  - [ ] F3 — Props e estado (hooks) entre componentes
  - [ ] F4 — Página de login
  - [ ] F5 — Página inicial e página de erro personalizada
  - [ ] F6 — Página para cada controller (tabela "Mapa controller → página" no README)
  - [ ] F7 — Front consome a API REST
  - [ ] E1 — ZIP com `api/`, `web/`, `database/ddl/` e README; sem `node_modules`; com `package.json` e `package-lock.json`
  - [ ] E1c — README com inicialização (back e front) e usuário de teste
  - [ ] E3 — ZIP validado em pasta limpa e enviado na plataforma

- [ ] **Passo 9: commit e PR** (PR é ação externa: confirmar com a Larissa)
```bash
git add README.md AGENTS.md docs/07-api-spring-react/ tasks/fase8/
git commit -m "docs: documentação de entrega e evidências da Fase 8"
```

---

## Autoverificação do plano (revisão 1)

| Requisito do enunciado | Onde |
|---|---|
| B1 — classes de modelo/entidades | B01 (6 entidades, incluindo Receita — user story 2) |
| B2 — Repository JPA por entidade | B01 (+ consultas de soma em B07) |
| B3 — Service com regras de negócio por repository | B02 Usuario/Auth · B03 Conta e Categoria · B04 Gasto · B05 Receita · B06 Investimento · B07 Resumo (tabela "Regras de negócio por service") |
| B4 — RestController GET/POST/PUT/DELETE | B02, B03, B04, B05, B06 (todos os controllers com os 4 verbos) |
| B5 — status por verbo | Contrato REST + testes MockMvc em B02–B07 |
| B6 — tabelas na FIAP | DDL aplicado (Fase 7) + `database/ddl/` no ZIP + evidência `USER_TABLES` (B08) |
| B7 — conexão obrigatória à FIAP | `application.properties` (B01) + execução e log (B08) + E2E (E01) |
| B8 — mínimo 3 entidades | Receita, Gasto, Investimento (+ Usuario, Conta, Categoria) |
| F1 — componentização | F02 (base) + F03–F07 (por domínio) |
| F2 — rotas SPA | F02 (`router.jsx`) |
| F3 — props e estado (hooks) | formulários controlados, `onSubmit`/`onExcluir`, `useDados`, `AuthContext`; seção 11 do README |
| F4 — login | F02 |
| F5 — página inicial e de erro | F03 (resumo da API), F02 (`ErroPage` em `*` e `errorElement`) |
| F6 — páginas por controller | Mapa controller → página: Auth/Usuario (F02, F07), Receita (F04), Gasto (F03), Investimento (F05), Conta e Categoria (F06) |
| F7 — consumo da API | F01 (`api/*`) usado em todas as páginas |
| Entregáveis | E01 (README em 13 seções, ZIP com DDL, validação em pasta limpa) |

Riscos conhecidos:
- **Validação do schema** (tipos `NUMBER(10)`/`CHAR(1)`): coberta por `SchemaOracleTest`, com a correção indicada em B01 passo 9.
- **Instância FIAP compartilhada**: `SchemaOracleTest` faz rollback; o `DadosDemo` só cria dados de exemplo uma vez; não usar senha errada repetidamente na conexão do banco (risco de ORA-28000, ver Fase 7).
- **Avaliador sem senha da FIAP**: sem o `application-local.properties` no ZIP, a API não sobe (pendência 1 do painel).
- **Sem autenticação por token** (D-F8-03): qualquer cliente pode chamar a API com outro `usuarioId`. Aceitável para o escopo acadêmico e documentado no README.
