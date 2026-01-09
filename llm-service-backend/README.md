# LLM Report Service - Backend

This is the backend microservice for the LLM Report Service, built with **Spring Boot 3**.

## 🧠 Core Features

*   **Asynchronous Processing**: Uses Spring `@Async` to handle LLM inference tasks in the background, ensuring the API remains responsive.
*   **Intelligent Analysis**: Integrates with OpenAI API (or compatible LLMs) to analyze text and generate structured reports.
*   **Caching Strategy**: Implements **Redis** caching for report results to reduce repetitive LLM calls and save costs.
*   **Robust Architecture**: Docker-ready, with clean separation of concerns (Controller, Service, Repository).

## 🔌 API Endpoints

### 1. Submit Report Task
*   **URL**: `POST /api/reports`
*   **Body**:
    ```json
    {
      "userInput": "Text content to analyze..."
    }
    ```
*   **Response**: Returns a Task ID + Initial Status (`PENDING`).

### 2. Get Report Status/Result
*   **URL**: `GET /api/reports/{taskId}`
*   **Response**:
    ```json
    {
      "id": 1,
      "status": "COMPLETED",
      "reportResult": "{ ...JSON String... }",
      "createAt": "2023-..."
    }
    ```

### 3. Get All Reports
*   **URL**: `GET /api/reports`
*   **Response**: List of all reports.

## 🛠 Configuration

Configuration is handled in `src/main/resources/application.properties`.

*   **Database**: MySQL
*   **Cache**: Redis
*   **AI Provider**: OpenAI (set `OPENAI_API_KEY` env var)
