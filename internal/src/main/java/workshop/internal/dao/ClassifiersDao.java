package workshop.internal.dao;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import workshop.internal.entities.Classifier;
import workshop.internal.entities.Task;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Optional;

@Repository
public class ClassifiersDao extends WorkshopEntitiesDaoAbstract<Classifier, Long> {
	
	public ClassifiersDao() {
		setEntityClass(Classifier.class);
		setKeyClass(Long.class);
	}
	
	/**
	 * @param pageSize Amount of Employees at once
	 * @param pageNum  Zero-based pagination.
	 * @param orderBy  Non-null and non-empty property name the Employees to be ordered by
	 * @param order    Ascending or Descending
	 * @param taskId   Id of the Position
	 * @return Optional.of(List Classifiers) or Optional.empty() if nothing found.
	 * @throws PersistenceException If some DateBase problems occurred (Locks, timeouts etc).
	 *                              IllegalStateException  - if called for a Java Persistence query language UPDATE or DELETE statement
	 *                              QueryTimeoutException - if the query execution exceeds the query timeout value set and only the statement is rolled back
	 *                              TransactionRequiredException - if a lock mode other than NONE has been set and there is no transaction or the persistence context has not been joined to the transaction
	 *                              PessimisticLockException - if pessimistic locking fails and the transaction is rolled back
	 *                              LockTimeoutException - if pessimistic locking fails and only the statement is rolled back
	 *                              PersistenceException - if the query execution exceeds the query timeout value set and the transaction is rolled back
	 */
	public Optional<List<Classifier>> findClassifiersByTask(Integer pageSize,
		Integer pageNum,
		String orderBy,
		Sort.Direction order,
		Long taskId)
		throws PersistenceException {
		
		verifyPageableValues(pageSize, pageNum, orderBy, order);
		pageSize = pageSize == 0 ? getPAGE_SIZE_DEFAULT() : pageSize;
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Classifier> cq = cb.createQuery(Classifier.class);
		
		Root<Classifier> classifierRoot = cq.from(Classifier.class);
		
		Join<Classifier, Task> leftJoinClassifierTasks = classifierRoot.join("tasks", JoinType.INNER);
		Predicate taskIdEqual = cb.equal(leftJoinClassifierTasks.get("identifier"), taskId);
		leftJoinClassifierTasks.on(taskIdEqual);
		
		if (order.isAscending()) {
			cq.orderBy(cb.asc(classifierRoot.get(orderBy)));
		} else {
			cq.orderBy(cb.desc(classifierRoot.get(orderBy)));
		}
		TypedQuery<Classifier> typedQuery = entityManager.createQuery(cq);
		typedQuery.setMaxResults(pageSize);
		typedQuery.setFirstResult(pageSize * pageNum);
		
		List<Classifier> classifiersByTask = typedQuery.getResultList();
		
		if (classifiersByTask != null && !classifiersByTask.isEmpty()) {
			return Optional.of(classifiersByTask);
		} else {
			return Optional.empty();
		}
	}
	
}
