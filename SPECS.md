# SPECS.md
# Sistema de Backoffice Empresarial – Gestión de Patrocinios de Atletas

## 1. Descripción del proyecto

Sistema web interno para gestionar atletas patrocinados por una empresa, sus contratos y los beneficios asociados.

El proyecto se desarrollará como un **MVP orientado a una demo de portafolio**. El objetivo principal es demostrar capacidades de desarrollo backend con Java y Spring Boot, modelado de relaciones, reglas de negocio, gestión de fechas, estados y una interfaz web sencilla con Thymeleaf.

No se busca construir un sistema empresarial completo ni incorporar características de escalabilidad, seguridad avanzada o infraestructura innecesaria para una demo.

---

## 2. Objetivo del MVP

Permitir que un usuario de backoffice pueda:

1. Registrar atletas.
2. Consultar la lista de atletas.
3. Ver el detalle de un atleta.
4. Registrar contratos asociados a un atleta.
5. Asociar beneficios a un contrato.
6. Consultar el estado de los contratos.
7. Identificar contratos próximos a vencer.
8. Consultar un dashboard general con el estado de los contratos.

---

## 3. Alcance

### Incluido

- CRUD de atletas.
- CRUD de contratos necesario para el flujo del MVP.
- Gestión de beneficios.
- Relación atleta → contratos.
- Relación contrato → beneficios.
- Estados de contrato.
- Cálculo de contratos por vencer según fechas.
- Dashboard de indicadores.
- Alertas visuales simples para próximos vencimientos.
- Interfaz web con Thymeleaf.
- Persistencia con PostgreSQL.
- Validaciones básicas de formularios.

### Fuera de alcance

No se implementarán en esta versión:

- Autenticación y autorización.
- Roles de usuario.
- Registro de auditoría.
- Notificaciones por correo.
- Integraciones con servicios externos.
- Carga de documentos o archivos.
- Firma digital de contratos.
- Pagos reales.
- API pública.
- Microservicios.
- Docker u orquestación.
- Redis, mensajería o caché distribuida.
- Frontend SPA con React, Angular o Vue.
- Sistema multiempresa.
- Internacionalización.

---

## 4. Stack tecnológico

### Backend

- Java 21 LTS.
- Spring Boot.
- Spring MVC / Spring Web.
- Spring Data JPA.
- Hibernate como implementación JPA.
- Bean Validation.
- Lombok.
- Maven.

### Frontend

- Thymeleaf.
- HTML5.
- CSS.
- JavaScript únicamente cuando aporte valor al MVP.

### Base de datos

- PostgreSQL.

### Herramientas de desarrollo

- IDE compatible con Java 21.
- Git.
- GitHub.

---

## 5. Arquitectura

Se utilizará una arquitectura monolítica sencilla basada en capas.

```text
Controller
   ↓
Service
   ↓
Repository
   ↓
Database
```

Las vistas seguirán el flujo:

```text
Controller
   ↓
Thymeleaf
   ↓
HTML
```

### Capas

#### Controller

Responsable de recibir las peticiones HTTP, validar los datos de entrada y devolver las vistas correspondientes.

#### Service

Contendrá la lógica de negocio, especialmente:

- creación y actualización de contratos;
- reglas sobre contratos activos;
- cálculo del estado relacionado con las fechas;
- validaciones que involucren más de una entidad;
- preparación de información para el dashboard.

#### Repository

Responsable del acceso a PostgreSQL mediante Spring Data JPA.

#### Entity

Representará las entidades persistidas en la base de datos.

#### DTO / Form Objects

Se utilizarán cuando ayuden a separar los datos de formularios o vistas de las entidades JPA. No se crearán DTOs innecesarios únicamente por seguir una estructura rígida.

---

## 6. Entidades principales

El MVP tendrá cuatro conceptos principales:

1. Atleta.
2. Contrato.
3. Beneficio.
4. Relación Contrato-Beneficio.

---

## 7. Entidad Atleta

Representa a un deportista patrocinado por la empresa.

### Campos

| Campo | Tipo | Requerido | Descripción |
|---|---|---:|---|
| id | Long | Sí | Identificador único. |
| fullName | String | Sí | Nombre completo del atleta. |
| discipline | String | Sí | Disciplina deportiva. |
| country | String | Sí | País del atleta. |
| status | Enum | Sí | Estado actual del atleta. |

