package internal.dao;

import internal.entities.Position;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Getter
@Setter
@Repository
public class PositionsDao extends DaoAbstract<Position, Long> {
	
	public PositionsDao() {
		this.setEntityClass(Position.class);
		this.setKeyClass(Long.class);
	}
}
