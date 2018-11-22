package pl.plusliga.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity
public class Team {

  @Id
  private Integer id;
  @Column
  private String name;
  @Column
  @Enumerated(EnumType.STRING)
  private League league;

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

  public League getLeague() {
    return league;
  }

  public void setLeague(League league) {
    this.league = league;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Team)) {
      return false;
    }

    return Objects.equals(name, ((Team) other).getName());
  }

  @Override
  public String toString() {
    return "Team [id=" + id + ", name=" + name + ", league=" + league + "]";
  }

}
