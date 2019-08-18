package internal.dao;

import internal.entities.Position;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.Collection;
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
	
	/**
	 * @param pageNum Zero based number of page
	 * @param departmentId Self-description parameter
	 * @return 'Optional.of(Collection(Position)) of the particular Department if present.'
	 * Optional.empty() will be returned if nothing found.
	 * Also Optional.empty() will be returned in case of ClassCastException or any PersistenceException type occurred.
	 */
//	@Query("SELECT ID, NAME FROM INTERNAL.POSITIONS JOIN INTERNAL.DEPARTMENTS_TO_POSITIONS\n" +
//		"    ON POSITIONS.ID = DEPARTMENTS_TO_POSITIONS.POSITION_ID WHERE DEPARTMENT_ID = departmentId")
	public Optional<List<Position>> findAllPositionsByDepartment(
		Integer pageSize,
		Integer pageNum,
		@Nullable String orderBy,
		@Nullable Sort.Direction order,
		Long departmentId) {
		if (departmentId == null || departmentId <= 0) {
			throw new IllegalArgumentException("ID=" + departmentId + " cannot be neither null nor zero or even below!");
		}
		super.verifyPageableParameters(pageSize, pageNum, orderBy, order);
		
		TypedQuery<Collection> typedQuery = entityManager.createQuery(
			"SELECT d.positions FROM Department d WHERE d.identifier = :departmentId", Collection.class);
		typedQuery.setParameter("departmentId", departmentId);
		//Set pagination
		typedQuery.setFirstResult(pageNum * pageSize);
		typedQuery.setMaxResults(pageSize);
		
		try {
			List<Position> departmentPositions = (List) typedQuery.getResultList();
			return departmentPositions != null && !departmentPositions.isEmpty() ? Optional.of(departmentPositions) :
				Optional.empty();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Optional.empty();
		}
	}
}