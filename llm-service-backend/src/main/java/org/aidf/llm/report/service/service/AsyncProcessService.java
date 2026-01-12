package org.aidf.llm.report.service.service;

import org.aidf.llm.report.service.model.dto.ReportResponse;
import org.aidf.llm.report.service.model.entity.ReportTask;
import org.aidf.llm.report.service.repository.ReportTaskRepository;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// separate from ReportService to avoid circular dependency as @Async need to be called from other class
@Service
public class AsyncProcessService {

    private final ReportTaskRepository reportTaskRepository;
    private final LlmService llmService;
    private final org.springframework.cache.CacheManager cacheManager;

    public AsyncProcessService(ReportTaskRepository theReportTaskRepository, LlmService theLlmService,
            org.springframework.cache.CacheManager theCacheManager) {
        reportTaskRepository = theReportTaskRepository;
        llmService = theLlmService;
        cacheManager = theCacheManager;
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

            // return ReportResponse use database data for id and to avoid discrepencies
            ReportResponse response = new ReportResponse();
            response.setId(task.getId());
            response.setStatus(task.getStatus().name());
            response.setCreateAt(task.getCreateAt());
            response.setReportResult(task.getReportResult());

            // // Manual Cache Put because @Async need void return
            // org.springframework.cache.Cache cache =
            // cacheManager.getCache("REPORT_CACHE");
            // if (cache != null) {
            // cache.put(taskId, response);
            // System.out.println("Cache updated for task: " + taskId);
            // }

        } catch (Exception e) { // handle exception when task failed
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("CRITICAL ERROR IN ASYNC PROCESS:");
            System.err.println("Task ID: " + taskId);
            System.err.println("Exception Message: " + e.getMessage());
            e.printStackTrace(); // print full stack trace
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            task.setStatus(ReportTask.TaskStatus.FAILED);
            reportTaskRepository.save(task);
            ReportResponse response = new ReportResponse();
            response.setId(task.getId());
            response.setStatus(task.getStatus().name());
            response.setCreateAt(task.getCreateAt());
            response.setReportResult(task.getReportResult());

            // Manual Cache Put (Update status to FAILED)
            org.springframework.cache.Cache cache = cacheManager.getCache("REPORT_CACHE");
            if (cache != null) {
                cache.put(taskId, response);
            }

        }

    }
}
