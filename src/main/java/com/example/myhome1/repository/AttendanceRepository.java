package com.example.myhome1.repository;

import com.example.myhome1.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // 추가적인 메소드가 필요하다면 여기에 선언 가능합니다.
}
