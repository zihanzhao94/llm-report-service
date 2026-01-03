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
 *   <li>Calling external LLM APIs (OpenAI, Claude, etc.)</li>
 *   <li>Formatting prompts for report generation</li>
 *   <li>Parsing and validating LLM responses</li>
 *   <li>Converting LLM output to the required report format</li>
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
public class llmService {


    public String generateReport(String userInput) {
    
        // 1. Format the user input into a prompt
        String prompt = String.format(
            "Analyze the following text and generate a report. " +
            "You MUST return ONLY valid JSON with this exact structure:\n" +
            "{\n" +
            "  \"summary\": \"A brief summary of the text\",\n" +
            "  \"key_points\": [\"Point 1\", \"Point 2\", \"Point 3\"],\n" +
            "  \"confidence_score\": 0.75\n" +
            "}\n\n" +
            "Text to analyze:\n%s\n\n" +
            "CRITICAL: Return ONLY the raw JSON object. " +
            "Do NOT wrap it in markdown code blocks (no or ```). " +
            "Do NOT add any explanation, comments, or additional text. " +
            "Start directly with { and end with }.",
            userInput != null ? userInput : ""
        );


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
        System.out.printf("rawResponse%s: ",rawResponse);

        try{
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(rawResponse);

            if (!jsonNode.has("summary") || !jsonNode.has("key_points") || !jsonNode.has("confidence_score")) {
                throw new IllegalArgumentException("Invalid JSON response format, missing required fields");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response: " + e);
        }
        return rawResponse;

    }
}
