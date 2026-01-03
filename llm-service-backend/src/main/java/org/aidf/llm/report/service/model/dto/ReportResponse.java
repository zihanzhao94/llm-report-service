package org.aidf.llm.report.service.model.dto;
import java.time.LocalDateTime;
import lombok.Data;
/**
 * The data Output to user
 */


// remove input in the entity not needed
@Data
public class ReportResponse {
    private long id; // distinguish from task
    String reportResult;
    private String status; // status to see if you need to wait or return
    private LocalDateTime createAt;  // user might want to know when created
}
