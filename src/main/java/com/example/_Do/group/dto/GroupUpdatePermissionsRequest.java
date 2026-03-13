package com.example._Do.group.dto;

import com.example._Do.group.entity.GroupPermission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupUpdatePermissionsRequest {
    private Set<GroupPermission> permissions = new HashSet<>();
}
