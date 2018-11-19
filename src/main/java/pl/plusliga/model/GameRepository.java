package pl.plusliga.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {
	
	public List<Game> findByHomeTeamIdOrVisitorTeamId(Integer homeTeamId, Integer visitorTeamId);
	public List<Game> findAllByOrderByDate();
	public List<Game> findByDateGreaterThanOrderByDate(Date date);
	
}
