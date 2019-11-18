package pl.plusliga.parser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public interface JsoupParser<E> {
  public static final int TIMEOUT = 60000;
  public static final Predicate<Element> ALL_ELEMENTS = element -> true;

  public E getEntity(Element element);

  default Document getDocument(String url) {
    try {
      Connection conn = Jsoup.connect(url);
      conn.timeout(TIMEOUT);
      conn.maxBodySize(0);
      return conn.get();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  default E getEntity(String url) {
    return getEntity(getDocument(url));
  }

  default List<E> getEntities(String url) {
    return Collections.singletonList(getEntity(url));
  }

  default List<E> getEntities(String url, String cssQuery) {
    return getEntities(getDocument(url), cssQuery, ALL_ELEMENTS, Objects::nonNull);
  }

  default List<E> getEntities(Element element, String cssQuery) {
    return getEntities(element, cssQuery, ALL_ELEMENTS, Objects::nonNull);
  }

  default List<E> getEntities(Element element, String cssQuery,
      Predicate<Element> elementPredicate) {
    return getEntities(element, cssQuery, elementPredicate, Objects::nonNull);
  }

  default List<E> getEntities(Element element, String cssQuery, Predicate<Element> elementPredicate,
      Predicate<E> entityPredicate) {
    return element.select(cssQuery).stream()
        .filter(elementPredicate)
        .map(this::getEntity)
        .filter(entityPredicate)
        .collect(Collectors.toList());
  }

  default Optional<String> getString(Element element, UnaryOperator<String> operator) {
    return Optional.ofNullable(element)
        .map(Element::text)
        .map(operator)
        .filter(t -> !t.isEmpty());
  }

  default Optional<Integer> getInteger(Element element) {
    return getInteger(element, UnaryOperator.identity());
  }

  default Optional<Integer> getInteger(Element element, UnaryOperator<String> operator) {
    return getString(element, operator)
        .map(Integer::parseInt);
  }

  default Optional<Integer> getInteger(String text, Pattern pattern, int group) {
    return Optional.ofNullable(text)
        .map(pattern::matcher)
        .filter(Matcher::matches)
        .map(matcher -> matcher.group(group))
        .filter(t -> !t.isEmpty())
        .map(Integer::parseInt);
  }

  default Optional<Float> getFloat(Element element) {
    return getString(element, s -> s.replace(',', '.'))
        .map(Float::parseFloat);
  }

  static DateTimeFormatter buildDateTimeFormatter(String pattern) {
    return new DateTimeFormatterBuilder().appendPattern(pattern)
        .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
        .toFormatter();
  }

  default Optional<Date> getDate(String text, DateTimeFormatter formatter) {
    return Optional.ofNullable(text)
        .filter(t -> !t.isEmpty())
        .map(t -> LocalDateTime.parse(t, formatter))
        .map(ldt -> ldt.toInstant(OffsetDateTime.now().getOffset()))
        .map(Date::from);
  }

}
