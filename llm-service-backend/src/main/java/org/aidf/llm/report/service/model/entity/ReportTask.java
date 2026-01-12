package org.aidf.llm.report.service.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "report_tasks")
public class ReportTask {

    // report id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // user input
    @Column(columnDefinition = "TEXT")
    private String userInput;

    // status of the request from customer
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    // report result from llm
    @Column(columnDefinition = "TEXT")
    private String reportResult;

    // time of creation
    private LocalDateTime createAt;

    // define enum status of task
    public enum TaskStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

}
