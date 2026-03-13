package com.example._Do.group.dto;

import com.example._Do.group.entity.GroupPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Set<GroupPermission> permissions;
    private boolean isOwner;
}
