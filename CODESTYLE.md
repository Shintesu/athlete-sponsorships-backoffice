# Code Style

Rules for writing code in this repository. Short and opinionated. No formatter or linter is configured, so every rule is caught in review.

If a rule below conflicts with what an IDE, a formatter preset or a tutorial suggests, this file wins. If it conflicts with `SPECS.md`, the SPECS wins.

---

## 0. The four rules that matter most

1. **English for all code.** Identifiers (domain names included), comments, commits, logs, tests, branch names. Spanish only for text shown to users (see §1).
2. **No decorative separators in source code.** Never `// ------------`, never `/* ===== */`, never box drawings.
3. **No over-explained comments.** If the code says it, the comment must not repeat it.
4. **Credentials stay out of logs and messages.** `DB_PASSWORD` and any connection string that carries credentials never reach a log line or an exception message.

---

## 1. Language

All code is written in English: identifiers (domain names included), table names, routes, template names, comments, commit messages, log lines, test names and branch names. `SPECS.md` §37 lists the English name of every domain term.

Spanish is used only for text the user sees on the interface:
- Thymeleaf labels and page text.
- Validation messages.
- Flash messages.
- The message of business exceptions.
- The Spanish label of each enum value shown in the UI (for example `ContractStatus.EXPIRING` → "Por vencer"), defined next to the enum.

Exceptions that signal programming errors (illegal state, null parameter) use English messages and never reach the user.

```java
// bad
// Recorre los contratos del atleta y busca solapamientos
for (Contrato contrato : contratos) { }
```

```java
// good
// Skip the contract being edited, otherwise it always overlaps with itself.
for (Contract contract : contracts) { }
```

Identifiers use ASCII only. No accents, no emoji, no non-Latin scripts in names, package paths or file names. String literals shown to users may contain any Unicode they need (`ñ`, accents).

---

## 2. Comments

### 2.1 Do not explain what the code already says

```java
// bad
// increment the counter by one
counter++;

// bad
/**
 * Gets the discipline.
 * @return the discipline
 */
public String getDiscipline() { return discipline; }
```

Delete both. The second one is worse than nothing: it costs a review, it costs a merge conflict, and it says less than the signature.

### 2.2 Comment the *why*, the non-obvious, and the trap

```java
// good
// Compare against the injected clock, never LocalDate.now(): tests rely on a fixed date.
LocalDate today = LocalDate.now(clock);
```

```java
// good
// A contract that ends today is still inside the 30-day window: endDate is inclusive (SPECS §13).
return !today.isAfter(contract.getEndDate());
```

Good comment triggers: a workaround for a framework or database quirk, an ordering constraint, a time-handling decision, a spec reference (`SPECS §13`), a deliberate deviation from the obvious approach.

### 2.3 Javadoc

Javadoc is optional. Use it only where a method has a contract the signature cannot express: preconditions, thrown exceptions, side effects. As filler it is forbidden.

Write it as a contract, not a paraphrase:

```java
/**
 * Creates a contract for the given athlete.
 *
 * @throws AthleteNotFoundException if the athlete does not exist
 * @throws InactiveAthleteException if the athlete is inactive
 * @throws InvalidDateRangeException if endDate is before startDate
 * @throws OverlappingContractException if it overlaps a contract of the same athlete that has not expired
 */
public Contract createContract(Long athleteId, ContractForm form) {
```

No `@author` tags. Git knows. No `@version` tags. The build knows.

### 2.4 Banned comment forms

- Separator banners of any kind, including Unicode box characters.
- Commented-out code. Delete it; git has it.
- Changelog comments (`// modified by X on 2026-01-01`).
- `// TODO` without an issue.

```java
// bad
// TODO fix this later

// good
// TODO(#12): extract the 30-day window into configuration.
```

`FIXME` and `HACK` are allowed under the same rule. A `TODO` that outlives the roadmap phase it was written in is a bug report, not a comment.

---

## 3. Formatting

- 4 spaces, no tabs.
- 120 column soft limit. Break at a semantic boundary, not at column 121.
- Braces on the same line, always present, even for a one-statement `if`.
- One statement per line. One declaration per line.
- Blank lines separate logical blocks. A method needing five blank-line groups probably wants to be five methods.
- Imports: no wildcards, no unused, ordered `java` → `javax` → third-party → `com.example`.
- Files end with a newline. No trailing whitespace.
- `final` on parameters is not required. `final` on fields is (see §7).

No formatter is configured. Set the IDE to 4 spaces and 120 columns. Do not argue formatting in review; change this file or move on.

