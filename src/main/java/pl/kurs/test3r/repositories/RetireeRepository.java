package pl.kurs.test3r.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.test3r.models.person.Retiree;

public interface RetireeRepository extends JpaRepository<Retiree, Long> {
}
