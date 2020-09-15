package pl.plusliga;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import pl.plusliga.model.Game;
import pl.plusliga.model.GameRepository;
import pl.plusliga.model.League;
import pl.plusliga.model.Player;
import pl.plusliga.model.PlayerGame;
import pl.plusliga.model.PlayerGameKey;
import pl.plusliga.model.PlayerGameRepository;
import pl.plusliga.model.PlayerRepository;
import pl.plusliga.model.Position;
import pl.plusliga.model.Team;
import pl.plusliga.model.TeamRepository;
import pl.plusliga.parser.JsoupParser;
import pl.plusliga.parser.ParserFactory;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "pl.plusliga.model")
public class PlsStats {

  @Inject
  TeamRepository teams;

  @Inject
  PlayerRepository players;

  @Inject
  GameRepository games;

  @Inject
  PlayerGameRepository playerGames;

  public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(PlsStats.class, League.PLUSLIGA.toString());
    SpringApplication.exit(context);
  }

  @Bean
  public CommandLineRunner run() {
    return (args) -> {
      League league = League.valueOf(args[0]);
      updateTeams(league);
      updateGames(league);
      Date start = Date.from(ZonedDateTime.now().minusMonths(2).toInstant());
      updatePlayerData(league, start);

      EnumMap<Position, List<PlayerStatistics>> stats = getStatistics(league, start);
      stats.forEach((position, posPlayers) -> {
        System.out.println();
        System.out.println(position.toString().toUpperCase());
        posPlayers.forEach(System.out::println);
      });
    };
  }

  @Transactional
  protected void purgeDatabase() {
    playerGames.deleteAll();
    players.deleteAll();
    games.deleteAll();
    teams.deleteAll();
  }

  @Transactional
  protected void deleteGamesFromDate(League league, Date start) {
    games.findByDateGreaterThanOrderByDate(start).stream()
        .filter(game -> game.isFrom(league))
        .peek(game -> System.out.println("deleting: " + game))
        .map(Game::getId)
        .forEach(this::deleteGame);
  }

  @Transactional
  protected void deleteGame(Integer gameId) {
    playerGames.deleteAll(playerGames.findByKeyGameId(gameId));
    games.deleteById(gameId);
  }

  @Transactional
  protected void updateTeams(League league) {
    teams.saveAll(
        ParserFactory.getParser(league, Team.class, league).getEntities(league.getTeamsUrl()));
  }

  @Transactional
  protected void updateGames(League league) {
    games.saveAll(ParserFactory.getParser(league, Game.class).getEntities(league.getGamesUrl()));
    //ParserFactory.getCupGameParser(league, teams.findAll())
    //    .ifPresent(parser -> games.saveAll(parser.getEntities(league.getCupGamesUrl())));
    //ParserFactory.getSuperCupGameParser(league, leagueTeams)
    //    .ifPresent(parser -> games.saveAll(parser.getEntities(league.getSuperCupGameUrl())));
  }

  @Transactional
  protected void updatePlayerData(League league, Date start) {
    List<PlayerGame> playerGameList = games.findByDateGreaterThanOrderByDate(start).stream()
        .filter(game -> game.isFrom(league))
        .filter(game -> playerGames.findByKeyGameId(game.getId()).isEmpty())
        .peek(System.out::println)
        .map(game -> ParserFactory.getParser(league, PlayerGame.class, game.getId())
            .getEntities(game.getStatsUrl()))
        .flatMap(List::stream)
        .collect(Collectors.toList());

    Set<Integer> playerIds = playerGameList.stream()
        .map(PlayerGame::getKey)
        .map(PlayerGameKey::getPlayerId)
        .collect(Collectors.toSet());
    updatePlayers(league, playerIds);

    playerGames.saveAll(playerGameList);
  }

  @Transactional
  protected void updatePlayers(League league, Collection<Integer> playerIds) {
    JsoupParser<Player> parser = ParserFactory.getParser(league, Player.class, teams.findAll());
    playerIds.stream()
        .map(league::getPlayerUrl)
        .map(parser::getEntity)
        .forEach(players::save);
    
  }

  protected EnumMap<Position, List<PlayerStatistics>> getStatistics(League league, Date start) {
    List<Game> gameList = games.findByDateGreaterThanOrderByDate(start);
    return players.findAll().stream()
        .filter(player -> player.getLeague() == league)
        .collect(Collectors.groupingBy(
            Player::getPosition,
            () -> new EnumMap<>(Position.class),
            Collectors.mapping(
                player -> new PlayerStatistics(player, gameList),
                Collectors.collectingAndThen(
                    Collectors.toList(), stats -> stats.stream()
                        .sorted(Comparator.comparingDouble(PlayerStatistics::getPointsMean).reversed()
                            .thenComparing(PlayerStatistics::getPlayerName))
                        .collect(Collectors.toList())))));
  }

}
