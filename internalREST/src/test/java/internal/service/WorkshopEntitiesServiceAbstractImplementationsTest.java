package internal.service;

import internal.dao.DepartmentsDao;
import internal.entities.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WorkshopEntitiesServiceAbstractImplementationsTest {
	
	@Mock
	DepartmentsDao departmentsDao;
//	@InjectMocks
	DepartmentsService departmentsService;
	
	
	@BeforeEach
	public void init() {
		Mockito.when(departmentsDao.getEntityClass()).thenReturn(Department.class);
		departmentsService = new DepartmentsService(departmentsDao);
	}
	
	@Test
	public void init_Test() {
		assertNotNull(departmentsService);
		assertNotNull(departmentsService.getWorkshopEntitiesDaoAbstract());
		assertEquals(Department.class, departmentsDao.getEntityClass());
	}
	
	@ParameterizedTest
	@ValueSource(ints = {1, 45, 149, 1499, 3000, 5100})
	public void pagination_With_Any_Values_Should_Always_Return_Page_Within_Limits(int pageSizeAndNum){
		//GIVEN limits and arguments
		int defaultPageSize = 50;
		int maxPageNum = 50;
		departmentsService.setDEFAULT_PAGE_SIZE(defaultPageSize);
		departmentsService.setMAX_PAGE_NUM(maxPageNum);
		String orderBy = "Property";
		Pageable pageable = PageRequest.of(pageSizeAndNum, pageSizeAndNum, Sort.Direction.DESC, orderBy);
		
		//Other preparations...
		Mockito.when(departmentsDao.countAllEntities()).thenReturn(100L);
		
		ArgumentCaptor<Integer> capturedPageSize = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> capturedPageNum = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<String> capturedOrderBy = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Sort.Direction> capturedDirection = ArgumentCaptor.forClass(Sort.Direction.class);
		
		Mockito.when(departmentsDao.findAllPaged(
			capturedPageSize.capture(), capturedPageNum.capture(), capturedOrderBy.capture(), capturedDirection.capture()))
			.thenReturn(Optional.empty());
		
		//WHEN
		Page<Department> emptyAllEntities = departmentsService.findAllEntities(pageable, orderBy);
		
		//THEN
		Mockito.verify(departmentsDao, Mockito.times(1))
			.findAllPaged(capturedPageSize.getValue(), capturedPageNum.getValue(), orderBy, Sort.Direction.DESC);
		
		assertTrue(capturedPageSize.getValue() <= defaultPageSize);
		assertTrue(capturedPageNum.getValue() <= maxPageNum);
	}
}