### 3.1 Separators, explicitly

```java
// bad
// =============================================================================
// Contract rules
// =============================================================================

// bad
/* --------------------------- helpers --------------------------- */

// bad
//////////////////////////////////////////////////////////
```

If a file needs visual sections to be navigable, the file is too big. Split it. Reviewers reject any comment line made of three or more repeated punctuation characters.

Markdown documentation may use `---` as a horizontal rule. That is documentation, not source.

---

## 4. Naming

| Kind              | Convention                | Example                            |
| ----------------- | ------------------------- | ---------------------------------- |
| Package           | lowercase, no underscores | `com.example.sponsorships.service` |
| Class / interface | `PascalCase`              | `ContractService`                  |
| Method / field    | `camelCase`               | `determineStatus`, `startDate`     |
| Constant          | `UPPER_SNAKE_CASE`        | `EXPIRING_WINDOW_DAYS`             |
| Enum value        | `UPPER_SNAKE_CASE`        | `EXPIRING`                         |
| Type parameter    | single letter or short    | `T`, `ID`                          |
| Test method       | `camelCase`, a sentence   | `rejectsOverlappingContracts`      |

Rules:

- No Hungarian notation, no `m_` or `_` prefixes, no `I` prefix on interfaces.
- No abbreviations except universally known ones (`id`, `dto`, `sql`, `uuid`).
- No `Impl` suffix. A class with a single implementation does not need an interface: `ContractService` is a class. If an interface is introduced, name it for the role and its implementations for the specific.
- Layer suffixes are part of the name: `AthleteController`, `AthleteService`, `AthleteRepository`, `ContractForm`, `OverlappingContractException`.
- Form objects end in `Form`. Read-only view models end in `View`.
- Boolean methods read as assertions: `isActive()`, `isExpiring()`.
- Do not name a class `Utils`, `Helper`, `Manager` or `Common`. Name it for what it does.

---

## 5. Packages and layer boundaries

```
com.example.sponsorships.controller   HTTP and views: receive, validate, delegate, return a view
com.example.sponsorships.service      business rules
com.example.sponsorships.repository   Spring Data JPA access
com.example.sponsorships.entity       JPA entities
com.example.sponsorships.dto          forms and view models, only where they help
com.example.sponsorships.enums        AthleteStatus, ContractStatus, BenefitType
com.example.sponsorships.config       configuration, including the Clock bean
```

Hard boundaries:

- Dependencies point one way: `controller` → `service` → `repository`.
- Controllers do not call repositories and contain no business rules.
- Services own the business rules. They do not depend on web types (`HttpServletRequest`, `Model`).
- Repositories contain no business rules.
- Templates contain no business logic. They display values the controller or the service prepared; the contract status is never computed in a template.
- Forms and DTOs exist only where they separate form or view data from JPA entities (SPECS §5). Do not create them to follow a rigid structure.

---

## 6. Dependencies

The stack is defined in `SPECS.md`. Do not add a dependency outside it.

Adding one requires: an issue explaining why the existing stack cannot cover it, its license (MIT / Apache-2.0 / BSD only), and explicit approval. Expect to be told no.

Lombok is part of the stack (SPECS §20), with limits:

- Allowed: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, and `@Builder` when it adds clarity. Any other annotation needs a justification in the PR.
- On JPA entities: no `@Data`, and no `@ToString` or `@EqualsAndHashCode` on entities with relationships. They hide behavior and can trigger lazy loading or infinite recursion.
- Services and controllers use hand-written constructors for injection.

AssertJ and Mockito come bundled with Spring Boot Test and need no separate approval. SLF4J comes with Spring Boot.

---

## 7. Design

- **Form objects that back Thymeleaf forms are plain mutable beans**, because form binding works through getters and setters. Read-only view models may be `record`.
- **JPA entities** keep a no-arg constructor and mutable fields, as JPA requires.
- **Fields in controllers, services and components are `private final`** and injected through the constructor. No field injection.
- **Time comes from an injected `Clock`.** Never call `LocalDate.now()` or `LocalDateTime.now()` in business logic (SPECS §22). Tests use `Clock.fixed`.
- **No static mutable state.** Shared state lives in Spring-managed beans.
- **Constructors do not do work.** No I/O, no database access in a constructor.
- **Return `Optional<T>` for a genuinely absent single result.** Never return `Optional` from a field, never as a parameter, never for collections. Return an empty collection.
- **Never return `null` from a public method.** Empty collection, `Optional`, or throw.
- **Never accept `null` silently.** Validate at the boundary with Bean Validation on forms. Inside services, throw `NullPointerException` with the parameter name (`Objects.requireNonNull(value, "value")`), or accept it explicitly and document it.
- **No checked exceptions.** Business exceptions are unchecked.

