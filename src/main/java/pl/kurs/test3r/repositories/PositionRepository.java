package pl.kurs.test3r.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kurs.test3r.models.position.PositionHistory;

import java.util.List;
import java.util.Optional;


public interface PositionRepository extends JpaRepository<PositionHistory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ph from PositionHistory ph where ph.employee = :employeeId")
    List<PositionHistory> findByEmployeeIdForUpdate(@Param("employeeId") Long employeeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ph from PositionHistory ph join fetch ph.employee.id where ph.id = :id")
    Optional<PositionHistory> findByIdWithEmployeeForUpdate(@Param("id") Long id);

    @Query("select ph from PositionHistory ph where ph.employee.id = :employeeId order by ph.dateFrom, ph.id")
    List<PositionHistory> findAllByEmployeeIdOrderByDateFrom(@Param("employeeId") Long employeeId);
}
