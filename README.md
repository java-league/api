# Java League API

API REST para organização de torneios FIFA com sistema de leilão de jogadores em tempo real.

## Sobre o projeto

O Java League é uma plataforma de fantasy football onde times disputam jogadores por meio de um sistema de lances com moeda virtual (Javalis). As atualizações de lance são transmitidas em tempo real via WebSocket para todos os participantes conectados.

**Tecnologias:**

- Java 17 + Spring Boot 3.1.1
- Spring Security + JWT (Auth0)
- Spring WebSocket (STOMP)
- PostgreSQL + Flyway (migrations)
- MapStruct (mapeamento de DTOs)
- Lombok

## Funcionalidades

- Autenticação via JWT com suporte a roles (ADMIN / USER)
- Seleção de time e gerenciamento de elenco
- Sistema de lances com três cenários: primeiro lance, lance maior e lance menor
- Saldo de Javalis por time (começa com 250.000)
- Transmissão de lances em tempo real via WebSocket
- Dados pré-cadastrados: 8 times e 46 jogadores de alto overall

## Regras de negócio - Lances

Cada jogador tem um preço inicial. Ao receber um lance:

- **Primeiro lance (FIRST_BID):** o preço do jogador é debitado do saldo do time. O jogador passa a pertencer ao time vencedor.
- **Lance maior (HIGHEST_BID):** o saldo do time anterior é devolvido e o novo valor é debitado do time vencedor. O jogador muda de dono.
- **Lance menor (LOWEST_BID):** o time dono do jogador paga a diferença entre o novo lance e o preço atual. O jogador permanece com o mesmo dono.

## Estrutura de pacotes

```
src/main/java/com/example/java_league/
├── controllers/      # Endpoints REST e WebSocket
├── domain/           # Entidades JPA
├── dto/              # Data Transfer Objects
├── mapper/           # Mappers MapStruct
├── service/          # Regras de negócio
├── repository/       # Acesso ao banco (Spring Data JPA)
├── security/         # Configuração Spring Security, JWT e filtros
├── record/           # Records para request/response de autenticação
├── enums/            # UserRole (ADMIN, USER)
└── webSocket/        # Configuração do broker STOMP
```

## Endpoints

### Autenticação

| Método | Rota             | Descrição               | Auth |
| ------ | ---------------- | ----------------------- | ---- |
| POST   | `/auth/register` | Cadastrar usuário       | Não  |
| POST   | `/auth/login`    | Login e obter token JWT | Não  |

**Login - resposta:** `{ "token": "...", "teamId": 1 }`

### Times

| Método | Rota                                    | Descrição                       | Auth |
| ------ | --------------------------------------- | ------------------------------- | ---- |
| GET    | `/api/team`                             | Listar todos os times           | Não  |
| GET    | `/api/team/available`                   | Listar times sem dono           | Não  |
| GET    | `/api/team/current`                     | Retornar time do usuário logado | Sim  |
| POST   | `/api/team/current`                     | Associar time ao usuário logado | Sim  |
| POST   | `/api/team/{playerId}/player?position=` | Adicionar jogador ao elenco     | Sim  |

### Jogadores

| Método | Rota                             | Descrição                        | Auth  |
| ------ | -------------------------------- | -------------------------------- | ----- |
| GET    | `/api/player`                    | Listar jogadores com maior lance | ADMIN |
| POST   | `/api/player`                    | Cadastrar jogador                | Sim   |
| PATCH  | `/api/player/{id}/bid?bidValue=` | Dar lance em jogador             | Sim   |

### Lances (WebSocket)

- **Endpoint STOMP:** `ws://localhost:8080/ws`
- **Enviar lance:** `/app/bid` com payload `BidDTO`
- **Receber atualizações:** subscribe em `/topic/bid`

## Como rodar

### Pré-requisitos

- Docker e Docker Compose
- Java 17+
- Maven

### 1. Subir o banco de dados

```bash
docker-compose up -d
```

Isso sobe o PostgreSQL na porta `5432` e o pgAdmin na porta `15432`.

| Serviço    | URL                      | Credenciais              |
| ---------- | ------------------------ | ------------------------ |
| PostgreSQL | `localhost:5432`         | `postgres / root`        |
| pgAdmin    | `http://localhost:15432` | `admin@admin.com / root` |

### 2. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

As migrations do Flyway são aplicadas automaticamente na inicialização, incluindo os dados de seed (times, jogadores e usuários de teste).

### 3. Usuários pré-cadastrados

| Login   | Senha              | Role  |
| ------- | ------------------ | ----- |
| `admin` | (ver migration V5) | ADMIN |
| `teste` | (ver migration V5) | USER  |

## Variáveis de ambiente

| Variável     | Padrão          | Descrição                          |
| ------------ | --------------- | ---------------------------------- |
| `JWT_SECRET` | `my-secret-key` | Segredo para assinar os tokens JWT |

> Em produção, sempre defina `JWT_SECRET` com um valor seguro.

## Documentação

- [Lista de melhorias](doc/melhorias.md)
