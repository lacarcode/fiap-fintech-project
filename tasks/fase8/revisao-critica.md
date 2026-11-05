# Revisão crítica do plano da Fase 8 (revisão 1)

Objetivo: confrontar o plano com **cada frase** do enunciado e com as fases anteriores, e corrigir o que
um avaliador rigoroso poderia descontar. Legenda de risco: 🔴 alto (pode perder ponto) · 🟡 médio
(depende da interpretação do avaliador) · 🟢 baixo (qualidade/robustez).

## 1. Matriz requisito → plano original → veredito

| # | Requisito (texto do enunciado) | Plano original | Veredito |
|---|---|---|---|
| B1 | "classes de modelos que representem o seu projeto do Fintech **que já desenvolveu até agora**" | 5 entidades: Usuario, Gasto, Investimento, ContaBancaria, Categoria | 🟡 Faltava **Receita**, que é a user story nº 2 do MVP e tem tela no protótipo (`receitas.html`). Investimento não é user story (entrou na Fase 6). |
| B2 | "Repository com JPA para cada entidade" | 1 por entidade | ✅ |
| B3 | "service com as regras de negócio… **para cada repository**" | Categoria só listava; Conta só listava/criava | 🟡 Service de Categoria sem regra de negócio. |
| B4 | "endpoints REST… com os métodos GET, POST, PUT, DELETE" | CRUD completo só para Usuario, Gasto e Investimento; Conta (GET/POST) e Categoria (GET) incompletos | 🔴 Um controller sem PUT/DELETE pode ser lido como requisito não atendido. |
| B5 | "código de status esperado para verbo HTTP" | 200/201+Location/204/400/401/404/409/422 com testes MockMvc | ✅ Falta só documentar a tabela verbo → status no README. |
| B6 | "Criar as tabelas na instância Oracle da FIAP, de acordo com as entidades" | DDL aplicado na Fase 7; **o DDL não ia no ZIP** | 🔴 O avaliador não tem como ver nem recriar as tabelas. |
| B7 | "Conectar obrigatoriamente à instância Oracle da FIAP" | URL só no `application-local.properties` (fora do git e do ZIP) | 🔴 Quem abre o `application.properties` não vê conexão com a FIAP. Sem as credenciais, a API não sobe. |
| B8 | "no mínimo 3 entidades" (Model, Repository, Service, Controller) | Usuario, Gasto, Investimento | 🟡 Se o avaliador não contar Usuario como entidade financeira, sobram 2. |
| F1 | "componentização" | componentes base + por domínio | ✅ |
| F2 | "rotas de navegação entre páginas (SPA)" | React Router | ✅ |
| F3 | "props e estado (hooks) para comunicação entre componentes" | useState, Context, useDados, `onSubmit`/`onExcluir` por props | ✅ Falta destacar no README onde isso aparece. |
| F4 | "página de autenticação (login)" | LoginPage | ✅ |
| F5 | "página inicial e página de erro personalizada" | Dashboard em `/` + ErroPage (`*` e `errorElement`) | ✅ O dashboard somava tudo no navegador; a regra de saldo não estava na API. |
| F6 | "páginas correspondentes **a cada controller** do backend" | Sem páginas para Conta e Categoria (contas ficavam dentro do Perfil) | 🔴 Leitura literal: todo controller precisa de página. |
| F7 | "Conectar o frontend ao backend via APIs REST" | `fetch` em `api/*` | ✅ |
| E1a/b | pasta com backend Spring Boot e frontend React | `api/`, `web/` | ✅ |
| E1 | "não incluir node_modules… dependências no package.json e package-lock.json" | `git archive` + verificação | ✅ Faltava garantir que o `mvnw` rode em Mac/Linux (LF e permissão de execução). |
| E1c | README com inicialização (back e front) e usuário de teste | previsto | ✅ Faltavam seções: tabelas, controller → página, regras de negócio. |
| E2 | mínimo 3 entidades no back **e no front** | 3 | 🟡 Mesmo risco do B8. |
| E3 | ZIP na plataforma | previsto | ✅ Nome do arquivo ainda não confirmado. |

## 2. Correções aceitas (aplicadas no plano)

