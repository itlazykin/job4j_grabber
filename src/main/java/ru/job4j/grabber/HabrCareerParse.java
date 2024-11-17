package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> result = new ArrayList<>();
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String titlePost = titleElement.text();
            String linkPost = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            Element dateElement = row.select(".vacancy-card__date").first();
            String vacancyDate = dateElement.child(0).attr("datetime");
            LocalDateTime datePost = dateTimeParser.parse(vacancyDate);
            Post post = new Post();
            post.setTitle(titlePost);
            post.setLink(linkPost);
            try {
                post.setDescription(retrieveDescription(linkPost));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            post.setCreated(datePost);
            result.add(post);
        });
        return result;
    }

    private String retrieveDescription(String link) throws IOException {
        StringBuilder description = new StringBuilder();
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-description__text");
        rows.forEach(
                row -> {
                    for (int i = 0; i < row.childrenSize(); i++) {
                        Element element = row.child(i);
                        description.append(element.text()).append(System.lineSeparator());
                    }
                });
        return description.toString();
    }
}