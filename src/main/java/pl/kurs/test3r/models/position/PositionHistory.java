package pl.kurs.test3r.models.position;

import jakarta.persistence.*;
import pl.kurs.test3r.models.person.Employee;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "positions")
public class PositionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String positionName;
    private double salary;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    @ManyToOne
    private Employee employee;

    public PositionHistory() {
    }

    public PositionHistory(String positionName, double salary, LocalDate dateFrom, LocalDate dateTo, Employee employee) {
        this.positionName = positionName;
        this.salary = salary;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.employee = employee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PositionHistory that = (PositionHistory) o;
        return Double.compare(that.salary, salary) == 0 && Objects.equals(id, that.id) && Objects.equals(positionName, that.positionName) && Objects.equals(dateFrom, that.dateFrom) && Objects.equals(dateTo, that.dateTo) && Objects.equals(employee, that.employee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, positionName, salary, dateFrom, dateTo, employee);
    }

    @Override
    public String toString() {
        return "PositionHistory{" +
                "id=" + id +
                ", positionName='" + positionName + '\'' +
                ", salary=" + salary +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", employee=" + employee +
                '}';
    }
}