### Estado del atleta

```text
ACTIVE
INACTIVE
```

### Reglas

- El nombre completo es obligatorio.
- La disciplina es obligatoria.
- El país es obligatorio.
- Un atleta inactivo no se elimina automáticamente y conserva su historial.
- Un atleta inactivo no puede recibir nuevos contratos.
- Un atleta puede marcarse como inactivo aunque tenga contratos que no hayan vencido; esos contratos se conservan.

---

## 8. Entidad Contrato

Representa un acuerdo de patrocinio entre la empresa y un atleta.

### Campos

| Campo | Tipo | Requerido | Descripción |
|---|---|---:|---|
| id | Long | Sí | Identificador único. |
| athlete | Athlete | Sí | Atleta asociado. |
| startDate | LocalDate | Sí | Inicio del contrato. |
| endDate | LocalDate | Sí | Finalización del contrato. |
| notes | String | No | Información adicional. |

### Estados

```text
DRAFT
IN_FORCE
EXPIRING
EXPIRED
```

### Regla de estados

El estado de vigencia **no se almacena**: el sistema lo calcula en cada consulta a partir de las fechas del contrato y la fecha actual (`Clock`, ver §22). Por lo tanto, el contrato no tiene un campo de estado en la base de datos.

Reglas:

```text
DRAFT
    Fecha actual < startDate
    (el contrato todavía no inicia).

IN_FORCE
    Fecha actual >= startDate
    y
    faltan más de 30 días para endDate.

EXPIRING
    Fecha actual >= startDate
    y
    faltan entre 0 y 30 días para endDate.

EXPIRED
    Fecha actual > endDate.
```

Los cuatro estados son mutuamente excluyentes: dada una fecha actual, todo contrato tiene exactamente uno.

### Regla de exclusividad

Un atleta podrá tener múltiples contratos históricos, pero no podrá tener contratos con fechas solapadas.

Al crear o editar un contrato, su rango de fechas (inclusivo en ambos extremos) se compara contra todos los demás contratos del mismo atleta cuya fecha de finalización no haya pasado (`IN_FORCE`, `EXPIRING` y `DRAFT`). Los contratos `DRAFT` cuentan porque pasarán a estar en vigor. Los contratos `EXPIRED` no se consideran. Al editar, se excluye el propio contrato.

### Regla de fechas

```text
endDate >= startDate
```

No se permitirán contratos donde la fecha de finalización sea anterior a la fecha de inicio.

Ambas fechas son obligatorias. No existen contratos indefinidos: todo contrato tiene fecha de finalización.

---

## 9. Entidad Beneficio

Representa un beneficio que puede formar parte de un contrato de patrocinio.

### Campos

| Campo | Tipo | Requerido | Descripción |
|---|---|---:|---|
| id | Long | Sí | Identificador único. |
| name | String | Sí | Nombre del beneficio. |
| type | Enum | Sí | Tipo de beneficio. |
| description | String | No | Descripción del beneficio. |

### Tipos de beneficio

```text
EQUIPMENT
PAYMENT
EVENT
```

Ejemplos:

- EQUIPMENT → Uniforme oficial.
- PAYMENT → Bonificación mensual.
- EVENT → Participación en evento de marca.

---

## 10. Relación Contrato – Beneficio

Un contrato puede contener múltiples beneficios y un mismo beneficio puede ser utilizado en múltiples contratos.

Relación conceptual:

```text
Contract N <------> N Benefit
```

En la base de datos se resolverá mediante una tabla intermedia.

```text
contract_benefit
```

### Regla

Un beneficio no pertenece exclusivamente a un contrato. Puede reutilizarse en diferentes contratos.

---

## 11. Relaciones entre entidades

```text
Athlete 1 ───────── N Contract

Contract N ─────── N Benefit
```

No se permitirá que un contrato exista sin un atleta asociado.

---

## 12. Gestión del estado de contratos

El estado deberá presentarse claramente en la interfaz mediante etiquetas visuales.

Ejemplo conceptual:

```text
DRAFT        → Contrato en preparación
IN_FORCE     → Contrato activo
EXPIRING     → Vence en máximo 30 días
EXPIRED      → Fecha de finalización superada
```

La fuente principal para determinar `IN_FORCE`, `EXPIRING` y `EXPIRED` serán las fechas del contrato.

