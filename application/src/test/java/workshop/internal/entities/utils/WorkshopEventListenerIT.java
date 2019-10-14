package workshop.internal.entities.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import workshop.internal.entities.Order;
import workshop.internal.entities.Task;
import workshop.internal.entities.User;
import workshop.internal.services.OrdersService;
import workshop.internal.services.TasksService;
import workshop.internal.services.UsersService;
import workshop.utils.WorkshopEventListener;

import java.time.ZonedDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
class WorkshopEventListenerIT {
	
	@MockBean
	WorkshopEventListener workshopEventListener;
	
	@Autowired
	OrdersService ordersService;
	
	@Autowired
	UsersService usersService;
	
	@Autowired
	TasksService tasksService;
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"ADMIN_FULL"})
	public void orderFinishedEvent_Should_Be_Generated_With_The_Order_That_Set_As_Finished() {
		//GIVEN
		User user = new User("user_order_finished@email.com");
		usersService.persistEntity(user);
		
		Order order = new Order();
		order.setCreatedFor(user);
		ordersService.persistEntity(order);
		
		//WHEN
		ArgumentCaptor<OrderFinishedEvent> finishedEventArgumentCaptor = ArgumentCaptor.forClass(OrderFinishedEvent.class);
		Mockito.doNothing().when(workshopEventListener).orderFinishedEventListener(finishedEventArgumentCaptor.capture());
		
		order.setFinished(ZonedDateTime.now());
		ordersService.mergeEntity(order);
		
		//THEN
		assertEquals(order.getIdentifier(), finishedEventArgumentCaptor.getValue().getOrder().getIdentifier());
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"ADMIN_FULL"})
	public void orderFinishedEvent_Should_Be_Generated_With_The_Order_Which_All_the_Tasks_Are_Set_As_Finished() {
		//GIVEN
		User user = new User("user_tasks_finished@email.com");
		usersService.persistEntity(user);
		
		Order order = new Order();
		order.setCreatedFor(user);
		ordersService.persistEntity(order);
		
		Task task1 = new Task("Task 1_to_be_finished", order);
		Task task2 = new Task("Task 2_to_be_finished", order);
		Collection<Task> tasks = tasksService.persistEntities(task1, task2);
		
		//WHEN Both Tasks are finished
		ArgumentCaptor<OrderFinishedEvent> finishedEventArgumentCaptor = ArgumentCaptor.forClass(OrderFinishedEvent.class);
		Mockito.doNothing().when(workshopEventListener).orderFinishedEventListener(finishedEventArgumentCaptor.capture());
		
		task1 = tasksService.findById(task1.getIdentifier());
		task1.setFinished(ZonedDateTime.now());
		tasksService.mergeEntity(task1);
		
		task2 = tasksService.findById(task2.getIdentifier());
		task2.setFinished(ZonedDateTime.now());
		tasksService.mergeEntity(task2);
		
		//THEN OrderFinishedEvent is generated with the Order
		Mockito.verify(workshopEventListener, Mockito.atMostOnce()).orderFinishedEventListener(Mockito.any(OrderFinishedEvent.class));
		assertEquals(order.getIdentifier(), finishedEventArgumentCaptor.getValue().getOrder().getIdentifier());
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"ADMIN_FULL"})
	public void orderFinishedEvent_Should_Not_Be_Generated_By_The_Order_Which_Not_All_Tasks_Are_Set_As_Finished() {
		//GIVEN
		User user = new User("user_one_task_unfinished@email.com");
		usersService.persistEntity(user);
		
		Order order = new Order();
		order.setCreatedFor(user);
		ordersService.persistEntity(order);
		
		Task task1 = new Task("Task 1_to_be_in_unfinished_Order", order);
		Task task2 = new Task("Task 2_to_be_in_unfinished_Order", order);
		tasksService.persistEntities(task1, task2);
		
		//WHEN Only one Task has been finished
		ArgumentCaptor<OrderFinishedEvent> finishedEventArgumentCaptor = ArgumentCaptor.forClass(OrderFinishedEvent.class);
		Mockito.doNothing().when(workshopEventListener).orderFinishedEventListener(finishedEventArgumentCaptor.capture());
		
		task1.setFinished(ZonedDateTime.now());
		tasksService.mergeEntities(task1, task2);
		
		//THEN No OrderFinishedEvent has been invoked
		Mockito.verify(workshopEventListener, Mockito.never()).orderFinishedEventListener(Mockito.any(OrderFinishedEvent.class));
	}
	
}