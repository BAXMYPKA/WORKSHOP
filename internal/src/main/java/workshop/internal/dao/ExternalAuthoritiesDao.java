package workshop.internal.dao;

import workshop.internal.entities.ExternalAuthority;
import workshop.internal.entities.User;
import workshop.internal.exceptions.InternalServerErrorException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Optional;

@Repository
public class ExternalAuthoritiesDao extends WorkshopEntitiesDaoAbstract<ExternalAuthority, Long> {
	
	public ExternalAuthoritiesDao() {
		super.setEntityClass(ExternalAuthority.class);
		super.setKeyClass(Long.class);
	}
	
	/**
	 * @param pageSize Amount of Tasks to be returned at once. Min = 1
	 * @param pageNum  Zero based index.
	 * @param orderBy  Property name to order by.
	 * @param order    Ascending or Descending {@link Sort.Direction}
	 * @return "Optional.of(List<WorkshopGrantedAuthrity>)" or Optional.empty() if nothing found.
	 * @throws IllegalArgumentException     1) If pageSize or pageNum are greater or less than their Min and Max values or < 0.
	 *                                      2) If 'orderBy' is null or empty 3) If 'order' is null
	 * @throws InternalServerErrorException IllegalStateException  - if called for a Java Persistence query language UPDATE or DELETE statement
	 *                                      QueryTimeoutException - if the query execution exceeds the query timeout value set and only the statement is rolled back
	 *                                      TransactionRequiredException - if a lock mode other than NONE has been set and there is no transaction or the persistence context has not been joined to the transaction
	 *                                      PessimisticLockException - if pessimistic locking fails and the transaction is rolled back
	 *                                      LockTimeoutException - if pessimistic locking fails and only the statement is rolled back
	 *                                      PersistenceException - if the query execution exceeds the query timeout value set and the transaction is rolled back
	 */
	public Optional<List<ExternalAuthority>> findAllGrantedAuthoritiesByUser(Integer pageSize,
																			 Integer pageNum,
																			 String orderBy,
																			 Sort.Direction order,
																			 Long userId)
		throws IllegalArgumentException, InternalServerErrorException {
		
		verifyPageableValues(pageSize, pageNum, orderBy, order);
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<ExternalAuthority> cqa = cb.createQuery(ExternalAuthority.class);
		Root<User> userRoot = cqa.from(User.class);
		Predicate userIdEqual = cb.equal(userRoot.get("identifier"), userId);
		cqa.where(userIdEqual);
		Join<User, ExternalAuthority> userAuthorityJoin = userRoot.join("grantedAuthorities");
		CriteriaQuery<ExternalAuthority> criteriaQuery = cqa.select(userAuthorityJoin);
		if (order.isAscending()) {
			criteriaQuery.orderBy(cb.asc(userRoot.get(orderBy)));
		} else {
			criteriaQuery.orderBy(cb.desc(userRoot.get(orderBy)));
		}
		TypedQuery<ExternalAuthority> authorityTypedQuery = getEntityManager().createQuery(criteriaQuery);
		authorityTypedQuery.setMaxResults(pageSize);
		authorityTypedQuery.setFirstResult(pageSize * pageNum);
		try {
			List<ExternalAuthority> grantedAuthorities = authorityTypedQuery.getResultList();
			if (grantedAuthorities != null && !grantedAuthorities.isEmpty()) {
				return Optional.of(grantedAuthorities);
			} else {
				return Optional.empty();
			}
		} catch (PersistenceException e) {
			throw new InternalServerErrorException(
				e.getMessage(), "httpStatus.internalServerError.common", HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
		//TODO: to test
	}
	
}
