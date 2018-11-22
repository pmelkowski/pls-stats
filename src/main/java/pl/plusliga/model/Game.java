package pl.plusliga.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Game implements Comparable<Game> {

  @Id
  private Integer id;
  @Column
  private Date date;
  @Column
  private Integer homeTeamId;
  @ManyToOne
  @JoinColumn(name = "homeTeamId", nullable = true, insertable = false, updatable = false)
  private Team homeTeam;
  @Column
  private Integer homeScore;
  @Column
  private Integer visitorTeamId;
  @ManyToOne
  @JoinColumn(name = "visitorTeamId", nullable = true, insertable = false, updatable = false)
  private Team visitorTeam;
  @Column
  private Integer visitorScore;
  @Column
  private Boolean cup;
  @Column
  private String statsUrl;

  public boolean isComplete() {
    return homeScore != null && visitorScore != null && (homeScore == 3 || visitorScore == 3);
  }

  public int getSets() {
    return homeScore + visitorScore;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Integer getHomeTeamId() {
    return homeTeamId;
  }

  public void setHomeTeamId(Integer homeTeamId) {
    this.homeTeamId = homeTeamId;
  }

  public Team getHomeTeam() {
    return homeTeam;
  }

  public void setHomeTeam(Team homeTeam) {
    this.homeTeam = homeTeam;
  }

  public Integer getHomeScore() {
    return homeScore;
  }

  public void setHomeScore(Integer homeScore) {
    this.homeScore = homeScore;
  }

  public Integer getVisitorTeamId() {
    return visitorTeamId;
  }

  public void setVisitorTeamId(Integer visitorTeamId) {
    this.visitorTeamId = visitorTeamId;
  }

  public Team getVisitorTeam() {
    return visitorTeam;
  }

  public void setVisitorTeam(Team visitorTeam) {
    this.visitorTeam = visitorTeam;
  }

  public Integer getVisitorScore() {
    return visitorScore;
  }

  public void setVisitorScore(Integer visitorScore) {
    this.visitorScore = visitorScore;
  }

  public boolean isFrom(League league) {
    return (homeTeam != null && homeTeam.getLeague() == league)
        || (visitorTeam != null && visitorTeam.getLeague() == league);
  }

  public Integer getScore(Integer teamId) {
    if (teamId.equals(homeTeamId)) {
      return homeScore - visitorScore;
    } else if (teamId.equals(visitorTeamId)) {
      return visitorScore - homeScore;
    } else {
      throw new IllegalArgumentException(teamId.toString());
    }
  }

  public boolean isCup() {
    return Boolean.TRUE.equals(cup);
  }

  public void setCup(boolean cup) {
    this.cup = cup;
  }

  public String getStatsUrl() {
    return statsUrl;
  }

  public void setStatsUrl(String statsUrl) {
    this.statsUrl = statsUrl;
  }

  @Override
  public int compareTo(Game other) {
    int result = date.compareTo(other.date);
    if (result != 0) {
      return result;
    }
    return id.compareTo(other.id);
  }

  @Override
  public String toString() {
    return "Game [id=" + id + ", date=" + date + ", homeTeamId=" + homeTeamId + ", homeTeam="
        + homeTeam + ", homeScore=" + homeScore + ", visitorTeamId=" + visitorTeamId
        + ", visitorTeam=" + visitorTeam + ", visitorScore=" + visitorScore + ", cup=" + cup
        + ", statsUrl=" + statsUrl + ", isComplete=" + isComplete() + "]";
  }

}
