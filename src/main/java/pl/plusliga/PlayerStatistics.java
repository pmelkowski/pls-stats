package pl.plusliga;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import pl.plusliga.model.Game;
import pl.plusliga.model.Player;
import pl.plusliga.model.PlayerGame;
import pl.plusliga.model.PlayerTeam;
import pl.plusliga.model.Team;

public class PlayerStatistics {
  protected static final int WINDOW = 6;
  protected static final int HALF_WINDOW = Math.round(WINDOW / 2.0f);

  public static BiPredicate<Player, Game> playedGame = (player, game) -> {
    return player.getTeams().stream().filter(team -> team.isCurrent(game.getDate())).findAny()
        .map(PlayerTeam::getTeam).map(Team::getId)
        .map(
            teamId -> teamId.equals(game.getHomeTeamId()) || teamId.equals(game.getVisitorTeamId()))
        .orElse(false);
  };

  public static BiPredicate<Player, Game> homeGame = (player, game) -> {
    return player.getTeams().stream().filter(team -> team.isCurrent(game.getDate())).findAny()
        .map(PlayerTeam::getTeam).map(Team::getId)
        .map(teamId -> teamId.equals(game.getHomeTeamId())).orElse(false);
  };

  public static Function<PlayerGame, PlayerTeam> playerTeam = game -> {
    return game.getPlayer().getTeams().stream()
        .filter(team -> team.isCurrent(game.getGame().getDate())).findAny().get();
  };

  public static BiFunction<PlayerGame, PlayerTeam, Integer> receptionScore = (game, team) -> {
    int multiplier;
    switch (team.getPosition()) {
      case libero:
        if (game.getRecNumber() >= 11) {
          multiplier = 4;
        } else if (game.getRecNumber() >= 5) {
          multiplier = 2;
        } else {
          return 0;
        }
        break;
      case przyjmująca:
      case przyjmujący:
        if (game.getRecNumber() >= 11) {
          multiplier = 2;
        } else if (game.getRecNumber() >= 5) {
          multiplier = 1;
        } else {
          return 0;
        }
        break;
      default:
        return 0;
    }

    if (game.getRecPct() >= 81) {
      return multiplier * 5;
    } else if (game.getRecPct() >= 66) {
      return multiplier * 4;
    } else if (game.getRecPct() >= 51) {
      return multiplier * 3;
    } else if (game.getRecPct() >= 36) {
      return multiplier * 2;
    } else if (game.getRecPct() >= 26) {
      return multiplier * 1;
    } else {
      return 0;
    }
  };

  public static BiFunction<PlayerGame, PlayerTeam, Integer> individualScore = (game, team) -> {
    if (!game.isPlayed()) {
      return 0;
    }
    switch (team.getPosition()) {
      case atakująca:
      case atakujący:
        return (game.isPrimary() ? 2 : 1) + game.getPoints() + 2 * game.getAces()
            + 2 * game.getBlocks();
      case libero:
        return 1 + receptionScore.apply(game, team);
      case przyjmująca:
      case przyjmujący:
        return (game.isPrimary() ? 2 : 1) + game.getPoints() + 2 * game.getAces()
            + 2 * game.getBlocks() + receptionScore.apply(game, team);
      case rozgrywająca:
      case rozgrywający:
        return (game.isPrimary() ? 2 : 1) + 3 * game.getPoints() + 3 * game.getAces()
            + 2 * game.getBlocks();
      case środkowa:
      case środkowy:
        return (game.isPrimary() ? 2 : 1) + (int) (1.5 * game.getPoints() + 0.5)
            + 2 * game.getAces() + 2 * game.getBlocks();
      default:
        return 0;
    }
  };

  public static BiFunction<PlayerGame, PlayerTeam, Integer> settingScore = (game, team) -> {
    switch (team.getPosition()) {
      case rozgrywająca:
      case rozgrywający:
        switch (game.getGame().getScore(team.getTeam().getId())) {
          case 3:
            return game.isPrimary() ? 10 : (game.isSecondary() ? 5 : 0);
          case 2:
            return game.isPrimary() ? 7 : (game.isSecondary() ? 3 : 0);
          case 1:
            return game.isPrimary() ? 4 : (game.isSecondary() ? 2 : 0);
          case -1:
            return game.isPrimary() ? 1 : 0;
        }
      default:
        return 0;
    }
  };

  public static BiFunction<PlayerGame, PlayerTeam, Integer> teamScore = (game, team) -> {
    if (!game.isPlayed()) {
      return 0;
    }
    switch (game.getGame().getScore(team.getTeam().getId())) {
      case 3:
      case 2:
        return settingScore.apply(game, team) + 3;
      case 1:
        return settingScore.apply(game, team) + 2;
      case -1:
        return settingScore.apply(game, team) + 1;
      default:
        return 0;
    }
  };

