# Baseline corporativo e política de upgrade

## Baseline confirmado para este repositório

- **Spring Boot:** `4.0.0`
- **Java:** `25`
- **Banco homologado:** PostgreSQL 16+

> Esta baseline fica codificada no `pom.xml` e deve ser atualizada apenas via PR explícito de upgrade.

## Política de upgrade

1. **Patch releases (x.y.Z):** aplicação automática mensal.
2. **Minor releases (x.Y.z):** avaliação trimestral em branch de upgrade com bateria completa de testes.
3. **Major releases (X.y.z):** ADR obrigatória + janela dedicada de migração.
4. **Dependências fora do BOM do Spring Boot:** evitar fixar versão manual sem justificativa técnica.
5. **Gate de upgrade:** só promover após:
   - testes unitários/integrados verdes,
   - análise de segurança (SCA),
   - validação de compatibilidade de banco/migração.

## Processo operacional

- Abrir PR com título: `upgrade(platform): spring-boot <old> -> <new>`.
- Incluir checklist de breaking changes e plano de rollback.
- Atualizar este arquivo e `REVISAO_TECNICA.md` no mesmo PR.
