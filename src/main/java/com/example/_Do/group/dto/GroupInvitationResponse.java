package com.example._Do.group.dto;

import com.example._Do.group.entity.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupInvitationResponse {
    private Long id;
    private Long groupId;
    private String groupName;
    private String inviterName;
    private LocalDateTime createdAt;
    private InvitationStatus status;
}
