# AGENTS.md

Instrucciones para agentes de IA y para quien contribuya a **Backoffice de Patrocinios de Atletas**. Este archivo apunta a los demás documentos en lugar de copiarlos: ante cualquier duda, el documento enlazado manda.

---

## Orden de lectura y precedencia

Leer en este orden antes de escribir código:

1. `SPECS.md`: qué se construye y qué queda fuera. Es la fuente de verdad.
2. `CODESTYLE.md`: cómo se escribe el código.
3. `ROADMAP.md`: en qué orden se construye.
4. `README.md`: vista externa del proyecto terminado.

Si dos documentos se contradicen, gana el de menor número. Si el SPECS parece incompleto o ambiguo, señalarlo y preguntar; no desviarse en silencio.

---

## Proyecto y comandos

Aplicación web monolítica en Java 21 con Spring Boot, Spring MVC, Thymeleaf, Spring Data JPA, PostgreSQL, Lombok y Maven. Paquete base: `com.example.sponsorships`.

```bash
mvn spring-boot:run
mvn test
```

Variables de entorno: `DB_PASSWORD` (obligatoria, sin valor por defecto), `DB_URL`, `DB_USERNAME`, `PORT` y `TEST_DB_URL` (opcionales, con valores por defecto para entorno local). `mvn test` necesita una base PostgreSQL local llamada `sponsorships_test`.

---

## Reglas del proyecto

Detalle en el SPECS; estas son las que no se negocian.

- Arquitectura por capas: `Controller` → `Service` → `Repository`. La lógica de negocio vive solo en los Services; `ContractService` concentra la mayor parte. Los controllers reciben, validan y devuelven vistas.
- Los objetos de formulario o DTO se usan solo cuando separan los datos de la vista de las entidades JPA (SPECS §5). No crearlos por seguir una estructura rígida.
- El estado de vigencia del contrato no se guarda: se calcula desde las fechas (SPECS §8). `DRAFT` si todavía no inicia, `IN_FORCE` si faltan más de 30 días para su fin, `EXPIRING` si faltan entre 0 y 30 y `EXPIRED` si la fecha final ya pasó. El formulario de contrato no tiene estado inicial.
- La fecha actual siempre sale de un `Clock` inyectado (SPECS §22). Nunca `LocalDate.now()` en la lógica de negocio.
- `endDate` debe ser igual o posterior a `startDate`.
- Un contrato no puede solaparse en fechas (inclusivas) con otro contrato del mismo atleta que no haya vencido (`IN_FORCE`, `EXPIRING` o `DRAFT`). Se valida al crear y editar; al editar se excluye el propio contrato.
- Todo contrato tiene fecha de inicio y de fin. No existen contratos indefinidos.
- No se pueden crear contratos para un atleta inactivo.
- Los atletas con historial no se eliminan: pasan a `INACTIVE`. Los contratos se conservan.
- Un beneficio puede asociarse a varios contratos mediante `contract_benefit`.
- Longitudes máximas de texto (SPECS §16): `fullName` 150, `discipline` y `country` 100, `notes` 500, nombre de beneficio 100 y descripción de beneficio 500. La longitud de columna debe coincidir.
- Un atleta se puede marcar como inactivo aunque tenga contratos sin vencer; esos contratos se conservan y siguen contando en el dashboard.
- El dashboard muestra los atletas activos, no el total. Los indicadores de contratos cuentan todos los contratos.
- Las pruebas de integración usan la base PostgreSQL local `sponsorships_test` con el perfil `test` (SPECS §31 y §32). Sin H2 ni Testcontainers.
- Los valores controlados son enums (SPECS §19).
- Los mensajes y textos que ve la persona usuaria están en español (SPECS §16 y §24).
- No hay autenticación ni autorización. No agregarlas.
- El esquema se gestiona con `ddl-auto=update` según el SPECS §32.
- Credenciales y secretos nunca se versionan; la configuración entra por variables de entorno.
- Lombok solo con `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` y `@Builder` cuando aporte claridad. Nunca `@Data` en entidades. Otras anotaciones solo si están justificadas (SPECS §20).

---

## Alcance cerrado

No agregar nada de la lista de exclusiones del SPECS §3: autenticación, roles, auditoría, notificaciones por correo, integraciones externas, carga de documentos, firma digital, pagos reales, API pública, microservicios, Docker, Redis o mensajería, frontend SPA, sistema multiempresa ni internacionalización. Tampoco dependencias fuera del stack del SPECS. Si algo parece necesario, preguntar primero.

El despliegue en AWS es la Fase 10 y es manual (JAR más `systemd`): no agregar Docker, CI/CD ni infraestructura como código. En AWS solo se usan datos de demo, porque la aplicación no tiene autenticación.

---

## Estilo de código

`CODESTYLE.md` aplica completo. Resumen de lo que más se rompe:

- Todo el código está en inglés: identificadores (incluidos los nombres del dominio: `Athlete`, `Contract`, `Benefit`), tablas, rutas, plantillas, comentarios, commits, logs y tests. El vocabulario completo está en el SPECS §37.
- El español se usa solo en lo que ve la persona usuaria: textos de la interfaz, mensajes de validación y mensajes de excepciones de negocio.
- Sin separadores decorativos, sin comentarios que repiten el código, sin código comentado.
- Commits convencionales en inglés, por ejemplo `feat(contract): reject overlapping contracts`.
- Logging con SLF4J, nunca `System.out`, y nunca se registran credenciales de la base de datos.

---

## Forma de trabajo

- Seguir el `ROADMAP.md` fase por fase. No empezar una fase sin cerrar la anterior.
- Cerrar una fase significa que la aplicación arranca, `mvn test` está en verde y los tests de esa fase existen. Después, agregar `(completada)` al título de la fase en `ROADMAP.md`.
- Cambios pequeños y enfocados: una fase, un tema por commit.
- Ante una ambigüedad entre lo pedido y el SPECS, preguntar en lugar de asumir.

---

## Definición de terminado

- [ ] Compila y `mvn test` pasa
- [ ] La aplicación arranca y el flujo afectado funciona en el navegador
- [ ] Respeta las reglas del proyecto y el CODESTYLE
- [ ] Sin credenciales, sin `System.out`, sin código comentado
- [ ] Mensajes de commit en inglés y convencionales
- [ ] Fase marcada en `ROADMAP.md`
- [ ] `SPECS.md` y `README.md` actualizados si cambió el comportamiento
