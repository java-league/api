# Melhorias - Modernização e Arquitetura

## 1. Atualização do Java e dependências

### Java 17 → 21

Java 21 é LTS assim como o 17 e traz ganhos concretos para este projeto:

- **Virtual Threads (Project Loom):** com `spring.threads.virtual.enabled=true` no `application.properties`, o Spring Boot 3.2+ passa a processar requisições e mensagens WebSocket em virtual threads automaticamente, sem nenhuma mudança de código. O ganho é direto no throughput do fluxo de lances concorrentes.

- **Pattern Matching em `switch`:** o método `bid()` em `PlayerService` possui três ramificações (`FIRST_BID`, `HIGHEST_BID`, `LOWEST_BID`) implementadas com `if-else`. Java 21 permite usar `switch` com pattern matching e sealed classes para tornar esses cenários exaustivos e verificados pelo compilador (ver seção 4).

- **Sequenced Collections:** métodos como `getFirst()` e `getLast()` em `List` eliminam o uso de `get(0)` e `get(list.size() - 1)`.

### Inconsistência no pom.xml

O `pom.xml` declara `java.version=17` nas properties, mas o `maven-compiler-plugin` está configurado com `<source>16</source><target>16</target>`. As duas configurações precisam ser alinhadas, e ao subir para Java 21, ambas devem refletir `21`.

### Dependências desatualizadas

| Dependência           | Versão atual | Versão recomendada            |
| --------------------- | ------------ | ----------------------------- |
| Spring Boot           | 3.1.1        | 3.4.x (última estável)        |
| MapStruct             | 1.5.3.Final  | 1.6.x                         |
| Auth0 JWT             | 4.4.0        | 4.5.x                         |
| maven-compiler-plugin | 3.5.1        | 3.13.x                        |
| JJWT                  | 0.9.1        | **remover** (não é utilizado) |

Subir para Spring Boot 3.2+ é o que destrava o suporte nativo a Virtual Threads e a versão mais recente do Hibernate com melhorias de performance em queries JPQL.

## 2. Remoção do Lombok em favor de Java moderno

O Lombok resolveu um problema real quando o Java era verboso. Com Java 21 e suporte das IDEs atuais, todas as anotações usadas no projeto têm equivalentes nativos - sem depender de um processador de anotações que acessa internals do compilador.

