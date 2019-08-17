package internal.entities.hateoasResources;

import internal.controllers.PositionsController;
import internal.entities.Position;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
public class PositionResourceAssembler extends WorkshopEntityResourceAssemblerAbstract<Position> {
	
	public PositionResourceAssembler() {
		setWorkshopControllerAbstractClass(PositionsController.class);
		setWorkshopEntityClass(Position.class);
	}
}
