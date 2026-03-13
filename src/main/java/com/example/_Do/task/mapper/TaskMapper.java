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
     * Note: 'user', 'group', 'assignee' fields are ignored here — set by the Service.
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "mentionedUserIds", ignore = true)
    Task toEntity(TaskRequest taskRequest);

    /**
     * Converts entity to response DTO.
     */
    @Mapping(target = "userId",            source = "user.id")
    @Mapping(target = "creatorFirstName",  source = "user.firstName")
    @Mapping(target = "creatorLastName",   source = "user.lastName")
    @Mapping(target = "groupId",           source = "group.id")
    @Mapping(target = "assigneeId",        source = "assignee.id")
    @Mapping(target = "assigneeFirstName", source = "assignee.firstName")
    @Mapping(target = "assigneeLastName",  source = "assignee.lastName")
    @Mapping(target = "mentionedUserNames", ignore = true)
    TaskResponse toResponse(Task task);

    /**
     * Updates an existing entity from a request (for Update operations).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(TaskRequest taskRequest, @MappingTarget Task task);
}
