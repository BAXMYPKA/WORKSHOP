package workshop.internal.dao;

import workshop.internal.entities.ExternalAuthority;
import workshop.internal.entities.User;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Optional;

@Repository
public class UsersDao extends WorkshopEntitiesDaoAbstract<User, Long> {
	
	public UsersDao() {
		setEntityClass(User.class);
		setKeyClass(Long.class);
	}
	
	public Optional<User> findByPhone(String phone) throws IllegalArgumentException {
		if (phone == null || phone.isEmpty()) {
			throw new IllegalArgumentException("User phone cannot be null or empty!");
		}
		Optional<User> userByPhone = findByProperty("phone", phone)
			.map(users -> users.get(0));
		
		//TODO: to test
		
		return userByPhone;
	}
	
	/**
	 * @return {@literal Optional.of(List<User>) who contains the given ExternalAuthority or Optional.empty() if no Users found}
	 * @throws PersistenceException IllegalStateException  - if called for a Java Persistence query language UPDATE or DELETE statement
	 *                              QueryTimeoutException - if the query execution exceeds the query timeout value set and only the statement is rolled back
	 *                              TransactionRequiredException - if a lock mode other than NONE has been set and there is no transaction or the persistence context has not been joined to the transaction
	 *                              PessimisticLockException - if pessimistic locking fails and the transaction is rolled back
	 *                              LockTimeoutException - if pessimistic locking fails and only the statement is rolled back
	 *                              PersistenceException - if the query execution exceeds the query timeout value set and the transaction is rolled back
	 */
	public Optional<List<User>> findUsersByExternalAuthority(Integer pageSize,
															 Integer pageNum,
															 String orderBy,
															 Sort.Direction order,
															 Long externalAuthorityId) throws PersistenceException {
		super.verifyIdForNull(externalAuthorityId);
		super.verifyPageableValues(pageSize, pageNum, orderBy, order);
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> userRoot = cq.from(User.class);
		
		Join<User, ExternalAuthority> userAuthorityJoin = userRoot.join("externalAuthorities", JoinType.INNER);
		Predicate authorityIdEquals = cb.equal(userAuthorityJoin.get("identifier"), externalAuthorityId);
		userAuthorityJoin.on(authorityIdEquals);
		
		if (order.isAscending()) {
			cq.orderBy(cb.asc(userRoot.get(orderBy)));
		} else {
			cq.orderBy(cb.desc(userRoot.get(orderBy)));
		}
		
		TypedQuery<User> userTypedQuery = entityManager.createQuery(cq);
		userTypedQuery.setMaxResults(pageSize);
		userTypedQuery.setFirstResult(pageNum * pageSize);
		
		List<User> usersByAuthority = userTypedQuery.getResultList();
		
		if (usersByAuthority != null && !usersByAuthority.isEmpty()) {
			return Optional.of(usersByAuthority);
		} else {
			return Optional.empty();
		}
	}
}
