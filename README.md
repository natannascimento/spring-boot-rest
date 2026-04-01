# Spring Boot REST API — Job Portal

API REST para cadastro e busca de vagas (`JobPost`) com persistência em PostgreSQL, migração de schema com Flyway, validação de payload e tratamento padronizado de erros.

## Sumário

- [Visão geral](#visão-geral)
- [Stack](#stack)
- [Pré-requisitos](#pré-requisitos)
- [Configuração](#configuração)
- [Como executar](#como-executar)
- [Modelo de dados](#modelo-de-dados)
- [Autenticação/segurança operacional](#autenticaçãosegurança-operacional)
- [Endpoints](#endpoints)
- [Contrato de erros](#contrato-de-erros)
- [Testes](#testes)
- [Migrações](#migrações)

---

## Visão geral

A API oferece operações CRUD para vagas e busca por palavra-chave:

- Criar vaga
- Listar vagas
- Buscar vaga por ID
- Atualizar vaga
- Excluir vaga
- Buscar por keyword
- Seed controlado de dados (`/load`)

---

## Stack

- Java 25
- Spring Boot
- Spring Web / Validation / Data JPA
- PostgreSQL
- Flyway
- Testes: JUnit 5, Mockito, MockMvc, Testcontainers

---

## Pré-requisitos

- Java 25+
- Maven 3.9+
- PostgreSQL (para execução local sem containers)
- Docker (opcional, para testes de integração com Testcontainers)

---

## Configuração

A aplicação usa variáveis de ambiente com fallback local.

| Variável | Obrigatória | Padrão | Descrição |
|---|---|---|---|
| `DB_URL` | Não | `jdbc:postgresql://localhost:5432/telusko_db` | URL do banco |
| `DB_USERNAME` | Não | `postgres` | Usuário do banco |
| `DB_PASSWORD` | Não | vazio | Senha do banco |
| `CORS_ALLOWED_ORIGINS` | Não | `http://localhost:3000` | Origens CORS (separadas por vírgula) |
| `SEED_ENABLED` | Não | `false` | Habilita endpoint `/load` |
| `SEED_TOKEN` | Não | vazio | Token opcional exigido em `X-Seed-Token` |

---

## Como executar

```bash
mvn spring-boot:run
```

Ou com variáveis explícitas:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/telusko_db
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
export SEED_ENABLED=true
export SEED_TOKEN=my-seed-token

mvn spring-boot:run
```

---

## Modelo de dados

### JobPost

```json
{
  "postId": 1,
  "postProfile": "Backend Developer",
  "postDesc": "Build and maintain APIs",
  "reqExperience": 3,
  "postTechStack": ["Java", "Spring Boot", "PostgreSQL"]
}
```

### Regras de validação

- `postId` >= 1
- `postProfile` obrigatório
- `postDesc` obrigatório
- `reqExperience` >= 0
- `postTechStack` obrigatório e não vazio

---

## Autenticação/segurança operacional

> A API não implementa autenticação de usuário final nesta versão.

### Endpoint de seed (`POST /load`)

- Só disponível quando `SEED_ENABLED=true`.
- Se `SEED_TOKEN` estiver configurado, o header `X-Seed-Token` é obrigatório.
- Comportamento idempotente:
  - primeira carga: `201 Created`
  - chamadas subsequentes: `200 OK` (`seed already loaded`)

---

## Endpoints

Base URL local: `http://localhost:8080`

### 1) Listar vagas

`GET /jobPosts`

**200 OK**

```bash
curl -X GET http://localhost:8080/jobPosts
```

---

### 2) Buscar vaga por ID

`GET /jobPost/{postId}`

**200 OK** / **404 Not Found**

```bash
curl -X GET http://localhost:8080/jobPost/1
```

---

### 3) Criar vaga

`POST /jobPost`

**Headers**
- `Content-Type: application/json`

**201 Created** + `Location`

```bash
curl -X POST http://localhost:8080/jobPost \
  -H "Content-Type: application/json" \
  -d '{
    "postId": 10,
    "postProfile": "Backend Developer",
    "postDesc": "Build APIs",
    "reqExperience": 3,
    "postTechStack": ["Java", "Spring"]
  }'
```

---

### 4) Atualizar vaga

`PUT /jobPost`

**Headers**
- `Content-Type: application/json`

**200 OK** / **404 Not Found**

```bash
curl -X PUT http://localhost:8080/jobPost \
  -H "Content-Type: application/json" \
  -d '{
    "postId": 10,
    "postProfile": "Senior Backend Developer",
    "postDesc": "Build and scale APIs",
    "reqExperience": 5,
    "postTechStack": ["Java", "Spring", "Kubernetes"]
  }'
```

---

### 5) Excluir vaga

`DELETE /jobPost/{postId}`

**204 No Content** / **404 Not Found**

```bash
curl -X DELETE http://localhost:8080/jobPost/10
```

---

### 6) Buscar por palavra-chave

`GET /jobPosts/keyword/{keyword}`

**200 OK**

```bash
curl -X GET http://localhost:8080/jobPosts/keyword/Java
```

---

### 7) Seed de dados

`POST /load`

**200 OK** / **201 Created** / **403 Forbidden** / **404 Not Found**

```bash
curl -X POST http://localhost:8080/load \
  -H "X-Seed-Token: my-seed-token"
```

---

## Contrato de erros

Formato padrão:

```json
{
  "timestamp": "2026-04-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/jobPost",
  "details": [
    "postId: postId must be greater than zero",
    "postProfile: postProfile is required"
  ]
}
```

---

## Testes

Executar suíte:

```bash
mvn test
```

### Cobertura atual

- Unit tests (service)
- Controller tests com MockMvc (contrato HTTP e erros)
- Integração JPA com PostgreSQL real (Testcontainers)
- Integração de endpoint `/load` com concorrência e token

---

## Migrações

As migrações ficam em:

- `src/main/resources/db/migration`

Migração inicial:

- `V1__create_job_post_tables.sql`

Flyway é executado no startup da aplicação.