  private final Player player;
  private final ExponentialMovingAverage inTeam = new ExponentialMovingAverage(WINDOW, MetricType.PERCENT);
  private final ExponentialMovingAverage played = new ExponentialMovingAverage(WINDOW, MetricType.PERCENT);
  private final ExponentialMovingAverage primary = new ExponentialMovingAverage(WINDOW, MetricType.PERCENT);
  private final ExponentialMovingAverage secondary = new ExponentialMovingAverage(WINDOW, MetricType.PERCENT);
  private final ExponentialMovingAverage sets = new ExponentialMovingAverage(WINDOW, MetricType.SMALLINT);
  private final ExponentialMovingAverage setsPlayed = new ExponentialMovingAverage(WINDOW, MetricType.SMALLINT);
  private final ExponentialMovingAverage setsPlayedPct = new ExponentialMovingAverage(WINDOW, MetricType.PERCENT);
  private final ExponentialMovingAverage pointsPerSet = new ExponentialMovingAverage(WINDOW, MetricType.FLOAT);
  private final ExponentialMovingAverage playerPoints = new ExponentialMovingAverage(WINDOW, MetricType.INT);
  private final ExponentialMovingAverage teamPoints = new ExponentialMovingAverage(WINDOW, MetricType.INT);
  private final ExponentialMovingAverage gamePoints = new ExponentialMovingAverage(WINDOW, MetricType.INT);
  private final ExponentialMovingAverage pointsPerSetHome = new ExponentialMovingAverage(HALF_WINDOW, MetricType.FLOAT);
  private final ExponentialMovingAverage playerPointsHome = new ExponentialMovingAverage(HALF_WINDOW, MetricType.INT);
  private final ExponentialMovingAverage teamPointsHome = new ExponentialMovingAverage(HALF_WINDOW, MetricType.INT);
  private final ExponentialMovingAverage gamePointsHome = new ExponentialMovingAverage(HALF_WINDOW, MetricType.INT);
  private final ExponentialMovingAverage pointsPerSetAway = new ExponentialMovingAverage(HALF_WINDOW, MetricType.FLOAT);
  private final ExponentialMovingAverage playerPointsAway = new ExponentialMovingAverage(HALF_WINDOW, MetricType.INT);
  private final ExponentialMovingAverage teamPointsAway = new ExponentialMovingAverage(HALF_WINDOW, MetricType.INT);
  private final ExponentialMovingAverage gamePointsAway = new ExponentialMovingAverage(HALF_WINDOW, MetricType.INT);
  private int totalPoints = 0;

  public PlayerStatistics(Player player, List<Game> allGames) {
    this.player = player;
    allGames.stream().filter(game -> playedGame.test(player, game)).map(Game::getId)
        .map(gameId -> player.getGames().get(gameId)).forEach(stats -> {
          if (stats != null) {
            inTeam.append(100);
            played.append(stats.isPlayed() ? 100 : 0);
            primary.append(stats.isPrimary() ? 100 : 0);
            secondary.append(stats.isSecondary() ? 100 : 0);

            sets.append(stats.getSets());
            float setsPlayed = stats.getSets();
            if (setsPlayed > stats.getGame().getSets()) {
              setsPlayed = stats.getGame().getSets();
            }
            this.setsPlayed.append(setsPlayed);
            setsPlayedPct.append(100 * setsPlayed / stats.getGame().getSets());

            if (setsPlayed > 0) {
              PlayerTeam gameTeam = playerTeam.apply(stats);
              float points = individualScore.apply(stats, gameTeam);
              float team = teamScore.apply(stats, gameTeam);

              pointsPerSet.append(points / setsPlayed);
              playerPoints.append(points);
              teamPoints.append(team);
              gamePoints.append(points + team);
              if (!stats.getGame().isCup()) {
                if (homeGame.test(player, stats.getGame())) {
                  pointsPerSetHome.append(points / setsPlayed);
                  playerPointsHome.append(points);
                  teamPointsHome.append(team);
                  gamePointsHome.append(points + team);
                } else {
                  pointsPerSetAway.append(points / setsPlayed);
                  playerPointsAway.append(points);
                  teamPointsAway.append(team);
                  gamePointsAway.append(points + team);
                }
                totalPoints += points + team;
              }
            }
          } else {
            inTeam.append(0);
            played.append(0);
            primary.append(0);
            secondary.append(0);
          }
        });
  }

  public double getPointsMean() {
    return gamePoints.getMean();
  }

  public String getPlayerName() {
    return player.getName();
  }

  @Override
  public String toString() {
    return String.format(
        "%-25s %-35s"
            + " %2d/%-2d %6.2f%% %6.2f%% %6.2f%% %6.2f%%"
            + "   %s   %s    %s   %s   %s   %s   %-4d\n"
            + "%149s %s   %s   %s   %s\n"
            + "%149s %s   %s   %s   %s",
            player.getName(), player.getTeam().getName(),
            setsPlayed.getCount(), inTeam.getCount(), inTeam.getMean(), played.getMean(), primary.getMean(), secondary.getMean(),
            setsPlayed, setsPlayedPct, pointsPerSet, playerPoints, teamPoints, gamePoints, totalPoints,
            "H", pointsPerSetHome, playerPointsHome, teamPointsHome, gamePointsHome,
            "A", pointsPerSetAway, playerPointsAway, teamPointsAway, gamePointsAway);
  }

}
