package pl.plusliga.parser;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pl.plusliga.model.Game;
import pl.plusliga.model.League;
import pl.plusliga.model.Team;

public class PlpsCupGameParser implements JsoupParser<Game> {
	protected static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	protected static Pattern MATCH_ID_PATTERN = Pattern.compile(".*mID=(\\d+).*");
	
	private final Map<String, Integer> teamIds;
	
	public PlpsCupGameParser(Collection<Team> teams) {
		teamIds = teams.stream().collect(Collectors.toMap(Team::getName, Team::getId));
	}

	@Override
	public Game getEntity(Element element) {
		Game game = new Game();
		game.setCup(true);
		
		Elements divs = element.select("div.row > div");
		
		try {
			game.setHomeScore(Integer.parseInt(divs.get(2).child(0).text()));
			game.setVisitorScore(Integer.parseInt(divs.get(2).child(1).text()));
		} catch (NumberFormatException e) {
			System.err.println(divs.get(2));
			return null;
		}
		if (!game.isComplete()) {
			return null;
		}
		
		game.setDate(getDate(divs.get(0).select("span").text(), DATE_FORMAT));
		game.setHomeTeamId(teamIds.get(divs.get(1).select("span").text()));
		game.setVisitorTeamId(teamIds.get(divs.get(3).select("span").text()));
		Elements hrefs = divs.get(4).select("span > a");
		if (hrefs.size() > 1) {
			game.setStatsUrl(hrefs.get(1).absUrl("href"));
			game.setId(getInteger(game.getStatsUrl(), MATCH_ID_PATTERN, 1));
		}
		
		return game;
	}
	
	@Override
	public List<Game> getEntities(String url) {
		return getEntities(url, "div.box_pp > div.pagecontent > div.row");
	}
	
	public static void main(String args[]) {
		List<Team> teams = new PlpsTeamParser(League.PLUSLIGA).getEntities(League.PLUSLIGA.getTeamsUrl());
		List<Game> games = new PlpsCupGameParser(teams).getEntities(League.PLUSLIGA.getCupGamesUrl());
		games.forEach(System.out::println);
	}
	
}