El estado `DRAFT` representa una condición previa al inicio del contrato.

---

## 13. Regla de contratos por vencer

Un contrato se considerará **por vencer** cuando su fecha de finalización esté dentro de los próximos 30 días y todavía no haya vencido.

Ejemplo:

```text
Hoy: 12/08/2026
Fin: 25/08/2026

Resultado: EXPIRING
```

Un contrato que finalice hoy también se considera dentro de la ventana de vencimiento, siempre que no haya pasado su fecha de finalización.

---

## 14. Dashboard

El dashboard será la pantalla inicial del backoffice.

### Indicadores

Debe mostrar como mínimo:

- Atletas activos (solo los atletas en estado `ACTIVE`).
- Contratos vigentes.
- Contratos por vencer.
- Contratos vencidos.
- Contratos en borrador.

Los indicadores de contratos cuentan todos los contratos, sin importar el estado del atleta.

### Próximos vencimientos

Se mostrará una lista de contratos por vencer, ordenada desde el vencimiento más cercano al más lejano.

Ejemplo:

```text
Próximos vencimientos

Laura Gómez      Natación      13 días
Andrés Ruiz      Ciclismo      21 días
Carlos Pérez     Atletismo     28 días
```

---

## 15. Pantallas

### 15.1 Dashboard

Ruta propuesta:

```text
/
```

o

```text
/dashboard
```

Contenido:

- tarjetas de indicadores;
- contratos próximos a vencer;
- acceso a atletas.

---

### 15.2 Lista de atletas

Ruta propuesta:

```text
/athletes
```

Debe permitir:

- visualizar atletas;
- acceder al detalle;
- crear un nuevo atleta.

Columnas sugeridas:

```text
Nombre | Disciplina | País | Estado | Acción
```

---

### 15.3 Crear atleta

Ruta propuesta:

```text
/athletes/new
```

Campos:

- nombre completo;
- disciplina;
- país;
- estado.

---

### 15.4 Detalle de atleta

Ruta propuesta:

```text
/athletes/{id}
```

Debe mostrar:

- información del atleta;
- contratos asociados;
- estado de cada contrato;
- beneficios del contrato;
- acceso para crear un contrato.

---

### 15.5 Crear contrato

Ruta propuesta:

```text
/athletes/{id}/contracts/new
```

Campos:

- fecha de inicio;
- fecha de finalización;
- observaciones;
- beneficios.

---

### 15.6 Detalle de contrato

Ruta propuesta:

```text
/contracts/{id}
```

Debe mostrar:

- atleta;
- fechas;
- estado;
- observaciones;
- beneficios asociados.

---

## 16. Validaciones

Se utilizará Bean Validation cuando sea apropiado.

### Atleta

- `fullName` no puede estar vacío ni superar los 150 caracteres.
- `discipline` no puede estar vacía ni superar los 100 caracteres.
- `country` no puede estar vacío ni superar los 100 caracteres.

### Contrato

- Atleta obligatorio y activo.
- Fecha de inicio obligatoria.
- Fecha de finalización obligatoria.
- Fecha final no puede ser menor que la inicial.
- No permitir solapamiento con otro contrato del mismo atleta que no haya vencido (ver §8).
- `notes` no puede superar los 500 caracteres.

### Beneficio

- Nombre obligatorio, máximo 100 caracteres.
- Tipo obligatorio.
- Descripción opcional, máximo 500 caracteres.

Las mismas validaciones se aplican al crear y al editar un beneficio.

Los mensajes de validación deben mostrarse en la interfaz de forma comprensible.

---

## 17. Eliminación de registros

Para el MVP se priorizará evitar eliminaciones que rompan el historial.

### Atletas

No se eliminará automáticamente un atleta con contratos históricos. Se utilizará su estado `INACTIVE`.

### Contratos

Se priorizará conservar los contratos para mantener el historial.

### Beneficios

Su eliminación deberá evitar romper asociaciones existentes. Un beneficio solo se elimina si no está asociado a ningún contrato; si está asociado, se muestra un mensaje en español y no se elimina.

---

## 18. Persistencia

La aplicación utilizará PostgreSQL como base de datos principal.

### Tablas conceptuales

```text
athlete
contract
benefit
contract_benefit
```

### Claves

