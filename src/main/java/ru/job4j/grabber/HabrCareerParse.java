package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    public static final int PAGES = 5;
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

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= PAGES; i++) {
            HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
            habrCareerParse.list("%s%s%d%s".formatted(SOURCE_LINK, PREFIX, i, SUFFIX))
                    .forEach(post ->
                            System.out.println(post.getTitle() + " " + post.getLink() + " " + post.getCreated()
                                    + " " + post.getDescription()));
        }
    }

    /**
     * Загружает и извлекает описание вакансии по указанной ссылке.
     * <p>
     * Метод использует библиотеку Jsoup для выполнения HTTP-запроса и
     * парсинга HTML-документа. Он ищет элементы с классом
     * "vacancy-description__text" на веб-странице, извлекает текст из всех найденных
     * элементов и объединяет его в одну строку.
     * </p>
     *
     * @param link URL-адрес страницы вакансии, откуда нужно извлечь описание.
     * @return Строка, содержащая описание вакансии, объединённое из всех текстовых блоков с классом "vacancy-description__text".
     * @throws IOException если произошла ошибка при подключении к странице или при загрузке HTML-документа.
     */
    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        List<String> description = document.select(".vacancy-description__text").eachText();
        StringBuilder stringBuilder = new StringBuilder();
        description.forEach(stringBuilder::append);
        return stringBuilder.toString();
    }
}