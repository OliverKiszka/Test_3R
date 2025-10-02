package pl.kurs.test3r.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kurs.test3r.models.person.Employee;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct e from Employee e left join fetch e.positions where  e.id=:id")
    Optional<Employee> findByIdWithPositionsForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = "positions")
    @Query("select e from Employee e where e.id = :id")
    Optional<Employee> findByIdWithPositions(@Param("id") Long id);

}
