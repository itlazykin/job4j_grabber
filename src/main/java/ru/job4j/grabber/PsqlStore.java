package ru.job4j.grabber;

import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private Connection connection;

    public PsqlStore() {
        init();
    }

    public PsqlStore(Connection connection) {
        this.connection = connection;
    }

    private void init() {
        try (InputStream input = PsqlStore.class.getClassLoader()
                .getResourceAsStream("db/liquibase.properties")) {
            Properties config = new Properties();
            config.load(input);
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO post (name, text, link, created) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING")) {
            preparedStatement.setString(1, post.getTitle());
            preparedStatement.setString(2, post.getDescription());
            preparedStatement.setString(3, post.getLink());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            preparedStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> result = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM post")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                result.add(createPost(resultSet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Post findById(int id) {
        Post result = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM post WHERE id = ?")) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                result = createPost(resultSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private Post createPost(ResultSet resultSet) throws SQLException {
        Post result = new Post();
        result.setId(resultSet.getInt(1));
        result.setTitle(resultSet.getString(2));
        result.setDescription(resultSet.getString(3));
        result.setLink(resultSet.getString(4));
        result.setCreated(resultSet.getTimestamp(5).toLocalDateTime());
        return result;
    }

    public static void main(String[] args) throws IOException {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        PsqlStore sqlStore = new PsqlStore();
        habrCareerParse.list("https://career.habr.com/vacancies?page=1&q=Java+developer&type=all")
                .forEach(sqlStore::save);
        List<Post> posts = sqlStore.getAll();
        posts.forEach(post ->
                System.out.println(post.getTitle() + " " + post.getLink() + " " + post.getCreated())
        );
        Post post = sqlStore.findById(3);
        System.out.println("Описание вакансии с id = 3: " + post.getDescription());
    }
}