package internal.service;

import internal.dao.EntitiesDaoAbstract;
import internal.entities.WorkshopEntity;
import internal.exceptions.PersistenceFailed;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Slf4j
@Transactional(propagation = Propagation.REQUIRED)
@Repository
public abstract class EntitiesServiceAbstract<T extends WorkshopEntity> {
	
	/**
	 * Default size of results on one page
	 */
	@Value("${default.page.size}")
	private int DEFAULT_PAGE_SIZE;
	/**
	 * Maximum available page number
	 */
	@Value("${default.page.max_num}")
	private int MAX_PAGE_NUM;
	
	private EntitiesDaoAbstract<T, Long> entitiesDaoAbstract;
	private Class<T> entityClass;
	
	/**
	 * @param entitiesDaoAbstract A concrete implementation of the EntitiesDaoAbstract<T,K> for the concrete
	 *                            implementation of this EntitiesServiceAbstract<T>.
	 *                            To be injected to all the superclasses.
	 *                            For instance, 'public OrdersService(OrdersDao ordersDao)'
	 */
	public EntitiesServiceAbstract(EntitiesDaoAbstract<T, Long> entitiesDaoAbstract) {
		this.entitiesDaoAbstract = entitiesDaoAbstract;
		setEntityClass(entitiesDaoAbstract.getEntityClass());
		log.trace("{} initialized successfully", entityClass.getSimpleName());
	}
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Optional<T> findById(long id) {
		if (id <= 0) {
			throw new IllegalArgumentException("The ID to be found cannot be 0 or even lower!");
		}
		return entitiesDaoAbstract.findById(id);
	}
	
	/**
	 * AKA 'persistOrUpdate'. If an Entity doesn't presented in the DataBase it will be persisted.
	 * Otherwise it will update its properties in the DataBase.
	 *
	 * @param entity If an Entity.id = 0 it will be persisted. If an Entity.id > 0 it will be merged (updated into DB)
	 * @return Persisted (or merged) managed copy of the Entity.
	 * @throws IllegalArgumentException                   If an Entity == null
	 * @throws AuthenticationCredentialsNotFoundException If this method is trying to be performed without an appropriate
	 *                                                    Authentication within the Spring's SecurityContext.
	 */
	public Optional<T> persistOrMergeEntity(T entity)
		throws IllegalArgumentException, AuthenticationCredentialsNotFoundException {
		if (entity == null) {
			throw new IllegalArgumentException("The entity argument cannot by null!");
		}
		return entitiesDaoAbstract.persistEntity(entity);
	}
	
	public Optional<T> persistEntity(T entity) throws IllegalArgumentException, EntityExistsException {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
		}
		Optional<T> persistedEntity = entitiesDaoAbstract.persistEntity(entity);
		log.debug("An {} is persisted? = {}", entityClass.getSimpleName(), persistedEntity.isPresent());
		return persistedEntity;
	}
	
	//TODO: to make a mergeEntity() method
	
	public void deleteEntity(T entity) {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
		}
		entitiesDaoAbstract.removeEntity(entity);
		log.debug("{} successfully removed", entityClass.getSimpleName());
	}
	
	/**
	 * @param entities {@link EntitiesServiceAbstract#persistOrMergeEntities(Collection)}
	 * @return {@link EntitiesServiceAbstract#persistOrMergeEntities(Collection)}
	 */
	public Optional<Collection<T>> persistOrMergeEntities(Collection<T> entities) {
		if (entities == null || entities.size() == 0) {
			throw new IllegalArgumentException("Collection<Entity> cannot be null or have a zero size!");
		}
		return entitiesDaoAbstract.persistEntities(entities);
	}
	
	
	/**
	 * @param pageable Must contain Sort.by(Sort.Direction, orderBy) or Sort.of(Sort.Direction, "created") property!
	 * @param orderBy
	 * @return
	 * @throws IllegalArgumentException
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Page<T> findAllEntities(Pageable pageable, String orderBy) throws IllegalArgumentException {
		int pageSize = pageable.getPageSize() < 0 || pageable.getPageSize() > DEFAULT_PAGE_SIZE ? DEFAULT_PAGE_SIZE : pageable.getPageSize();
		int pageNum = pageable.getPageNumber() <= 0 || pageable.getPageNumber() > MAX_PAGE_NUM ? 1 :
			pageable.getPageNumber();
		try {
			Optional<List<T>> entities = entitiesDaoAbstract.findAll(
				pageSize,
				pageNum,
				orderBy == null ? "" : orderBy,
				pageable.getSort().getOrderFor(orderBy == null || orderBy.isEmpty() ? "created" : orderBy).getDirection());
			long total = entitiesDaoAbstract.countAllEntities();
			
			Page<T> page = new PageImpl<T>(entities.orElse(Collections.<T>emptyList()), pageable, total);
			log.debug("A Page with the collection of {}s is found? = {}", entityClass.getSimpleName(), page.isEmpty());
			return page;
		} catch (PersistenceException e) {
			log.debug("No {} found. A Page with a Collections.EMPTY_LIST will be returned.", entityClass.getSimpleName());
			return new PageImpl<T>(Collections.<T>emptyList());
		}
	}
	
	/**
	 * @param pageSize min = 1, max = this.DEFAULT_PAGE_SIZE In case of incorrect values the size will be set in between min and max
	 * @param pageNum  min = 1, max = 100. In case of incorrect values the page will be set in between min and max
	 * @param orderBy  If "default" or empty - a List will be ordered by CreationDate
	 * @param order    ENUM from Sort.Direction with "ASC" or "DESC" values
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Optional<List<T>> findAllEntities(int pageSize, int pageNum, String orderBy, Sort.Direction order)
		throws IllegalArgumentException {
		pageSize = pageSize <= 0 || pageSize > DEFAULT_PAGE_SIZE ? DEFAULT_PAGE_SIZE : pageSize;
		pageNum = pageNum <= 0 || pageNum > MAX_PAGE_NUM ? 1 : pageNum;
		try {
			Optional<List<T>> entities = entitiesDaoAbstract.findAll(
				pageSize,
				pageNum,
				orderBy == null ? "" : orderBy,
				order);
			log.debug("An empty={} collection of {}s will be returned", entities.isPresent(), entityClass.getSimpleName());
			return entities;
		} catch (PersistenceException e) {
			log.trace("No {} found. See the included PersistenceException stack trace.", entityClass.getSimpleName(), e);
			return Optional.of(Collections.<T>emptyList());
		}
	}
}
