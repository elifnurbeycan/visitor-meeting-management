package com.yasarbilgi.visitormeetingmanagment.userpermission.repository;

import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPermissionUserRepository extends JpaRepository<User, Long> {
}
