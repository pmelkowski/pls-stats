package pl.plusliga.parser.pls;

import java.util.List;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.plusliga.model.PlayerGame;
import pl.plusliga.model.PlayerGameKey;
import pl.plusliga.parser.JsoupParser;

public class PlpsPlayerGameParser implements JsoupParser<PlayerGame> {
  protected static Pattern PLAYER_ID_PATTERN = Pattern.compile(".*/players/id/(\\d+)/.*");

  private final Integer gameId;

  public PlpsPlayerGameParser(Integer gameId) {
    this.gameId = gameId;
  }

  @Override
  public PlayerGame getEntity(Element element) {
    Elements columns = element.getElementsByTag("td");
    Element playerLink = columns.get(0).select("a").first();
    if (playerLink == null) {
      return null;
    }

    PlayerGameKey key = new PlayerGameKey();
    getInteger(playerLink.absUrl("href"), PLAYER_ID_PATTERN, 1)
       .ifPresent(key::setPlayerId);
    key.setGameId(gameId);
    PlayerGame playerGame = new PlayerGame();
    playerGame.setKey(key);

    int setsPlayed = 0;
    for (int set = 1; set <= 5; set++) {
      String position = columns.get(set).text();
      if (position.isEmpty())
        continue;

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

    getInteger(columns.get(8), text -> text)
        .ifPresent(playerGame::setAces);
    getInteger(columns.get(19), text -> text)
        .ifPresent(playerGame::setPoints);
    getInteger(columns.get(21), text -> text)
        .ifPresent(playerGame::setBlocks);

    getInteger(columns.get(11), text -> text)
        .ifPresent(recTotal -> {
          playerGame.setRecNumber(recTotal);
          if (recTotal > 0) {
            int recFault = getInteger(columns.get(12), text -> text).orElse(0);
            int recNegative = getInteger(columns.get(13), text -> text).orElse(0);
            float recPositive = recTotal - recFault - recNegative;
            playerGame.setRecPct(Math.round(100 * recPositive / recTotal));
          }
        }
    );

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
