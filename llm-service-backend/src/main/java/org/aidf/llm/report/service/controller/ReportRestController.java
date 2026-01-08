package org.aidf.llm.report.service.controller;

import org.aidf.llm.report.service.model.dto.ReportRequest;
import org.aidf.llm.report.service.model.dto.ReportResponse;
import org.aidf.llm.report.service.service.ReportService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
@RestController
@RequestMapping("/api")
public class ReportRestController {

    ReportService reportService;

    // inject report service
    ReportRestController(ReportService theReportService) {
        reportService = theReportService;
    }

    // POST /api/reports to create report
    @PostMapping("reports")
    public ReportResponse createReport(@RequestBody ReportRequest request) {
        ReportResponse response = reportService.submitTask(request);
        return response;
    }

    // GET /api/reports/{taskId} to get report response by task id and return to
    // user ReportResponse
    @GetMapping("reports/{taskId}")
    public ReportResponse getResponse(@PathVariable long taskId) {
        ReportResponse response = reportService.getResponse(taskId);
        return response;

    }

    // GET /api/reports to get all report response and return to user ReportResponse
    @GetMapping("reports")
    public List<ReportResponse> getAllResponse() {
        List<ReportResponse> response = reportService.getAllResponse();
        return response;
    }

}
