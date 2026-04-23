# Contribuindo

## Pré-requisitos

- Java 21
- Maven 3.9+
- PostgreSQL (para rodar a aplicação localmente)

## Rodando os testes

Os testes usam H2 em memória - não é necessário banco externo:

```bash
mvn test
```

## Padrões do projeto

### Estrutura de pacotes

| Pacote        | Responsabilidade                             |
| ------------- | -------------------------------------------- |
| `domain`      | Entidades JPA                                |
| `dto`         | Records de transferência de dados            |
| `request`     | Records de entrada (payloads de API)         |
| `service`     | Regras de negócio                            |
| `controllers` | Endpoints REST e WebSocket                   |
| `mapper`      | Conversões MapStruct entre entidade e DTO    |
| `repository`  | Acesso ao banco via Spring Data JPA          |
| `security`    | Filtro JWT, configuração Spring Security     |
| `event`       | Eventos de domínio (Spring ApplicationEvent) |
| `enums`       | Enumerações compartilhadas                   |

### Convenções de código

- **DTOs**: sempre `record` - imutáveis por construção.
- **Entidades JPA**: classes com Lombok (`@Getter @Setter @NoArgsConstructor @AllArgsConstructor`).
- **Services e controllers**: Lombok (`@RequiredArgsConstructor` / `@AllArgsConstructor`).
- **Notificações WebSocket**: publicar `BidProcessedEvent` via `ApplicationEventPublisher` - nunca chamar `SimpMessagingTemplate` diretamente em services ou controllers.
- **Fluxo de lance**: toda lógica de negócio passa por `PlayerService.bid()`.

### Commits

Use prefixos semânticos:

```
feat:     nova funcionalidade
fix:      correção de bug
refactor: mudança sem alteração de comportamento
chore:    dependências, CI, configuração
docs:     documentação
test:     testes
```

## Abrindo uma Pull Request

1. Crie uma branch a partir de `main`.
2. Implemente e teste localmente (`mvn test`).
3. Abra a PR preenchendo o template.
4. Aguarde o workflow de CI passar antes de solicitar revisão.
