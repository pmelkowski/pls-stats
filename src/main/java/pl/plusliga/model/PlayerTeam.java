package pl.plusliga.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class PlayerTeam implements Comparable<PlayerTeam> {

  @EmbeddedId
  private PlayerTeamKey key;
  @ManyToOne
  @JoinColumn(name = "teamId", insertable = false, updatable = false)
  private Team team;
  @Column
  private Date dateTo;
  @Column
  @Enumerated(EnumType.STRING)
  private Position position;

  public PlayerTeamKey getKey() {
    return key;
  }

  public void setKey(PlayerTeamKey key) {
    this.key = key;
  }

  public boolean isCurrent(Date date) {
    return date.after(key.getDateFrom()) && date.before(dateTo);
  }

  public Team getTeam() {
    return team;
  }

  public void setTeam(Team team) {
    this.team = team;
  }

  public Date getDateTo() {
    return dateTo;
  }

  public void setDateTo(Date dateTo) {
    this.dateTo = dateTo;
  }

  public Position getPosition() {
    return position;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  @Override
  public int compareTo(PlayerTeam other) {
    return key.getDateFrom().compareTo(other.key.getDateFrom());
  }

}
