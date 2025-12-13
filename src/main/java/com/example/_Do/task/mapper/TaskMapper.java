package com.example._Do.task.mapper;

import com.example._Do.task.dto.TaskRequest;
import com.example._Do.task.dto.TaskResponse;
import com.example._Do.task.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TaskMapper {

    /**
     * Converts request to entity.
     * Note: 'user' field is ignored here, it must be set by the Service.
     */
    @Mapping(target = "user", ignore = true)
    Task toEntity(TaskRequest taskRequest);

    /**
     * Converts entity to response DTO.
     */
    TaskResponse toResponse(Task task);

    /**
     * Updates an existing entity from a request (for Update operations).
     * This avoids creating a new object manually.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(TaskRequest taskRequest, @MappingTarget Task task);
}
