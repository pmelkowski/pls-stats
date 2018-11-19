package pl.plusliga.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Embeddable;

@Embeddable
public class PlayerTeamKey implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Integer playerId;
	private Integer teamId;
	private Date dateFrom;
	
	public Integer getPlayerId() {
		return playerId;
	}
	public void setPlayerId(Integer playerId) {
		this.playerId = playerId;
	}
		
	public Integer getTeamId() {
		return teamId;
	}
	public void setTeamId(Integer teamId) {
		this.teamId = teamId;
	}
	
	public Date getDateFrom() {
		return dateFrom;
	}
	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(playerId, teamId, dateFrom);
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PlayerTeamKey)) {
			return false;
		}
			
		return Objects.equals(playerId, ((PlayerTeamKey) other).playerId) &&
				Objects.equals(teamId, ((PlayerTeamKey) other).teamId) &&
				Objects.equals(dateFrom, ((PlayerTeamKey) other).dateFrom);
	}
	
}
