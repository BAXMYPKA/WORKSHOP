package workshop.internal.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import workshop.internal.dao.AuthorityPermissionsDao;
import workshop.internal.entities.AuthorityPermission;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AuthorityPermissionsService extends WorkshopEntitiesServiceAbstract<AuthorityPermission> {
	
	@Autowired
	private AuthorityPermissionsDao authorityPermissionsDao;
	
	public AuthorityPermissionsService(AuthorityPermissionsDao authorityPermissionsDao) {
		super(authorityPermissionsDao);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, readOnly = true)
	public Page<AuthorityPermission> findAuthorityPermissionsByWorkshopEntityType(
		Pageable pageable, Long workshopEntityTypeId) {
		
		super.verifyIdForNullZeroBelowZero(workshopEntityTypeId);
		pageable = super.getVerifiedAndCorrectedPageable(pageable);
		
		String orderBy = pageable.getSort().iterator().next().getProperty();
		Sort.Direction order = pageable.getSort().getOrderFor(orderBy).getDirection();
		
		Optional<List<AuthorityPermission>> authorityPermissions =
			authorityPermissionsDao.findAuthorityPermissionsByWorkshopEntityType(
				pageable.getPageSize(),
				pageable.getPageNumber(),
				orderBy,
				order,
				workshopEntityTypeId);
		return getVerifiedEntitiesPage(pageable, authorityPermissions);
	}
}
