package pl.plusliga.model;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity
public class Player {

	@Id
	private Integer id;
	@Column
	private String name;
	@Column
	private Date birthDate;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "key.playerId") @OrderBy("key.dateFrom desc")
	private List<PlayerTeam> teams;
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "key.playerId") @MapKey(name = "key.gameId")
	private Map<Integer, PlayerGame> games;	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Date getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	
	public List<PlayerTeam> getTeams() {
		return teams;
	}
	public void setTeams(List<PlayerTeam> teams) {
		this.teams = teams;
	}
	
	public Map<Integer, PlayerGame> getGames() {
		return games;
	}
	public void setGames(Map<Integer, PlayerGame> games) {
		this.games = games;
	}
	
	public Optional<PlayerTeam> getCurrentPlayerTeam() {
		return Optional.ofNullable(teams).map(List::stream).flatMap(Stream::findFirst);
	}
	public Optional<Team> getCurrentTeam() {
		return getCurrentPlayerTeam().map(PlayerTeam::getTeam);
	}
	public Team getTeam() {
		return getCurrentTeam().orElse(null);
	}
	public League getLeague() {
		return getCurrentTeam().map(Team::getLeague).orElse(null);
	}
	public Position getPosition() {
		return getCurrentPlayerTeam().map(PlayerTeam::getPosition).orElse(null);
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Player)) {
			return false;
		}
			
		return Objects.equals(id, ((Player) other).getId());
	}
	
}
