package pl.plusliga.parser;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
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
	
	default List<E> getEntities(Element element, String cssQuery, Predicate<Element> elementPredicate) {
		return getEntities(element, cssQuery, elementPredicate, Objects::nonNull);
	}
	
	default List<E> getEntities(Element element, String cssQuery, Predicate<Element> elementPredicate, Predicate<E> entityPredicate) {
		return element.select(cssQuery).stream()
				.filter(elementPredicate)
				.map(this::getEntity)
				.filter(entityPredicate)
				.collect(Collectors.toList());
	}
	
	default Integer getInteger(String text, Pattern pattern, int group) {
		Matcher matcher = pattern.matcher(text);
		if (matcher.matches()) {
		    return Integer.parseInt(matcher.group(group));
		} else {
			return null;
		}
	}
	
	default Date getDate(String text, DateFormat format) {
		if ((text == null) || text.isEmpty()) {
			return null;
		}
		try {
			return format.parse(text);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
