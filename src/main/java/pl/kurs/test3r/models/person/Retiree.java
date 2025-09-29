package pl.kurs.test3r.models.person;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import pl.kurs.test3r.models.Identificationable;

import java.util.Objects;

@Entity
@Table(name = "retirees")
@DiscriminatorValue("RETIREE")
public class Retiree extends Person implements Identificationable {

    private double pensionAmount;
    private int yearsWorked;

    public Retiree() {
    }

    public Retiree(String firstName, String lastName, String pesel, double height, double weight, String email, Long version, double pensionAmount, int yearsWorked) {
        super(firstName, lastName, pesel, height, weight, email, version);
        this.pensionAmount = pensionAmount;
        this.yearsWorked = yearsWorked;
    }

    public double getPensionAmount() {
        return pensionAmount;
    }

    public void setPensionAmount(double pensionAmount) {
        this.pensionAmount = pensionAmount;
    }

    public int getYearsWorked() {
        return yearsWorked;
    }

    public void setYearsWorked(int yearsWorked) {
        this.yearsWorked = yearsWorked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Retiree retiree = (Retiree) o;
        return Double.compare(retiree.pensionAmount, pensionAmount) == 0 && yearsWorked == retiree.yearsWorked;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), pensionAmount, yearsWorked);
    }

    @Override
    public String toString() {
        return "Retiree{" +
                "pensionAmount=" + pensionAmount +
                ", yearsWorked=" + yearsWorked +
                '}';
    }
}
