package workshop.internal.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import workshop.internal.entities.AuthorityPermission;
import workshop.internal.entities.WorkshopEntityType;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class WorkshopEntityTypesDao extends WorkshopEntitiesDaoAbstract<WorkshopEntityType, Long> {
	
	public WorkshopEntityTypesDao() {
		setEntityClass(WorkshopEntityType.class);
		setKeyClass(Long.class);
		
	}
	
	public Optional<List<WorkshopEntityType>> findEntityTypesByAuthorityPermission(
		Integer pageSize,
		Integer pageNum,
		String orderBy,
		Sort.Direction order,
		Long authorityPermissionId) throws PersistenceException {
		
		verifyPageableValues(pageSize, pageNum, orderBy, order);
		pageSize = pageSize == 0 ? getPAGE_SIZE_DEFAULT() : pageSize;
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<WorkshopEntityType> cq = cb.createQuery(WorkshopEntityType.class);
		
		Root<WorkshopEntityType> entityTypeRoot = cq.from(WorkshopEntityType.class);
		
		Join<WorkshopEntityType, AuthorityPermission> innerJoinEntityTypePermission =
			entityTypeRoot.join("authorityPermissions", JoinType.INNER);
		Predicate permissionIdEquals = cb.equal(innerJoinEntityTypePermission.get("identifier"), authorityPermissionId);
		innerJoinEntityTypePermission.on(permissionIdEquals);
		
		if (order.isAscending()) {
			cq.orderBy(cb.asc(entityTypeRoot.get(orderBy)));
		} else {
			cq.orderBy(cb.desc(entityTypeRoot.get(orderBy)));
		}
		TypedQuery<WorkshopEntityType> typedQuery = entityManager.createQuery(cq);
		typedQuery.setMaxResults(pageSize);
		typedQuery.setFirstResult(pageSize * pageNum);
		
		List<WorkshopEntityType> entityTypesByPermission = typedQuery.getResultList();
		
		if (entityTypesByPermission != null && !entityTypesByPermission.isEmpty()) {
			return Optional.of(entityTypesByPermission);
		} else {
			return Optional.empty();
		}
	}
}
