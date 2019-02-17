package pl.plusliga.parser.pls;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import pl.plusliga.model.Player;
import pl.plusliga.model.PlayerTeam;
import pl.plusliga.model.PlayerTeamKey;
import pl.plusliga.model.Position;
import pl.plusliga.parser.JsoupParser;

public class PlpsPlayerTeamParser implements JsoupParser<PlayerTeam> {
  protected static DateTimeFormatter DATE_FORMAT = JsoupParser.buildDateTimeFormatter("dd.MM.yyyy");
  protected static Date START_DATE = Date.from(ZonedDateTime.now().minusYears(1).toInstant());
  protected static Pattern TEAM_ID_PATTERN = Pattern.compile(".*/teams/id/(\\d+)/.*");

  private final Player player;

  public PlpsPlayerTeamParser(Player player) {
    this.player = player;
  }

  @Override
  public PlayerTeam getEntity(Element element) {
    Date dateFrom = getDate(element.child(0).text(), DATE_FORMAT)
        .filter(date -> date.after(START_DATE))
        .orElse(null);
    if (dateFrom == null) {
      return null;
    }

    PlayerTeamKey key = new PlayerTeamKey();
    key.setPlayerId(player.getId());
    getInteger(element.child(4).getElementsByTag("a").first().absUrl("href"), TEAM_ID_PATTERN, 1)
        .ifPresent(key::setTeamId);
    key.setDateFrom(dateFrom);

    PlayerTeam team = new PlayerTeam();
    team.setKey(key);
    getDate(element.child(1).text(), DATE_FORMAT).ifPresent(key::setDateFrom);

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
