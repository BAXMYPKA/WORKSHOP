package workshop.internal.services;

import workshop.internal.dao.AuthorityPermissionsDao;
import workshop.internal.dao.InternalAuthoritiesDao;
import workshop.internal.dao.PositionsDao;
import workshop.internal.entities.AuthorityPermission;
import workshop.internal.entities.InternalAuthority;
import workshop.internal.entities.Position;
import workshop.internal.exceptions.EntityNotFoundException;
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

@Slf4j
@Service
public class InternalAuthoritiesService extends WorkshopEntitiesServiceAbstract<InternalAuthority> {
	
	@Autowired
	private InternalAuthoritiesDao internalAuthoritiesDao;
	@Autowired
	private PositionsDao positionsDao;
	@Autowired
	private AuthorityPermissionsDao authorityPermissionsDao;
	
	public InternalAuthoritiesService(InternalAuthoritiesDao internalAuthoritiesDao) {
		super(internalAuthoritiesDao);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public Page<InternalAuthority> findInternalAuthoritiesByPosition(Pageable pageable, Long positionId)
		throws EntityNotFoundException {
		
		super.verifyIdForNullBelowZero(positionId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		Sort.Direction order = verifiedPageable.getSort().getOrderFor(orderBy).getDirection();
		
		List<InternalAuthority> internalAuthoritiesByPosition =
			internalAuthoritiesDao.findInternalAuthoritiesByPosition(
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
			internalAuthoritiesByPosition, verifiedPageable, totalInternalAuthorities);
		return internalAuthoritiesPage;
	}
	
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public void addPositionToInternalAuthority(Long positionId, Long authorityId) {
		super.verifyIdForNullZeroBelowZero(positionId, authorityId);
		//For future optimization should be lowered down to DAO layer to be performed by SQL
		InternalAuthority authority = getWorkshopEntitiesDaoAbstract().findById(authorityId)
			.orElseThrow(() -> getEntityNotFoundException(getEntityClassSimpleName()));
		Position position = positionsDao.findById(positionId)
			.orElseThrow(() -> getEntityNotFoundException("Position"));
		authority.getPositions().add(position);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public void removePositionFromInternalAuthority(Long positionId, Long authorityId) {
		super.verifyIdForNullZeroBelowZero(positionId, authorityId);
		//For future optimization should be lowered down to DAO layer to be performed by SQL
		InternalAuthority authority = getWorkshopEntitiesDaoAbstract().findById(authorityId)
			.orElseThrow(() -> getEntityNotFoundException(getEntityClassSimpleName()));
		Position position = positionsDao.findById(positionId)
			.orElseThrow(() -> getEntityNotFoundException("Position"));
		authority.getPositions().remove(position);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, readOnly = true)
	public InternalAuthority findInternalAuthorityByAuthorityPermission(Long authorityPermissionId)
		throws EntityNotFoundException {
		
		AuthorityPermission authorityPermission =
			authorityPermissionsDao.findById(authorityPermissionId).orElseThrow(()->
				getEntityNotFoundException("AuthorityPermission"));
		return authorityPermission.getInternalAuthority();
	}
}
