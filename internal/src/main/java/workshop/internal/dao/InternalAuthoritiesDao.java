package workshop.internal.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import workshop.internal.entities.InternalAuthority;
import workshop.internal.entities.Position;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class InternalAuthoritiesDao extends WorkshopEntitiesDaoAbstract<InternalAuthority, Long> {
	
	public InternalAuthoritiesDao() {
		super.setEntityClass(InternalAuthority.class);
		super.setKeyClass(Long.class);
	}
	
	/**
	 * @param positionId The Position witch has to contain all the desired InternalAuthorities.
	 * @return {@literal Optional.of(List<InternalAuthority>) or Optional.empty() if nothing found.}
	 * @throws PersistenceException IllegalStateException  - if called for a Java Persistence query language UPDATE or DELETE statement
	 *                              QueryTimeoutException - if the query execution exceeds the query timeout value set and only the statement is rolled back
	 *                              TransactionRequiredException - if a lock mode other than NONE has been set and there is no transaction or the persistence context has not been joined to the transaction
	 *                              PessimisticLockException - if pessimistic locking fails and the transaction is rolled back
	 *                              LockTimeoutException - if pessimistic locking fails and only the statement is rolled back
	 *                              PersistenceException - if the query execution exceeds the query timeout value set and the transaction is rolled back
	 */
	public Optional<List<InternalAuthority>> findInternalAuthoritiesByPosition(
		Integer pageSize,
		Integer pageNum,
		String orderBy,
		Sort.Direction order,
		Long positionId)
		throws PersistenceException {
		
		super.verifyPageableValues(pageSize, pageNum, orderBy, order);
		super.verifyIdForNull(positionId);
		log.debug("Received pageable of: pageSize={}, pageNum={}, orderBy={}, order={}, for Position.ID={}",
			pageSize, pageNum, orderBy, order.name(), positionId);
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<InternalAuthority> cq = cb.createQuery(InternalAuthority.class);
		Root<InternalAuthority> authorityRoot = cq.from(InternalAuthority.class);
		
		Join<InternalAuthority, Position> authorityPositionJoin = authorityRoot.join("positions", JoinType.INNER);
		Predicate positionIdEquals = cb.equal(authorityPositionJoin.get("identifier"), positionId);
		authorityPositionJoin.on(positionIdEquals);
		
		if (order.isAscending()) {
			cq.orderBy(cb.asc(authorityRoot.get("created")));
		} else {
			cq.orderBy(cb.desc(authorityRoot.get("created")));
		}
		TypedQuery<InternalAuthority> typedQuery = getEntityManager().createQuery(cq);
		typedQuery.setMaxResults(pageSize);
		typedQuery.setFirstResult(pageNum * pageSize);
		
		List<InternalAuthority> authoritiesByPosition = typedQuery.getResultList();
		if (authoritiesByPosition != null && !authoritiesByPosition.isEmpty()) {
			return Optional.of(authoritiesByPosition);
		} else {
			return Optional.empty();
		}
	}
}
