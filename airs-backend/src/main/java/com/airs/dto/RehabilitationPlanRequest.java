package com.airs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RehabilitationPlanRequest {

    @NotNull(message = "伤病记录ID不能为空")
    private Long injuryRecordId;

    private String planName;

    private Long therapistId;

    private String therapistName;

    private String description;

    private String status;

    @NotNull(message = "康复阶段不能为空")
    private List<PhaseRequest> phases;

    @Data
    public static class PhaseRequest {
        private Long id;

        @NotNull(message = "阶段顺序不能为空")
        private Integer phaseOrder;

        @NotBlank(message = "阶段类型不能为空")
        private String phaseType;

        private String phaseName;

        private Integer rpeUpperLimit;

        private Integer estimatedDays;

        private String description;

        private String goals;
    }
}
