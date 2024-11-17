package ru.job4j.grabber;

import java.util.List;

/**
 * Проект будет хранить данные в базе Postgresql. Связь с базой будет осуществляться через интерфейс Store.
 */
public interface Store extends AutoCloseable {
    void save(Post post);

    List<Post> getAll();

    Post findById(int id);
}
