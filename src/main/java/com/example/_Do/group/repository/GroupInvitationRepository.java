package com.example._Do.group.repository;

import com.example._Do.group.entity.GroupInvitation;
import com.example._Do.group.entity.InvitationStatus;
import com.example._Do.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {

    List<GroupInvitation> findAllByInviteeAndStatus(User invitee, InvitationStatus status);

    Optional<GroupInvitation> findByIdAndInvitee(Long id, User invitee);

    boolean existsByGroupIdAndInviteeIdAndStatus(Long groupId, Long inviteeId, InvitationStatus status);

    List<GroupInvitation> findAllByGroupId(Long groupId);
}
