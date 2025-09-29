package pl.kurs.test3r.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.test3r.models.position.PositionHistory;

import java.util.List;

public interface PositionRepository extends JpaRepository<PositionHistory,Long> {
    List<PositionHistory> findByEmployeeId(Long employeeId);
}
