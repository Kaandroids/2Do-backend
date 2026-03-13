package com.example._Do.group.controller;

import com.example._Do.group.dto.*;
import com.example._Do.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Group Management", description = "Endpoints for managing groups and members")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @Operation(summary = "Create a new group")
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(request));
    }

    @GetMapping
    @Operation(summary = "Get my groups")
    public ResponseEntity<List<GroupResponse>> getMyGroups() {
        return ResponseEntity.ok(groupService.getMyGroups());
    }

    @PutMapping("/{groupId}")
    @Operation(summary = "Update group name/description (owner only)")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupRequest request
    ) {
        return ResponseEntity.ok(groupService.updateGroup(groupId, request));
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "Delete a group (owner only)")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/members")
    @Operation(summary = "List group members")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupMembers(groupId));
    }

    @PostMapping("/{groupId}/invitations")
    @Operation(summary = "Invite a member to the group (owner only)")
    public ResponseEntity<Void> inviteMember(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupInviteRequest request
    ) {
        groupService.inviteMember(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{groupId}/members/{userId}/permissions")
    @Operation(summary = "Update member permissions (owner only)")
    public ResponseEntity<Void> updateMemberPermissions(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestBody GroupUpdatePermissionsRequest request
    ) {
        groupService.updateMemberPermissions(groupId, userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @Operation(summary = "Remove a member from the group (owner only)")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId
    ) {
        groupService.removeMember(groupId, userId);
        return ResponseEntity.noContent().build();
    }
}
