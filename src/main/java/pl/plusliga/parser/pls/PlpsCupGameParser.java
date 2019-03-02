package pl.plusliga.parser.pls;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.plusliga.model.Game;
import pl.plusliga.model.League;
import pl.plusliga.model.Team;
import pl.plusliga.parser.JsoupParser;

public class PlpsCupGameParser implements JsoupParser<Game> {
  protected static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
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
    game.setHomeScore(
        getInteger(divs.get(2).child(0), UnaryOperator.identity()).orElse(0));
    game.setVisitorScore(
        getInteger(divs.get(2).child(1), UnaryOperator.identity()).orElse(0));
    getDate(divs.get(0).select("span").text(), DATE_FORMAT)
        .ifPresent(game::setDate);
    game.setHomeTeamId(teamIds.get(divs.get(1).select("span").text()));
    game.setVisitorTeamId(teamIds.get(divs.get(3).select("span").text()));
    Elements hrefs = divs.get(4).select("span > a");
    if (hrefs.size() > 1) {
      game.setStatsUrl(hrefs.get(1).absUrl("href"));
      getInteger(game.getStatsUrl(), MATCH_ID_PATTERN, 1).ifPresent(game::setId);
    }

    if (!game.isComplete()) {
      System.err.println(game);
      return null;
    }
    return game;
  }

  @Override
  public List<Game> getEntities(String url) {
    return getEntities(url, "div.box_pp > div.pagecontent > div.row");
  }

  public static void main(String args[]) {
    List<Team> teams =
        new PlpsTeamParser(League.PLUSLIGA).getEntities(League.PLUSLIGA.getTeamsUrl());
    List<Game> games = new PlpsCupGameParser(teams).getEntities(League.PLUSLIGA.getCupGamesUrl());
    games.forEach(System.out::println);
  }

}
