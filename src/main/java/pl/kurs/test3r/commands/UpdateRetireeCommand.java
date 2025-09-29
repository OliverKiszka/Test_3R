package pl.kurs.test3r.commands;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class UpdateRetireeCommand extends UpdatePersonCommand{
    @NotNull
    @PositiveOrZero
    private Double pensionAmount;
    @NotNull
    @Min(0)
    private Integer yearsWorked;

    public Double getPensionAmount() {
        return pensionAmount;
    }

    public void setPensionAmount(Double pensionAmount) {
        this.pensionAmount = pensionAmount;
    }

    public Integer getYearsWorked() {
        return yearsWorked;
    }

    public void setYearsWorked(Integer yearsWorked) {
        this.yearsWorked = yearsWorked;
    }
}
