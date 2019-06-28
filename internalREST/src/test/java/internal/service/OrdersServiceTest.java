package internal.service;

import internal.dao.DaoAbstract;
import internal.dao.OrdersDao;
import internal.entities.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class OrdersServiceTest {
	
	@Mock
	OrdersDao ordersDao;
	
	@Mock
	DaoAbstract daoAbstract;
	
	@InjectMocks
	OrdersService ordersService;
	
	@BeforeEach
	public void init() {
	}
	
	@ParameterizedTest
	@ValueSource(ints = {-150, -100 - 2, -1, 0, 1, 2, 49, 50, 51, 99, 100, 101, 1200})
	public void regardless_Of_Input_Size_And_Page_All_Of_Them_Should_Be_Set_Between_Their_Min_Max(int sizeAndPage) {
		
		//GIVEN
		
		ArgumentCaptor<Integer> sizeCaptured = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> pageCaptured = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<String> sortByCaptured = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> ascDescCaptured = ArgumentCaptor.forClass(String.class);
		
		//WHEN
		
		Mockito.lenient().when(daoAbstract.findAll(sizeAndPage, sizeAndPage, "", ""))
			.thenReturn(Optional.empty());
		
		Mockito.lenient().when(daoAbstract.findAll(sizeAndPage, sizeAndPage, "", ""))
			.thenReturn(Optional.empty());
		
		ordersService.findAllOrders(sizeAndPage, sizeAndPage, "", "");
		
		//THEN
		
		Mockito.verify(ordersDao, Mockito.atLeastOnce()).findAll(
			sizeCaptured.capture(), pageCaptured.capture(), sortByCaptured.capture(), ascDescCaptured.capture());
		
		System.out.println("Size=" + sizeCaptured.getValue() + " Page=" + pageCaptured.getValue());
		
		assertTrue(sizeCaptured.getValue() > 0 && sizeCaptured.getValue() <= 50);
		assertTrue(pageCaptured.getValue() >= 1 && pageCaptured.getValue() <= 100);
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"other", "AsC", "null", "dEsc"})
	@DisplayName("SortBy can only by empty or any value, AscDesc can only by 'asc' or 'desc' in the lowerCase")
	public void sortBy_and_AscDesc_Values_Should_Be_Corrected(String variable) {
		
		//GIVEN
		
		ArgumentCaptor<Integer> sizeCaptured = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> pageCaptured = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<String> sortByCaptured = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> ascDescCaptured = ArgumentCaptor.forClass(String.class);
		
		//WHEN
		
		Mockito.lenient().when(ordersDao.findAll(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
			.thenReturn(Optional.empty());
		
		ordersService.findAllOrders(1, 1, variable, variable);
		
		//THEN
		
		Mockito.verify(ordersDao, Mockito.atLeastOnce()).findAll(
			sizeCaptured.capture(), pageCaptured.capture(), sortByCaptured.capture(), ascDescCaptured.capture());
		
		System.out.println("SortBy=" + sortByCaptured.getValue() + " || AscDesc=" + ascDescCaptured.getValue());
		
		//'SortBy' should be either empty or equals to input value
		assertTrue(sortByCaptured.getValue() != null &&
			(sortByCaptured.getValue().isEmpty() || sortByCaptured.getValue().equals(variable)));
		
		assertTrue(ascDescCaptured.getValue() != null &&
			("asc".equals(ascDescCaptured.getValue()) || "desc".equals(ascDescCaptured.getValue())));
	}
}