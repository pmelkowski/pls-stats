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

public class PlpsSuperCupGameParser implements JsoupParser<Game> {
	
	protected static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	protected static Pattern MATCH_ID_PATTERN = Pattern.compile(".*mID=(\\d+).*");
	
	private final Map<String, Integer> teamIds;
	
	public PlpsSuperCupGameParser(Collection<Team> teams) {
		teamIds = teams.stream().collect(Collectors.toMap(Team::getName, Team::getId));
	}

	@Override
	public Game getEntity(Element element) {
		Game game = new Game();
		game.setCup(true);
		
		Elements scores = element.select("div.team_score_container_pp > div > span");
		try {
			game.setHomeScore(Integer.parseInt(scores.get(0).text()));
			game.setVisitorScore(Integer.parseInt(scores.get(1).text()));
		} catch (NumberFormatException e) {
			System.err.println(scores);
			return null;
		}
		if (!game.isComplete()) {
			return null;
		}
		
		game.setDate(getDate(element.select("div.team_score_pp > h3").text(), DATE_FORMAT));
		game.setHomeTeamId(teamIds.get(element.select("div.team_a_pp > h5").text()));
		game.setVisitorTeamId(teamIds.get(element.select("div.team_b_pp > h5").text()));
		game.setStatsUrl(element.select("div.team_score_pp > a").get(0).absUrl("href"));
		game.setId(getInteger(game.getStatsUrl(), MATCH_ID_PATTERN, 1));
		
		return game;
	}

	@Override
	public List<Game> getEntities(String url) {
		return getEntities(url, "div.game_pp");
	}

	public static void main(String args[]) {
		List<Team> teams = new PlpsTeamParser(League.PLUSLIGA).getEntities(League.PLUSLIGA.getTeamsUrl());
		List<Game> games = new PlpsSuperCupGameParser(teams).getEntities(League.PLUSLIGA.getSuperCupGameUrl());
		games.forEach(System.out::println);
	}

}
