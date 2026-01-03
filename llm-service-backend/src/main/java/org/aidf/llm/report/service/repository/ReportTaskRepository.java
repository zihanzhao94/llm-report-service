package org.aidf.llm.report.service.repository;

import org.aidf.llm.report.service.model.entity.ReportTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportTaskRepository extends JpaRepository<ReportTask, Long> {
}
