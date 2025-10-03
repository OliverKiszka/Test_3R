package pl.kurs.test3r.commands;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import pl.kurs.test3r.services.person.EmployeePersonTypeModule;

import java.time.LocalDate;

@JsonTypeName(EmployeePersonTypeModule.TYPE)
public class UpdateEmployeeCommand extends UpdatePersonCommand {
    @NotNull
    private LocalDate startDate;
    @NotBlank
    private String currentPosition;
    @NotNull
    @PositiveOrZero
    private Double currentSalary;

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
}
