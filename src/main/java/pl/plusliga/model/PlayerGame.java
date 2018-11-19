package pl.plusliga.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class PlayerGame implements Comparable<PlayerGame> {
	
	@EmbeddedId
	private PlayerGameKey key;
	@ManyToOne @JoinColumn(name = "playerId", insertable = false, updatable = false)
	private Player player;
	@ManyToOne @JoinColumn(name = "gameId", insertable = false, updatable = false)
	private Game game;
	@Column
	private boolean played;
	@Column(name = "as_primary")
	private boolean primary;
	@Column(name = "as_secondary")
	private boolean secondary;
	@Column
	private int sets;
	@Column
	private int points;
	@Column
	private int aces;
	@Column
	private int blocks;
	@Column
	private int recNumber;
	@Column
	private int recPct;
	@Column
	private boolean mvp;
	
	public PlayerGameKey getKey() {
		return key;
	}
	public void setKey(PlayerGameKey key) {
		this.key = key;
	}

	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}

	public Game getGame() {
		return game;
	}
	public void setGame(Game game) {
		this.game = game;
	}

	public boolean isPlayed() {
		return played;
	}
	public void setPlayed(boolean played) {
		this.played = played;
	}

	public boolean isPrimary() {
		return primary;
	}
	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public boolean isSecondary() {
		return secondary;
	}
	public void setSecondary(boolean secondary) {
		this.secondary = secondary;
	}

	public int getSets() {
		return sets;
	}
	public void setSets(int sets) {
		this.sets = sets;
	}

	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}

	public int getAces() {
		return aces;
	}
	public void setAces(int aces) {
		this.aces = aces;
	}

	public int getBlocks() {
		return blocks;
	}
	public void setBlocks(int blocks) {
		this.blocks = blocks;
	}

	public int getRecNumber() {
		return recNumber;
	}
	public void setRecNumber(int recNumber) {
		this.recNumber = recNumber;
	}

	public int getRecPct() {
		return recPct;
	}
	public void setRecPct(int recPct) {
		this.recPct = recPct;
	}

	public boolean isMvp() {
		return mvp;
	}
	public void setMvp(boolean mvp) {
		this.mvp = mvp;
	}

	@Override
	public int compareTo(PlayerGame other) {
		return game.compareTo(other.getGame());
	}
	
	@Override
	public String toString() {
		return "PlayerGame [key=" + key + ", player=" + player + ", game=" + game + ", played=" + played + ", primary="
				+ primary + ", secondary=" + secondary + ", sets=" + sets + ", points=" + points + ", aces=" + aces
				+ ", blocks=" + blocks + ", recNumber=" + recNumber + ", recPct=" + recPct + ", mvp=" + mvp + "]";
	}
		
}
