package pl.kurs.test3r.dto;

import java.time.LocalDate;

public class EmployeeDto extends PersonDto{

    private LocalDate startDate;
    private String currentPosition;
    private Double currentSalary;

    private int positionCount;
    private int professionCount;

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

    public Double getCurrentSalary() {
        return currentSalary;
    }

    public void setCurrentSalary(Double currentSalary) {
        this.currentSalary = currentSalary;
    }

    public int getPositionCount() {
        return positionCount;
    }

    public void setPositionCount(int positionCount) {
        this.positionCount = positionCount;
    }

    public int getProfessionCount() {
        return professionCount;
    }

    public void setProfessionCount(int professionCount) {
        this.professionCount = professionCount;
    }
}
