# Backoffice de Patrocinios de Atletas

Sistema web interno para gestionar atletas patrocinados por una empresa, sus contratos y los beneficios asociados. MVP de portafolio construido con **Java 21**, **Spring Boot** y **Thymeleaf**, que pone el foco en el modelado de relaciones, las reglas de negocio y el manejo de fechas y estados.

---

## Funcionalidades

- Registro, consulta y edición de atletas. Los atletas con historial no se eliminan: pasan a `INACTIVE`. Un atleta inactivo no puede recibir nuevos contratos, pero se puede desactivar aunque tenga contratos sin vencer.
- Múltiples contratos históricos por atleta. Todo contrato tiene fecha de inicio y de fin: no existen contratos indefinidos.
- Estado del contrato determinado por sus fechas: `DRAFT`, `IN_FORCE`, `EXPIRING` (faltan entre 0 y 30 días) y `EXPIRED`.
- Validación que impide contratos con fechas solapadas con las de otro contrato del atleta que no haya vencido.
- Beneficios (`EQUIPO`, `PAGO`, `EVENTO`) reutilizables en varios contratos, con edición y eliminación segura: solo se elimina un beneficio que no esté asociado a ningún contrato.
- Dashboard con indicadores y lista de próximos vencimientos con alerta visual.
- Datos de demostración que mantienen representados los cuatro estados con el paso del tiempo.

---

## Stack tecnológico

| Tecnología                    | Uso                                              |
| ----------------------------- | ------------------------------------------------ |
| Java 21 LTS                   | Lenguaje principal                               |
| Spring Boot                   | Framework backend                                |
| Spring MVC                    | Controladores web                                |
| Spring Data JPA / Hibernate   | Persistencia y ORM                               |
| Bean Validation               | Validaciones de formularios                      |
| Lombok                        | Reducción de código repetitivo (uso acotado)     |
| Thymeleaf                     | Vistas renderizadas en el servidor               |
| PostgreSQL                    | Base de datos relacional                         |
| AWS (EC2 y RDS)               | Despliegue de la demo (plan gratuito)            |
| Maven                         | Gestión del proyecto                             |
| JUnit 5, AssertJ y Mockito    | Pruebas (incluidos en Spring Boot Test)          |

---

## Arquitectura

Monolito por capas:

```text
Navegador
   │
   ▼
Controller   (recibe peticiones, valida formularios, devuelve vistas)
   │
   ▼
Service      (reglas de negocio, estados por fecha, solapamientos)
   │
   ▼
Repository   (acceso a datos con Spring Data JPA)
   │
   ▼
PostgreSQL
```

Las vistas siguen el flujo `Controller → Thymeleaf → HTML`. Los objetos de formulario o DTO se usan solo cuando ayudan a separar los datos de la vista de las entidades JPA.

---

## Modelo de dominio

```text
Athlete 1 ───────── N Contract N ─────── N Benefit
                                  (tabla contract_benefit)
```

| Entidad  | Campos principales                             |
| -------- | ---------------------------------------------- |
| Athlete  | id, fullName, discipline, country, status      |
| Contract | id, athlete, startDate, endDate, notes         |
| Benefit  | id, name, type, description                    |

Un contrato no puede existir sin atleta, y un beneficio puede pertenecer a varios contratos.

---

## Reglas de negocio

- Un fin de contrato nunca puede ser anterior a su inicio.
- Un atleta puede tener varios contratos históricos, pero las fechas de un contrato no pueden solaparse con las de otro que no haya vencido, incluidos los que todavía no inician.
- El estado de vigencia no se guarda: se calcula con la fecha actual.

| Estado     | Condición                                        |
| ---------- | ------------------------------------------------ |
| `DRAFT`    | Todavía no inicia (su fecha de inicio no llega)  |
| `IN_FORCE` | Ya inició y faltan más de 30 días para su fin    |
| `EXPIRING` | Ya inició y faltan entre 0 y 30 días para su fin |
| `EXPIRED`  | Su fecha de fin ya pasó                          |

- El historial de atletas y contratos se conserva.
- Los campos de texto tienen longitud máxima: nombre completo 150, disciplina y país 100, observaciones 500, y en beneficios nombre 100 y descripción 500.

