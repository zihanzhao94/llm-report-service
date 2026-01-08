package org.aidf.llm.report.service.service;

import lombok.extern.slf4j.Slf4j;
import org.aidf.llm.report.service.model.dto.ReportRequest;
import org.aidf.llm.report.service.model.dto.ReportResponse;
import org.aidf.llm.report.service.model.entity.ReportTask;
import org.aidf.llm.report.service.repository.ReportTaskRepository;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing report generation tasks.
 * <p>
 * This service handles the complete lifecycle of report generation tasks:
 * <ul>
 * <li>Creating new tasks from user input</li>
 * <li>Asynchronously processing tasks by calling LLM services</li>
 * <li>Querying task status and results</li>
 * </ul>
 * <p>
 * Tasks are processed asynchronously to avoid blocking HTTP requests during
 * slow LLM inference operations.
 *
 * @author Zihan Zhao
 * @since 1.0
 */
@Slf4j
@Service
public class ReportService {
    private ReportTaskRepository reportTaskRepository;
    private llmService llmService;

    /**
     * Constructs a new ReportService with the specified repository.
     *
     * @param theReportTaskRepository the repository for report task data access
     */
    public ReportService(ReportTaskRepository theReportTaskRepository, llmService theLlmService) {
        reportTaskRepository = theReportTaskRepository;
        llmService = theLlmService;
    }

    /**
     * Submits a new report generation task.
     * <p>
     * This method creates a
     * {@link org.aidf.llm.report.service.model.entity.ReportTask}
     * from the user's input, persists it to the database, and returns a
     * {@link org.aidf.llm.report.service.model.dto.ReportResponse} that contains
     * the generated task id and initial status.
     * <p>
     * The actual LLM report generation is handled asynchronously based on this
     * task.
     *
     * @param reportRequest the request DTO containing the user's input text
     * @return a response DTO containing the task id, current status, and creation
     *         time
     */

    @CachePut(value = "REPORT_CACHE", key = "#result.id")
    public ReportResponse submitTask(ReportRequest reportRequest) {

        // get data from request
        String userInput = reportRequest.getUserInput();
        // generate other data
        String reportResult = null;

        ReportTask.TaskStatus status = ReportTask.TaskStatus.PENDING;

        LocalDateTime createAt = LocalDateTime.now(); // user might want to know when created

        // save to the database
        ReportTask task = new ReportTask();
        task.setUserInput(userInput);
        task.setStatus(status);
        task.setCreateAt(createAt);
        task.setReportResult(reportResult);
        ReportTask saved = reportTaskRepository.save(task);

        // wake up async processing
        processReport(saved.getId());

        // return ReportResponse use database data for id and to avoid discrepencies

        ReportResponse response = new ReportResponse();
        response.setId(saved.getId());
        response.setStatus(saved.getStatus().name()); // convert enum to string
        response.setCreateAt(saved.getCreateAt());
        response.setReportResult(saved.getReportResult());

        return response;

    }

    /**
     * Retrieves a report task by its ID and converts it to a response DTO.
     * <p>
     * This method fetches the complete task information including status,
     * creation time, and the generated report result (if available).
     *
     * @param taskId the unique identifier of the task
     * @return a response DTO containing the task id, status, creation time, and
     *         report result
     * @throws RuntimeException if the task with the given ID is not found
     */
    @Cacheable(value = "REPORT_CACHE", key = "taskId")
    public ReportResponse getResponse(long taskId) {
        // check if task is found
        ReportTask task = reportTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        // get data from task entity
        ReportResponse response = new ReportResponse();
        response.setId(task.getId());
        response.setStatus(task.getStatus().name());
        response.setCreateAt(task.getCreateAt());
        response.setReportResult(task.getReportResult());
        return response;
    }

    /**
     * Asynchronously processes a report generation task by calling the LLM service.
     * <p>
     * This method is executed asynchronously to avoid blocking the HTTP request
     * thread.
     * It performs the following steps:
     * <ol>
     * <li>Retrieves the task from the database</li>
     * <li>Updates the task status to PROCESSING</li>
     * <li>Calls the LLM service to generate the report</li>
     * <li>Updates the task status to COMPLETED and saves the result</li>
     * <li>On failure, updates the task status to FAILED</li>
     * </ol>
     * <p>
     * The task status transitions: PENDING → PROCESSING → COMPLETED (or FAILED)
     *
     * @param taskId the unique identifier of the task to process
     * @throws RuntimeException if the task with the given ID is not found
     */
    @Async
    public void processReport(long taskId) {

        System.out.println("Async thread start!");

        // get task according to taskId
        ReportTask task = reportTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // check any procedure in execution lead to fail
        try {
            // Change status to PROCESSING
            task.setStatus(ReportTask.TaskStatus.PROCESSING);
            reportTaskRepository.save(task);

            // execute llm logic
            String result = llmService.generateReport(task.getUserInput());

            // mock
            // Thread.sleep(5000);
            // String result = "Create llm with task id" + taskId;

            // update status to completed (set entity object)
            task.setStatus(ReportTask.TaskStatus.COMPLETED);
            task.setReportResult(result);

            // update entity changes to the database
            reportTaskRepository.save(task);

        } catch (Exception e) { // handle exception when task failed
            System.out.println("report task creation failure: " + e);
            System.out.println("OPENAI_API_KEY = " + System.getenv("OPENAI_API_KEY"));
            task.setStatus(ReportTask.TaskStatus.FAILED);
            reportTaskRepository.save(task);

        }

    }

    public List<ReportResponse> getAllResponse() {
        List<ReportTask> tasks = reportTaskRepository.findAll();
        // convert list of entities to list of dtos
        return tasks.stream().map(this::convertToResponse).toList();
    }

    // helper to convert entity to dto
    private ReportResponse convertToResponse(ReportTask task) {
        ReportResponse response = new ReportResponse();
        response.setId(task.getId());
        response.setStatus(task.getStatus().name());
        response.setCreateAt(task.getCreateAt());
        response.setReportResult(task.getReportResult());
        return response;
    }

}
