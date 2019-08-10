package internal.entities;

import org.springframework.hateoas.ResourceSupport;

public abstract class WorkshopEntityAbstract extends ResourceSupport implements WorkshopEntity {
	
	/**
	 * First it compares by 'identifier', second by 'created' ZonedDateTime
	 *
	 * @param o WorkshopEntity instance
	 * @return 0 (as equal) if both WorkshopEntities have 'identifier' and 'created' null.
	 */
	@Override
	public int compareTo(WorkshopEntity o) {
		if (getIdentifier() != null && o.getIdentifier() != null) {
			return getIdentifier().compareTo(o.getIdentifier());
		} else if (getCreated() != null && o.getCreated() != null) {
			return getCreated().compareTo(o.getCreated());
		} else {
			return 0;
		}
	}
}
