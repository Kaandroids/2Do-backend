package com.example._Do.group.repository;

import com.example._Do.group.entity.Group;
import com.example._Do.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("""
            SELECT DISTINCT g FROM Group g
            WHERE g.owner = :user
               OR EXISTS (SELECT m FROM GroupMember m WHERE m.group = g AND m.user = :user)
            """)
    List<Group> findAllByOwnerOrMember(@Param("user") User user);
}
