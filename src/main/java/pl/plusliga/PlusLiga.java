package pl.plusliga;

import java.text.SimpleDateFormat;
import java.util.Arrays;
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
public class PlusLiga {

    public static void main(String[] args) {
    	SpringApplication.run(PlusLiga.class);
    }
    
    @Bean
    public CommandLineRunner run(TeamRepository teams, PlayerRepository players, GameRepository games, PlayerGameRepository playerGames) {
    	return (args) -> {
    		League league = League.ORLENLIGA;
        	Date startDate = new SimpleDateFormat("ddMMyyyy").parse("01102018");

        	/*
        	playerGames.delete(playerGames.findByKeyGameId(27602));
        	games.delete(games.findOne(27602));
        	playerGames.delete(playerGames.findByKeyGameId(27603));
        	games.delete(games.findOne(27603));
        	playerGames.delete(playerGames.findByKeyGameId(27671));
        	games.delete(games.findOne(27671));
        	playerGames.delete(playerGames.findByKeyGameId(27672));
        	games.delete(games.findOne(27672));
        	*/
        	//playerGames.deleteAll();
        	//players.deleteAll();
        	/*
        	Team azsCzestochowa = new Team();
        	azsCzestochowa.setId(1409);
        	azsCzestochowa.setName("AZS Częstochowa");
        	azsCzestochowa.setLeague(League.PLUSLIGA);
        	teams.save(azsCzestochowa);
        	Team atomTreflSopot = new Team();
        	atomTreflSopot.setId(86949);
        	atomTreflSopot.setName("Atom Trefl Sopot");
        	atomTreflSopot.setLeague(League.ORLENLIGA);
        	teams.save(atomTreflSopot);
            Team energaMksKalisz = new Team();
            energaMksKalisz.setId(95960);
            energaMksKalisz.setName("Energa MKS Kalisz");
            energaMksKalisz.setLeague(League.ORLENLIGA);
            teams.save(energaMksKalisz);
        	*/
        	
        	List<Team> teamList = teams.save(new PlpsTeamParser(league).getEntities(league.getTeamsUrl()));
        	List<Player> playerList = players.save(new PlpsPlayerParser().getEntities(league.getPlayersUrl()));
        	games.save(new PlpsGameParser().getEntities(league.getGamesUrl()));
        	games.save(new PlpsCupGameParser(teamList).getEntities(league.getCupGamesUrl()));
            games.save(new PlpsSuperCupGameParser(teamList).getEntities(league.getSuperCupGameUrl()));
            List<Game> allGames = games.findByDateGreaterThanOrderByDate(startDate);
            allGames.stream()
            	//.peek(System.out::println)
            	.filter(game -> game.isFrom(league))
            	.filter(game -> playerGames.findByKeyGameId(game.getId()).isEmpty())
            	.peek(System.out::println)
            	.map(game -> new DataprojectPlayerGameParser(playerList, game.getId()).getEntities(game.getStatsUrl()))
            	.forEach(playerGames::save);
            
            //List<Integer> teamIds = Arrays.asList(87055, 92335, 86947, 95294, 1401, 1405, 1406, 1407, 1410, 1411);
			players.findAll().stream()
				.filter(player -> player.getLeague() == league)
				//.filter(player -> player.getCurrentTeam().map(Team::getId).map(teamIds::contains).orElse(false))
				.collect(Collectors.groupingBy(Player::getPosition, () -> new EnumMap<>(Position.class), Collectors.toList()))
				.forEach((position, posPlayers) -> {
					System.out.println();
					System.out.println(position.toString().toUpperCase());
					posPlayers.stream()
						.map(player -> new PlayerStatistics(player, allGames))
						.sorted(Comparator.comparingDouble(PlayerStatistics::getPointsMean).reversed().thenComparing(PlayerStatistics::getPlayerName))
						.forEach(System.out::println);
				}
			);
        };
    }

}
