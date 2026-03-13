package com.example._Do.group.repository;

import com.example._Do.group.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    List<GroupMember> findAllByGroupId(Long groupId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
}
