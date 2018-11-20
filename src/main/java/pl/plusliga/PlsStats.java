package pl.plusliga;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import pl.plusliga.model.Game;
import pl.plusliga.model.GameRepository;
import pl.plusliga.model.League;
import pl.plusliga.model.Player;
import pl.plusliga.model.PlayerGameRepository;
import pl.plusliga.model.PlayerRepository;
import pl.plusliga.model.Position;
import pl.plusliga.model.Team;
import pl.plusliga.model.TeamRepository;
import pl.plusliga.parser.DataprojectPlayerGameParser;
import pl.plusliga.parser.PlpsCupGameParser;
import pl.plusliga.parser.PlpsGameParser;
import pl.plusliga.parser.PlpsPlayerParser;
import pl.plusliga.parser.PlpsSuperCupGameParser;
import pl.plusliga.parser.PlpsTeamParser;

@SpringBootApplication
@ComponentScan
@EnableJpaRepositories(basePackages = "pl.plusliga.model")
public class PlsStats {

 	public static void main(String[] args) {
		SpringApplication.run(PlsStats.class, League.PLUSLIGA.toString());
	}

	@Bean
	public CommandLineRunner run(TeamRepository teams, PlayerRepository players, GameRepository games, PlayerGameRepository playerGames) {
		return (args) -> {
			League league = League.valueOf(args[0]);

			updateDatabase(league, teams, players, games);
			List<Game> recentGames = games.findByDateGreaterThanOrderByDate(Date.from(
				LocalDate.now().minusMonths(2).atStartOfDay(ZoneId.systemDefault()).toInstant()));
			updateGames(league, players, playerGames, recentGames);

			players.findAll().stream()
				.filter(player -> player.getLeague() == league)
				.collect(Collectors.groupingBy(Player::getPosition, () -> new EnumMap<>(Position.class), Collectors.toList()))
				.forEach((position, posPlayers) -> {
					System.out.println();
					System.out.println(position.toString().toUpperCase());
					posPlayers.stream()
						.map(player -> new PlayerStatistics(player, recentGames))
						.sorted(Comparator.comparingDouble(PlayerStatistics::getPointsMean).reversed().thenComparing(PlayerStatistics::getPlayerName))
						.forEach(System.out::println);
				});
		};
	}

	protected void purgeDatabase(PlayerRepository players, PlayerGameRepository playerGames) {
		playerGames.deleteAll();
		players.deleteAll();
	}

	protected void deleteGame(GameRepository games, PlayerGameRepository playerGames, Integer gameId) {
		playerGames.deleteAll(playerGames.findByKeyGameId(gameId));
		games.deleteById(gameId);
	}

	protected void updateDatabase(League league, TeamRepository teams, PlayerRepository players, GameRepository games) {
		List<Team> teamList = teams.saveAll(new PlpsTeamParser(league).getEntities(league.getTeamsUrl()));
		players.saveAll(new PlpsPlayerParser(teamList).getEntities(league.getPlayersUrl()));
		games.saveAll(new PlpsGameParser().getEntities(league.getGamesUrl()));
		games.saveAll(new PlpsCupGameParser(teamList).getEntities(league.getCupGamesUrl()));
		games.saveAll(new PlpsSuperCupGameParser(teamList).getEntities(league.getSuperCupGameUrl()));
	}

	protected void updateGames(League league, PlayerRepository players, PlayerGameRepository playerGames, List<Game> gameList) {
		List<Player> playerList = players.findAll();
		gameList.stream()
		.filter(game -> game.isFrom(league))
		.filter(game -> playerGames.findByKeyGameId(game.getId()).isEmpty())
			.peek(System.out::println)
			.map(game -> new DataprojectPlayerGameParser(playerList, game.getId()).getEntities(game.getStatsUrl()))
			.forEach(playerGames::saveAll);
	}

}
