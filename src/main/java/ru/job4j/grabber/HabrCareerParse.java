package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HabrCareerParse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";

    public static void main(String[] args) throws IOException {
        /* формируем ссылку с учетом номера страницы и получаем саму страницу, чтобы с ней можно было работать */
        int pageNumber = 1;
        String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
        Connection connection = Jsoup.connect(fullLink);
        Document document = connection.get();
        /* получаем все вакансии страницы */
        Elements rows = document.select(".vacancy-card__inner");
        /*
        Проходимся по каждой вакансии и извлекаем нужные для нас данные.
        Сначала получаем элементы содержащие название и ссылку.
        Стоит обратить внимание, что дочерние элементы можно получать через индекс - метод child(0)
        или же через селектор - select(".vacancy-card__title").
         */
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            Element dateElement = row.select(".vacancy-card__date").first();
            /*
            Получаем данные непосредственно. text() возвращает все содержимое элемента в виде текста,
            т.е. весь текст что находится вне тегов HTML. Ссылка находится в виде атрибута,
            поэтому ее значение надо получить как значение атрибута. Для этого служит метод attr()
             */
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            System.out.printf("%s %s %s %n", dateElement.text(), titleElement.text(), link);
        });
    }
}