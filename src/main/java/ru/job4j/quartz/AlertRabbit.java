package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;

/**
 * Класс AlertRabbit демонстрирует использование библиотеки Quartz для
 * планирования задач с сохранением результатов выполнения в базу данных.
 * Приложение периодически выполняет задачу, записывая текущее время в базу данных.
 */
public class AlertRabbit {
    public static void main(String[] args) throws ClassNotFoundException {
        /* Загрузка драйвера базы данных из конфигурационного файла */
        Class.forName(init().getProperty("jdbc.driver-class-name"));
        try {
            /* Создание соединения с базой данных с параметрами из конфигурационного файла */
            Connection connection = DriverManager.getConnection(
                    init().getProperty("jdbc.url"),
                    init().getProperty("jdbc.username"),
                    init().getProperty("jdbc.password")
            );
            /* Создание и запуск планировщика Quartz */
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            /* Создание мапы данных для передачи соединения в задание */
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            /* Создание задания с использованием класса Rabbit и передачей мапы данных */
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            /* Настройка расписания для выполнения задания через указанный интервал времени */
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(
                            init().getProperty("rabbit.interval"))
                    )
                    .repeatForever();
            /* Создание триггера для немедленного запуска задания и повторного выполнения */
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            /* Назначение задания планировщику с указанным триггером */
            scheduler.scheduleJob(job, trigger);
            /* Пауза на 10 секунд перед завершением работы приложения */
            Thread.sleep(10000);
            /* Остановка планировщика и закрытие соединения с базой данных */
            scheduler.shutdown();
            connection.close();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    /**
     * Класс Rabbit реализует интерфейс Job из библиотеки Quartz.
     * Логика выполнения задачи заключается в записи текущей даты и времени в базу данных.
     */
    private static Properties init() {
        try (InputStream input = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            Properties config = new Properties();
            config.load(input);
            return config;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /*
    Конструктор выводит в консоль уникальный hashCode объекта Rabbit.
    Это сделано для демонстрации того, что каждый раз создается новый экземпляр задачи.
     */
    public static class Rabbit implements Job {
        public Rabbit() {
            System.out.println(hashCode());
        }

        /* Метод execute выполняется каждый раз, когда планировщик запускает задачу.
         * В данном случае, метод записывает текущую дату и время в таблицу базы данных */
        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            /* Извлечение соединения с базой данных из мапы данных задания */
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            /* Выполнение SQL-запроса для вставки текущей даты и времени в таблицу */
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO rabbit(created_date) values (?)")) {
                preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                preparedStatement.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}