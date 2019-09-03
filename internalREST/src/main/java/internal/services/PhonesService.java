package internal.services;

import internal.dao.EmployeesDao;
import internal.dao.PhonesDao;
import internal.dao.UsersDao;
import internal.entities.Classifier;
import internal.entities.Employee;
import internal.entities.Phone;
import internal.entities.User;
import internal.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class PhonesService extends WorkshopEntitiesServiceAbstract<Phone> {
	
	@Autowired
	private PhonesDao phonesDao;
	@Autowired
	private UsersDao usersDao;
	@Autowired
	private EmployeesDao employeesDao;
	
	public PhonesService(PhonesDao phonesDao) {
		super(phonesDao);
	}
	
	/**
	 * @param userId Self-description.
	 * @return Non-paged "Collection<Phone>" or throw EntityNotFoundException
	 * @throws EntityNotFoundException If no such "User" or "User.Set<Phone>" was found.
	 */
	public Page<Phone> findAllPhonesByUser(Pageable pageable, Long userId) throws EntityNotFoundException {
		super.verifyIdForNullZeroBelowZero(userId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		Sort.Direction order = verifiedPageable.getSort().getOrderFor(orderBy).getDirection();
		
		Optional<List<Phone>> allPhonesByUser = phonesDao.findAllPhonesByUser(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			orderBy,
			order,
			userId);
		
		Page<Phone> verifiedClassifiersPageFromDao =
			super.getVerifiedEntitiesPage(verifiedPageable, allPhonesByUser);
		
		return verifiedClassifiersPageFromDao;
	}
	
	/**
	 * @param employeeId Self-description.
	 * @return Non-paged "Collection<Phone>" or throw EntityNotFoundException
	 * @throws EntityNotFoundException If no such "Employee" or "Employee.Set<Phone>" was found.
	 */
	public Page<Phone> findAllPhonesByEmployee(Pageable pageable, Long employeeId) throws EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(employeeId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		Sort.Direction order = verifiedPageable.getSort().getOrderFor(orderBy).getDirection();
		
		Optional<List<Phone>> allPhonesByEmployee = phonesDao.findAllPhonesByEmployee(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			orderBy,
			order,
			employeeId);
		
		Page<Phone> verifiedClassifiersPageFromDao =
			super.getVerifiedEntitiesPage(verifiedPageable, allPhonesByEmployee);
		
		return verifiedClassifiersPageFromDao;
	}
}
