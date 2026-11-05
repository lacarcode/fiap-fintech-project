# Plano de Tarefas — Fase 8: API Spring Boot + Front-end React

Plano detalhado (código, testes e comandos de cada tarefa): [`plano-implementacao.md`](plano-implementacao.md).
Revisão crítica contra o enunciado (matriz requisito × plano, correções R1–R11): [`revisao-critica.md`](revisao-critica.md).

Entidades com CRUD completo (back + front): **Usuario, Receita, Gasto, Investimento, ContaBancaria, Categoria**.
Entidades financeiras com CRUD (mínimo exigido = 3): Receita, Gasto e Investimento.
Banco: DDL atual (`database/ddl/01-create-tables.sql`) aplicado na FIAP; o JPA só **valida** (`ddl-auto=validate`).
Entrega: ZIP com `api/`, `web/` (sem `node_modules`), `database/ddl/` e `README.md`.
Branch: `feat/spring-react` (criada a partir da `main` depois do merge da Fase 7).

Status: `⬜ A fazer` · `🟨 Em andamento` · `🟥 Bloqueada` · `✅ Concluída`

| ID  | Tarefa | Depende de | Status |
|-----|--------|------------|--------|
| P00 | Integrar Fase 7 na `main`, criar branch, `.gitignore` e `.gitattributes` | — | ✅ |
| B01 | Projeto Spring Boot + 6 entidades JPA + repositories (validados contra o DDL) | P00 | ✅ |
| B02 | Tratamento de erros + Usuario (service, login, controller) | B01 | ✅ |
| B03 | ContaBancaria e Categoria — CRUD completo com regras | B02 | ✅ |
| B04 | Gasto (DTOs, service, controller) | B03 | ✅ |
| B05 | Receita (DTOs, service, controller) | B03 | ✅ |
| B06 | Investimento (DTOs, service, controller) | B03 | ✅ |
| B07 | Resumo financeiro (`GET /api/usuarios/{id}/resumo`) | B04, B05, B06 | ⬜ |
| B08 | Dados de demonstração + execução na FIAP + evidências | B07 | ⬜ |
| F01 | Projeto React (Vite, Tailwind, Router, Vitest) + cliente da API + AuthContext | P00 | ✅ |
| F02 | Componentes base, layout, rotas, Login, Cadastro e página de erro | F01 | ✅ |
| F03 | Página inicial (resumo) + páginas de Gastos | F02 | ⬜ |
| F04 | Páginas de Receitas | F03 | ⬜ |
| F05 | Páginas de Investimentos | F02 | ⬜ |
| F06 | Páginas de Contas bancárias e Categorias | F02 | ⬜ |
| F07 | Perfil (consultar/editar/excluir usuário) | F02 | ⬜ |
| E01 | README de entrega, AGENTS.md, teste ponta a ponta e ZIP validado | B08, F03–F07 | ⬜ |

Trilhas paralelas: **B01→B08** (back) e **F01→F07** (front) só se encontram em E01.
O contrato da API (seção "Contrato REST" do plano) é o que as duas trilhas compartilham.

## Decisões
- D-F8-01 — Manter o DDL atual; Hibernate com `ddl-auto=validate` (nunca cria nem apaga tabela).
- D-F8-02 — Projetos novos em `api/` e `web/`; `backend/` (JDBC, Fase 7) e `frontend/` (protótipo HTML) ficam como histórico.
- D-F8-03 — Login simples: `POST /api/auth/login` confere o hash BCrypt (só `spring-security-crypto`, sem Spring Security/JWT); a sessão fica no front (Context + localStorage). Limitação conhecida: a API não verifica posse dos dados.
- D-F8-04 — Pacotes por camada (`model`, `repository`, `service`, `controller`, `dto`), porque o enunciado avalia as camadas explicitamente.
- D-F8-05 — Usuário de teste garantido na subida da API (`DadosDemo`), com senha configurável; resolve a falta de hash conhecido na FIAP.
- D-F8-06 — `DELETE /api/usuarios/{id}` remove receitas, gastos, investimentos e contas do usuário na mesma transação; se houver metas/dívidas, responde 409.
- D-F8-07 (revisão) — Todo controller tem GET/POST/PUT/DELETE e página no front (R2/R3); Receita entra como 4ª entidade com CRUD (R1).
- D-F8-08 (revisão) — URL e usuário da FIAP ficam no `application.properties`; só a senha fica fora do git (R5).
- D-F8-09 (revisão) — A página inicial é `/` (dashboard logado), alimentada por `GET /api/usuarios/{id}/resumo` (R4).

## Pendências para decidir antes de E01
- [ ] Credenciais da FIAP no ZIP. **Recomendado:** incluir `api/application-local.properties` só no ZIP (nunca no git), como na Fase 7; sem isso, o avaliador não consegue subir a API.
- [ ] Nome do ZIP (`RM571266.zip`, como na Fase 7, ou o padrão pedido na plataforma).
- [ ] Confirmar se esta atividade é mesmo a "Fase 8" (nomes de pasta `tasks/fase8/` e `docs/07-api-spring-react/`).

## Notas de execução
- P00 (2026-11-04): branch `feat/spring-react` criada a partir da `feat/dao-jdbc` (o PR da Fase 7 ainda não foi aberto; depende de confirmação da Larissa). Se a Fase 7 for mesclada antes, basta rebasear.
- B01: o Spring Initializr só aceita Boot >= 4.0 hoje; ele foi usado só para obter o Maven Wrapper (3.10.0). O `pom.xml` fixa Boot 3.5.7. `SchemaOracleTest` passou no Oracle local com `ddl-auto=validate` **sem** `columnDefinition` extra nos ids (o risco previsto não ocorreu). 2 testes unitários + 1 de integração.
- F01: Vite 8 + React 19 (exige Node 20.19+; máquina com Node 26). 4 testes passando; build OK. `oxlint` acusa 2 avisos de boas práticas (setState em efeito no `useDados`; `AuthContext` exporta hook e componente) — não bloqueiam.
- B02: 22 testes na API (1 de integração ignorado sem `-Dit.oracle=true`). Verificado com curl no Oracle local: POST 201 + `Location`, e-mail duplicado 409, validação 400 com detalhes, 404, login 200, CORS liberado para a 5173, DELETE 204 e GET seguinte 404; a senha nunca aparece na resposta.
- F02: 7 testes no web. Verificado no navegador com API real: `/gastos` sem login redireciona para `/login`; cadastro cria o usuário, entra sozinho e cai em `/` com a sessão salva; rota inexistente mostra o 404 personalizado; "Sair" limpa a sessão. Páginas de domínio ainda são provisórias ("Em construção") até F03–F07.
- B03–B06: 93 testes na API (1 de integração ignorado sem `-Dit.oracle=true`); `./mvnw test` verde. Bateria real no Oracle local (POST/GET/PUT/DELETE nos 4 controllers + regras): 201/200/204/404 nos ciclos; 422 para conta de outro usuário, categoria de tipo errado, tipo de conta inválido, vencimento anterior à aplicação, PUT transferindo dono; 409 para categoria duplicada, conta com lançamentos e categoria em uso; 400 para valor negativo. Normalização de acentos confirmada (`Variável`→`VARIAVEL`, `Salário`→`SALARIO`).
- Armadilha de ambiente: no Git Bash do Windows, `curl -d` com acento envia bytes fora de UTF-8 e a API responde 400 (comportamento correto). Em testes manuais com acento, usar `--data-binary @arquivo.json` (UTF-8).
