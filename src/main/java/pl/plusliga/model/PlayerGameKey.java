package pl.plusliga.model;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;

@Embeddable
public class PlayerGameKey implements Serializable {

  private static final long serialVersionUID = 1L;

  private Integer playerId;
  private Integer gameId;

  public Integer getPlayerId() {
    return playerId;
  }

  public void setPlayerId(Integer playerId) {
    this.playerId = playerId;
  }

  public Integer getGameId() {
    return gameId;
  }

  public void setGameId(Integer gameId) {
    this.gameId = gameId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(playerId, gameId);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof PlayerGameKey)) {
      return false;
    }

    return Objects.equals(playerId, ((PlayerGameKey) other).playerId)
        && Objects.equals(gameId, ((PlayerGameKey) other).gameId);
  }

  @Override
  public String toString() {
    return "PlayerGameKey [playerId=" + playerId + ", gameId=" + gameId + "]";
  }

}