| ID | Correção | Fecha | Custo |
|---|---|---|---|
| R1 | **Receita com CRUD completo** no back (B05) e no front (F04), com categoria do tipo RECEITA, origem e recorrência. Ficam **3 entidades financeiras** (Receita, Gasto, Investimento), mais Usuario. | B1, B8, E2 | médio |
| R2 | **CRUD completo para ContaBancaria e Categoria**, com regras: não excluir conta com lançamentos (409, sugere desativar), não excluir categoria em uso (409), nome único por tipo (409), tipo de domínio (422). | B3, B4 | médio |
| R3 | **Páginas para todos os controllers**: Contas e Categorias (F06). O Perfil deixa de gerenciar contas. README com a tabela controller → página → verbos. | F6 | médio |
| R4 | **Resumo financeiro no backend**: `GET /api/usuarios/{id}/resumo?mes=` (saldo = receitas − gastos, total investido, rendimento, gastos por categoria). Retoma o antigo `calcularImpactoNoSaldo` e a tela de dashboard do protótipo; a página inicial passa a consumir esse endpoint. | B3, F5 | baixo |
| R5 | **FIAP visível no `application.properties`** (URL + usuário RM); só a senha fica fora do git. | B7 | baixo |
| R6 | **DDL dentro do ZIP** (`database/ddl/`) + seção no README "Tabelas na FIAP" + evidência (consulta em `USER_TABLES` e log do `validate`). | B6 | baixo |
| R7 | **Regra de posse no PUT**: um lançamento não pode ser transferido para outro usuário (422). | B3 | baixo |
| R8 | **`mvnw` com LF e permissão de execução**; `*.cmd` com CRLF. Sem isso, `./mvnw` quebra no Mac/Linux do avaliador. | E1 | baixo |
| R9 | **`strictPort` no Vite**: se a 5173 estiver ocupada, o Vite troca de porta e o CORS bloqueia tudo sem explicação. | F7 | baixo |
| R10 | README com as seções "Regras de negócio por service", "Verbo → status", "Onde estão props/estado/hooks" e Node **20.19+** (exigência do Vite atual). | B3, B5, F3 | baixo |
| R11 | Usuário de teste também ganha **categorias e receitas de exemplo**; a exclusão de usuário passa a remover as receitas dele. | coerência | baixo |

## 3. Sugestões que avaliei e **não** incorporei

| Sugestão | Por que ficou de fora |
|---|---|
| Página pública de boas-vindas em `/` e dashboard em `/painel` | Para a SPA, "página inicial" é a tela inicial logada. Uma landing extra só criaria duas "páginas iniciais" para o avaliador interpretar. O README declara explicitamente que `/` é a página inicial. |
| Página de detalhe (`/gastos/:id`) e página própria de exclusão | O CRUD já usa listagem (consultar), formulário (inserir/atualizar) e confirmação (remover), e o `GET /{id}` já é usado na edição. Adicionaria 4 páginas sem ganho de requisito. |
| Mapear Meta, Dívida, Parcela e Aporte | O mínimo é 3 entidades e com R1 já são 4 com CRUD completo. Mapear tabelas sem tela só aumenta o risco no `validate`. Fica como evolução. |
| Spring Security + JWT | O enunciado pede página de login, não autenticação por token. Adicionaria filtro, CORS com credenciais e testes autenticados. A limitação fica documentada (D-F8-03). |
| `ddl-auto=update` para "criar as tabelas pela aplicação" | O DDL validado da Fase 6 é a fonte da verdade. Com `update`, o Hibernate criaria colunas e constraints divergentes na instância compartilhada da FIAP. |

## 4. Riscos que continuam (e como estão cobertos)

1. **Tipos do Oracle no `validate`** (`NUMBER(10)`, `CHAR(1)`): coberto pelo `SchemaOracleTest` (B01), com a correção já descrita.
2. **Avaliador sem credenciais** da FIAP: decisão da Larissa. A recomendação é incluir o `application-local.properties` **só no ZIP**, como na Fase 7. Sem isso, a API não sobe e nenhuma tela funciona.
3. **Categorias são globais**: a tabela não tem `id_usuario`, então editar uma categoria afeta todos os usuários. Fica documentado no README.
4. **Escopo maior**: a revisão acrescenta cerca de 35% de trabalho (Receita, CRUD de Conta/Categoria, 2 páginas novas, resumo). As tarefas novas seguem os moldes já escritos.
