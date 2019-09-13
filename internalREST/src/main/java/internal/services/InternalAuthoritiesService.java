package internal.services;

import internal.dao.InternalAuthoritiesDao;
import internal.entities.InternalAuthority;
import internal.exceptions.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class InternalAuthoritiesService extends WorkshopEntitiesServiceAbstract<InternalAuthority> {
	
	@Autowired
	private InternalAuthoritiesDao internalAuthoritiesDao;
	
	public InternalAuthoritiesService(InternalAuthoritiesDao internalAuthoritiesDao) {
		super(internalAuthoritiesDao);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public Page<InternalAuthority> findAllInternalAuthoritiesByPosition(Pageable pageable, Long positionId)
		throws EntityNotFoundException {
		
		super.verifyIdForNullBelowZero(positionId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		Sort.Direction order = verifiedPageable.getSort().getOrderFor(orderBy).getDirection();
		
		Set<InternalAuthority> internalAuthoritiesByPosition =
			internalAuthoritiesDao.findAllInternalAuthoritiesByPosition(
				verifiedPageable.getPageSize(),
				verifiedPageable.getPageNumber(),
				orderBy,
				order,
				positionId)
				.orElseThrow(() -> new EntityNotFoundException(
					"No " + getEntityClassSimpleName() + "s were found in the Position.ID=" + positionId,
					HttpStatus.NOT_FOUND,
					getMessageSource().getMessage(
						"httpStatus.notFound(2)",
						new Object[]{getEntityClassSimpleName() + "s ", "Position.ID=" + positionId},
						LocaleContextHolder.getLocale())));
		
		long totalInternalAuthorities = getWorkshopEntitiesDaoAbstract().countAllEntities();
		Page<InternalAuthority> internalAuthoritiesPage = new PageImpl<>(
			(List<InternalAuthority>) internalAuthoritiesByPosition, verifiedPageable, totalInternalAuthorities);
		return internalAuthoritiesPage;
	}
}
