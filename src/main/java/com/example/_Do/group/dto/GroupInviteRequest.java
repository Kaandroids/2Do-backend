package com.example._Do.group.dto;

import com.example._Do.group.entity.GroupPermission;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupInviteRequest {

    @NotBlank(message = "Invitee email is required")
    @Email(message = "Must be a valid email")
    private String inviteeEmail;

    private Set<GroupPermission> permissions = new HashSet<>();
}
