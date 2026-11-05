# Teste ponta a ponta — SPA React + API Spring Boot + Oracle da FIAP

Evidência da tarefa E01. Roteiro executado em 2026-11-04 com a **API conectada à instância Oracle da FIAP**
(`jdbc:oracle:thin:@ORACLE.FIAP.COM.BR:1521:ORCL`, usuário `RM571266`) e a SPA em `http://localhost:5173`,
exatamente como descrito no `README.md`: URL e usuário vindos do `application.properties`, senha por variável de
ambiente (`SPRING_DATASOURCE_PASSWORD`; não consta aqui nem em arquivo versionado).

Método: navegador real (painel de navegação do Claude Code) contra a API real. Os formulários foram preenchidos
por script (disparando os eventos do React) e os resultados lidos do DOM da página; **não há capturas de tela**
(a verificação foi por texto e estado da página).

## Log de subida

```
HikariPool-1 - Start completed.
Started FintechApplication in 11.813 seconds
DadosDemo : Usuário de teste demo@fintech.com já existe: senha e status redefinidos.
```

## Roteiro

| # | Passo | Resultado |
|---|---|---|
| 1 | Login com senha errada | Permanece em `/login` com o alerta "E-mail ou senha inválidos" |
| 2 | Login com `demo@fintech.com` / `Fintech@2026` | Entra; página inicial com saldo R$ 4.220,75, receitas R$ 6.200,00, gastos R$ 1.979,25, total investido R$ 7.000,00 (rendimento R$ 840,00/ano), gastos por categoria e últimas transações |
| 3 | **Gastos**: consultar → inserir → editar → excluir | Total 1.979,25 → 2.012,55 (inserido, "Gasto cadastrado com sucesso.") → 2.029,25 (editado, formulário pré-preenchido) → 1.979,25 ("Gasto excluído com sucesso.") |
| 4 | **Receitas**: consultar → inserir → editar → excluir | Total 6.200,00 → 6.600,00 → 6.650,00 → 6.200,00 ("Receita excluída com sucesso.") |
| 5 | **Investimentos**: consultar → inserir (taxa 8 % a.a.) → editar para Resgatado → excluir | Rendimento estimado calculado pela API: R$ 80,00; status RESGATADO; excluído, restando Tesouro Selic e CDB |
| 6 | **Contas**: inserir → desativar → tentar excluir a conta do demo → excluir a de teste | Conta criada e marcada Inativa; exclusão da conta com lançamentos bloqueada com "A conta possui lançamentos vinculados; desative-a em vez de excluir" (409); conta de teste excluída |
| 7 | **Categorias**: inserir → nome duplicado → renomear → excluir categoria em uso → excluir a de teste | "Já existe a categoria 'moradia' do tipo GASTO" (409, ignora maiúsculas); renomeada; "A categoria está em uso por lançamentos e não pode ser excluída" (409, Moradia permanece); categoria de teste excluída (10 categorias ao final) |
| 8 | **Cadastro → perfil**: criar usuário pela tela, editar o nome, excluir a conta | Entra automaticamente após o cadastro; "Perfil atualizado com sucesso."; ao excluir a conta, a sessão é encerrada e vai para `/login` |
| 9 | Rota inexistente `/rota-que-nao-existe` | Página 404 personalizada ("Página não encontrada", botão "Voltar ao início") |
| 10 | Sem sessão, abrir `/gastos` | Redireciona para `/login` |

## Estado do banco antes e depois

O teste criou e removeu seus próprios registros. Contagem de linhas nas tabelas da FIAP imediatamente antes e
depois do roteiro (idêntica):

| Tabela | Antes | Depois |
|---|---|---|
| `T_SF_USUARIO` | 11 | 11 |
| `T_SF_CONTA_BANCARIA` | 5 | 5 |
| `T_SF_CATEGORIA` | 10 | 10 |
| `T_SF_RECEITA` | 2 | 2 |
| `T_SF_GASTO` | 13 | 13 |
| `T_SF_INVESTIMENTO` | 12 | 12 |

Objetos inválidos no schema: 0.

## Observações

- O login da etapa 2 levou a `/perfil` em vez de `/`: o navegador ainda guardava a página que o usuário tentava
  abrir antes de sair (comportamento intencional da `RotaProtegida`, que devolve o usuário à página de origem).
- O resumo financeiro (`GET /usuarios/{id}/resumo`) e as 48 chamadas diretas à API estão em
  `execucao-api-fiap.md`.
