package pl.plusliga.parser;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.nodes.Element;

import pl.plusliga.model.League;
import pl.plusliga.model.Player;
import pl.plusliga.model.PlayerGame;
import pl.plusliga.model.PlayerGameKey;

public class DataprojectPlayerGameParser implements JsoupParser<PlayerGame> {
	protected static Pattern TEAM_ID_PATTERN = Pattern.compile(".*/teams/id/(\\d+).html");
	protected static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
	protected static Pattern MATCH_ID_PATTERN = Pattern.compile(".*mID=(\\d+).*");

	private final Map<String, Integer> playerIds;
	private final Integer gameId;
	
	public DataprojectPlayerGameParser(Collection<Player> players, Integer gameId) {
		playerIds = players.stream().collect(Collectors.toMap(Player::getName, Player::getId));
		this.gameId = gameId;
	}

	@Override
	public PlayerGame getEntity(Element element) {
        String playerName = element.getElementById("PlayerName").text().replace(" (L)", "");
        if (playerName.equals("Szumera")) {
        	playerName = "Szumera Wiktoria";
        }
        Integer playerId = playerIds.get(playerName);
        if (playerId == null) {
        	System.err.println(playerName);
        	return null;
        }
        
		PlayerGameKey key = new PlayerGameKey();
		key.setPlayerId(playerId);
		key.setGameId(gameId);
		PlayerGame playerGame = new PlayerGame();
		playerGame.setKey(key);

		int setsPlayed = 0;
		for (int set = 1; set <= 5; set++) {
			String position = element.getElementById("Set" + set).text();
			if (position.isEmpty()) continue;
			
			playerGame.setPlayed(true);
			setsPlayed++;
			if (!position.equals("*")) {
				if (set == 1) playerGame.setPrimary(true);
				else if (!playerGame.isPrimary()) playerGame.setSecondary(true);
			}
		}
		playerGame.setSets(setsPlayed);
		
		playerGame.setAces(Integer.parseInt(element.getElementById("ServeAce").text().replace('-', '0')));
		playerGame.setBlocks(Integer.parseInt(element.getElementById("BlockWin").text().replace('-', '0')));
		playerGame.setPoints(Integer.parseInt(element.getElementById("PointsTot").text().replace('-', '0')) - playerGame.getAces() - playerGame.getBlocks());
		playerGame.setRecNumber(Integer.parseInt(element.getElementById("RecTot").text().replace('-', '0')));
		Optional.ofNullable(element.getElementById("RecPos")).map(recPos -> Integer.parseInt(recPos.text().replace("%", ""))).ifPresent(playerGame::setRecPct);
		
		return playerGame;
	}
	
	@Override
	public List<PlayerGame> getEntities(String url) {
		return getEntities(url, "#Content_Main_ctl15_RP_MatchStats_RPL_MatchStats_0 tr.rgRow, #Content_Main_ctl15_RP_MatchStats_RPL_MatchStats_0 tr.rgAltRow");
	}
	
	public static void main(String args[]) {
		List<Player> players = new PlpsPlayerParser().getEntities(League.PLUSLIGA.getPlayersUrl());
		List<PlayerGame> games = new DataprojectPlayerGameParser(players, 27669).getEntities("http://pls-web.dataproject.com/MatchStatistics.aspx?mID=27669&ID=1052");
		games.forEach(System.out::println);
	}
	
}
