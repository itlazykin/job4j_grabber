package ru.job4j.grabber;

import ru.job4j.quartz.AlertRabbit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlPostStore implements Store {
    private Connection connection;
    private static final Logger LOG = LoggerFactory.getLogger(PsqlPostStore.class.getName());

    public PsqlPostStore(Properties config) throws SQLException {
        connection = DriverManager.getConnection(
                config.getProperty("url"),
                config.getProperty("username"),
                config.getProperty("password")
        );
        try {
            Class.forName(config.getProperty("driver-class-name"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        String sql = "INSERT INTO post (name, text, link, created) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, post.getTitle());
            preparedStatement.setString(2, post.getDescription());
            preparedStatement.setString(3, post.getLink());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            preparedStatement.execute();
        } catch (SQLException e) {
            LOG.error("Something wrong with request to database ", e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> allPosts = new ArrayList<>();
        String sql = "SELECT * FROM post";
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    allPosts.add(getPost(resultSet));
                }
            }
        } catch (SQLException e) {
            LOG.error("Something wrong with request to database ", e);
        }
        return allPosts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        String sql = "SELECT * FROM post WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    post = getPost(resultSet);
                }
            }
        } catch (SQLException e) {
            LOG.error("Something wrong with request to database ", e);
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    private Post getPost(ResultSet resultSet) throws SQLException {
        Post post = new Post();
        post.setId(resultSet.getInt("id"));
        post.setTitle(resultSet.getString("name"));
        post.setLink(resultSet.getString("link"));
        post.setDescription(resultSet.getString("text"));
        post.setCreated(resultSet.getTimestamp("created").toLocalDateTime());
        return post;
    }

    public static void main(String[] args) {
        Post test = new Post();
        test.setTitle("Title");
        test.setDescription("Text");
        test.setLink("some link");
        test.setCreated(LocalDateTime.now());
        System.out.println(test);
        try (Store psqlPostStore = new PsqlPostStore(AlertRabbit.readProps("rabbit.properties"))) {
            psqlPostStore.save(test);
            List<Post> posts = psqlPostStore.getAll();
            System.out.println(posts);
            Post post = psqlPostStore.findById(posts.get(0).getId());
            System.out.println(post);
            System.out.println(test.getLink().equals(post.getLink()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}