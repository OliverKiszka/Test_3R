package pl.kurs.test3r.models.person;

public enum PersonType {
    EMPLOYEE(Employee.class),
    STUDENT(Student.class),
    RETIREE(Retiree.class);

    private final Class<? extends Person> entityClass;

    PersonType(Class<? extends Person> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<? extends Person> getEntityClass(){
        return entityClass;
    }
}
