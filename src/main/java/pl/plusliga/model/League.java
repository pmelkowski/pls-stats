package pl.plusliga.model;

import java.util.Arrays;
import java.util.EnumSet;

public enum League {

  LSK("https://www.lsk.plps.pl/teams.html",
      "https://www.lsk.pls.pl/players.html?memo=%7B%22players%22%3A%7B%22mainFilter%22%3A%22letter%22%2C%22subFilter%22%3A%22all%22%7D%7D",
      "https://www.lsk.pls.pl/players/id/",
      "https://www.lsk.pls.pl/games.html?memo=%7B%22games%22%3A%7B%7D%7D",
      "https://www.lsk.pls.pl/pcup/s/games.html",
      "https://www.lsk.pls.pl/scup/s/game.html",
      Position.atakująca, Position.libero, Position.przyjmująca, Position.rozgrywająca,
      Position.środkowa),
  PLUSLIGA("http://www.plusliga.pl/teams.html",
      "https://www.plusliga.pl/players.html?memo=%7B%22players%22%3A%7B%22mainFilter%22%3A%22letter%22%2C%22subFilter%22%3A%22all%22%7D%7D",
      "https://www.plusliga.pl/players/id/",
      "https://www.plusliga.pl/games.html?memo=%7B%22games%22%3A%7B%7D%7D",
      "https://www.plusliga.pl/pcup/s/games.html",
      "https://www.plusliga.pl/scup/s/game.html",
      Position.atakujący, Position.libero, Position.przyjmujący, Position.rozgrywający,
      Position.środkowy);

  final String teamsUrl;
  final String playersUrl;
  final String playerUrl;
  final String gamesUrl;
  final String cupGamesUrl;
  final String superCupGameUrl;
  final EnumSet<Position> positions;

  League(String teamsUrl, String playersUrl, String playerUrl, String gamesUrl, String cupGamesUrl,
      String suoerCupGameUrl, Position... positions) {
    this.teamsUrl = teamsUrl;
    this.playersUrl = playersUrl;
    this.playerUrl = playerUrl;
    this.gamesUrl = gamesUrl;
    this.cupGamesUrl = cupGamesUrl;
    this.superCupGameUrl = suoerCupGameUrl;
    this.positions = EnumSet.of(positions[0], Arrays.copyOfRange(positions, 1, positions.length));
  }

  public String getTeamsUrl() {
    return teamsUrl;
  }

  public String getPlayersUrl() {
    return playersUrl;
  }

  public String getPlayerUrl(Integer playerId) {
    return playerUrl + playerId;
  }

  public String getGamesUrl() {
    return gamesUrl;
  }

  public String getCupGamesUrl() {
    return cupGamesUrl;
  }

  public EnumSet<Position> getPositions() {
    return positions;
  }

  public String getSuperCupGameUrl() {
    return superCupGameUrl;
  }

}