- Todas las entidades principales tendrán una clave primaria `id`.
- Las relaciones utilizarán claves foráneas.
- `contract_benefit` tendrá claves foráneas hacia `contract` y `benefit`.
- La longitud de las columnas de texto será coherente con los límites de validación de §16.

---

## 19. Enums

Se utilizarán enums Java para los valores controlados.

Ejemplo conceptual:

```java
public enum AthleteStatus {
    ACTIVE,
    INACTIVE
}
```

```java
public enum ContractStatus {
    DRAFT,
    IN_FORCE,
    EXPIRING,
    EXPIRED
}
```

```java
public enum BenefitType {
    EQUIPMENT,
    PAYMENT,
    EVENT
}
```

---

## 20. Lombok

Se utilizará Lombok para reducir código repetitivo en las entidades y clases donde resulte conveniente.

Uso esperado:

- `@Getter`
- `@Setter`
- `@NoArgsConstructor`
- `@AllArgsConstructor`
- `@Builder` cuando aporte claridad
- otras anotaciones únicamente cuando estén justificadas.

No se utilizará Lombok indiscriminadamente. Se evitarán anotaciones que oculten demasiado comportamiento en entidades JPA si perjudican la claridad del proyecto.

---

## 21. Servicios principales

Se espera como mínimo una separación aproximada de responsabilidades:

```text
AthleteService
ContractService
BenefitService
DashboardService
```

### ContractService

Será el servicio con mayor lógica de negocio.

Responsabilidades:

- crear contratos;
- modificar contratos;
- consultar contratos;
- validar fechas;
- comprobar solapamientos;
- determinar estado por fechas.

### DashboardService

Responsable de reunir los datos necesarios para mostrar:

- conteo de atletas activos;
- conteo por estado de contrato;
- próximos vencimientos.

---

## 22. Manejo del estado y fecha actual

La lógica de contratos deberá utilizar la fecha actual de manera controlada para facilitar pruebas.

Se recomienda utilizar `LocalDate` y, cuando sea necesario centralizar la fecha actual, apoyarse en `Clock` en la capa de servicio.

Esto permitirá probar fácilmente escenarios como:

- contrato en borrador;
- contrato vigente;
- contrato por vencer;
- contrato vencido.

---

## 23. Datos iniciales para la demo

El proyecto deberá disponer de datos de ejemplo suficientes para que el dashboard sea atractivo inmediatamente después de arrancar la aplicación.

Se recomienda incluir:

- varios atletas;
- contratos vigentes;
- al menos un contrato por vencer;
- al menos un contrato vencido;
- algún contrato en borrador;
- beneficios de los tres tipos.

Los datos deberán permitir demostrar todas las reglas principales durante la presentación.

---

## 24. Manejo de errores

El MVP tendrá manejo sencillo de errores orientado a usuario.

Casos mínimos:

- atleta inexistente;
- contrato inexistente;
- datos de formulario inválidos;
- fechas inválidas;
- intento de crear contrato solapado.

No se implementará una plataforma avanzada de observabilidad o manejo distribuido de errores.

---

## 25. Estructura aproximada del proyecto

```text
src/main/java/com/example/sponsorships/
│
├── controller/
│   ├── DashboardController.java
│   ├── AthleteController.java
│   ├── ContractController.java
│   └── BenefitController.java
│
├── service/
│   ├── DashboardService.java
│   ├── AthleteService.java
│   ├── ContractService.java
│   └── BenefitService.java
│
├── repository/
│   ├── AthleteRepository.java
│   ├── ContractRepository.java
│   └── BenefitRepository.java
│
├── entity/
│   ├── Athlete.java
│   ├── Contract.java
│   └── Benefit.java
│
├── dto/
│   └── ...
│
├── enums/
│   ├── AthleteStatus.java
│   ├── ContractStatus.java
│   └── BenefitType.java
│
└── config/
    └── ...
```

La estructura podrá ajustarse durante la implementación cuando exista una razón concreta para hacerlo.

---

## 26. Thymeleaf

Las vistas se organizarán de forma sencilla.

Estructura aproximada:

```text
src/main/resources/
│
├── templates/
│   ├── dashboard.html
│   ├── athletes/
│   │   ├── list.html
│   │   ├── form.html
│   │   └── detail.html
│   │
│   ├── contracts/
│   │   ├── form.html
│   │   └── detail.html
│   │
│   └── fragments/
│       ├── navbar.html
│       └── messages.html
│
└── static/
    ├── css/
    │   └── styles.css
    └── js/
        └── app.js
```

