package pl.kurs.test3r.models.person;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import pl.kurs.test3r.models.Identificationable;

import java.util.Objects;

@Entity
@Table(name = "students")
@DiscriminatorValue("STUDENT")
public class Student extends Person implements Identificationable {

    private String university;
    private int studyYear;
    private String fieldOfStudy;
    private double scholarship;



    public Student() {
    }

    public Student(String firstName, String lastName, String pesel, double height, double weight, String email, Long version, String university, int studyYear, String fieldOfStudy, double scholarship) {
        super(firstName, lastName, pesel, height, weight, email, version);
        this.university = university;
        this.studyYear = studyYear;
        this.fieldOfStudy = fieldOfStudy;
        this.scholarship = scholarship;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public int getStudyYear() {
        return studyYear;
    }

    public void setStudyYear(int studyYear) {
        this.studyYear = studyYear;
    }

    public String getFieldOfStudy() {
        return fieldOfStudy;
    }

    public void setFieldOfStudy(String fieldOfStudy) {
        this.fieldOfStudy = fieldOfStudy;
    }

    public double getScholarship() {
        return scholarship;
    }

    public void setScholarship(double scholarship) {
        this.scholarship = scholarship;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Student student = (Student) o;
        return studyYear == student.studyYear && Double.compare(student.scholarship, scholarship) == 0 && Objects.equals(university, student.university) && Objects.equals(fieldOfStudy, student.fieldOfStudy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), university, studyYear, fieldOfStudy, scholarship);
    }

    @Override
    public String toString() {
        return "Student{" +
                "university='" + university + '\'' +
                ", studyYear=" + studyYear +
                ", fieldOfStudy='" + fieldOfStudy + '\'' +
                ", scholarship=" + scholarship +
                '}';
    }
}
