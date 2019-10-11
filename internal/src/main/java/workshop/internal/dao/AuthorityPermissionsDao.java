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
public class AuthorityPermissionsDao extends WorkshopEntitiesDaoAbstract<AuthorityPermission, Long> {
	public AuthorityPermissionsDao() {
		setEntityClass(AuthorityPermission.class);
		setKeyClass(Long.class);
	}
	
	/**
	 * @return {@literal Optional.of(List<AuthorityPermission>) or Optional.empty() if nothing was found.}
	 * @throws PersistenceException In case of DataBase problems.
	 */
	public Optional<List<AuthorityPermission>> findAuthorityPermissionsByWorkshopEntityType(
		Integer pageSize,
		Integer pageNum,
		String orderBy,
		Sort.Direction order,
		Long workshopEntityTypeId) throws PersistenceException {
		
		super.verifyPageableValues(pageSize, pageNum, orderBy, order);
		pageSize = pageSize == 0 ? getPAGE_SIZE_DEFAULT() : pageSize;
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<AuthorityPermission> cq = cb.createQuery(AuthorityPermission.class);
		
		Root<AuthorityPermission> permissionRoot = cq.from(AuthorityPermission.class);
		
		Join<AuthorityPermission, WorkshopEntityType> joinPermissionOnEntityType =
			permissionRoot.join("workshopEntityTypes", JoinType.INNER);
		Predicate workshopEntityTypeIdEquals = cb.equal(joinPermissionOnEntityType.get("identifier"), workshopEntityTypeId);
		joinPermissionOnEntityType.on(workshopEntityTypeIdEquals);
		
		if (order.isAscending()) {
			cq.orderBy(cb.asc(permissionRoot.get(orderBy)));
		} else {
			cq.orderBy(cb.desc(permissionRoot.get(orderBy)));
		}
		TypedQuery<AuthorityPermission> typedQuery = getEntityManager().createQuery(cq);
		typedQuery.setMaxResults(pageSize);
		typedQuery.setFirstResult(pageSize * pageNum);
		
		List<AuthorityPermission> permissionsByEntityType = typedQuery.getResultList();
		
		if (permissionsByEntityType != null && !permissionsByEntityType.isEmpty()) {
			return Optional.of(permissionsByEntityType);
		} else {
			return Optional.empty();
		}
	}
}
