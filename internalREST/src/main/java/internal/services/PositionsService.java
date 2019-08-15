package internal.services;

import internal.dao.PositionsDao;
import internal.entities.Position;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class PositionsService extends WorkshopEntitiesServiceAbstract<Position> {
	
	/**
	 * @param positionsDao A concrete implementation of the EntitiesDaoAbstract<T,K> for the concrete
	 *                            implementation of this EntitiesServiceAbstract<T>.
	 *                            To be injected to all the superclasses.
	 *                            For instance, 'public OrdersService(OrdersDao ordersDao)'
	 */
	public PositionsService(PositionsDao positionsDao) {
		super(positionsDao);
	}
}
