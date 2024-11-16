package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class HabrCareerParse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    public static final int PAGES = 5;

    public static void main(String[] args) throws IOException {
        HabrCareerParse parser = new HabrCareerParse();
        /* формируем ссылку с учетом номера страницы и получаем саму страницу, чтобы с ней можно было работать */
        for (int i = 1; i <= PAGES; i++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, i, SUFFIX);
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
                try {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element linkElement = titleElement.child(0);
                    Element dateElement = row.select(".vacancy-card__date").first();
                    /*
                    Получаем данные непосредственно. text() возвращает все содержимое элемента в виде текста,
                    т.е. весь текст что находится вне тегов HTML. Ссылка находится в виде атрибута,
                    поэтому ее значение надо получить как значение атрибута. Для этого служит метод attr()
                    */
                    String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                    System.out.printf(
                            "Дата: %s | Название: %s | Ссылка: %s%nОписание: %s%n%n",
                            dateElement.text(), titleElement.text(), link, parser.retrieveDescription(link));
                } catch (Exception e) {
                    System.out.println("Не удалось загрузить описание для вакансии: " + e.getMessage());
                }
            });
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