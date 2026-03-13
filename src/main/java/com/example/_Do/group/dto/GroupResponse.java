package com.example._Do.group.dto;

import com.example._Do.group.entity.GroupPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private String ownerName;
    private int memberCount;
    private long pendingTaskCount;
    private Set<GroupPermission> myPermissions;
    private LocalDateTime createdAt;
    private boolean isOwner;
}
