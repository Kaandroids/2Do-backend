package com.example._Do.group.controller;

import com.example._Do.group.dto.GroupInvitationResponse;
import com.example._Do.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
@Tag(name = "Invitations", description = "Endpoints for managing group invitations")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class InvitationController {

    private final GroupService groupService;

    @GetMapping
    @Operation(summary = "Get my pending invitations")
    public ResponseEntity<List<GroupInvitationResponse>> getMyPendingInvitations() {
        return ResponseEntity.ok(groupService.getMyPendingInvitations());
    }

    @PostMapping("/{invitationId}/accept")
    @Operation(summary = "Accept a group invitation")
    public ResponseEntity<Void> acceptInvitation(@PathVariable Long invitationId) {
        groupService.acceptInvitation(invitationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{invitationId}/decline")
    @Operation(summary = "Decline a group invitation")
    public ResponseEntity<Void> declineInvitation(@PathVariable Long invitationId) {
        groupService.declineInvitation(invitationId);
        return ResponseEntity.ok().build();
    }
}
