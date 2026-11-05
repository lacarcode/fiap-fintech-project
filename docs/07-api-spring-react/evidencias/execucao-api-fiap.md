# Execução da API na instância Oracle da FIAP

Evidência da tarefa B08: a API Spring Boot (`api/`) conectada à instância Oracle da FIAP, com o Hibernate
em `ddl-auto=validate` (confere o mapeamento das entidades contra as tabelas já existentes, sem alterá-las).

| Item | Valor |
|---|---|
| Data | 2026-11-04 |
| Java | Temurin 21.0.12 |
| Spring Boot | 3.5.7 (Hibernate 6.6.33) |
| URL | `jdbc:oracle:thin:@ORACLE.FIAP.COM.BR:1521:ORCL` (definida em `api/src/main/resources/application.properties`) |
| Usuário | `RM571266` (também no `application.properties`) |
| Senha | informada só por variável de ambiente (`SPRING_DATASOURCE_PASSWORD`); não consta em arquivo versionado nem neste documento |
| Banco | Oracle Database 19c Enterprise Edition 19.0.0.0.0 |

A API foi iniciada **sem** `application-local.properties`, ou seja, URL e usuário vieram exatamente do
`application.properties` que segue na entrega.

## 1. Tabelas existentes na FIAP

Consulta executada na instância da FIAP (`select table_name from user_tables where table_name like 'T_SF_%'`)
e contagem de linhas após a bateria abaixo:

```
10 tabelas T_SF_*:
  T_SF_APORTE_META       0 linhas
  T_SF_CATEGORIA         10 linhas
  T_SF_CONTA_BANCARIA    5 linhas
  T_SF_DIVIDA            0 linhas
  T_SF_GASTO             13 linhas
  T_SF_INVESTIMENTO      12 linhas
  T_SF_META              0 linhas
  T_SF_PARCELA           0 linhas
  T_SF_RECEITA           2 linhas
  T_SF_USUARIO           11 linhas

  objetos invalidos: 0
```

O DDL que criou essas tabelas está em `database/ddl/01-create-tables.sql` (aplicado na FIAP na Fase 7).
As 6 entidades JPA usadas pela API mapeiam: `T_SF_USUARIO`, `T_SF_CONTA_BANCARIA`, `T_SF_CATEGORIA`,
`T_SF_RECEITA`, `T_SF_GASTO` e `T_SF_INVESTIMENTO`.

## 2. Log de subida

Trechos do log (sem erro de *schema validation*; a API só sobe se cada entidade bater com a tabela):

```
HHH000412: Hibernate ORM core version 6.6.33.Final
HikariPool-1 - Starting...
HikariPool-1 - Added connection oracle.jdbc.driver.T4CConnection@...
HikariPool-1 - Start completed.
Started FintechApplication in 11.902 seconds
DadosDemo : Usuário de teste demo@fintech.com criado com conta, receitas, gastos e investimentos de exemplo.
```

## 3. Bateria de chamadas (esperado × obtido)

Cada um dos 6 controllers foi exercitado com o ciclo POST → GET por id → PUT → DELETE → GET (404), mais as
regras de negócio e os códigos de erro. Tudo o que a bateria criou foi removido ao final.

| Chamada | Esperado | Obtido | Resultado |
|---|---|---|---|
| POST /auth/login (usuario de teste) | 200 | 200 | OK |
| POST /auth/login (senha errada) | 401 | 401 | OK |
| OPTIONS /gastos (CORS, origem 5173) | 200 | 200 | OK |
| GET /usuarios/{demo}/resumo | 200 | 200 | OK |
| GET /usuarios/{demo}/resumo?mes=2026-13 | 400 | 400 | OK |
| GET /usuarios/999999 | 404 | 404 | OK |
| DELETE /contas/{conta do demo} (tem lancamentos) | 409 | 409 | OK |
| POST /usuarios | 201 | 201 | OK |
| POST /usuarios (e-mail duplicado) | 409 | 409 | OK |
| POST /usuarios (invalido) | 400 | 400 | OK |
| GET /usuarios/{id} | 200 | 200 | OK |
| PUT /usuarios/{id} | 200 | 200 | OK |
| POST /contas | 201 | 201 | OK |
| GET /contas/{id} | 200 | 200 | OK |
| PUT /contas/{id} | 200 | 200 | OK |
| PUT /contas/{id} (outro usuario) | 422 | 422 | OK |
| POST /categorias | 201 | 201 | OK |
| POST /categorias (duplicada) | 409 | 409 | OK |
| GET /categorias?tipo=GASTO | 200 | 200 | OK |
| PUT /categorias/{id} | 200 | 200 | OK |
| POST /gastos | 201 | 201 | OK |
| GET /gastos?usuarioId= | 200 | 200 | OK |
| GET /gastos/{id} | 200 | 200 | OK |
| PUT /gastos/{id} | 200 | 200 | OK |
| POST /gastos (valor negativo) | 400 | 400 | OK |
| POST /gastos (conta de outro usuario) | 422 | 422 | OK |
| POST /gastos (categoria de receita) | 422 | 422 | OK |
| POST /receitas | 201 | 201 | OK |
| GET /receitas/{id} | 200 | 200 | OK |
| PUT /receitas/{id} | 200 | 200 | OK |
| POST /receitas (categoria de gasto) | 422 | 422 | OK |
| POST /investimentos | 201 | 201 | OK |
| GET /investimentos/{id} | 200 | 200 | OK |
| PUT /investimentos/{id} (resgatar) | 200 | 200 | OK |
| POST /investimentos (vencimento anterior) | 422 | 422 | OK |
| DELETE /contas/{id} (com lancamentos) | 409 | 409 | OK |
| DELETE /categorias/{id} (em uso) | 409 | 409 | OK |
| DELETE /gastos/{id} | 204 | 204 | OK |
| GET /gastos/{id} apos DELETE | 404 | 404 | OK |
| DELETE /receitas/{id} | 204 | 204 | OK |
| GET /receitas/{id} apos DELETE | 404 | 404 | OK |
| DELETE /investimentos/{id} | 204 | 204 | OK |
| GET /investimentos/{id} apos DELETE | 404 | 404 | OK |
| DELETE /contas/{id} | 204 | 204 | OK |
| DELETE /categorias/{id} (gasto) | 204 | 204 | OK |
| DELETE /categorias/{id} (receita) | 204 | 204 | OK |
| DELETE /usuarios/{id} | 204 | 204 | OK |
| GET /usuarios/{id} apos DELETE | 404 | 404 | OK |

**Total: 48 chamadas, 0 divergências.**

## 4. Resumo financeiro do usuário de teste

`GET /api/usuarios/{id}/resumo` para `demo@fintech.com` (mês 2026-11), calculado pela API a partir dos dados na FIAP:

```json
{"mes":"2026-11","totalReceitas":6200,"totalGastos":1979.25,"saldo":4220.75,"totalInvestido":7000,"rendimentoEstimadoAnual":840.00,"gastosPorCategoria":[{"categoria":"Moradia","total":1500},{"categoria":"Alimentação","total":420.35},{"categoria":"Transporte","total":58.9}]}
```

## 5. Observações

- Cada subida da API redefine a senha do usuário de teste para a do README e não duplica os dados de exemplo
  (verificado no Oracle local, ver `tasks/fase8/README.md`).
- A carga de demonstração roda cerca de 0,4 s depois de a porta abrir; esperar a linha do log antes de entrar.
