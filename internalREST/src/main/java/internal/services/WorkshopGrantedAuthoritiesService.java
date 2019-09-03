package internal.services;

import internal.dao.WorkshopEntitiesDaoAbstract;
import internal.dao.WorkshopGrantedAuthoritiesDao;
import internal.entities.Task;
import internal.entities.WorkshopGrantedAuthority;
import internal.exceptions.EntityNotFoundException;
import internal.exceptions.IllegalArgumentsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class WorkshopGrantedAuthoritiesService extends WorkshopEntitiesServiceAbstract<WorkshopGrantedAuthority> {
	
	@Autowired
	private WorkshopGrantedAuthoritiesDao workshopGrantedAuthoritiesDao;
	
	public WorkshopGrantedAuthoritiesService(WorkshopGrantedAuthoritiesDao workshopGrantedAuthoritiesDao) {
		super(workshopGrantedAuthoritiesDao);
	}
	
	/**
	 * @param userId Self-description.
	 * @return 'Page<Task>' with included List of Tasks and pageable info for constructing pages.
	 * @throws IllegalArgumentsException If the given 'orderID' is null, zero or below.
	 * @throws EntityNotFoundException   If no Order or Tasks from if were found.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, readOnly = true)
	public Page<WorkshopGrantedAuthority> findAllGrantedAuthoritiesByUser(Pageable pageable, Long userId)
		throws IllegalArgumentsException, EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(userId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		Sort.Direction order = verifiedPageable.getSort().getOrderFor(orderBy).getDirection();
		
		Optional<List<WorkshopGrantedAuthority>> allTasksModifiedByEmployee =
			workshopGrantedAuthoritiesDao.findAllGrantedAuthoritiesByUser(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			orderBy,
			order,
			userId);
		
		Page<WorkshopGrantedAuthority> verifiedEntitiesPageFromDao =
			super.getVerifiedEntitiesPage(verifiedPageable, allTasksModifiedByEmployee);
		
		return verifiedEntitiesPageFromDao;
	}
	
}