No se creará una estructura de frontend compleja.

---

## 27. Endpoints / rutas web principales

Aunque el proyecto utilizará Spring MVC y Thymeleaf en lugar de un frontend SPA, las rutas seguirán una organización RESTful siempre que tenga sentido.

### Atletas

```text
GET    /athletes
GET    /athletes/new
POST   /athletes
GET    /athletes/{id}
GET    /athletes/{id}/edit
POST   /athletes/{id}
```

### Contratos

```text
GET    /athletes/{id}/contracts/new
POST   /athletes/{id}/contracts
GET    /contracts/{id}
GET    /contracts/{id}/edit
POST   /contracts/{id}
```

### Beneficios

```text
GET    /benefits
GET    /benefits/new
POST   /benefits
GET    /benefits/{id}/edit
POST   /benefits/{id}
POST   /benefits/{id}/delete
```

Un beneficio solo se puede eliminar si no está asociado a ningún contrato. Si lo está, el sistema muestra un mensaje en español y no lo elimina (ver §17).

---

## 28. Reglas de negocio resumidas

1. Un atleta puede tener múltiples contratos históricos.
2. Un atleta no puede tener un contrato cuyas fechas se solapen con las de otro contrato suyo que no haya vencido, incluidos los que todavía no inician.
3. La fecha final debe ser igual o posterior a la fecha inicial.
4. Los contratos que todavía no inician se consideran en borrador.
5. Los contratos iniciados se consideran vigentes mientras falten más de 30 días para su finalización.
6. Un contrato se considera por vencer cuando faltan entre 0 y 30 días.
7. Un contrato cuya fecha final ya pasó se considera vencido.
8. Un contrato puede tener varios beneficios.
9. Un beneficio puede asociarse a varios contratos.
10. El dashboard debe mostrar los principales estados de los contratos.
11. Los contratos próximos a vencer deben aparecer como alerta visual simple.
12. El historial de contratos debe conservarse.
13. Todo contrato tiene fecha de inicio y de finalización; no existen contratos indefinidos.
14. No se pueden crear contratos para un atleta inactivo.
15. El estado de vigencia no se almacena; se calcula con la fecha actual.

---

## 29. Flujo principal del usuario

```text
Dashboard
    ↓
Lista de atletas
    ↓
Seleccionar atleta
    ↓
Ver contratos
    ↓
Crear contrato
    ↓
Seleccionar beneficios
    ↓
Guardar contrato
    ↓
El sistema determina su estado
    ↓
Dashboard actualizado
```

---

## 30. Flujo de demo recomendado

La aplicación deberá poder demostrarse en pocos minutos.

### Escenario 1 – Dashboard

Mostrar:

- cantidad de atletas activos;
- contratos vigentes;
- contratos por vencer;
- contratos vencidos;
- contratos en borrador;
- próximos vencimientos.

### Escenario 2 – Atleta

Entrar a un atleta y mostrar sus contratos históricos.

### Escenario 3 – Nuevo contrato

Crear un contrato con fechas determinadas y asociarle varios beneficios.

### Escenario 4 – Regla de negocio

Intentar crear un contrato que se solape con otro contrato que no haya vencido y mostrar el mensaje de validación.

### Escenario 5 – Estado automático

Mostrar cómo un contrato queda clasificado como `EXPIRING` según su fecha de finalización.

---

## 31. Pruebas

El objetivo no es lograr una cobertura exhaustiva, sino demostrar que las reglas principales funcionan correctamente.

### Pruebas prioritarias

- Crear atleta correctamente.
- Validar atleta con campos obligatorios vacíos.
- Crear contrato válido.
- Rechazar contrato con fecha final anterior a fecha inicial.
- Detectar contrato por vencer.
- Detectar contrato vencido.
- Detectar contrato vigente.
- Detectar contrato en borrador (todavía no inicia).
- Impedir solapamiento con contratos que no hayan vencido, incluidos los que todavía no inician.
- Permitir un contrato nuevo cuando los anteriores ya vencieron.
- Asociar múltiples beneficios a un contrato.
- Mostrar correctamente los indicadores del dashboard.

