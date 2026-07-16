package com.yasarbilgi.visitormeetingmanagment.room.repository;

import com.yasarbilgi.visitormeetingmanagment.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}
