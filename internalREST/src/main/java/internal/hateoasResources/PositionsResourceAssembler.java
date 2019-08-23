package internal.hateoasResources;

import internal.controllers.PositionsController;
import internal.entities.Position;
import org.springframework.stereotype.Component;

@Component
public class PositionsResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Position> {
	
	public PositionsResourceAssembler() {
		super(PositionsController.class, Position.class);
	}
}
