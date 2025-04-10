
# Proovitöö - Code List Application

This project implements a web application for working with code lists, based on the "Proovitöö ülesanne". It consists of three main components running in Docker containers: a React frontend, a Spring Boot backend (BFF), and a Spring Boot Base64 decoding microservice, all integrated with Keycloak for authentication and authorization[cite: 2, 63].

## Project Structure

The repository is organized as follows:

```
.
├── compose.yml           # Main Docker Compose file orchestrating all services
├── .env.example          # Example environment file for secrets
├── .gitignore            # Root gitignore file (add cert dirs, .env)
│
├── backend/              # Phase 1: Spring Boot Backend (BFF)
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── .gitignore        # Backend specific ignores (e.g., target/)
│
├── base64-service/       # Phase 2: Spring Boot Base64 Decoding Microservice
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── .gitignore        # Microservice specific ignores (e.g., target/)
│
├── frontend/             # Phase 3: React Frontend Application
│   ├── src/
│   ├── package.json
│   ├── vite.config.ts   
│   ├── tailwind.config.js
│   ├── postcss.config.js
│   ├── Dockerfile
│   ├── nginx.conf
│   └── .gitignore        # Frontend specific ignores (e.g., node_modules/, dist/)
│
├── certs-gen/            # Scripts/config for generating local certificates
├── database/             # Scripts/config for generating data.json
├── keycloak-setup/       # Scripts/config for setting up Keycloak realm/clients
│
└── # Other generated cert directories (ignored by git, e.g., certs/, ca-certs/, etc.)

```

## Prerequisites

* **Docker:** Required to build and run the containerized application stack. Install Docker Desktop or Docker Engine.
* **Docker Compose:** Required to orchestrate the multi-container setup. Usually included with Docker Desktop.
* **Git:** Required for cloning the repository.
* **Maven:** Required locally to build the Java application artifacts (`.war` files) before building the Docker images. Ensure Maven is installed and configured in your environment.
* **Node.js & npm:** Required locally if you need to modify frontend dependencies or run frontend commands outside of Docker (though the Docker build handles it).

## Configuration

This project uses Docker Compose. Some configuration, particularly secrets, is managed via an environment file.

1.  **Create Environment File:**
    * In the root directory (where `docker-compose.yml` is located), copy the example environment file:
        ```bash
        cp .env.example .env
        ```
    * **Important:** The `.env` file is ignored by Git (`.gitignore`) and should **not** be committed to version control.

2.  **Set Secret(s) in `.env`:**
    * Open the newly created `.env` file.
    * **`KEYCLOAK_BACKEND_CLIENT_SECRET`**: Replace the placeholder value with the actual client secret for the `devdemo-backend` client. This value can be found in the Keycloak setup documentation provided with the assignment (e.g., "Proovitöö keskkonna alus STO arendaja 2025.pdf" [cite: 59]). *(Note: This secret might only be strictly necessary if the backend were performing OIDC client flows, not just resource server validation).*

3.  **Other Configuration:**
    * Other necessary environment variables for the services (like Keycloak Issuer URI, Audience values, internal service URLs, data file paths) are configured directly within the `environment:` sections of the respective services in the `docker-compose.yml` file. Review this file if you need to adjust non-secret settings (though the defaults provided should work for the standard Docker Compose setup).

## Building the Application

Before running Docker Compose, you need to build the deployable artifacts for the Java services:

1.  **Build Backend:**
    ```bash
    cd backend
    mvn clean package
    cd ..
    ```
2.  **Build Base64 Service:**
    ```bash
    cd base64-service
    mvn clean package
    cd ..
    ```

## Running the Application

1.  **Navigate:** Open a terminal in the project's root directory (where `docker-compose.yml` is located).
2.  **Build Docker Images (Optional First Step):** Ensure the prerequisite Java builds are done, then build the Docker images:
    ```bash
    docker-compose build
    ```
3.  **Start Services:** Start all services using Docker Compose:
    ```bash
    # Run in foreground (logs visible, stop with Ctrl+C)
    docker-compose up

    # OR Run in detached mode (background)
    docker-compose up -d
    ```
    *(Using `docker-compose up --build` combines steps 2 and 3)*
4.  **Wait for Startup:** Allow a minute or two for all services to initialize, especially Keycloak (`idp`) running its setup scripts and the `datagen` service creating the data file[cite: 57].
5.  **Monitor Logs (Optional):** If running detached or want to check progress/errors:
    ```bash
    # Follow logs for all services
    docker-compose logs -f

    # Follow logs for a specific service (e.g., backend)
    docker-compose logs -f backend
    ```

## Accessing the Application

Once all services have started successfully:

* **Frontend UI:** Open your web browser to `http://localhost:8543` [cite: 65]
* **Backend API (Directly):** The API is available at `http://localhost:8080` (for testing with tools like Postman/curl) [cite: 65]
* **Keycloak Admin Console:** Access at `https://localhost:8864` [cite: 59, 65]
    * Login with username: `admin`, password: `admin` [cite: 59]

## Test Users

The Keycloak instance is configured with the following test users[cite: 60]:

* **Administrator:**
    * Username: `testadmin`
    * Password: `test`
    * Roles: `admin`
* **Regular User:**
    * Username: `test`
    * Password: `test`
    * Roles: (None specified, treated as authenticated user)
* **(From Docs) "Väärkasutaja":**
    * Username: `testv`
    * Password: `test`
    * Roles: `kasutaja` (This role isn't explicitly used by the application logic as described).

## Stopping the Application

1.  If running in the foreground (`docker-compose up`), press `Ctrl+C`.
2.  If running detached (`docker-compose up -d`), navigate to the root directory and run:
    ```bash
    docker-compose down
    ```
3.  To also remove the data volume (`devdata`) created by `datagen`, run:
    ```bash
    docker-compose down -v
    ```

## Technology Stack

* **Backend:** Java 21, Spring Boot 3, Spring Security (OAuth2 Resource Server), Maven, Tomcat 11
* **Base64 Service:** Java 21, Spring Boot 3, Spring Security (OAuth2 Resource Server), Maven, Tomcat 11
* **Frontend:** React 19, TypeScript, Vite, Tailwind CSS, Axios, react-oidc-context, Nginx
* **Authentication:** Keycloak (via Docker)
* **Orchestration:** Docker, Docker Compose
* **Utilities:** Bash scripting (for setup)
