# LLM Report Service

This is a Monorepo for an LLM-based intelligent report generation system, containing a complete backend service and frontend interface.

## 📂 Project Structure

The project uses a Monorepo structure to manage both frontend and backend code:

```
llm-report-service/
├── docker-compose.yml       # One-click startup for backend, frontend, and database
├── llm-service-backend/     # Backend Service (Spring Boot + MySQL)
└── llm-service-frontend/    # Frontend Interface (React + TypeScript + Vite)
```

## 🚀 Quick Start

You can start the entire application using Docker Compose.

### Prerequisites
*   [Docker Desktop](https://www.docker.com/products/docker-desktop/)
*   OpenAI API Key

### Steps to Run

1.  **Set Environment Variable**
    Set your OpenAI API Key in your terminal:
    ```bash
    export OPENAI_API_KEY=sk-your-api-key-here
    ```

2.  **Build and Start**
    Run the following command in the project root directory:
    ```bash
    docker-compose up --build
    ```

3.  **Access the Application**
    *   **Frontend**: [http://localhost](http://localhost) (Default port 80)
    *   **Backend API**: [http://localhost:8080/api](http://localhost:8080/api)
    *   **API Documentation (Swagger)**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
    *   **Database**: `localhost:3306` (Credentials found in `docker-compose.yml`)

## 🛠 Development Guide

If you need to develop a specific module individually, please refer to their respective documentation:

*   [Backend Documentation](./llm-service-backend/README.md)
*   [Frontend Documentation](./llm-service-frontend/README.md)
