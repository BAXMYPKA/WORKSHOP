package internal.entities;

import org.springframework.hateoas.ResourceSupport;

public abstract class WorkshopEntityAbstract extends ResourceSupport implements WorkshopEntity {
	
	/**
	 * First it compares by 'created', if null, by 'created' ZonedDateTime
	 *
	 * @param o WorkshopEntity instance
	 * @return If both WorkshopEntities's 'identifier' and 'created' are null, 0 will be returned as if they're equal.
	 */
	@Override
	public int compareTo(WorkshopEntity o) {
		if (getCreated() != null && o.getCreated() != null) {
			return getCreated().compareTo(o.getCreated());
		} else if (getIdentifier() != null && o.getIdentifier() != null) {
			return getIdentifier().compareTo(o.getIdentifier());
		} else {
			return 0;
		}
	}
}
