package pl.plusliga.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerGameRepository extends JpaRepository<PlayerGame, PlayerGameKey> {
	
	public List<PlayerGame> findByKeyPlayerId(Integer playerId);
	public List<PlayerGame> findByKeyGameId(Integer gameId);
	
}
