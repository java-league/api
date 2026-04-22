# Melhorias - Java League API

Lista de melhorias identificadas após análise do projeto.

## Segurança

- **Regras de autorização com wildcard permissivas.** As regras `POST *` e `GET *` com `permitAll()` em `SecurityConfigurations` tornam a maioria dos endpoints publicamente acessíveis, sobrescrevendo as restrições definidas abaixo delas. A configuração deveria ser invertida: bloquear tudo por padrão e liberar apenas o necessário.

- **Credenciais do banco de dados expostas.** O `application.properties` tem usuário, senha e URL do PostgreSQL em texto claro. Mover para variáveis de ambiente (ex: `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`).

- **Segredo JWT sem valor obrigatório.** O fallback `${JWT_SECRET:my-secret-key}` permite subir a aplicação em produção com uma chave pública e conhecida. Remover o valor padrão e tornar a variável obrigatória.

- **CORS liberado para todas as origens.** O WebSocket está com `setAllowedOriginPatterns("*")`. Em produção, restringir para os domínios do frontend.

## Tratamento de Erros

- **Sem handler global de exceções.** Criar um `@ControllerAdvice` com `@ExceptionHandler` para retornar respostas padronizadas (ex: `{error, message, status}`) em vez de stack traces do Spring.

- **`getPlayerById()` lança exceção não tratada.** O método usa `.get()` no Optional sem verificar se o valor existe. Substituir por `.orElseThrow(EntityNotFoundException::new)` e tratar no handler global.

- **Sem validação do valor do lance.** `bidValue` recebido via query param não tem nenhuma validação (valor mínimo, máximo, nulo). Adicionar `@Positive` e `@NotNull` ou validação manual no service.

## Qualidade de Código

- **Typo no nome de método.** `getAllTeamsAavailable()` em `TeamService` e `TeamRepository` tem "Aavailable" com dois A. Corrigir para `getAllTeamsAvailable()`.

- **Dependência duplicada de JWT.** O `pom.xml` tem tanto `com.auth0:java-jwt` quanto `io.jsonwebtoken:jjwt`. Apenas o Auth0 é usado. Remover o JJWT.

- **Imports não utilizados.** `io.jsonwebtoken.JwtParser` importado mas nunca utilizado. Limpar imports mortos.

- **Endpoint `/send` fantasma.** A rota `POST /send` aparece na configuração de segurança mas não existe em nenhum controller. Remover da config.

- **`@SendTo` não utilizado no BidController.** A anotação está presente mas o broadcast é feito manualmente via `SimpMessagingTemplate`. Manter só uma das abordagens.

## Design de API

- **Uso de `PATCH` para criação de lance.** `PATCH /api/player/{id}/bid` cria um recurso novo (Bid). Semanticamente, deveria ser `POST /api/player/{id}/bid`.

- **Inconsistência de prefixo nas rotas.** Endpoints de autenticação usam `/auth/*` e os demais usam `/api/*`, mas TeamController expõe `GET /api/team` sem autenticação. Definir uma convenção clara e documentada.

- **`POST /api/team/current` recebe `userId` pelo body desnecessariamente.** O userId já está disponível no JWT. O endpoint deveria usar apenas o `teamId` e extrair o userId do token, como já faz em outros endpoints.

## Banco de Dados

- **Ausência de índices.** Colunas muito consultadas em JOINs e filtros (`bid.player_id`, `bid.team_id`, `team.user_id`) não têm índices explícitos. Adicionar via migração Flyway.

- **Nome de constraint incorreto.** A constraint de FK em `bid.team_id` está nomeada `fk_bid_user` quando deveria ser `fk_bid_team`. Corrigir via migração.

- **Credencial padrão do pgAdmin exposta.** O `docker-compose.yml` tem `admin@admin.com / root` para o pgAdmin. Mover para um `.env` e adicionar ao `.gitignore`.

## WebSocket

- **Broker simples inadequado para múltiplos servidores.** O `SimpleBroker` é in-memory e não escala horizontalmente. Para produção com mais de uma instância, integrar com um message broker externo (RabbitMQ ou Redis pub/sub).

- **SockJS comentado sem remoção.** O código de configuração do SockJS está comentado. Remover ou ativar definitivamente.

## Testes

- **Ausência de testes.** O projeto não tem nenhum teste implementado além das dependências declaradas. Implementar ao menos:
  - Testes unitários para `PlayerService.bid()` cobrindo os três cenários (FIRST_BID, HIGHEST_BID, LOWEST_BID).
  - Testes de integração para os endpoints de autenticação e lance.

## Observabilidade

- **Sem logging estruturado.** Nenhuma das camadas de service usa `@Slf4j` ou similar. Adicionar logs nos pontos críticos do fluxo de lance (valor, teamId, playerId, resultado).

- **Sem métricas ou health check personalizado.** Habilitar o Spring Boot Actuator com endpoints `/actuator/health` e `/actuator/metrics` para monitoramento básico.

## Outros

- **Senha do usuário `teste` provavelmente em texto claro na migration V5.** Verificar se o hash BCrypt na seed está correto e documentar qual é a senha padrão para desenvolvimento.

- **Fuso horário fixo no TokenService.** O token é gerado com `-03:00` hardcoded. Usar `ZoneOffset.UTC` e converter na apresentação ao invés de fixar o offset no token.
