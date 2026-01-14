package org.aidf.llm.report.service.service;

import com.openai.models.ChatModel;
import org.springframework.stereotype.Service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.ResponseCreateParams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service class for interacting with Large Language Model (LLM) APIs.
 * <p>
 * This service is responsible for:
 * <ul>
 * <li>Calling external LLM APIs (OpenAI, Claude, etc.)</li>
 * <li>Formatting prompts for report generation</li>
 * <li>Parsing and validating LLM responses</li>
 * <li>Converting LLM output to the required report format</li>
 * </ul>
 * <p>
 * The service abstracts the details of LLM API integration, allowing
 * the business logic layer to focus on task management rather than
 * LLM-specific implementation details.
 *
 * @author Zihan Zhao
 * @since 1.0
 */
@Service
public class LlmService {

    public String generateReport(String userInput) {

        // 1. Format the user input into a prompt
        // 1. Format the user input into a prompt
        String prompt = String.format(
                "You are an expert business analyst. Analyze the following text and generate a comprehensive professional report. "
                        +
                        "You MUST return ONLY valid JSON with this exact structure:\n" +
                        "{\n" +
                        "  \"summary\": \"A detailed executive summary (at least 3 sentences)\",\n" +
                        "  \"key_points\": [\"Insight 1\", \"Insight 2\", \"Insight 3\", \"Insight 4\", \"Insight 5\"],\n"
                        +
                        "  \"analysis\": \"A deep dive analysis of the content, discussing implications and context (at least 500 words)\",\n"
                        +
                        "  \"recommendations\": [\"Actionable item 1\", \"Actionable item 2\"],\n" +
                        "  \"confidence_score\": \n" +
                        "}\n\n" +
                        "Text to analyze:\n%s\n\n" +
                        "CRITICAL: Return ONLY the raw JSON object. " +
                        "Do NOT wrap it in markdown code blocks (no ```json). " +
                        "Ensure the JSON is minified or properly escaped to be valid.",
                userInput != null ? userInput : "");

        // get key from env var
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        // model param
        ResponseCreateParams createParams = ResponseCreateParams.builder()
                .input(prompt)
                .model(ChatModel.GPT_4O)
                .build();

        // 3. Parse the response
        StringBuilder result = new StringBuilder();

        client.responses().create(createParams).output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .forEach(outputText -> result.append(outputText.text()));

        String rawResponse = result.toString();
        System.out.printf("rawResponse%s: ", rawResponse);

        try {
            int jsonStartIndex = rawResponse.indexOf("{");
            int jsonEndIndex = rawResponse.lastIndexOf("}");
            ObjectMapper mapper = new ObjectMapper();
            // if json is provided, parse it
            if (jsonStartIndex != -1 && jsonEndIndex != -1 && jsonEndIndex > jsonStartIndex) {
                rawResponse = rawResponse.substring(jsonStartIndex, jsonEndIndex + 1);
            } else {
                // if json is not provided, create a fallback
                // Fallback: Create a valid JSON manually with the raw text as summary
                com.fasterxml.jackson.databind.node.ObjectNode fallback = mapper.createObjectNode();
                fallback.put("summary", rawResponse);
                fallback.putArray("key_points");
                fallback.put("analysis", "Model refused to generate report due to insufficient input.");
                fallback.putArray("recommendations");
                fallback.put("confidence_score", 0);

                rawResponse = fallback.toString();
            }

            JsonNode jsonNode = mapper.readTree(rawResponse);

            if (!jsonNode.has("summary")) {
                throw new IllegalArgumentException("Invalid JSON response format, missing required fields");
            }
            // check all fields
            // if (!jsonNode.has("summary") || !jsonNode.has("key_points") ||
            // !jsonNode.has("analysis")
            // || !jsonNode.has("recommendations") || !jsonNode.has("confidence_score")) {
            // throw new IllegalArgumentException("Invalid JSON response format, missing
            // required fields");
            // }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response: " + e);
        }
        return rawResponse;

    }
}
