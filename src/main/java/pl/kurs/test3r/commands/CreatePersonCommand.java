package pl.kurs.test3r.commands;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateStudentCommand.class, name = "STUDENT"),
        @JsonSubTypes.Type(value = CreateEmployeeCommand.class, name = "EMPLOYEE"),
        @JsonSubTypes.Type(value = CreateRetireeCommand.class, name = "RETIREE")
})
public abstract class CreatePersonCommand {
    @NotBlank(message = "Type is required and must be one of: STUDENT, EMPLOYEE, RETIREE")
    private String type;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    @Pattern(regexp = "\\d{11}")
    private String pesel;
    @NotNull
    @Positive
    private Double height;
    @NotNull
    @Positive
    private Double weight;
    @NotBlank
    @Email
    private String email;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPesel() {
        return pesel;
    }

    public void setPesel(String pesel) {
        this.pesel = pesel;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
