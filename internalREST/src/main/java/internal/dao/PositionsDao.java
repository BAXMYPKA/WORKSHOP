package internal.dao;

import internal.entities.Department;
import internal.entities.Position;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
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
	 * If 'order' OR 'orderBy' is null the default ordering will be applied as descending by 'created' property.
	 *
	 * @param pageNum      Zero based number of page
	 * @param departmentId Self-description parameter
	 * @param order        @Nullable Sort.Direction ENUM order. If null, default ordering will be applied.
	 * @param orderBy      @Nullable Name of property to be ordered by. If null, default ordering will be applied.
	 * @return 'Optional.of(Collection(Position)) of the particular Department if present.'
	 * Optional.empty() will be returned if nothing found.
	 * Also Optional.empty() will be returned in case of ClassCastException or any PersistenceException type occurred.
	 * @throws IllegalArgumentException 1) If 'departmentId' is null or < 0.
	 *                                  2) If 'pageSize' or 'pageNum' is incorrect.
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
		pageSize = pageSize == 0 ? getPAGE_SIZE_DEFAULT() : pageSize;
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Position> cq = cb.createQuery(Position.class);
		
		Root<Department> departmentRoot = cq.from(Department.class);
		Predicate departmentIdEqual = cb.equal(departmentRoot.get("identifier"), departmentId);
		
		cq.select(departmentRoot.get("positions")).where(departmentIdEqual);
		
		if (order != null && order.isAscending()){
			cq.orderBy(cb.asc(departmentRoot.get("positions").get("created")));
		} else {
			cq.orderBy(cb.desc(departmentRoot.get("positions").get("created")));
		}
		
		TypedQuery<Position> typedQuery = entityManager.createQuery(cq);
		
/*
		TypedQuery<Collection> typedQuery;
		if (order == null) { //Default ordering
			super.verifyPageableValues(pageSize, pageNum); //Can throw IllegalArgumentException if not correct
			typedQuery = entityManager.createQuery(
				"SELECT dep.positions FROM Department dep WHERE dep.identifier = :departmentId",	Collection.class);
		} else { //Custom ordering
			super.verifyPageableValues(pageSize, pageNum, orderBy, order);
			typedQuery = entityManager.createQuery(
				"SELECT dep.positions AS pos FROM Department dep WHERE dep.identifier = :departmentId ORDER BY pos." + orderBy + " " + order.name(),
				Collection.class);
		}
		typedQuery.setParameter("departmentId", departmentId);
*/
		//Set pagination
		typedQuery.setFirstResult(pageNum * pageSize);
		typedQuery.setMaxResults(pageSize);
		try {
			List<Position> departmentPositions = (List) typedQuery.getResultList();
			if (departmentPositions != null && !departmentPositions.isEmpty()) {
				sortEntitiesResultList(departmentPositions, orderBy, order);
				return Optional.of(departmentPositions);
			} else {
				return Optional.empty();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Optional.empty();
		}
	}
}