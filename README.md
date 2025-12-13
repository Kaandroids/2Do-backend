# 2Do - Task Management API 🚀

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED)
![Security](https://img.shields.io/badge/JWT-Security-red)

## 📖 About The Project

**2Do** is a robust, scalable, and secure Backend REST API designed to demonstrate enterprise-level software development practices. It provides a comprehensive solution for task management with role-based access control.

This project follows **Clean Architecture** principles, utilizing **SOLID** design patterns, and implements industry-standard security measures using **JWT (JSON Web Tokens)**.

## 🛠 Tech Stack

* **Core:** Java 21, Spring Boot 3.4
* **Database:** PostgreSQL (Containerized via Docker)
* **Security:** Spring Security 6, JWT Authentication & Authorization
* **ORM & Mapping:** Hibernate / JPA, MapStruct (Compile-time mapping)
* **Tools:** Docker & Docker Compose, Lombok, Maven
* **Documentation:** OpenAPI (Swagger UI)

## 🏗 Key Features & Architecture

* **Role-Based Access Control (RBAC):** Granular permission management for `ADMIN` and `USER` roles using Spring Security `PreAuthorize`.
* **Stateless Authentication:** Secure and scalable authentication via JWT.
* **DTO Pattern:** Strict separation between persistence entities and API exposure layers.
* **Performance:** Utilizes `MapStruct` for high-performance, type-safe object mapping.
* **Validation:** Robust input validation using Jakarta Validation (`@Valid`, `@NotBlank`, etc.).
* **Containerization:** Fully dockerized environment for easy deployment and consistency.

## 🚀 Getting Started

Follow these steps to get the project up and running on your local machine.

### Prerequisites
* Docker & Docker Compose
* Java 21 JDK
* Maven

### Installation

1.  **Clone the repository**
    ```bash
    git clone [https://github.com/kaandroids/2Do.git](https://github.com/kaandroids/2Do.git)
    cd 2Do
    ```

2.  **Start the Database (Docker)**
    Spin up the PostgreSQL container using Docker Compose:
    ```bash
    docker-compose up -d
    ```

3.  **Run the Application**
    ```bash
    mvn spring-boot:run
    ```
    The application will start on `http://localhost:8080`.

## 📚 API Documentation

Once the application is running, you can access the interactive API documentation via Swagger UI:

👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

### How to Authenticate in Swagger:
1.  Use the `/api/v1/auth/register` endpoint to create a new user.
2.  Use the `/api/v1/auth/authenticate` endpoint to login and receive a **JWT Token**.
3.  Click the **"Authorize"** button at the top right of the Swagger page.
4.  Paste the token directly (e.g., `eyJhbGci...`). **Do not** add the "Bearer " prefix, Swagger handles it automatically.

## 🔐 Security & Roles

The system implements a dual-role mechanism:

| Role | Permissions |
| :--- | :--- |
| **ROLE_USER** | Can manage their own tasks and view their own profile. |
| **ROLE_ADMIN** | Has full access to all resources, including user management and system-wide task oversight. |

## 🤝 Contact

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/kaan-kara-0a720439b/)
[![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:kaan403@icloud.com)