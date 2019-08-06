package internal.entities.hateoasResources;

import internal.entities.Position;

public class PositionResource extends WorkshopEntityResource<Position> {
	/**
	 * @param position The concrete instance of the WorkshopEntity to be the HATEOAS Resource.
	 */
	public PositionResource(Position position) {
		super(position);
	}
}
