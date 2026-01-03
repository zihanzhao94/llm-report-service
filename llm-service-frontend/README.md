# [⬅️ Back to Root](../README.md)

# LLM Report Service - Frontend

This is the frontend interface for the LLM Report Service, built with React + TypeScript + Vite, and styled using TailwindCSS.

## 🛠 Tech Stack
*   **Framework**: React 19
*   **Build Tool**: Vite
*   **Language**: TypeScript
*   **Styling**: TailwindCSS 4
*   **HTTP Client**: Axios

## 💻 Local Development

If you want to run the frontend locally (outside of Docker):

1.  **Navigate to directory**
    ```bash
    cd llm-service-frontend
    ```

2.  **Install Dependencies**
    ```bash
    npm install
    ```

3.  **Start Development Server**
    ```bash
    npm run dev
    ```
    The application will start at `http://localhost:5173`.

## 🔌 Connecting to Backend

In development mode, ensure the backend service is running at `localhost:8080`.
(If you encounter CORS issues, check the proxy configuration in `vite.config.ts`).

## 📦 Production Build

```bash
npm run build
```
The build artifacts will be output to the `dist` directory.
