package workshop.internal.dao;

import workshop.internal.entities.Department;
import workshop.internal.entities.InternalAuthority;
import workshop.internal.entities.Position;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
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
	public Optional<List<Position>> findPositionsByDepartment(
		Integer pageSize,
		Integer pageNum,
		String orderBy,
		Sort.Direction order,
		Long departmentId) {
		
		verifyIdForNull(departmentId);
		verifyPageableValues(pageSize, pageNum, orderBy, order);
		
		log.debug("Received pageable of: pageSize={}, pageNum={}, orderBy={}, order={}, for Department.ID={}",
			pageSize, pageNum, orderBy, order.name(), departmentId);
		
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
	
	/**
	 * @param internalAuthorityId InternalAuthority.ID from which all its Positions have to be derived.
	 * @return Optional.of{@literal (List<Position>} which contain such an InternalAuthority or Optional.empty().
	 * @throws IllegalArgumentException If some of the given parameters are incorrect or null.
	 */
	public Optional<List<Position>> findPositionsByInternalAuthority(Integer pageSize,
																	 Integer pageNum,
																	 String orderBy,
																	 Sort.Direction order,
																	 Long internalAuthorityId)
		throws IllegalArgumentException {
		
		super.verifyPageableValues(pageSize, pageNum, orderBy, order);
		super.verifyIdForNull(internalAuthorityId);
		
		log.debug("Received pageable of: pageSize={}, pageNum={}, orderBy={}, order={}, for InternalAuthority.ID={}",
			pageSize, pageNum, orderBy, order.name(), internalAuthorityId);
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Position> cq = cb.createQuery(Position.class);
		
		Root<Position> positionRoot = cq.from(Position.class);
		
		Join<Position, InternalAuthority> positionAuthorityJoin = positionRoot.join("internalAuthorities", JoinType.INNER);
		Predicate authorityIdEquals =
			cb.equal(positionAuthorityJoin.get("identifier"), internalAuthorityId);
		positionAuthorityJoin.on(authorityIdEquals);
		
		if (order.isAscending()) {
			cb.asc(positionRoot.get(orderBy));
		} else {
			cb.desc(positionRoot.get(orderBy));
		}
		TypedQuery<Position> positionsByAuthorityQuery = entityManager.createQuery(cq);
		positionsByAuthorityQuery.setMaxResults(pageSize);
		positionsByAuthorityQuery.setFirstResult(pageNum * pageSize);
		
		List<Position> positionsByAuthority = positionsByAuthorityQuery.getResultList();
		
		if (positionsByAuthority == null || positionsByAuthority.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(positionsByAuthority);
		}
	}
}