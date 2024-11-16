package ru.job4j.grabber;

import org.quartz.SchedulerException;

/**
 * В этом проекты мы будем использовать quartz для запуска парсера. Но напрямую мы не будем его использовать.
 * Абстрагируемся через интерфейс Grab.
 */
public interface Grab {
    void init() throws SchedulerException;
}