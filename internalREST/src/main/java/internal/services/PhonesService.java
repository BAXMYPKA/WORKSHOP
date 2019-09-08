package internal.services;

import internal.dao.EmployeesDao;
import internal.dao.PhonesDao;
import internal.dao.UsersDao;
import internal.entities.Classifier;
import internal.entities.Employee;
import internal.entities.Phone;
import internal.entities.User;
import internal.exceptions.EntityNotFoundException;
import internal.exceptions.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PhonesService extends WorkshopEntitiesServiceAbstract<Phone> {
	
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
		
		Optional<List<Phone>> allPhonesByUser = ((PhonesDao) getWorkshopEntitiesDaoAbstract()).findAllPhonesByUser(
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
		
		Optional<List<Phone>> allPhonesByEmployee = ((PhonesDao) getWorkshopEntitiesDaoAbstract()).findAllPhonesByEmployee(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			orderBy,
			order,
			employeeId);
		
		Page<Phone> verifiedClassifiersPageFromDao =
			super.getVerifiedEntitiesPage(verifiedPageable, allPhonesByEmployee);
		
		return verifiedClassifiersPageFromDao;
	}
	
	/**
	 * Sets a new or changed Phone to an existing Employee.
	 *
	 * @param employeeId Existing Employee.ID
	 * @param phone      Phone entity to be persisted or merged (if ID is set).
	 * @return The Employee with the new Phone set.
	 * @throws EntityNotFoundException      If no Employee with the given ID found.
	 * @throws InternalServerErrorException When update fails on underlying DAO layer.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public Phone addPhoneToEmployee(Long employeeId, Phone phone)
		throws EntityNotFoundException, InternalServerErrorException {
		
		super.verifyIdForNullZeroBelowZero(employeeId);
		
		if (!getWorkshopEntitiesDaoAbstract().isExist(employeeId)) {
			throw getEntityNotFoundException(getEntityClassSimpleName() + ".ID=" + employeeId);
		}
		Phone phonePersisted = persistOrMergeEntity(phone);
		return phonePersisted;
	}
	
	/**
	 * Sets an existing Phone to an existing Employee.
	 *
	 * @param employeeId Existing Employee.ID
	 * @param phoneId    Existing Phone.ID
	 * @return An Employee with the new Phone set
	 * @throws EntityNotFoundException If 'employeeId' or 'phoneId' is wrong.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public Employee addPhoneToEmployee(Long employeeId, Long phoneId) throws EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(employeeId, phoneId);
		Optional<Employee> employee = ((PhonesDao) getWorkshopEntitiesDaoAbstract()).addPhoneToEmployee(employeeId, phoneId);
		return employee.orElseThrow(() -> getEntityNotFoundException(getEntityClassSimpleName()));
	}
	
	/**
	 * Checks if the given Employee and Phone exist, and if the Phone belongs to that Employee.
	 * If yes, deletes Phone from the DataBase.
	 *
	 * @param employeeId To delete Phone from.
	 * @param phoneId    Phone to be deleted.
	 * @throws InternalServerErrorException If an error occurred on the DAO layer. It exposed an inner DB inconsistency.
	 * @throws EntityNotFoundException      If a given Employee of Phone doesn't exist.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public void deletePhoneFromEmployee(Long employeeId, Long phoneId)
		throws InternalServerErrorException, EntityNotFoundException {
		Phone phone = getWorkshopEntitiesDaoAbstract().findById(phoneId).orElseThrow(() ->
			getEntityNotFoundException(getEntityClassSimpleName() + ".ID=" + phoneId));
		if (phone.getEmployee().getIdentifier().equals(employeeId)) {
			try {
				phone.getEmployee().getPhones().remove(phone);
				getWorkshopEntitiesDaoAbstract().removeEntity(phone);
			} catch (Exception e) {
				throw new InternalServerErrorException(
					e.getMessage(), "httpStatus.internalServerError", HttpStatus.INTERNAL_SERVER_ERROR, e);
			}
		} else {
			throw getEntityNotFoundException("Employee.ID=" + employeeId);
		}
	}
}
