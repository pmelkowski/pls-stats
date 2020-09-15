package pl.plusliga.parser.pls;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.plusliga.model.PlayerGame;
import pl.plusliga.model.PlayerGameKey;
import pl.plusliga.parser.JsoupParser;

public class PlpsPlayerGameParser implements JsoupParser<PlayerGame> {
  protected static Pattern PLAYER_ID_PATTERN = Pattern.compile(".*/players/id/(\\d+)/.*");
  protected static Pattern PCT_PATTERN = Pattern.compile("(-?\\d+)%");

  private final Integer gameId;

  public PlpsPlayerGameParser(Integer gameId) {
    this.gameId = gameId;
  }

  @Override
  public PlayerGame getEntity(Element element) {
    Elements columns = element.children();
    Optional<Integer> playerId = Optional.of(columns)
        .map(Elements::first)
        .map(header -> header.select("a"))
        .map(Elements::first)
        .map(anchor -> anchor.attr("href"))
        .filter(url -> !url.isEmpty())
        .flatMap(url -> getInteger(url, PLAYER_ID_PATTERN, 1));
    if (!playerId.isPresent()) {
      return null;
    }
    String playerNo = columns.first().children().first().text().trim();
    if (playerNo.isEmpty()) {
      System.err.println(columns.first());
      return null;
    }

    PlayerGameKey key = new PlayerGameKey();
    key.setPlayerId(playerId.get());
    key.setGameId(gameId);
    PlayerGame playerGame = new PlayerGame();
    playerGame.setKey(key);

    int setsPlayed = 0;
    for (int set = 1; set <= 5; set++) {
      String position = columns.get(set).text();
      if (position.isEmpty()) {
        continue;
      }
      playerGame.setPlayed(true);
      setsPlayed++;
      if (!position.equals("*")) {
        if (set == 1)
          playerGame.setPrimary(true);
        else if (!playerGame.isPrimary())
          playerGame.setSecondary(true);
      }
    }
    playerGame.setSets(setsPlayed);

    getInteger(columns.get(11)).ifPresent(playerGame::setAces);
    getInteger(columns.get(13)).ifPresent(playerGame::setRecNumber);
    getInteger(columns.get(15).text(), PCT_PATTERN, 1).ifPresent(playerGame::setRecPct);
    getInteger(columns.get(20)).ifPresent(playerGame::setPoints);
    getInteger(columns.get(23)).ifPresent(playerGame::setBlocks);

    return playerGame;
  }

  @Override
  public List<PlayerGame> getEntities(String url) {
    return getEntities(url, "table.rs-standings-table.stats-table tbody tr");
  }

  public static void main(String args[]) {
    List<PlayerGame> games = new PlpsPlayerGameParser(1100521)
        .getEntities("http://www.lsk.pls.pl/games/id/1100521.html");
    games.forEach(System.out::println);
  }

}
