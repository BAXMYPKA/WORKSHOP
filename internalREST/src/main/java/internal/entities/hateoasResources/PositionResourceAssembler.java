package internal.entities.hateoasResources;

import internal.controllers.PositionsController;
import internal.entities.Position;
import org.springframework.stereotype.Component;

@Component
public class PositionResourceAssembler extends WorkshopEntityResourceAssembler<Position> {
	
	public PositionResourceAssembler(PositionsController positionsController) {
		super(positionsController);
	}
}