---

## 8. Errors

- Business exceptions are unchecked and named for the cases in SPECS §7 and §24: `AthleteNotFoundException`, `ContractNotFoundException`, `InactiveAthleteException`, `InvalidDateRangeException`, `OverlappingContractException`.
- A business exception carries the message shown to the user, in Spanish, without technical details.
- Expected business errors (invalid form, not found, overlap) reach the user as a readable message in the page (SPECS §16, §24). Unexpected errors show a generic error page. Never show a stack trace, SQL or a vendor message.
- Every message names the concrete thing: the athlete, the contract, the date range. `"Datos inválidos"` is not a message. `"El contrato se solapa con otro contrato activo del atleta"` is.
- Messages never contain credentials.
- Persistence exceptions are translated into business exceptions and keep the original as the cause.
- Never `catch (Exception e) { }`. Never catch to log-and-rethrow the same thing twice.
- Fail early: validate in the Form and in the Service before touching the database.
- Do not use exceptions for control flow. Ask with `existsBy...`; do not catch `ContractNotFoundException` to decide.

---

## 9. Logging

### 9.1 The rules

- Log through SLF4J: `private static final Logger log = LoggerFactory.getLogger(ContractService.class);`. Never `System.out.println`, never `System.err.println`, never `e.printStackTrace()`.
- Messages are parameterized, never concatenated.

```java
// bad
log.debug("created contract " + contractId + " for athlete " + athleteId);

// good
log.debug("created contract {} for athlete {}", contractId, athleteId);
```

- Guard expensive argument computation with `log.isDebugEnabled()`.
- Never log database credentials or connection strings that carry them.
- Expected business errors (invalid form, not found, overlap) are not logged above `DEBUG`.

### 9.2 Levels

| Level   | Use for                                                           |
| ------- | ----------------------------------------------------------------- |
| `ERROR` | The operation failed unexpectedly (a 500)                         |
| `WARN`  | Handled but suspicious                                            |
| `INFO`  | Once-per-lifecycle facts: application started, demo data loaded   |
| `DEBUG` | Per-operation detail: contract created, status determined         |
| `TRACE` | Per-item detail                                                   |

`INFO` must stay quiet in a steady-state application. If a request handler can reach it, it is `DEBUG`.

---

## 10. Tests

- JUnit 5, AssertJ and Mockito. Mockito is for unit tests of Services, replacing repositories. If a unit needs heavy mocking, the design is wrong.
- Unit tests of Services run without a database or a Spring context, with `Clock.fixed` wherever dates matter.
- Integration tests run against the local `sponsorships_test` PostgreSQL database under the `test` profile. No H2, no Testcontainers (SPECS §31).
- Test names are sentences: `rejectsOverlappingContracts`, `classifiesContractAsExpiringWhenItEndsToday`.
- One behaviour per test. No assertion-free tests. No `Thread.sleep`.
- Priority tests follow SPECS §31: valid and invalid creation, date range, the three date-derived states, overlap, multiple benefits, dashboard indicators.
- Every bug fix begins with a failing test.
- Route and template changes are verified by walking the demo flow (SPECS §30).

---

## 11. Git

- Conventional commits: `feat(contract): reject overlapping contracts`, `fix(dashboard): count contracts ending today as expiring`, `docs(readme): …`, `refactor`, `test`, `perf`, `build`, `chore`.
- Imperative mood, lowercase subject, no trailing period, under 72 characters.
- The body explains *why*. The diff already explains *what*.
- One logical change per commit. Formatting-only changes go in their own commit.
- Branches: `feat/dashboard-indicators`, `fix/overlap-on-edit`.
- Never commit build output (`target/`), IDE files, or credentials.

---

## 12. PR checklist

Copy this into the PR description:

```
- [ ] English everywhere except text shown to users
- [ ] No separator comments, no commented-out code, no redundant javadoc
- [ ] No dependency added outside the SPECS stack, Lombok within the allowed list
- [ ] Business rules only in Services, no business logic in templates
- [ ] Time comes from the injected Clock
- [ ] No credentials in code, logs or exception messages
- [ ] Tests added and mvn test is green
- [ ] SPECS.md and README.md updated if behavior changed
```

---

## 13. Enforcement

No formatter or linter is configured in this project. Everything above is caught in review. Reviewers are expected to cite the rule number.

```bash
mvn test
```
