package internal.dao;

import internal.entities.Position;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Slf4j
@Getter
@Setter
@Repository
public class PositionsDao extends WorkshopEntitiesDaoAbstract<Position, Long> {
	
	public PositionsDao() {
		this.setEntityClass(Position.class);
		this.setKeyClass(Long.class);
	}
	
//	@Query("SELECT ID, NAME FROM INTERNAL.POSITIONS JOIN INTERNAL.DEPARTMENTS_TO_POSITIONS\n" +
//		"    ON POSITIONS.ID = DEPARTMENTS_TO_POSITIONS.POSITION_ID WHERE DEPARTMENT_ID = departmentId")
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true, isolation = Isolation.READ_COMMITTED)
	public Optional<List<Position>> findAllPositionsByDepartment(Long departmentId) {
		if (departmentId == null || departmentId <= 0) {
			throw new IllegalArgumentException("ID="+departmentId+" cannot be neither null nor zero or even below!");
		}
		
		TypedQuery<Position> typedQuery = entityManager.createQuery("SELECT p FROM Position p INNER JOIN Department " +
			"WHERE " +
			"DEPARTMENT_ID = :departmentId", Position.class);
		typedQuery.setParameter("departmentId", departmentId);
//
//		TypedQuery<Position> t = entityManager.createQuery("SELECT p FROM "+getEntityClass().getSimpleName()+" p",
//			Position.class);
		
		List<Position> resultList = t.getResultList();
		
		return Optional.of(resultList);
	}
}