> Referência: [You Don't Need Lombok Anymore - Loiane Groner](https://loiane.com/2026/03/you-dont-need-lombok-anymore/)

### Mapeamento de anotações usadas no projeto

| Lombok                                       | Substituto Java moderno                              |
| -------------------------------------------- | ---------------------------------------------------- |
| `@Getter` / `@Setter`                        | Geração por IDE ou `record`                          |
| `@NoArgsConstructor` / `@AllArgsConstructor` | Construtores explícitos ou `record`                  |
| `@RequiredArgsConstructor`                   | Injeção via construtor explícito                     |
| `@ToString`                                  | `record` (automático) ou `toString()` gerado por IDE |
| `@Data`                                      | `record` (para tipos imutáveis)                      |
| `@Builder`                                   | Builder explícito ou static factory                  |

### DTOs: substituir por records

Os DTOs do projeto são objetos de transporte sem comportamento e sem necessidade de mutabilidade após construção - o caso de uso ideal para `record`.

**Antes (`PlayerDTO` com Lombok):**

```java
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class PlayerDTO {
    private Long id;
    private String name;
    private Long overall;
    private Long price;
    private String imageUrl;
    private Long teamId;
    private Long priceLimit;
    private Boolean hasBidForTeam;
}
```

**Depois (record Java 16+):**

```java
public record PlayerDTO(
    Long id,
    String name,
    Long overall,
    Long price,
    String imageUrl,
    Long teamId,
    Long priceLimit,
    Boolean hasBidForTeam
) {}
```

O `record` entrega `toString()`, `equals()`, `hashCode()` e accessores (`id()`, `name()`, etc.) sem anotações. A desvantagem de ser imutável é, na verdade, uma vantagem: o código em `PlayerService.getAllPlayersWithMaxBid()` que chama `playerDTO.setPriceLimit(maxBidValue)` seria substituído por construção direta com todos os valores - o que elimina o estado parcialmente preenchido.

**Observação:** `BidResponseDTO` é mutável por necessidade (é preenchido progressivamente dentro do `if-else` de `PlayerService.bid()`). Ao refatorar a lógica de lance para retornar o DTO já montado (ver seção 4), ele também se torna um candidato a `record`.

### Entidades JPA: manter como classes

`@Entity` requer construtor sem argumentos e campos mutáveis - incompatível com `record`. As entidades (`Player`, `Team`, `Bid`, `TeamPlayers`, `User`) devem permanecer como classes, com construtores e accessores gerados pela IDE no lugar das anotações Lombok.

### Services: substituir `@AllArgsConstructor` por construtor explícito

`@AllArgsConstructor` no service serve apenas para injeção de dependência via construtor. O substituto direto é declarar o construtor explicitamente - o Spring injeta automaticamente quando há apenas um construtor.

**Antes:**

```java
@Service
@AllArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    // ...
}
```

**Depois:**

```java
@Service
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    // ...

    public PlayerService(PlayerRepository playerRepository, TeamRepository teamRepository, ...) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        // ...
    }
}
```

### Remover dependências do pom.xml após migração

```xml
<!-- remover -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

E no `maven-compiler-plugin`, remover os `annotationProcessorPaths` referentes ao Lombok e ao `lombok-mapstruct-binding`.

## 3. Problemas de arquitetura

### 3.1 `@Transactional` ausente no fluxo de lance

O método `PlayerService.bid()` modifica três entidades distintas (`Team`, `Player`, `Bid`) em sequência, sem usar `@Transactional`. Se qualquer `save()` intermediário falhar, o banco fica em estado inconsistente - por exemplo, o saldo do time sendo debitado mas o lance não sendo registrado.

Adicionar `@Transactional` no método `bid()` garante que todas as operações sejam atômicas.

### 3.2 N+1 queries em `getAllPlayersWithMaxBid()`

O método itera sobre todos os jogadores e, para cada um, dispara duas queries adicionais ao banco:

```java
// Para cada player: 2 queries extras
Long maxBidValue = bidRepository.findMaxBidValueForPlayer(player1.getId());
boolean hasBidForTeam = bidRepository.existsBidForPlayerAndTeam(player1.getId(), teamId);
```

Com 46 jogadores, isso gera até **93 queries** por chamada. A solução é uma única query JPQL com projeção, retornando os dados agregados de todos os jogadores de uma vez.

### 3.3 Responsabilidade excessiva em `PlayerService`

`PlayerService` acumula três responsabilidades distintas:

- CRUD de jogadores
- Lógica de lances com movimentação financeira de Javalis
- Consulta de jogadores com dados agregados de bid

A lógica de lance deveria estar em `BidService` (que existe, mas só salva o bid sem regra de negócio) ou em um `BidProcessor` dedicado. Isso tornaria cada classe testável isoladamente.

### 3.4 `BidService.save()` e `PlayerService.bid()` duplicam responsabilidade

Existem dois caminhos para registrar um lance: o endpoint REST `PATCH /api/player/{id}/bid` chama `PlayerService.bid()`, enquanto `BidService.save()` (chamado pelo controller de WebSocket REST) salva o bid sem aplicar as regras de negócio. Um dos dois está incompleto. O fluxo deve ser unificado em um único ponto.

### 3.5 Pacote `record/` com nome de palavra reservada

O pacote `com.example.java_league.record` usa uma palavra reservada do Java como nome. Renomear para `request` ou `payload` evita ambiguidades com ferramentas de análise e futuros releases do compilador.

## 4. Design Patterns

### 4.1 Strategy + Sealed Classes para os cenários de lance

O `if-else` triplo em `PlayerService.bid()` representa três estratégias de negócio distintas. Com Java 21, sealed classes tornam esses cenários um tipo fechado, verificado pelo compilador.

**Sealed interface para o resultado do lance:**

```java
public sealed interface BidOutcome
    permits BidOutcome.FirstBid, BidOutcome.HighestBid, BidOutcome.LowestBid {

    record FirstBid(Long teamId, Long playerId, Long newPrice) implements BidOutcome {}
    record HighestBid(Long teamIdWinner, Long teamIdLoser, Long playerId, Long newPrice) implements BidOutcome {}
    record LowestBid(Long teamIdOwner, Long teamIdBidder, Long playerId, Long newPrice) implements BidOutcome {}
}
```

O service retorna um `BidOutcome`, e o controller/WebSocket usa pattern matching para transformá-lo em `BidResponseDTO`:

```java
BidResponseDTO response = switch (outcome) {
    case BidOutcome.FirstBid fb    -> new BidResponseDTO(fb.newPrice(), fb.teamId(), null, "FIRST_BID", ...);
    case BidOutcome.HighestBid hb  -> new BidResponseDTO(hb.newPrice(), hb.teamIdWinner(), hb.teamIdLoser(), "HIGHEST_BID", ...);
    case BidOutcome.LowestBid lb   -> new BidResponseDTO(lb.newPrice(), lb.teamIdOwner(), lb.teamIdBidder(), "LOWEST_BID", ...);
};
```

O compilador exige que todos os casos sejam tratados - sem risco de esquecer um cenário.

### 4.2 Enum para os tipos de lance

Mesmo sem sealed classes, substituir as strings `"FIRST_BID"`, `"HIGHEST_BID"`, `"LOWEST_BID"` por um `enum` elimina erros de digitação e permite autocompletar:

```java
public enum BidType {
    FIRST_BID, HIGHEST_BID, LOWEST_BID
}
```

Atualmente o campo `message` em `BidResponseDTO` é `String`. Alterá-lo para `BidType` torna o contrato da API explícito.

### 4.3 Factory Method para `BidResponseDTO`

A construção do `BidResponseDTO` hoje é feita com setters espalhados pelo `if-else`. Um static factory method por cenário deixa cada construção declarativa e autocontida:

```java
public record BidResponseDTO(...) {
    public static BidResponseDTO firstBid(Player player, Team team, ZonedDateTime date) {
        return new BidResponseDTO(player.getPrice(), player.getId(), date, BidType.FIRST_BID, null, team.getId());
    }

    public static BidResponseDTO highestBid(Player player, Team winner, Team loser, ZonedDateTime date) {
        return new BidResponseDTO(player.getPrice(), player.getId(), date, BidType.HIGHEST_BID, loser.getId(), winner.getId());
    }

    public static BidResponseDTO lowestBid(Player player, Team owner, Team bidder, ZonedDateTime date) {
        return new BidResponseDTO(player.getPrice(), player.getId(), date, BidType.LOWEST_BID, bidder.getId(), owner.getId());
    }
}
```

### 4.4 Observer (já presente via WebSocket - consolidar)

O padrão Observer está parcialmente implementado: o `SimpMessagingTemplate` notifica os clientes WebSocket quando um lance ocorre. O problema é que a notificação está acoplada dentro do service (`BidService.save()`), mas o fluxo principal de lance (`PlayerService.bid()`) notifica pelo controller.

Centralizar a publicação via Spring Events (`ApplicationEventPublisher`) desacopla o service de mensageria:

```java
// No service, após processar o lance:
eventPublisher.publishEvent(new BidProcessedEvent(this, bidResponseDTO));

// Em um listener separado:
@EventListener
public void onBidProcessed(BidProcessedEvent event) {
    simpMessagingTemplate.convertAndSend("/topic/bid", event.getBidResponse());
}
```

Assim o `PlayerService` não depende mais de `SimpMessagingTemplate` - a responsabilidade de notificação fica isolada.
