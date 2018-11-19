package pl.plusliga.parser;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pl.plusliga.model.Player;

public class PlpsPlayerParser implements JsoupParser<Player> {
	protected static Pattern PLAYER_ID_PATTERN = Pattern.compile(".*/id/(\\d+)\\.html");
	protected static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	
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
		player.setTeams(new PlpsPlayerTeamParser(player).getEntities(element, "div.pagecontent > table > tbody > tr", row -> row.child(0).tagName().equals("td")));

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
		Player player = new PlpsPlayerParser().getEntity("http://www.orlenliga.pl/players/id/85775.html");
		System.out.println(player);
	}
	
}
