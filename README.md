# 2Do - Task Management API 🚀

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17.7-blue)
![Redis](https://img.shields.io/badge/Redis-Distributed-red?logo=redis)
![Azure](https://img.shields.io/badge/Azure-Deployed-0078D4?logo=microsoft-azure)
![CI/CD](https://img.shields.io/badge/GitHub_Actions-Enabled-2088FF?logo=github-actions)
![Security](https://img.shields.io/badge/JWT-Security-red)

## 🚀 Live Demo
Access the latest version of the application here:
👉 **[2Do Web Application](https://gentle-cliff-06c31ee03.6.azurestaticapps.net/)**

## 📖 About The Project

**2Do** is a robust, scalable, and secure Backend REST API designed to demonstrate enterprise-level software development practices. It provides a comprehensive solution for task management with role-based access control.

This project follows **Clean Architecture** principles, utilizing **SOLID** design patterns, and implements industry-standard security measures using **JWT (JSON Web Tokens)** and **Distributed Rate Limiting**.

## 🚀 Live API Documentation (Swagger)

The API is deployed and ready for testing. You can explore the endpoints through the interactive Swagger UI:

👉 **[Live Swagger Documentation](https://todo-backend.icyfield-7f90fdb3.germanywestcentral.azurecontainerapps.io/swagger-ui/index.html)**

> **Note:** Since this is a cold-start environment, the first request might take a few seconds to wake up the service.

## 🔗 Frontend Repository

This project serves as the **Backend API** for the 2Do Task Management System.
The user interface is built with **Angular 17+** and **Bootstrap 5**.

You can find the frontend source code and installation instructions here:

👉 **[View Frontend Repository (Angular UI)](https://github.com/kaandroids/2Do-frontend)**

## 🛠 Tech Stack

* **Core:** Java 21, Spring Boot 3.4
* **Database:** PostgreSQL 16, Redis (Distributed Caching, Rate Limiting & JWT Blacklist)
* **Migrations:** Flyway (Schema Management)
* **Containerization:** Docker & Docker Compose
* **Testing:** JUnit 5, Mockito
* **Security:** Spring Security 6, JWT, Bucket4j (Rate Limiting)
* **ORM & Mapping:** Hibernate / JPA, MapStruct
* **Tools:** Lombok, Maven
* **Documentation:** OpenAPI (Swagger UI)

## 🏗 Key Features & Architecture

* **Automated CI/CD:** Every push to the main and feat branch is automatically tested, containerized, and deployed to Azure via GitHub Actions.
* **Distributed Rate Limiting:** Integrated request throttling using **Bucket4j** and **Redis**. This protects authentication endpoints (`/auth/**`) from brute-force and DDoS attacks by enforcing a limit of 10 requests per minute per IP.
* **Cloud-Ready Security:** Implements `X-Forwarded-For` header resolution to ensure accurate client IP tracking when deployed behind Azure Load Balancers or Gateways.
* **Role-Based Access Control (RBAC):** Granular permission management for `ADMIN` and `USER` roles using Spring Security `PreAuthorize`.
* **Task Management:** Full CRUD operations for managing tasks with ownership security (Users can only access their own tasks).
* **Stateless Authentication & Blacklisting:** Secure authentication via JWT, enhanced with a Redis-backed Blacklist.
* **DTO Pattern:** Strict separation between persistence entities and API exposure layers.
* **Global Exception Handling:** Centralized error management using `@RestControllerAdvice` (AOP) with SRP-compliant mapping via `ErrorResponseMapper` and standardized JSON responses.
* **Unit Testing:** Comprehensive unit tests using **JUnit 5** and **Mockito** to ensure business logic reliability.
* **Performance:** Utilizes `MapStruct` for high-performance, type-safe object mapping and `FetchType.LAZY` for database optimization.
* **Validation:** Robust input validation using Jakarta Validation (`@Valid`, `@NotBlank`, etc.).
* **API Documentation:** Integrated **Swagger UI (OpenAPI 3)** for interactive API exploration and testing.
* **Database Migration & Versioning:** Integrated **Flyway** to ensure robust, version-controlled database schema management.

## 📂 Project Structure

A quick look at the top-level directory structure which adheres to Clean Architecture principles:

```text
src
├── main
│   ├── java/com/example/_Do
│   │   ├── auth          # Authentication logic (Register/Login)
│   │   ├── config        # Security, Swagger, Application Configs
│   │   ├── exception     # Global Exception Handling
│   │   ├── task          # Task Domain (Controller, Service, Entity, DTO)
│   │   ├── user          # User Domain (Controller, Service, Entity, DTO)
│   │   └── Application.java
│   └── resources         # Application properties
└── test                  # Unit Tests
```

### 🚀 Getting Started

Follow these steps to get the project up and running on your local machine.

### Prerequisites
* **Docker Desktop** (Required for database and containerized app)
* **Java 21 JDK** & **Maven** (Only required for local development)

### Installation

### Option 1: Full Docker Setup (Recommended)
This method builds the application image and starts both the Database and the API in isolated containers.

1.  **Clone the repository**
    ```bash
    git clone https://github.com/kaandroids/2Do.git
    cd 2Do
    ```

2.  **Build and Run**
    ```bash
    docker-compose up --build
    ```
    *This command compiles the code, creates the Docker image, and starts the services.*

3.  **Access the API**
    The application will be available at `http://localhost:8080`.

---

### Option 2: Local Development Setup
Use this method if you want to run the database in Docker but debug the Java code locally in your IDE.

1.  **Start only the Database**
    ```bash
    docker-compose up postgres -d
    ```

2.  **Run the Application**
    ```bash
    mvn spring-boot:run
    ```
    *Ensure your `application.properties` points to `localhost:5432` for this method.*

## 📚 API Documentation

Once the application is running, you can access the interactive API documentation via Swagger UI:

👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

### How to Authenticate in Swagger:
1.  Use the `/api/v1/auth/register` endpoint to create a new user.
2.  Use the `/api/v1/auth/authenticate` endpoint to log in and receive a **JWT Token**.
3.  Click the **"Authorize"** button at the top right of the Swagger page.
4.  Paste the token directly (e.g., `eyJhbGci...`). **Do not** add the "Bearer " prefix, Swagger handles it automatically.

## 🔐 Security & Roles

The system implements a dual-role mechanism:

| Role | Permissions |
| :--- | :--- |
| **ROLE_USER** | Can manage their own tasks and view their own profile. |
| **ROLE_ADMIN** | Has full access to all resources, including user management and system-wide task oversight. |

### 📝 Task Endpoints

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/tasks` | Create a new task | USER, ADMIN |
| `GET` | `/api/v1/tasks` | Get all tasks for current user | USER, ADMIN |
| `GET` | `/api/v1/tasks/{id}` | Get specific task details | USER, ADMIN |
| `PUT` | `/api/v1/tasks/{id}` | Update an existing task | USER, ADMIN |
| `DELETE` | `/api/v1/tasks/{id}` | Delete a task | USER, ADMIN |

## 🔧 Troubleshooting

**"Password authentication failed" or Database Connection Issues:**
If you change database credentials in `docker-compose.yml`, the existing database volume might still retain the old password. To fix this, you need to reset the volume:

```bash
# Stop containers and remove volumes
docker-compose down -v

# Rebuild and start
docker-compose up --build
```

## 🧪 Running Tests

> **⚠️ Important Note:**
> This project is optimized for **Java 21 LTS**.
> If you are using **Java 25 (Early Access)** or newer, you may encounter compatibility issues with testing libraries (like Mockito/ByteBuddy).
> Please ensure your `JAVA_HOME` environment variable is set to **JDK 21** before running the wrapper.

The application covers business logic with Unit Tests using **JUnit 5** and **Mockito**.

To run the tests using Maven Wrapper (ensures compatibility):
```powershell
./mvnw test
```

## 🔮 Roadmap & Future Enhancements

The project is continuously evolving. The following features are planned for future releases to enhance security and scalability:

- [x] **Cloud Deployment:** Live on Azure.
- [X] **Advanced Security (Rate Limiting):** Implement request throttling (using Bucket4j or Redis) to protect endpoints against Brute-Force and DDoS attacks.
- [X] **Secure Logout (JWT Blacklisting):** Integrate **Redis** to blacklist expired/logged-out tokens for true stateless session management.
- [x] **Database Migration:** Integrate **Flyway** for robust, version-controlled database schema management.
- [ ] **OAuth2 Integration:** Support for social login (Google/GitHub) authentication.
- [ ] **Email Notification System:** Asynchronous email delivery for user registration verification and password resets.
- [X] **CI/CD Pipeline:** Automate testing and deployment workflows using **GitHub Actions**.



## 🤝 Contact

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/kaan-kara-0a720439b/)
[![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:kaan403@icloud.com)