package pl.plusliga.parser.pls;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pl.plusliga.model.League;
import pl.plusliga.model.Player;
import pl.plusliga.model.Team;
import pl.plusliga.parser.JsoupParser;

public class PlpsPlayerParser implements JsoupParser<Player> {
	protected static Pattern PLAYER_ID_PATTERN = Pattern.compile(".*/id/(\\d+)\\.html");
	protected static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

	protected final Set<Integer> teamIds;

	public PlpsPlayerParser(Collection<Team> teams) {
	    teamIds = teams.stream().map(Team::getId).collect(Collectors.toSet());
	}

	@Override
	public Player getEntity(Element element) {
		Player player = new Player();
		
		player.setId(getInteger(element.baseUri(), PLAYER_ID_PATTERN, 1));
		
		Elements rows = element.select("div.pagecontent > div.row > div > div.row");
		
		String[] names = rows.get(0).select("div > h1").first().text().split(" ");
		switch (names.length) {
		case 2:
			player.setName(names[1] + " " + names[0]);
			break;
		case 3:
			if (names[2].equals("Oliva")) {
				player.setName(names[2] + " " + names[0] + " " + names[1]);
			} else {
				player.setName(names[1] + " " + names[2] + " " + names[0]);
			}
			break;
		case 4:
			player.setName("van de Voorde Simon");
			break;
		case 6:
			player.setName("de Leon Guimaraes da Silva Hugo");
			break;
		default:
			throw new RuntimeException("Unknown names: " + Arrays.asList(names));
		}

		player.setBirthDate(getDate(rows.get(1).select("div > div.datainfo > span").first().text(), DATE_FORMAT));
		player.setTeams(new PlpsPlayerTeamParser(player)
			.getEntities(element, "div.pagecontent > table > tbody > tr", row -> row.child(0).tagName().equals("td")).stream()
			.filter(team -> teamIds.contains(team.getKey().getTeamId()))
			.collect(Collectors.toList())
		);

		if (player.getId() == null) {
			throw new RuntimeException("No id: " + player.getName());
		}
		return player;
	}

	@Override
	public List<Player> getEntities(String url) {
		return getDocument(url).select("div.player > a").stream().map(element -> element.absUrl("href")).map(this::getEntity).collect(Collectors.toList());
	}
	
	public static void main(String args[]) {
		List<Team> teams = new PlpsTeamParser(League.ORLENLIGA).getEntities(League.ORLENLIGA.getTeamsUrl());
		Player player = new PlpsPlayerParser(teams).getEntity("http://www.orlenliga.pl/players/id/85775.html");
		System.out.println(player);
	}
	
}
