package pl.kurs.test3r.services.crud;

import java.util.List;

public interface ICrudService<T> {
    T add(T entity);
    T edit(T entity);
    T get(Long id);
    List<T> getAll();
}
