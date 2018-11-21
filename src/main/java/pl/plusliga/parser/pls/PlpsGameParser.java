package pl.plusliga.parser.pls;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;

import pl.plusliga.model.Game;
import pl.plusliga.model.League;
import pl.plusliga.parser.JsoupParser;

public class PlpsGameParser implements JsoupParser<Game> {
	protected static Pattern TEAM_ID_PATTERN = Pattern.compile(".*/teams/id/(\\d+).html");
	protected static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
	protected static Pattern MATCH_ID_PATTERN = Pattern.compile(".*mID=(\\d+).*");

	@Override
	public Game getEntity(Element element) {
		Game game = new Game();
		game.setCup(false);
		
		Element result = element.select("div.gameresult").first();
		game.setHomeScore(Integer.parseInt(result.child(0).text()));
		game.setVisitorScore(Integer.parseInt(result.child(2).text()));
		
		game.setDate(getDate(element.select("div.date").text().replaceFirst("^\\D+", ""), DATE_FORMAT));
		game.setHomeTeamId(getInteger(element.select("h2 > a").get(0).absUrl("href"), TEAM_ID_PATTERN, 1));
		game.setVisitorTeamId(getInteger(element.select("h2 > a").get(1).absUrl("href"), TEAM_ID_PATTERN, 1));
		
		game.setStatsUrl(element.select("a.btn").last().absUrl("href"));
		game.setId(getInteger(game.getStatsUrl(), MATCH_ID_PATTERN, 1));
		
		return game;
	}
	
	@Override
	public List<Game> getEntities(String url) {
		return getEntities(url, game -> game.isComplete());
	}
	
	public List<Game> getEntities(String url, Predicate<Game> predicate) {
		return getEntities(getDocument(url), "div.faza > div.runda > div.kolejka > div.gameData > div.games", ALL_ELEMENTS, predicate);
	}
	
	public static void main(String args[]) {
		List<Game> games = new PlpsGameParser().getEntities(League.PLUSLIGA.getGamesUrl(), game -> true);
		games.forEach(System.out::println);
	}
	
}
