package pl.kurs.test3r.models.person;

import jakarta.persistence.*;
import pl.kurs.test3r.models.Identificationable;
import pl.kurs.test3r.models.position.PositionHistory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "employees")
@DiscriminatorValue("EMPLOYEE")
public class Employee  extends Person implements Identificationable {

    private LocalDate startDate;
    private String currentPosition;
    private double currentSalary;
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PositionHistory> positions = new ArrayList<>();

    public Employee() {
    }

    public Employee(String firstName, String lastName, String pesel, double height, double weight, String email, Long version, LocalDate startDate, String currentPosition, double currentSalary, List<PositionHistory> positions) {
        super(firstName, lastName, pesel, height, weight, email, version);
        this.startDate = startDate;
        this.currentPosition = currentPosition;
        this.currentSalary = currentSalary;
        this.positions = positions;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(String currentPosition) {
        this.currentPosition = currentPosition;
    }

    public double getCurrentSalary() {
        return currentSalary;
    }

    public void setCurrentSalary(double currentSalary) {
        this.currentSalary = currentSalary;
    }

    public List<PositionHistory> getPositions() {
        return positions;
    }

    public void setPositions(List<PositionHistory> positions) {
        this.positions = positions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Employee employee = (Employee) o;
        return Double.compare(employee.currentSalary, currentSalary) == 0 && Objects.equals(startDate, employee.startDate) && Objects.equals(currentPosition, employee.currentPosition) && Objects.equals(positions, employee.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startDate, currentPosition, currentSalary, positions);
    }
}
