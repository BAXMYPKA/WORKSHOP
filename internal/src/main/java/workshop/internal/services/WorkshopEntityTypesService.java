package workshop.internal.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import workshop.internal.dao.WorkshopEntityTypesDao;
import workshop.internal.entities.WorkshopEntityType;

import java.util.List;

@Slf4j
@Service
public class WorkshopEntityTypesService extends WorkshopEntitiesServiceAbstract<WorkshopEntityType> {
	
	@Autowired
	private WorkshopEntityTypesDao workshopEntityTypesDao;
	
	public WorkshopEntityTypesService(WorkshopEntityTypesDao workshopEntityTypesDao) {
		super(workshopEntityTypesDao);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, readOnly = true)
	public Page<WorkshopEntityType> findEntityTypesByAuthorityPermission(Pageable pageable, Long authorityPermissionId) {
		pageable = super.getVerifiedAndCorrectedPageable(pageable);
		String orderBy = pageable.getSort().iterator().next().getProperty();
		Sort.Direction order = pageable.getSort().getOrderFor(orderBy).getDirection();
		List<WorkshopEntityType> entityTypeList =
			workshopEntityTypesDao.findEntityTypesByAuthorityPermission(
				pageable.getPageSize(),
				pageable.getPageNumber(),
				orderBy,
				order, authorityPermissionId)
				.orElseThrow(() -> getEntityNotFoundException(getEntityClassSimpleName()));
		long workshopEntityTypesTotal = workshopEntityTypesDao.countAllEntities();
		
		return new PageImpl<>(entityTypeList, pageable, workshopEntityTypesTotal);
	}
}
