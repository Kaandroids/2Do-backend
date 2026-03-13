package com.example._Do.task.service;

import com.example._Do.group.entity.Group;
import com.example._Do.group.entity.GroupPermission;
import com.example._Do.group.repository.GroupMemberRepository;
import com.example._Do.group.repository.GroupRepository;
import com.example._Do.group.service.GroupService;
import com.example._Do.task.dto.TaskRequest;
import com.example._Do.task.dto.TaskResponse;
import com.example._Do.task.entity.Task;
import com.example._Do.task.mapper.TaskMapper;
import com.example._Do.task.repository.TaskRepository;
import com.example._Do.user.entity.User;
import com.example._Do.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public TaskResponse createTask(TaskRequest taskRequest) {
        User currentUser = getCurrentUser();
        log.info("Creating task for user: {}", currentUser.getId());

        Task task = taskMapper.toEntity(taskRequest);
        task.setUser(currentUser);

        if (taskRequest.getGroupId() != null) {
            Group group = groupRepository.findById(taskRequest.getGroupId())
                    .orElseThrow(() -> new EntityNotFoundException("Group not found"));

            boolean isOwner = group.getOwner().getId().equals(currentUser.getId());
            boolean hasPerm = groupMemberRepository.findByGroupIdAndUserId(group.getId(), currentUser.getId())
                    .map(m -> m.getPermissions().contains(GroupPermission.CAN_CREATE))
                    .orElse(false);

            if (!isOwner && !hasPerm) {
                throw new AccessDeniedException("You don't have permission to create tasks in this group");
            }

            task.setGroup(group);

            if (taskRequest.getAssigneeId() != null) {
                User assignee = userRepository.findById(taskRequest.getAssigneeId())
                        .orElseThrow(() -> new EntityNotFoundException("Assignee not found"));
                task.setAssignee(assignee);
            }

            if (taskRequest.getAssigneeIds() != null && !taskRequest.getAssigneeIds().isEmpty()) {
                task.setAssigneeIds(new HashSet<>(taskRequest.getAssigneeIds()));
            }
            task.setPrivate(taskRequest.isPrivate());
        }

        Task savedTask = taskRepository.save(task);
        return toResponseWithDetails(savedTask);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        User currentUser = getCurrentUser();
        Long currentUserId = currentUser.getId();
        log.info("Retrieving all tasks for user: {}", currentUserId);

        // Personal tasks (no group)
        List<Task> personal = taskRepository.findAllByUserIdAndGroupIsNull(currentUserId);

        // Visible tasks from every group the user belongs to
        List<Task> groupTasks = groupRepository.findAllByOwnerOrMember(currentUser).stream()
                .flatMap(g -> {
                    boolean isGroupOwner = g.getOwner().getId().equals(currentUserId);
                    return taskRepository.findAllByGroupId(g.getId()).stream()
                            .filter(t -> !t.isPrivate()
                                    || t.getUser().getId().equals(currentUserId)
                                    || isGroupOwner
                                    || t.getAssigneeIds().contains(currentUserId));
                })
                .toList();

        List<Task> all = new ArrayList<>();
        all.addAll(personal);
        all.addAll(groupTasks);
        all.sort(Comparator.comparing(Task::getCreatedAt).reversed());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<TaskResponse> page = start >= all.size() ? List.of() :
                all.subList(start, end).stream().map(this::toResponseWithDetails).toList();
        return new PageImpl<>(page, pageable, all.size());
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getGroupTasks(Long groupId, Pageable pageable) {
        User currentUser = getCurrentUser();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        boolean isOwner = group.getOwner().getId().equals(currentUser.getId());
        boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId());
        if (!isOwner && !isMember) {
            throw new AccessDeniedException("Access denied to this group");
        }

        Long currentUserId = currentUser.getId();
        List<TaskResponse> visible = taskRepository.findAllByGroupId(groupId).stream()
                .filter(t -> !t.isPrivate()
                        || t.getUser().getId().equals(currentUserId)
                        || isOwner
                        || t.getAssigneeIds().contains(currentUserId))
                .map(this::toResponseWithDetails)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), visible.size());
        List<TaskResponse> page = start > visible.size() ? List.of() : visible.subList(start, end);
        return new PageImpl<>(page, pageable, visible.size());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        Task task = getTaskOrThrow(taskId);
        return toResponseWithDetails(task);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskRequest taskRequest) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (task.getGroup() != null) {
            // Group task: check CAN_EDIT or is owner
            boolean isGroupOwner = task.getGroup().getOwner().getId().equals(currentUser.getId());
            boolean isTaskOwner = task.getUser().getId().equals(currentUser.getId());
            boolean hasPerm = groupMemberRepository.findByGroupIdAndUserId(task.getGroup().getId(), currentUser.getId())
                    .map(m -> m.getPermissions().contains(GroupPermission.CAN_EDIT))
                    .orElse(false);

            if (!isGroupOwner && !isTaskOwner && !hasPerm) {
                throw new AccessDeniedException("You don't have permission to edit tasks in this group");
            }
        } else {
            // Personal task: must own it
            if (!task.getUser().getId().equals(currentUser.getId())) {
                throw new EntityNotFoundException("Task not found");
            }
        }

        log.info("Updating task ID: {}", taskId);
        taskMapper.updateEntityFromRequest(taskRequest, task);

        // Update assignee if provided
        if (task.getGroup() != null && taskRequest.getAssigneeId() != null) {
            User assignee = userRepository.findById(taskRequest.getAssigneeId())
                    .orElseThrow(() -> new EntityNotFoundException("Assignee not found"));
            task.setAssignee(assignee);
        } else if (taskRequest.getAssigneeId() == null && task.getGroup() != null) {
            task.setAssignee(null);
        }

        return toResponseWithDetails(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long taskId) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (task.getGroup() != null) {
            boolean isGroupOwner = task.getGroup().getOwner().getId().equals(currentUser.getId());
            boolean isTaskOwner = task.getUser().getId().equals(currentUser.getId());
            boolean hasPerm = groupMemberRepository.findByGroupIdAndUserId(task.getGroup().getId(), currentUser.getId())
                    .map(m -> m.getPermissions().contains(GroupPermission.CAN_DELETE))
                    .orElse(false);

            if (!isGroupOwner && !isTaskOwner && !hasPerm) {
                throw new AccessDeniedException("You don't have permission to delete tasks in this group");
            }
        } else {
            if (!task.getUser().getId().equals(currentUser.getId())) {
                throw new EntityNotFoundException("Task not found");
            }
        }

        log.info("Deleting task ID: {}", taskId);
        taskRepository.delete(task);
    }

    private TaskResponse toResponseWithDetails(Task task) {
        TaskResponse resp = taskMapper.toResponse(task);
        if (task.getAssigneeIds() != null && !task.getAssigneeIds().isEmpty()) {
            List<String> names = userRepository.findAllById(task.getAssigneeIds())
                    .stream()
                    .map(u -> u.getFirstName() + " " + u.getLastName())
                    .toList();
            resp.setAssigneeNames(names);
        }
        return resp;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private Task getTaskOrThrow(Long taskId) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (task.getGroup() != null) {
            boolean isGroupOwner = task.getGroup().getOwner().getId().equals(user.getId());
            boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(task.getGroup().getId(), user.getId());
            if (!isGroupOwner && !isMember) {
                throw new EntityNotFoundException("Task not found");
            }
        } else {
            if (!task.getUser().getId().equals(user.getId())) {
                log.warn("User {} tried to access Task {} which belongs to User {}",
                        user.getId(), taskId, task.getUser().getId());
                throw new EntityNotFoundException("Task not found");
            }
        }

        return task;
    }
}
