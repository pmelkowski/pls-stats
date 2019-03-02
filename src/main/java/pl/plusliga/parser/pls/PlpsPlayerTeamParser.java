package pl.plusliga.parser.pls;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
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
    Optional<Date> dateTo = getDate(element.child(1).text(), DATE_FORMAT);
    if (dateTo.filter(date -> date.before(START_DATE)).isPresent()) {
      return null;
    }

    PlayerTeamKey key = new PlayerTeamKey();
    key.setPlayerId(player.getId());
    getInteger(element.child(4).getElementsByTag("a").first().absUrl("href"), TEAM_ID_PATTERN, 1)
        .ifPresent(key::setTeamId);
    getDate(element.child(0).text(), DATE_FORMAT).ifPresent(key::setDateFrom);

    PlayerTeam team = new PlayerTeam();
    team.setKey(key);
    dateTo.ifPresent(team::setDateTo);
    team.setPosition(Position.valueOf(element.child(2).text().toLowerCase()));

    return team;
  }

}
