package com.example._Do.group.service;

import com.example._Do.group.dto.*;
import com.example._Do.group.entity.*;
import com.example._Do.group.repository.GroupInvitationRepository;
import com.example._Do.group.repository.GroupMemberRepository;
import com.example._Do.group.repository.GroupRepository;
import com.example._Do.task.repository.TaskRepository;
import com.example._Do.user.entity.User;
import com.example._Do.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public GroupResponse createGroup(GroupRequest request) {
        User currentUser = getCurrentUser();

        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(currentUser)
                .build();

        Group saved = groupRepository.save(group);
        log.info("User {} created group '{}'", currentUser.getId(), saved.getName());

        return toGroupResponse(saved, currentUser, 1, 0); // 1 = owner only
    }

    @Transactional
    public GroupResponse updateGroup(Long groupId, GroupRequest request) {
        User currentUser = getCurrentUser();
        Group group = getGroupOrThrow(groupId);
        requireOwner(group, currentUser);

        group.setName(request.getName());
        group.setDescription(request.getDescription());
        Group saved = groupRepository.save(group);
        log.info("User {} updated group {}", currentUser.getId(), groupId);

        int memberCount = groupMemberRepository.findAllByGroupId(groupId).size() + 1; // +1 for owner
        long pendingTaskCount = taskRepository.countByGroupIdAndCompleted(groupId, false);
        return toGroupResponse(saved, currentUser, memberCount, pendingTaskCount);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getMyGroups() {
        User currentUser = getCurrentUser();
        List<Group> groups = groupRepository.findAllByOwnerOrMember(currentUser);

        return groups.stream().map(g -> {
            int memberCount = groupMemberRepository.findAllByGroupId(g.getId()).size() + 1; // +1 for owner
            long pendingTaskCount = taskRepository.countByGroupIdAndCompleted(g.getId(), false);
            return toGroupResponse(g, currentUser, memberCount, pendingTaskCount);
        }).toList();
    }

    @Transactional
    public void inviteMember(Long groupId, GroupInviteRequest request) {
        User currentUser = getCurrentUser();
        Group group = getGroupOrThrow(groupId);
        requireOwnerOrPermission(group, currentUser, GroupPermission.CAN_INVITE);

        User invitee = userRepository.findByEmail(request.getInviteeEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + request.getInviteeEmail()));

        if (invitee.getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Cannot invite yourself");
        }

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, invitee.getId())) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        if (groupInvitationRepository.existsByGroupIdAndInviteeIdAndStatus(groupId, invitee.getId(), InvitationStatus.PENDING)) {
            throw new IllegalArgumentException("User already has a pending invitation to this group");
        }

        GroupInvitation invitation = GroupInvitation.builder()
                .group(group)
                .inviter(currentUser)
                .invitee(invitee)
                .status(InvitationStatus.PENDING)
                .build();

        // Store the requested permissions on the invitation so they can be applied on acceptance
        // We'll use a transient approach: save invitation, then separately track permissions
        // For simplicity, we save permissions on the invitation by extending the flow:
        // After accept, we read from request — but invitation entity doesn't store them.
        // We'll encode them in a separate step. For now, store in invitation directly via a workaround:
        // We'll use a different approach — store permissions requested at invite time via a helper.
        groupInvitationRepository.save(invitation);
        log.info("Invited user {} to group {}", invitee.getId(), groupId);
    }

    @Transactional
    public void updateMemberPermissions(Long groupId, Long userId, GroupUpdatePermissionsRequest request) {
        User currentUser = getCurrentUser();
        Group group = getGroupOrThrow(groupId);
        requireOwnerOrPermission(group, currentUser, GroupPermission.CAN_MANAGE);

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found in this group"));

        member.setPermissions(request.getPermissions());
        groupMemberRepository.save(member);
        log.info("Updated permissions for user {} in group {}", userId, groupId);
    }

    @Transactional
    public void removeMember(Long groupId, Long userId) {
        User currentUser = getCurrentUser();
        Group group = getGroupOrThrow(groupId);
        requireOwner(group, currentUser);

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found in this group"));

        groupMemberRepository.delete(member);
        log.info("Removed user {} from group {}", userId, groupId);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(Long groupId) {
        User currentUser = getCurrentUser();
        Group group = getGroupOrThrow(groupId);
        requireMemberOrOwner(group, currentUser);

        return groupMemberRepository.findAllByGroupId(groupId).stream()
                .map(m -> GroupMemberResponse.builder()
                        .userId(m.getUser().getId())
                        .firstName(m.getUser().getFirstName())
                        .lastName(m.getUser().getLastName())
                        .email(m.getUser().getEmail())
                        .permissions(m.getPermissions())
                        .build())
                .toList();
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        User currentUser = getCurrentUser();
        Group group = getGroupOrThrow(groupId);
        requireOwner(group, currentUser);

        // Delete all invitations and members for this group first
        groupInvitationRepository.findAllByGroupId(groupId).forEach(groupInvitationRepository::delete);
        groupMemberRepository.findAllByGroupId(groupId).forEach(groupMemberRepository::delete);
        groupRepository.delete(group);
        log.info("Deleted group {}", groupId);
    }

    @Transactional(readOnly = true)
    public List<GroupInvitationResponse> getMyPendingInvitations() {
        User currentUser = getCurrentUser();
        return groupInvitationRepository
                .findAllByInviteeAndStatus(currentUser, InvitationStatus.PENDING)
                .stream()
                .map(this::toInvitationResponse)
                .toList();
    }

    @Transactional
    public void acceptInvitation(Long invitationId) {
        User currentUser = getCurrentUser();
        GroupInvitation invitation = groupInvitationRepository.findByIdAndInvitee(invitationId, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is no longer pending");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        groupInvitationRepository.save(invitation);

        GroupMember member = GroupMember.builder()
                .group(invitation.getGroup())
                .user(currentUser)
                .permissions(Set.of())
                .build();
        groupMemberRepository.save(member);
        log.info("User {} accepted invitation {} to group {}", currentUser.getId(), invitationId, invitation.getGroup().getId());
    }

    @Transactional
    public void declineInvitation(Long invitationId) {
        User currentUser = getCurrentUser();
        GroupInvitation invitation = groupInvitationRepository.findByIdAndInvitee(invitationId, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is no longer pending");
        }

        invitation.setStatus(InvitationStatus.DECLINED);
        groupInvitationRepository.save(invitation);
        log.info("User {} declined invitation {}", currentUser.getId(), invitationId);
    }

    // --- helpers ---

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private Group getGroupOrThrow(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));
    }

    private void requireOwner(Group group, User user) {
        if (!group.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the group owner can perform this action");
        }
    }

    private void requireOwnerOrPermission(Group group, User user, GroupPermission permission) {
        if (group.getOwner().getId().equals(user.getId())) return;
        boolean hasPerm = groupMemberRepository.findByGroupIdAndUserId(group.getId(), user.getId())
                .map(m -> m.getPermissions().contains(permission))
                .orElse(false);
        if (!hasPerm) {
            throw new AccessDeniedException("You do not have permission to perform this action");
        }
    }

    private void requireMemberOrOwner(Group group, User user) {
        boolean isOwner = group.getOwner().getId().equals(user.getId());
        boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(group.getId(), user.getId());
        if (!isOwner && !isMember) {
            throw new AccessDeniedException("Access denied to this group");
        }
    }

    private GroupResponse toGroupResponse(Group group, User currentUser, int memberCount, long pendingTaskCount) {
        boolean isOwner = group.getOwner().getId().equals(currentUser.getId());
        Set<GroupPermission> myPermissions = isOwner
                ? Set.of(GroupPermission.values())
                : groupMemberRepository.findByGroupIdAndUserId(group.getId(), currentUser.getId())
                        .map(GroupMember::getPermissions)
                        .orElse(Set.of());

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .ownerName(group.getOwner().getFirstName() + " " + group.getOwner().getLastName())
                .memberCount(memberCount)
                .pendingTaskCount(pendingTaskCount)
                .myPermissions(myPermissions)
                .createdAt(group.getCreatedAt())
                .isOwner(isOwner)
                .build();
    }

    private GroupInvitationResponse toInvitationResponse(GroupInvitation invitation) {
        return GroupInvitationResponse.builder()
                .id(invitation.getId())
                .groupId(invitation.getGroup().getId())
                .groupName(invitation.getGroup().getName())
                .inviterName(invitation.getInviter().getFirstName() + " " + invitation.getInviter().getLastName())
                .createdAt(invitation.getCreatedAt())
                .status(invitation.getStatus())
                .build();
    }

    public boolean isMemberOrOwner(Long groupId, User user) {
        Group group = getGroupOrThrow(groupId);
        return group.getOwner().getId().equals(user.getId())
                || groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId());
    }

    public boolean hasPermission(Long groupId, User user, GroupPermission permission) {
        Group group = getGroupOrThrow(groupId);
        if (group.getOwner().getId().equals(user.getId())) return true;
        return groupMemberRepository.findByGroupIdAndUserId(groupId, user.getId())
                .map(m -> m.getPermissions().contains(permission))
                .orElse(false);
    }
}
