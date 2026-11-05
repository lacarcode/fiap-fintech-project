# Sistema Fintech — API Spring Boot + SPA React

Gestão financeira pessoal: o usuário se cadastra, mantém contas bancárias e registra **receitas, gastos e
investimentos**, e acompanha o saldo do mês na página inicial.

**Autora:** Larissa Gomes de Carvalho — RM 571266 — Turma 1TDSOA (FIAP)

## Sumário

1. [Estrutura da entrega](#1-estrutura-da-entrega)
2. [Pré-requisitos](#2-pré-requisitos)
3. [Tabelas na instância Oracle da FIAP](#3-tabelas-na-instância-oracle-da-fiap)
4. [Como iniciar o backend](#4-como-iniciar-o-backend)
5. [Como iniciar o frontend](#5-como-iniciar-o-frontend)
6. [Usuário de teste (login)](#6-usuário-de-teste-login)
7. [Arquitetura do backend e regras de negócio](#7-arquitetura-do-backend-e-regras-de-negócio)
8. [Endpoints e códigos HTTP](#8-endpoints-e-códigos-http)
9. [Frontend: páginas e rotas](#9-frontend-páginas-e-rotas)
10. [Componentização, props e estado](#10-componentização-props-e-estado)
11. [Testes](#11-testes)
12. [Decisões e limitações](#12-decisões-e-limitações)

## 1. Estrutura da entrega

```
api/            Backend: Java 21 + Spring Boot 3.5 + Spring Data JPA (Maven Wrapper incluído)
web/            Frontend: React 19 + Vite + React Router + Tailwind CSS (sem node_modules; ver package.json e package-lock.json)
database/ddl/   Scripts SQL que criam as tabelas na instância Oracle (00-drop-tables.sql e 01-create-tables.sql)
README.md       Este arquivo
```

## 2. Pré-requisitos

| Item | Versão | Observação |
|---|---|---|
| JDK | **21** | Defina `JAVA_HOME` apontando para o JDK 21 (a atividade aceita 17 ou 21) |
| Node.js | **20.19 ou superior** | Exigência do Vite atual; vem com o `npm` |
| Acesso à rede da FIAP | — | O backend conecta em `ORACLE.FIAP.COM.BR:1521` |

Não é preciso instalar o Maven: o projeto traz o Maven Wrapper (`api/mvnw` e `api/mvnw.cmd`).

## 3. Tabelas na instância Oracle da FIAP

As 10 tabelas `T_SF_*` do modelo foram criadas na instância da FIAP com `database/ddl/01-create-tables.sql`
(para recriar em um schema vazio, basta executar esse script no SQL Developer).

A API usa `spring.jpa.hibernate.ddl-auto=validate`: o Hibernate **só confere** se cada entidade bate com a
tabela existente, sem criar nem alterar nada. Se alguma entidade divergir da tabela, a API não sobe.

| Entidade JPA (`api/.../model`) | Tabela | Tem CRUD completo (API e telas) |
|---|---|---|
| `Usuario` | `T_SF_USUARIO` | sim |
| `ContaBancaria` | `T_SF_CONTA_BANCARIA` | sim |
| `Categoria` | `T_SF_CATEGORIA` | sim |
| `Receita` | `T_SF_RECEITA` | sim |
| `Gasto` | `T_SF_GASTO` | sim |
| `Investimento` | `T_SF_INVESTIMENTO` | sim |

As tabelas de dívidas, parcelas, metas e aportes existem no banco (modelo completo), mas ficam fora desta
entrega, que cobre mais de 3 entidades de ponta a ponta (backend e frontend).

A conexão está em `api/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:oracle:thin:@ORACLE.FIAP.COM.BR:1521:ORCL
spring.datasource.username=RM571266
```

A **senha** não fica nesse arquivo (ver passo 4).

## 4. Como iniciar o backend

1. **Senha do banco.** Informe a senha de `RM571266` de uma das duas formas:
   - arquivo `api/application-local.properties` (modelo: `api/application-local.properties.example`) com a linha
     `spring.datasource.password=SUA_SENHA`; **se o ZIP já trouxer esse arquivo, não precisa fazer nada**; ou
   - variável de ambiente `SPRING_DATASOURCE_PASSWORD`.
2. **Suba a API:**

   ```bash
   cd api
   ./mvnw spring-boot:run
   ```

   No Windows (PowerShell ou CMD): `.\mvnw.cmd spring-boot:run`. A API fica em `http://localhost:8080/api`.
   Também abre no IntelliJ como projeto Maven (executar a classe `FintechApplication`).
3. **Aguarde esta linha no log** antes de fazer login:
   `DadosDemo : Usuário de teste demo@fintech.com criado...` (ou `... já existe: senha e status redefinidos`).
   Ela é gravada cerca de meio segundo depois de a porta abrir; um login feito nesse instante responde 401.

## 5. Como iniciar o frontend

Em outro terminal, com o backend no ar:

```bash
cd web
npm install
npm run dev
```

Abra `http://localhost:5173`. A porta é fixa (o CORS da API só libera essa origem); se ela estiver ocupada, o
Vite avisa em vez de trocar de porta. Se a API estiver em outro endereço, copie `web/.env.example` para
`web/.env.local` e ajuste `VITE_API_URL`.

## 6. Usuário de teste (login)

| Campo | Valor |
|---|---|
| E-mail | `demo@fintech.com` |
| Senha | `Fintech@2026` |

Esse usuário é criado na primeira subida da API e **redefinido a cada subida** (a senha volta a ser a acima),
com 1 conta bancária, 2 receitas, 3 gastos e 2 investimentos de exemplo. Também é possível criar outro usuário
na tela de cadastro (`/cadastro`).

## 7. Arquitetura do backend e regras de negócio

Pacote `br.com.fiap.fintech`, uma camada por pacote:

```
controller/   @RestController: recebe HTTP, valida o corpo (@Valid) e devolve o status correto
service/      Regras de negócio (@Service, @Transactional)
repository/   Spring Data JPA (interfaces JpaRepository)
model/        Entidades JPA mapeadas nas tabelas T_SF_*
dto/          Objetos de entrada e saída da API (a senha nunca é devolvida)
exception/    Exceções de negócio e GlobalExceptionHandler (converte em resposta HTTP)
config/       CORS, BCrypt e carga do usuário de teste
```

| Service | Regras de negócio |
|---|---|
| `UsuarioService` | E-mail único e normalizado em minúsculas (409); senha guardada só como hash BCrypt; PUT sem senha mantém a atual; excluir o usuário remove também suas receitas, gastos, investimentos e contas |
| `AuthService` | Login só para usuário ativo; a mesma mensagem para e-mail ou senha inválidos (401) |
| `ContaBancariaService` | Tipo de conta do domínio (422); a conta não muda de dono (422); não é excluída se tiver lançamentos (409, a mensagem sugere desativar); lançamentos só usam conta **ativa** do próprio usuário (422) |
| `CategoriaService` | Tipo RECEITA ou GASTO (422); nome único por tipo (409); não é excluída se estiver em uso (409); o tipo de uma categoria em uso não muda (422) |
| `ReceitaService` | Categoria precisa ser do tipo RECEITA; origem e recorrência do domínio; recorrência padrão UNICA; não muda de dono |
| `GastoService` | Categoria precisa ser do tipo GASTO; tipo FIXO ou VARIAVEL (aceita "Variável"); não muda de dono |
| `InvestimentoService` | Tipo do domínio; vencimento não pode ser anterior à aplicação; status padrão ATIVO; resgate pelo PUT com status RESGATADO; rendimento anual estimado = valor × taxa |
| `ResumoFinanceiroService` | Saldo do mês = receitas − gastos; total investido e rendimento só de investimentos ATIVO; gastos agrupados por categoria |

Valores de domínio são normalizados (`Dominios.normalizar`: "Variável" → `VARIAVEL`), dinheiro usa `BigDecimal`
e as taxas são frações anuais (0,1250 = 12,5% a.a.).

## 8. Endpoints e códigos HTTP

Base: `http://localhost:8080/api`. Todo controller expõe **GET (lista e por id), POST, PUT e DELETE**.

| Recurso | Rotas |
|---|---|
| Login | `POST /auth/login` |
| Usuários | `GET /usuarios` · `GET /usuarios/{id}` · `POST /usuarios` · `PUT /usuarios/{id}` · `DELETE /usuarios/{id}` · `GET /usuarios/{id}/resumo?mes=AAAA-MM` |
| Receitas | `GET /receitas?usuarioId=` · `GET /receitas/{id}` · `POST /receitas` · `PUT /receitas/{id}` · `DELETE /receitas/{id}` |
| Gastos | `GET /gastos?usuarioId=` · `GET /gastos/{id}` · `POST /gastos` · `PUT /gastos/{id}` · `DELETE /gastos/{id}` |
| Investimentos | `GET /investimentos?usuarioId=` · `GET /investimentos/{id}` · `POST /investimentos` · `PUT /investimentos/{id}` · `DELETE /investimentos/{id}` |
| Contas | `GET /contas?usuarioId=` · `GET /contas/{id}` · `POST /contas` · `PUT /contas/{id}` · `DELETE /contas/{id}` |
| Categorias | `GET /categorias?tipo=` · `GET /categorias/{id}` · `POST /categorias` · `PUT /categorias/{id}` · `DELETE /categorias/{id}` |

| Verbo / situação | Status |
|---|---|
| GET com sucesso | `200 OK` |
| POST com sucesso | `201 Created` + cabeçalho `Location` com a URL do novo recurso |
| PUT com sucesso | `200 OK` com o recurso atualizado |
| DELETE com sucesso | `204 No Content` |
| Recurso inexistente | `404 Not Found` |
| Campos inválidos ou JSON malformado | `400 Bad Request` (lista os campos em `detalhes`) |
| Login com e-mail ou senha inválidos | `401 Unauthorized` |
| E-mail, nome ou registro em conflito (duplicado, conta/categoria em uso) | `409 Conflict` |
| Regra de negócio violada | `422 Unprocessable Entity` |

Todo erro tem o corpo `{"status": 422, "mensagem": "...", "detalhes": []}`.

## 9. Frontend: páginas e rotas

SPA com React Router. As rotas `/login` e `/cadastro` são públicas; as demais exigem login e redirecionam para
`/login` quando não há sessão. **A página inicial é `/`** e qualquer endereço inexistente mostra a **página de erro
personalizada** (404).

| Controller do backend | Página(s) React | Verbos usados pela tela |
|---|---|---|
| `AuthController` | `/login` | POST |
| `UsuarioController` | `/cadastro` (inserir), `/perfil` (consultar, atualizar, remover), `/` (resumo do mês) | POST, GET, PUT, DELETE |
| `ReceitaController` | `/receitas`, `/receitas/novo`, `/receitas/:id/editar` | GET, POST, PUT, DELETE |
| `GastoController` | `/gastos`, `/gastos/novo`, `/gastos/:id/editar` | GET, POST, PUT, DELETE |
| `InvestimentoController` | `/investimentos`, `/investimentos/novo`, `/investimentos/:id/editar` | GET, POST, PUT, DELETE |
| `ContaBancariaController` | `/contas` | GET, POST, PUT, DELETE |
| `CategoriaController` | `/categorias` | GET, POST, PUT, DELETE |

## 10. Componentização, props e estado

```
web/src/
  api/          Uma função por endpoint (fetch) + client.js (erros da API viram ApiError)
  contexts/     AuthContext: sessão do usuário (useAuth)
  hooks/        useDados: carrega dados assíncronos (loading, erro, recarregar)
  components/   Layout, RotaProtegida, Campo, Botao, Alerta, CardResumo, ConfirmarExclusao, PageHeader, ...
                e subpastas por domínio (gastos/, receitas/, investimentos/, contas/, categorias/, perfil/)
  pages/        Uma página por tela, ligada ao router em router.jsx
  utils/        Formatação de moeda, data e percentual
```

- **Props, do pai para o filho:** `GastoForm` recebe `inicial`, `categorias`, `contas` e `enviando`; `Campo` recebe
  `label`, `name` e as propriedades do campo.
- **Props, do filho para o pai (callbacks):** `GastoForm` chama `onSubmit(valores)`; `TabelaGastos` chama
  `onExcluir(gasto)`; `ConfirmarExclusao` chama `onConfirmar` e `onCancelar`.
- **Estado local (`useState`):** valores dos formulários, item a excluir, mensagens de sucesso e erro.
- **Efeitos (`useEffect`):** `useDados` busca os dados na API ao abrir cada página.
- **Estado global (Context):** `AuthContext` guarda o usuário logado (persistido em `localStorage`) e expõe
  `entrar`, `sair` e `atualizarSessao`; `RotaProtegida` lê esse contexto.

## 11. Testes

```bash
cd api && ./mvnw test                      # 97 testes unitários e de controller (MockMvc)
cd api && ./mvnw test -Dit.oracle=true     # inclui o teste que valida as entidades contra o Oracle (98 testes)
cd web && npm test                         # 19 testes (Vitest + Testing Library)
```

O teste `-Dit.oracle=true` usa a conexão configurada no passo 4 (grava dados de teste e desfaz ao final).

## 12. Decisões e limitações

- **Tabelas existentes, sem alteração automática:** o DDL validado nas fases anteriores é a fonte da verdade e o
  Hibernate só valida o mapeamento (`ddl-auto=validate`).
- **Login simples, sem token:** `POST /auth/login` confere o hash BCrypt e o React guarda o usuário no navegador.
  Não há JWT nem Spring Security; portanto a API não verifica quem faz cada chamada (limitação aceita para o
  escopo acadêmico).
- **Categorias são compartilhadas** por todos os usuários (a tabela não tem dono); editar uma categoria vale para todos.
- **Exclusão de usuário em cascata:** remove contas, receitas, gastos e investimentos do usuário; dívidas e metas
  ficam fora desta entrega.
- **Senha do banco fora do código:** a URL e o usuário da FIAP estão no `application.properties`; a senha é
  informada por `application-local.properties` ou variável de ambiente.
