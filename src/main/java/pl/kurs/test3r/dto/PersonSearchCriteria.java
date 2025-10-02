package pl.kurs.test3r.dto;

import pl.kurs.test3r.models.person.PersonType;

import java.time.LocalDate;

public class PersonSearchCriteria {

    private PersonType type;
    private String firstName;
    private String lastName;
    private String pesel;
    private String email;
    private Double heightFrom;
    private Double heightTo;
    private Double weightFrom;
    private Double weightTo;

    private LocalDate startDateFrom;
    private LocalDate startDateTo;
    private String currentPosition;
    private Double salaryFrom;
    private Double salaryTo;
    private Integer positionCountFrom;
    private Integer positionCountTo;
    private Integer professionCountFrom;
    private Integer professionCountTo;

    private String university;
    private Integer studyYearFrom;
    private Integer studyYearTo;
    private String fieldOfStudy;
    private Double scholarshipFrom;
    private Double scholarshipTo;

    private Double pensionAmountFrom;
    private Double pensionAmountTo;
    private Integer yearsWorkedFrom;
    private Integer yearsWorkedTo;

    public PersonType getType() {
        return type;
    }

    public void setType(PersonType type) {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getHeightFrom() {
        return heightFrom;
    }

    public void setHeightFrom(Double heightFrom) {
        this.heightFrom = heightFrom;
    }

    public Double getHeightTo() {
        return heightTo;
    }

    public void setHeightTo(Double heightTo) {
        this.heightTo = heightTo;
    }

    public Double getWeightFrom() {
        return weightFrom;
    }

    public void setWeightFrom(Double weightFrom) {
        this.weightFrom = weightFrom;
    }

    public Double getWeightTo() {
        return weightTo;
    }

    public void setWeightTo(Double weightTo) {
        this.weightTo = weightTo;
    }

    public LocalDate getStartDateFrom() {
        return startDateFrom;
    }

    public void setStartDateFrom(LocalDate startDateFrom) {
        this.startDateFrom = startDateFrom;
    }

    public LocalDate getStartDateTo() {
        return startDateTo;
    }

    public void setStartDateTo(LocalDate startDateTo) {
        this.startDateTo = startDateTo;
    }

    public String getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(String currentPosition) {
        this.currentPosition = currentPosition;
    }

    public Double getSalaryFrom() {
        return salaryFrom;
    }

    public void setSalaryFrom(Double salaryFrom) {
        this.salaryFrom = salaryFrom;
    }

    public Double getSalaryTo() {
        return salaryTo;
    }

    public void setSalaryTo(Double salaryTo) {
        this.salaryTo = salaryTo;
    }

    public Integer getPositionCountFrom() {
        return positionCountFrom;
    }

    public void setPositionCountFrom(Integer positionCountFrom) {
        this.positionCountFrom = positionCountFrom;
    }

    public Integer getPositionCountTo() {
        return positionCountTo;
    }

    public void setPositionCountTo(Integer positionCountTo) {
        this.positionCountTo = positionCountTo;
    }

    public Integer getProfessionCountFrom() {
        return professionCountFrom;
    }

    public void setProfessionCountFrom(Integer professionCountFrom) {
        this.professionCountFrom = professionCountFrom;
    }

    public Integer getProfessionCountTo() {
        return professionCountTo;
    }

    public void setProfessionCountTo(Integer professionCountTo) {
        this.professionCountTo = professionCountTo;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public Integer getStudyYearFrom() {
        return studyYearFrom;
    }

    public void setStudyYearFrom(Integer studyYearFrom) {
        this.studyYearFrom = studyYearFrom;
    }

    public Integer getStudyYearTo() {
        return studyYearTo;
    }

    public void setStudyYearTo(Integer studyYearTo) {
        this.studyYearTo = studyYearTo;
    }

    public String getFieldOfStudy() {
        return fieldOfStudy;
    }

    public void setFieldOfStudy(String fieldOfStudy) {
        this.fieldOfStudy = fieldOfStudy;
    }

    public Double getScholarshipFrom() {
        return scholarshipFrom;
    }

    public void setScholarshipFrom(Double scholarshipFrom) {
        this.scholarshipFrom = scholarshipFrom;
    }

    public Double getScholarshipTo() {
        return scholarshipTo;
    }

    public void setScholarshipTo(Double scholarshipTo) {
        this.scholarshipTo = scholarshipTo;
    }

    public Double getPensionAmountFrom() {
        return pensionAmountFrom;
    }

    public void setPensionAmountFrom(Double pensionAmountFrom) {
        this.pensionAmountFrom = pensionAmountFrom;
    }

    public Double getPensionAmountTo() {
        return pensionAmountTo;
    }

    public void setPensionAmountTo(Double pensionAmountTo) {
        this.pensionAmountTo = pensionAmountTo;
    }

    public Integer getYearsWorkedFrom() {
        return yearsWorkedFrom;
    }

    public void setYearsWorkedFrom(Integer yearsWorkedFrom) {
        this.yearsWorkedFrom = yearsWorkedFrom;
    }

    public Integer getYearsWorkedTo() {
        return yearsWorkedTo;
    }

    public void setYearsWorkedTo(Integer yearsWorkedTo) {
        this.yearsWorkedTo = yearsWorkedTo;
    }
}