---

## Pantallas y rutas

| Pantalla            | Ruta                           |
| ------------------- | ------------------------------ |
| Dashboard           | `/` o `/dashboard`             |
| Lista de atletas    | `/athletes`                    |
| Crear atleta        | `/athletes/new`                |
| Detalle de atleta   | `/athletes/{id}`               |
| Editar atleta       | `/athletes/{id}/edit`          |
| Crear contrato      | `/athletes/{id}/contracts/new` |
| Detalle de contrato | `/contracts/{id}`              |
| Editar contrato     | `/contracts/{id}/edit`         |
| Lista de beneficios | `/benefits`                    |
| Crear beneficio     | `/benefits/new`                |
| Editar beneficio    | `/benefits/{id}/edit`          |

---

## Demo

La aplicación se puede demostrar en pocos minutos:

1. **Dashboard:** atletas activos, contratos por estado y próximos vencimientos.
2. **Atleta:** historial de contratos de un atleta.
3. **Nuevo contrato:** crear un contrato con fechas y varios beneficios.
4. **Regla de negocio:** intentar un contrato solapado y ver el mensaje de validación.
5. **Estado automático:** ver cómo un contrato queda como `EXPIRING` según su fecha de fin.

---

## Cómo ejecutar el proyecto

### Requisitos previos
- Java 21
- Maven
- PostgreSQL en ejecución

### Pasos

```bash
# 1. Clonar el repositorio
git clone <url-del-repositorio>
cd athlete-sponsorships-backoffice

# 2. Crear la base de datos
createdb sponsorships

# 3. Definir las variables de entorno
export DB_PASSWORD=postgres
# Opcionales (valores por defecto para entorno local):
# export DB_URL=jdbc:postgresql://localhost:5432/sponsorships
# export DB_USERNAME=postgres
# export PORT=8080

# 4. Ejecutar con los datos de demostración
SPRING_PROFILES_ACTIVE=demo mvn spring-boot:run
```

La aplicación quedará disponible en `http://localhost:8080`. Las credenciales nunca se almacenan en el repositorio.

---

## Despliegue en AWS

La demo corre en AWS con el plan gratuito: una instancia EC2 ejecuta el JAR y una base RDS PostgreSQL guarda los datos.

- **Demo en vivo:** `<url-de-la-demo>`, disponible mientras dure el plan gratuito de AWS (hasta abril de 2027).
- Funciona con datos de demostración y no tiene autenticación, por lo que no debe usarse con datos reales.
- La configuración entra por variables de entorno (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PORT`); no se versionan credenciales.
- El despliegue es manual: JAR más servicio `systemd`. No usa Docker ni CI/CD.

---

## Pruebas

- **Unit tests** de los Services con un `Clock` fijo: contrato vigente, por vencer y vencido; fechas inválidas; solapamiento; múltiples beneficios.
- **Integration tests** de los casos principales de persistencia, contra una base PostgreSQL local independiente (`sponsorships_test`). Requieren `DB_PASSWORD` definido, igual que la aplicación.

```bash
createdb sponsorships_test
mvn test
```

---

## Estructura del proyecto

```text
src/main/java/com/example/sponsorships/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── enums/
└── config/

src/main/resources/
├── application.properties
├── templates/
│   ├── dashboard.html
│   ├── athletes/
│   ├── contracts/
│   └── fragments/
└── static/
    ├── css/
    └── js/
```

---

## Fuera de alcance

Este MVP no incluye, de forma deliberada: autenticación y autorización, roles, registro de auditoría, notificaciones por correo, integraciones externas, carga de documentos, firma digital, pagos reales, API pública, microservicios, Docker, Redis o mensajería, frontend SPA, sistema multiempresa ni internacionalización.

---

## Objetivo del proyecto

Demostrar, en una aplicación pequeña pero completa, el modelado de entidades y relaciones JPA, CRUD, validaciones, lógica de negocio, manejo de fechas y estados, consultas agregadas y una interfaz empresarial sencilla, con decisiones que se pueden explicar con claridad en una entrevista técnica.
