package internal.hateoasResources;

import internal.controllers.PositionsController;
import internal.entities.Position;
import org.springframework.stereotype.Component;

@Component
public class PositionResourceAssembler extends WorkshopEntityResourceAssemblerAbstract<Position> {
	
	public PositionResourceAssembler() {
		setWorkshopControllerAbstractClass(PositionsController.class);
		setWorkshopEntityClass(Position.class);
	}
}
