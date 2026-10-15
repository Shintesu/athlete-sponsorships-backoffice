# Roadmap de Desarrollo — Backoffice de Patrocinios de Atletas

> Cada fase debe cerrar con la aplicación funcionando y sus pruebas en verde antes de avanzar a la siguiente.

## Fase 1 — Inicializar el proyecto (completada)
- Crear el proyecto con Spring Initializr (Java 21, Maven).
- Agregar dependencias: Spring Web, Thymeleaf, Spring Data JPA, Validation, driver de PostgreSQL y Lombok.
- Crear la estructura de paquetes de `com.example.sponsorships`: `controller`, `service`, `repository`, `entity`, `dto`, `enums`, `config`.
- Configurar `application.properties` con variables de entorno y valores por defecto locales (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PORT`).
- Crear la base de datos `sponsorships` y verificar que la aplicación arranque.

## Fase 2 — Modelo (completada)
- Crear los enums `AthleteStatus`, `ContractStatus` y `BenefitType`, con la etiqueta en español de cada valor junto al enum para mostrarla en la interfaz. `ContractStatus` no se guarda en la base de datos: se calcula.
- Crear las entidades `Athlete` (fullName, discipline, country, status), `Contract` (athlete, startDate, endDate, notes) y `Benefit` (name, type, description).
- Definir los límites de texto del SPECS §16: `fullName` 150, `discipline` y `country` 100, `notes` 500, nombre de beneficio 100 y descripción de beneficio 500, con la longitud de columna coherente.
- Configurar las relaciones: Athlete 1:N Contract y Contract N:M Benefit mediante la tabla `contract_benefit`.
- Crear los repositories con Spring Data JPA.
- Usar Lombok solo con las anotaciones permitidas por el SPECS §20, sin `@Data` en entidades.

## Fase 3 — Atletas (completada)
- Crear `AthleteService`, `AthleteController` y las vistas `list`, `form` y `detail`.
- Rutas: `GET /athletes`, `GET /athletes/new`, `POST /athletes`, `GET /athletes/{id}`, `GET /athletes/{id}/edit`, `POST /athletes/{id}`.
- Validar nombre completo, disciplina y país obligatorios, con mensajes en español.
- Mostrar un mensaje legible cuando el atleta no exista.
- No eliminar atletas con historial: marcarlos como `INACTIVE`.
- Permitir marcar como inactivo a un atleta aunque tenga contratos sin vencer; esos contratos se conservan.

## Fase 4 — Contratos (completada)
- Crear `ContractService` (el servicio con más lógica de negocio), `ContractController` y las vistas `form` y `detail`.
- Rutas: `GET /athletes/{id}/contracts/new`, `POST /athletes/{id}/contracts`, `GET /contracts/{id}`, `GET /contracts/{id}/edit`, `POST /contracts/{id}`.
- Validar que `endDate` sea igual o posterior a `startDate`, y que ambas fechas sean obligatorias: no existen contratos indefinidos.
- Rechazar contratos para atletas inactivos.
- Determinar el estado a partir de las fechas usando un `Clock` inyectado, sin persistirlo. El formulario de contrato no incluye estado inicial.
- Detectar solapamientos: comparar el rango de fechas (inclusivo en ambos extremos) contra todos los demás contratos del atleta que no hayan vencido (`IN_FORCE`, `EXPIRING` y `DRAFT`). Al editar, excluir el propio contrato.
- Mostrar mensajes legibles para contrato inexistente, atleta inactivo, fechas inválidas y contrato solapado.

## Fase 5 — Beneficios
- Crear `BenefitService` y una sección sencilla para consultar y crear beneficios.
- Asociar varios beneficios a un contrato desde su formulario; un beneficio puede reutilizarse en varios contratos.
- Mostrar los beneficios en el detalle del contrato y en el del atleta.

## Fase 6 — Dashboard
- Crear las consultas: atletas activos, contratos por estado (vigentes, por vencer, vencidos, en borrador) y próximos vencimientos ordenados del más cercano al más lejano.
- Crear `DashboardService`, `DashboardController` (ruta `/` o `/dashboard`) y la vista con tarjetas de indicadores.
- El indicador de atletas cuenta solo los activos ("Atletas activos"); los indicadores de contratos cuentan todos los contratos, sin importar el estado del atleta.
- Mostrar los próximos vencimientos con los días restantes como alerta visual simple.
- Dar acceso a la lista de atletas desde el dashboard.
- Calcular los conteos con las mismas condiciones de fecha que `ContractService` y verificar que coincidan.

## Fase 7 — Datos de demo
- Crear datos suficientes para que el dashboard se vea completo al arrancar: varios atletas, contratos en los cuatro estados y beneficios de los tres tipos.
- Generar las fechas relativas a la fecha actual, mediante un `CommandLineRunner` activado con el perfil `demo`, para que "por vencer" y "vencido" sigan representados con el paso del tiempo. No usar fechas fijas.
- Evitar duplicar los datos cuando la aplicación se reinicia.
- Preparar los cinco escenarios de demo del SPECS §30.

## Fase 8 — Pruebas y pulido
- Unit tests de los Services con `Clock.fixed`: contrato en borrador, vigente, por vencer y vencido; fecha final anterior a la inicial; solapamiento con contratos que no hayan vencido (incluidos los futuros) y contrato nuevo permitido cuando los anteriores ya vencieron; atleta con campos vacíos; contrato con múltiples beneficios.
- Verificar que el dashboard muestre los indicadores correctos.
- Integration tests para los casos más importantes de persistencia, contra la base PostgreSQL local `sponsorships_test` con el perfil `test` (SPECS §31 y §32). Crear la base antes de ejecutar las pruebas.
- Revisar validaciones y navegación, y mejorar la presentación visual sin sobrecargar el proyecto.

## Fase 9 — Documentación y preparación del portafolio
- Completar el README con descripción, stack, instrucciones de ejecución y capturas o GIF de los escenarios de demo.
- Verificar que no haya credenciales en el repositorio ni en su historial.
- Revisar la consistencia entre `SPECS.md` y la implementación final.
- Preparar la explicación de las decisiones técnicas para entrevistas: estado derivado de fechas con `Clock`, validación de solapamientos, relación N:M, borrado lógico y consultas agregadas.

## Fase 10 — Despliegue en AWS
- Desplegar solo cuando el proyecto esté terminado y listo para mostrarse: los créditos se consumen desde que se encienden los recursos.
- Usar el plan gratuito de AWS en la región us-east-2. El plan vence el 7 de abril de 2027: la demo se apaga entonces, salvo que se pase al plan de pago.
- Antes de crear recursos: activar alertas de presupuesto (AWS Budgets) y completar las actividades de créditos (EC2, RDS, Lambda, Bedrock y Budgets) para llegar a unos USD 200.
- Presupuesto aproximado: unos USD 22 al mes en créditos (RDS `db.t4g.micro`, EC2 `t3.micro` y 20 GB de almacenamiento), sin contar IPv4 pública, el disco de la EC2 ni el tráfico. Verificar en la calculadora de precios de AWS.
- Base de datos: RDS PostgreSQL `db.t4g.micro`, una sola zona de disponibilidad, 20 GB, sin acceso público.
- Aplicación: EC2 con Java 21, JAR generado con `mvn package` y ejecutado como servicio `systemd`, con el heap limitado (`-Xmx`) según la RAM. Empezar con `t3.micro`; subir a `t3.small` si la memoria o el arranque resultan lentos.
- Variables de entorno (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PORT`) y `SPRING_PROFILES_ACTIVE=demo` en un archivo con permisos restringidos, fuera del repositorio.
- Security Groups: HTTP abierto, SSH solo desde la IP propia, puerto 5432 solo desde la instancia EC2.
- En el primer arranque, `ddl-auto=update` crea el esquema y el perfil `demo` carga los datos.
- La aplicación no tiene autenticación: cualquiera con la URL puede crear y modificar datos. Usar solo datos de demo, y apagar la instancia o restringir el acceso por IP fuera de las demostraciones.
- Sin dominio propio la demo corre en HTTP.
- Recorrer los cinco escenarios de demo contra la URL pública.
- Documentar el despliegue y el enlace de la demo en el README, sin secretos, y detener los recursos cuando no se usen.

---

**Principios a mantener durante todo el desarrollo:**
- Simplicidad y buenas prácticas por encima de tecnología innecesaria.
- Lógica de negocio únicamente en los Services.
- Estados de vigencia derivados de las fechas con `Clock`, nunca de un valor manual.
- El historial de atletas y contratos se conserva.
- Credenciales fuera del repositorio.
- Despliegue solo al final y solo con datos de demo.
