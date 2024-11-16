package ru.job4j.grabber;

import java.io.IOException;
import java.util.List;

/**
 * Операция извлечения данных с  сайта описывается отдельным интерфейсом Parse.
 * Этот компонент позволяет собрать короткое описание всех объявлений, а так же загрузить детали по каждому объявлению.
 */
public interface Parse {
    List<Post> list(String link) throws IOException;
}
