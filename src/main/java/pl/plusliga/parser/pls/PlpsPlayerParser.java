package pl.plusliga.parser.pls;

import java.time.format.DateTimeFormatter;
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
  protected static Pattern PLAYER_ID_PATTERN = Pattern.compile(".*/id/(\\d+)(\\.html)?");
  protected static DateTimeFormatter DATE_FORMAT = JsoupParser.buildDateTimeFormatter("dd.MM.yyyy");

  protected final Set<Integer> teamIds;

  public PlpsPlayerParser(Collection<Team> teams) {
    teamIds = teams.stream().map(Team::getId).collect(Collectors.toSet());
  }

  @Override
  public Player getEntity(Element element) {
    Player player = new Player();

    getInteger(element.baseUri(), PLAYER_ID_PATTERN, 1).ifPresent(player::setId);

    Elements rows = element.select("div.pagecontent > div.row > div > div.row");

    player.setName(rows.get(0).select("div > h1").first().text());
    getDate(rows.get(1).select("div > div.datainfo > span").first().text(), DATE_FORMAT)
        .ifPresent(player::setBirthDate);
    player.setTeams(new PlpsPlayerTeamParser(player)
        .getEntities(element, "div.pagecontent > table > tbody > tr",
            row -> row.child(0).tagName().equals("td"))
        .stream().filter(team -> teamIds.contains(team.getKey().getTeamId()))
        .collect(Collectors.toList()));

    if (player.getId() == null) {
      throw new RuntimeException("No id: " + player.getName());
    }
    return player;
  }

  @Override
  public List<Player> getEntities(String url) {
    return getDocument(url).select("div.player > a").stream()
        .map(element -> element.absUrl("href"))
        .map(this::getEntity)
        .collect(Collectors.toList());
  }

  public static void main(String args[]) {
    List<Team> teams =
        new PlpsTeamParser(League.LSK).getEntities(League.LSK.getTeamsUrl());
    Player player =
        new PlpsPlayerParser(teams).getEntity("http://www.orlenliga.pl/players/id/85775.html");
    System.out.println(player);
  }

}