Se podrán utilizar pruebas unitarias para `Service` y pruebas de integración para los casos más importantes de persistencia.

Las pruebas de integración usarán una base de datos PostgreSQL local independiente (`sponsorships_test`) mediante un perfil `test`, con el esquema recreado en cada ejecución. No se utilizará H2 ni contenedores.

---

## 32. Configuración

La configuración de la aplicación se mantendrá sencilla.

Ejemplo conceptual:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/sponsorships}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}
server.port=${PORT:8080}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

spring.thymeleaf.cache=false
```

La configuración real se define mediante variables de entorno; los valores por defecto sirven solo para el entorno local.

Las credenciales no deberán subirse al repositorio.

Perfil de pruebas (`application-test.properties`):

```properties
spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/sponsorships_test}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## 33. Criterios de aceptación del MVP

El proyecto podrá considerarse terminado cuando:

- La aplicación arranque correctamente con Java 21.
- PostgreSQL almacene los datos correctamente.
- Se puedan registrar atletas.
- Se puedan consultar atletas.
- Se puedan crear y consultar contratos.
- Se puedan asociar beneficios a contratos.
- Las fechas determinen correctamente los estados.
- Se impidan contratos con fechas solapadas con otro contrato del atleta que no haya vencido.
- El dashboard muestre información correcta.
- Exista una alerta visual de contratos próximos a vencer.
- La interfaz sea suficientemente clara para una demo de portafolio.
- Existan datos iniciales de demostración.
- Las reglas principales tengan pruebas.

---

## 34. Decisiones deliberadas de simplicidad

Este proyecto es un MVP de portafolio. Por lo tanto:

- Se mantendrá como monolito.
- No habrá arquitectura de microservicios.
- No habrá autenticación.
- No habrá frontend independiente.
- El MVP se desarrolla y se prueba en local. Su despliegue en AWS es la fase final, posterior al desarrollo, y no modifica el alcance funcional ni los criterios de aceptación.
- No habrá despliegue distribuido.
- No habrá funcionalidades de administración innecesarias.
- No se añadirá complejidad hasta que una necesidad real del MVP la justifique.

La prioridad será demostrar **buen modelado, lógica de negocio clara y código mantenible**.

---

## 35. Orden de implementación

El desarrollo se realizará en este orden aproximado:

### Fase 1 – Inicialización

- Crear proyecto Spring Boot.
- Configurar Java 21.
- Configurar Maven.
- Agregar dependencias.
- Configurar PostgreSQL.
- Configurar Thymeleaf.
- Configurar Lombok.

### Fase 2 – Modelo

- Crear enums.
- Crear entidades.
- Configurar relaciones JPA.
- Crear repositories.

### Fase 3 – Atletas

- Crear service.
- Crear controller.
- Crear vistas Thymeleaf.
- Implementar validaciones.

### Fase 4 – Contratos

- Crear service.
- Crear controller.
- Crear vistas.
- Implementar reglas de fechas.
- Implementar detección de solapamientos.

### Fase 5 – Beneficios

- Crear gestión de beneficios.
- Implementar relación con contratos.

### Fase 6 – Dashboard

- Crear consultas necesarias.
- Crear DashboardService.
- Crear vista principal.
- Agregar indicadores.
- Agregar próximos vencimientos.

### Fase 7 – Datos de demo

- Crear datos iniciales.
- Verificar los cuatro estados.
- Preparar escenario de demostración.

### Fase 8 – Pruebas y pulido

- Probar reglas de negocio.
- Corregir validaciones.
- Revisar navegación.
- Mejorar presentación visual sin sobrecargar el proyecto.

---

## 36. Resultado esperado

El resultado final será una aplicación web de backoffice que permita gestionar atletas patrocinados, contratos y beneficios desde una interfaz sencilla.

La demo debe transmitir principalmente estas capacidades técnicas:

```text
Java 21
   ↓
Spring Boot
   ↓
Spring MVC
   ↓
Service / lógica de negocio
   ↓
Spring Data JPA
   ↓
PostgreSQL
   ↓
Thymeleaf
```

El proyecto deberá ser suficientemente pequeño para completarse y explicarse fácilmente en una entrevista, pero suficientemente completo para demostrar:

- modelado de entidades;
- relaciones JPA;
- CRUD;
- validaciones;
- lógica de negocio;
- manejo de fechas;
- estados;
- consultas agregadas;
- persistencia;
- construcción de una interfaz empresarial sencilla.

