package pl.plusliga.parser.pls;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;

import pl.plusliga.model.Player;
import pl.plusliga.model.PlayerTeam;
import pl.plusliga.model.PlayerTeamKey;
import pl.plusliga.model.Position;
import pl.plusliga.parser.JsoupParser;

public class PlpsPlayerTeamParser implements JsoupParser<PlayerTeam> {
	protected static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	protected static Date START_DATE;
	static {
		try {
			START_DATE = DATE_FORMAT.parse("01.09.2016");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	protected static Pattern TEAM_ID_PATTERN = Pattern.compile(".*/teams/id/(\\d+)/.*");
	
	private final Player player;

	public PlpsPlayerTeamParser(Player player) {
		this.player = player;
	}

	@Override
	public PlayerTeam getEntity(Element element) {
		Date dateFrom = getDate(element.child(0).text(), DATE_FORMAT);
		if (dateFrom.before(START_DATE)) {
			return null;
		}
		
		PlayerTeamKey key = new PlayerTeamKey();
		key.setPlayerId(player.getId());
		key.setTeamId(getInteger(element.child(4).getElementsByTag("a").first().absUrl("href"), TEAM_ID_PATTERN, 1));
		key.setDateFrom(dateFrom);
		
		PlayerTeam team = new PlayerTeam();
		team.setKey(key);
		team.setDateTo(getDate(element.child(1).text(), DATE_FORMAT));
		
		String position = element.child(2).text().toLowerCase();
		if (player.getName().equals("Cutura Hana") || position.isEmpty()) {
			System.err.println(player.getName() + " -> przyjmująca");
			team.setPosition(Position.przyjmująca);
		} else {
			team.setPosition(Position.valueOf(position));
		}
		
		return team;
	}

}
