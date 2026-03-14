# 2Do - Task Management API

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17.7-blue)
![Redis](https://img.shields.io/badge/Redis-Distributed-red?logo=redis)
![Azure](https://img.shields.io/badge/Azure-Deployed-0078D4?logo=microsoft-azure)
![CI/CD](https://img.shields.io/badge/GitHub_Actions-Enabled-2088FF?logo=github-actions)
![Security](https://img.shields.io/badge/JWT-Security-red)

## Live Demo
**[2Do Web Application](https://gentle-cliff-06c31ee03.6.azurestaticapps.net/)**
> **Note:** Please allow **15-20 seconds** for the initial request. The backend Docker container may take a moment to wake up if it has been idle.

## About The Project

**2Do** is a robust, scalable, and secure Backend REST API providing a comprehensive solution for personal and collaborative task management with role-based access control.

This project follows **Clean Architecture** principles, utilizing **SOLID** design patterns, and implements industry-standard security measures using **JWT** and **Distributed Rate Limiting**.

## Live API Documentation (Swagger)

**[Live Swagger Documentation](https://todo-backend.icyfield-7f90fdb3.germanywestcentral.azurecontainerapps.io/swagger-ui/index.html)**

## Frontend Repository

**[View Frontend Repository (Angular UI)](https://github.com/kaandroids/2Do-frontend)**

## Tech Stack

* **Core:** Java 21, Spring Boot 3.4
* **Database:** PostgreSQL 16, Redis (Caching, Rate Limiting & JWT Blacklist)
* **Migrations:** Flyway
* **Containerization:** Docker & Docker Compose
* **Testing:** JUnit 5, Mockito
* **Security:** Spring Security 6, JWT, Bucket4j (Rate Limiting)
* **ORM & Mapping:** Hibernate / JPA, MapStruct
* **Tools:** Lombok, Maven
* **Documentation:** OpenAPI (Swagger UI)

## Key Features

### Task Management
* Full CRUD for personal and group tasks
* Priority levels (HIGH, MEDIUM, LOW) and due dates
* Completion tracking with toggle
* **Task privacy** — private tasks are only visible to the creator and explicitly assigned members
* **Assignees** — multiple group members can be assigned to a single task

### Groups & Collaboration
* Create named workspaces (groups) with a description
* Invite members by email — invitee must accept or decline before joining
* Per-member granular permissions:

| Permission | Description |
|---|---|
| `CAN_CREATE` | Create tasks within the group |
| `CAN_EDIT` | Edit any group task |
| `CAN_DELETE` | Delete any group task |
| `CAN_INVITE` | Invite new members to the group |
| `CAN_MANAGE` | Manage member permissions — automatically grants all other permissions |

* Group owner always has full unrestricted access
* Privacy-aware task visibility: members only see tasks they are entitled to

### AI Voice Task Generation
* **Google Gemini API** transforms natural language voice recordings into structured tasks
* Extracts title, description, priority, and due date automatically via prompt engineering

### Security & Infrastructure
* Stateless JWT authentication with Redis-backed blacklist on logout
* Distributed rate limiting via Bucket4j + Redis (10 req/min per IP on auth endpoints)
* `X-Forwarded-For` resolution for accurate IP tracking behind Azure Load Balancers
* Automated CI/CD — every push to `main` is tested, containerised, and deployed to Azure Container Apps

## Project Structure

```text
src/main/java/com/example/_Do
├── auth/          # Registration, login, JWT
├── config/        # Security, Swagger, app config
├── exception/     # Global exception handling
├── group/         # Groups, members, invitations, permissions
│   ├── controller/
│   ├── dto/
│   ├── entity/    # Group, GroupMember, GroupInvitation, GroupPermission
│   ├── repository/
│   └── service/
├── task/          # Tasks (personal + group)
│   ├── controller/
│   ├── dto/
│   ├── entity/    # Task (with assignees, privacy, group FK)
│   ├── mapper/
│   ├── repository/
│   └── service/
└── user/          # User domain
```

## API Endpoints

### Auth
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Register a new user |
| `POST` | `/api/v1/auth/authenticate` | Login, returns JWT |
| `POST` | `/api/v1/auth/logout` | Invalidate JWT |

### Tasks
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/tasks` | Create a task (personal or group) |
| `GET` | `/api/v1/tasks` | Get all accessible tasks |
| `PUT` | `/api/v1/tasks/{id}` | Update a task |
| `DELETE` | `/api/v1/tasks/{id}` | Delete a task |
| `PATCH` | `/api/v1/tasks/{id}/toggle` | Toggle completion |

### Groups
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/groups` | Create a group |
| `GET` | `/api/v1/groups` | Get my groups |
| `DELETE` | `/api/v1/groups/{id}` | Delete a group (owner only) |
| `GET` | `/api/v1/groups/{id}/members` | List members with permissions |
| `POST` | `/api/v1/groups/{id}/invitations` | Invite a member by email |
| `PUT` | `/api/v1/groups/{id}/members/{uid}/permissions` | Update member permissions |
| `DELETE` | `/api/v1/groups/{id}/members/{uid}` | Remove a member |

### Invitations
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/invitations` | Get my pending invitations |
| `POST` | `/api/v1/invitations/{id}/accept` | Accept an invitation |
| `POST` | `/api/v1/invitations/{id}/decline` | Decline an invitation |

## Getting Started

### Prerequisites
* **Docker Desktop**
* **Java 21 JDK** & **Maven** (for local development only)

### Option 1: Full Docker Setup (Recommended)

```bash
git clone https://github.com/kaandroids/2Do-backend.git
cd 2Do-backend
docker-compose up --build
```

API available at `http://localhost:8080`.

### Option 2: Local Development

```bash
# Start only the database and Redis
docker-compose up postgres redis -d

# Run the app
./mvnw spring-boot:run
```

## Swagger Authentication

1. Register via `/api/v1/auth/register`
2. Login via `/api/v1/auth/authenticate` — copy the JWT token
3. Click **Authorize** in Swagger UI and paste the token (no `Bearer ` prefix needed)

## Running Tests

```bash
./mvnw test
```

> Requires **Java 21**. Java 25+ may have Mockito/ByteBuddy compatibility issues.

## Roadmap

- [x] Cloud deployment on Azure
- [x] JWT authentication + Redis blacklist
- [x] Distributed rate limiting (Bucket4j + Redis)
- [x] Flyway database migrations
- [x] CI/CD pipeline (GitHub Actions)
- [x] AI voice task generation (Google Gemini)
- [x] Groups & collaborative task management
- [x] Per-member granular permissions with Master role
- [x] Task privacy & assignees
- [ ] OAuth2 social login (Google/GitHub)
- [ ] Email notifications for invitations and registration
- [ ] Real-time updates via WebSockets

## Contact

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/kaan-kara-0a720439b/)
[![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:kaan403@icloud.com)