---

## 37. Idioma del código y vocabulario

Todo el código está en inglés: identificadores, nombres de tablas, rutas, plantillas, comentarios, commits, logs y pruebas. El español se usa solo en lo que ve la persona usuaria en la interfaz (textos de pantallas, mensajes de validación y mensajes de excepciones de negocio) y en la documentación.

La etiqueta en español de cada valor de enum que se muestra en la interfaz (por ejemplo, `EXPIRING` → "Por vencer") se define junto al propio enum.

| Término del documento | Nombre en el código |
|---|---|
| Atleta | `Athlete`, tabla `athlete`, ruta `/athletes` |
| Contrato | `Contract`, tabla `contract`, ruta `/contracts` |
| Beneficio | `Benefit`, tabla `benefit` |
| Relación Contrato–Beneficio | tabla `contract_benefit` |
| Estado del atleta (`ACTIVO`, `INACTIVO`) | `AthleteStatus` (`ACTIVE`, `INACTIVE`) |
| Estado del contrato (`BORRADOR`, `VIGENTE`, `POR_VENCER`, `VENCIDO`) | `ContractStatus` (`DRAFT`, `IN_FORCE`, `EXPIRING`, `EXPIRED`) |
| Tipo de beneficio (`EQUIPO`, `PAGO`, `EVENTO`) | `BenefitType` (`EQUIPMENT`, `PAYMENT`, `EVENT`) |
| Nombre completo, disciplina, país | `fullName`, `discipline`, `country` |
| Fecha de inicio, fecha de finalización, observaciones | `startDate`, `endDate`, `notes` |
| Paquete base y base de datos | `com.example.sponsorships`, `sponsorships` |

---

## 38. Estado del documento

**Versión:** 1.6

**Tipo de proyecto:** MVP de portafolio

**Estado:** Especificación inicial lista para implementación.

### Cambios respecto a la versión 1.5

- §21: los próximos vencimientos se calculan en `DashboardService` (que reutiliza `ContractService.determineStatus`); se retira esa responsabilidad de `ContractService` para que coincida con la implementación.

### Cambios respecto a la versión 1.4

- §16, §17 y §27: se habilita editar beneficios (nombre, tipo y descripción, con las mismas validaciones) y eliminar un beneficio solo si no está asociado a ningún contrato; si lo está, se muestra un mensaje en español y no se elimina. Rutas `GET /benefits/{id}/edit`, `POST /benefits/{id}` y `POST /benefits/{id}/delete`.

### Cambios respecto a la versión 1.0

- §32: la configuración de base de datos y el puerto se leen de variables de entorno (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PORT`), con valores por defecto solo para el entorno local.
- §34: el despliegue en AWS pasa a ser la fase final, posterior al desarrollo del MVP; no modifica el alcance funcional ni los criterios de aceptación.
- Moneda: este documento no maneja importes, por lo que no hubo nada que convertir a USD.
- Todo el código pasa a inglés (identificadores, tablas, rutas, plantillas, paquete y base de datos); el español queda solo para la interfaz. Se agregó el vocabulario en §37.
- §7, §8, §16 y §28: todo contrato tiene fecha de finalización (no hay contratos indefinidos) y un atleta inactivo no puede recibir nuevos contratos.
- §8, §12, §15.5, §16, §28 y §31: el estado de vigencia ya no se almacena, se calcula desde las fechas con `Clock`; `DRAFT` significa que el contrato todavía no inicia y se elimina el campo "estado inicial" del formulario.
- §8, §16 y §28: el solapamiento se comprueba contra todos los contratos del atleta que no hayan vencido (`IN_FORCE`, `EXPIRING` y `DRAFT`), con fechas inclusivas en ambos extremos; al editar se excluye el propio contrato.
- §16 y §18: límites de texto (`fullName` 150, `discipline` y `country` 100, `notes` 500, nombre de beneficio 100 y descripción 500), con longitud de columna coherente.
- §7: un atleta puede marcarse como inactivo aunque tenga contratos sin vencer.
- §14 y §21: el dashboard muestra los atletas activos; los indicadores de contratos cuentan todos los contratos.
- §31 y §32: las pruebas de integración usan una base PostgreSQL local `sponsorships_test` con el perfil `test`.
