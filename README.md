# LLM Report Service

This is a Monorepo for an LLM-based intelligent report generation system, containing a complete backend service and frontend interface.

## 💡 Project Concept

**LLM Report Service** is an intelligent document analysis and report generation system powered by Large Language Models (LLM). It automates the process of extracting insights from raw text and presenting them in a structured, easy-to-read format.

### Problems We Solve
1.  **Information Overload**: Parsing through large amounts of text to find key information is time-consuming.
    *   *Solution*: Automatically summarizes content and extracts key points using AI.
2.  **Lack of Standardization**: Manual reports often vary in quality and format.
    *   *Solution*: Generates consistent, structured JSON outputs with standard fields (Summary, Key Points, Confidence Score).
3.  **Slow Turnaround**: Waiting for manual analysis delays decision-making.
    *   *Solution*: Asynchronous background processing + Redis caching ensures rapid response times and high availability.
4.  **Integration Complexity**: connecting AI models to legacy systems is hard.
    *   *Solution*: A clean, dockeriazed microservice architecture with standard RESTful APIs.
## 🛠 Tech Stack

### Frontend
- **React 19**
- **TypeScript**
- **Vite**
- **Tailwind CSS 4**
- **Axios**

### Backend
- **Spring Boot 3.5.8** (Java 21)
- **Spring Data JPA**
- **MySQL 8.0**
- **Redis** (Cache & Data Structures)
- **OpenAI Java SDK**

## 📂 Project Structure

The project uses a Monorepo structure to manage both frontend and backend code:

```
llm-report-service/
├── docker-compose.yml       # One-click startup for backend, frontend, database, and redis
├── llm-service-backend/     # Backend Service (Spring Boot + MySQL + Redis)
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
    docker compose up --build
    ```

3.  **Access the Application**
    *   **Frontend**: [http://localhost:3000](http://localhost:3000)
    *   **Backend API**: [http://localhost:8080/api](http://localhost:8080/api)
    *   **API Documentation (Swagger)**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
    *   **Database**: `localhost:3307` (Credentials found in `docker-compose.yml`)
    *   **Redis**: `localhost:6379`

## 🛠 Development Guide

If you need to develop a specific module individually, please refer to their respective documentation:

*   [Backend Documentation](./llm-service-backend/README.md)
*   [Frontend Documentation](./llm-service-frontend/README.md)
