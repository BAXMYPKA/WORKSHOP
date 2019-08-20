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
		order = order == null ? Sort.Direction.DESC : order;
		orderBy = orderBy == null || orderBy.isEmpty() ? getDEFAULT_ORDER_BY() : orderBy;
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Position> cq = cb.createQuery(Position.class);
		
		Root<Position> positionRoot = cq.from(Position.class);
		Join<Position, Department> departmentJoin = positionRoot.join("department");
		Predicate departmentIdEqual = cb.equal(departmentJoin.get("identifier"), departmentId);
		departmentJoin.on(departmentIdEqual);
		cq.select(positionRoot);
		
		if (order.isAscending()) {
			cq.orderBy(cb.asc(positionRoot.get(orderBy)));
		} else {
			cq.orderBy(cb.desc(positionRoot.get(orderBy)));
		}
		TypedQuery<Position> typedQuery = entityManager.createQuery(cq);
		//Set pagination
		typedQuery.setFirstResult(pageNum * pageSize);
		typedQuery.setMaxResults(pageSize);
		try {
			List<Position> departmentPositions = typedQuery.getResultList();
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