# Revisão técnica inicial — spring-boot-rest

Data da análise: 2026-03-31

> Atualização: o segredo em `application.properties` foi externalizado para variáveis de ambiente em etapa posterior (DB_URL/DB_USERNAME/DB_PASSWORD).

## Status de execução

- **Aplicação não validada em execução completa neste ambiente**.
- O build/teste falha antes de compilar devido à resolução de dependências Maven, com erro de acesso ao `repo.maven.apache.org` e parent `spring-boot-starter-parent:4.0.0` não resolvido.

## Não conformidades encontradas

### 1) Segredos em texto plano (crítico) — **resolvido**

- Situação original (histórica): havia usuário/senha em texto puro no `application.properties`.
- Situação atual: configuração externalizada por variáveis de ambiente (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).

### 2) Modelagem JPA incompleta para coleção (alto) — **resolvido**

- Situação original (histórica): `JobPost` declarava `List<String> postTechStack` sem mapeamento JPA explícito.
- Situação atual: coleção mapeada com `@ElementCollection`/`@CollectionTable` e versionamento de schema com Flyway (`V1__create_job_post_tables.sql`).

### 3) API sem tratamento de erros e sem códigos HTTP adequados (alto) — **resolvido**

- Situação original (histórica): havia retorno de objeto vazio para não encontrado e resposta de sucesso sem validação de existência.
- Situação atual: endpoints usam `ResponseEntity` com códigos HTTP corretos e tratamento global via `@RestControllerAdvice`.

### 4) Endpoint de escrita aceitando apenas XML (médio) — **resolvido**

- Situação original (histórica): `jobPost` aceitava somente XML em escrita.
- Situação atual: endpoints de escrita padronizados para JSON.

### 5) CORS restrito a localhost fixo e sem externalização (médio) — **resolvido**

- Situação original (histórica): CORS estava hardcoded no controller para `http://localhost:3000`.
- Situação atual: CORS externalizado por ambiente via propriedade `app.cors.allowed-origins` (variável `CORS_ALLOWED_ORIGINS`).

### 6) Endpoint de carga de dados sem proteção e não idempotente (médio) — **parcialmente resolvido**

- Situação atual: endpoint de carga foi alterado para `POST /load`, corrigindo a semântica HTTP de escrita.
- Pendente: ainda é necessário controlar acesso e idempotência para uso seguro em produção.

### 7) Ausência de validação de entrada (alto) — **resolvido**

- Situação original (histórica): entidade/controlador não aplicavam Bean Validation.
- Situação atual: validações com `jakarta.validation` e respostas de erro padronizadas para payload inválido.

### 8) Testes insuficientes (alto)

- Há apenas teste de contexto (`contextLoads`), sem cobertura de controller/service/repository.
- Não há testes de integração de endpoints, contrato HTTP, validação nem cenários de erro.

### 9) Dependências e versão de plataforma potencialmente inconsistentes (médio)

- Parent em `Spring Boot 4.0.0` (a validar conforme baseline do time).
- Uso de `spring-boot-starter-webmvc` e `spring-boot-starter-webmvc-test` em vez dos starters convencionais (`spring-boot-starter-web` e `spring-boot-starter-test`) aumenta risco de incompatibilidade.
- `jackson-dataformat-xml` com versão fixa pode conflitar com o BOM do Spring Boot.

### 10) Qualidade de código/manutenibilidade (médio)

- Uso extensivo de field injection (`@Autowired` em atributo), prática menos recomendada frente a constructor injection.
- Código legado comentado em `JobRepo` deve ser removido.
- Ausência de versionamento de API (ex.: `/api/v1/...`).

## Próximos passos sugeridos (ordem recomendada)

1. Endurecer segurança/idempotência do endpoint `/load` (ou mover para processo de seed controlado).
2. Fortalecer testes (unit + integração + contrato de API).
3. Revisar alinhamento de versões/dependências com política do projeto.

