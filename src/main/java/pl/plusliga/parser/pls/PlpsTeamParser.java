package pl.plusliga.parser.pls;

import java.util.List;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import pl.plusliga.model.League;
import pl.plusliga.model.Team;
import pl.plusliga.parser.JsoupParser;

public class PlpsTeamParser implements JsoupParser<Team> {
  protected static Pattern TEAM_ID_PATTERN = Pattern.compile(".*/teams/id/(\\d+).html");

  private final League league;

  public PlpsTeamParser(League league) {
    this.league = league;
  }

  @Override
  public Team getEntity(Element element) {
    Team team = new Team();
    team.setId(getInteger(element.absUrl("href"), TEAM_ID_PATTERN, 1));
    team.setName(element.text());
    team.setLeague(league);
    return team;
  }

  @Override
  public List<Team> getEntities(String url) {
    return getEntities(url, "div.teamlist > div.caption > h3 > a");
  }

  public static void main(String args[]) {
    List<Team> teams =
        new PlpsTeamParser(League.ORLENLIGA).getEntities(League.ORLENLIGA.getTeamsUrl());
    System.out.println(teams);
  }

}
