package pl.kurs.test3r.commands;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import pl.kurs.test3r.services.person.StudentPersonTypeModule;

@JsonTypeName(StudentPersonTypeModule.TYPE)
public class UpdateStudentCommand extends UpdatePersonCommand {

    @NotBlank
    private String university;
    @NotNull
    @Min(1)
    private Integer studyYear;
    @NotBlank
    private String fieldOfStudy;
    @NotNull
    @PositiveOrZero
    private Double scholarship;

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public Integer getStudyYear() {
        return studyYear;
    }

    public void setStudyYear(Integer studyYear) {
        this.studyYear = studyYear;
    }

    public String getFieldOfStudy() {
        return fieldOfStudy;
    }

    public void setFieldOfStudy(String fieldOfStudy) {
        this.fieldOfStudy = fieldOfStudy;
    }

    public Double getScholarship() {
        return scholarship;
    }

    public void setScholarship(Double scholarship) {
        this.scholarship = scholarship;
    }
}
