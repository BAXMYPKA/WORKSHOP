package workshop.internal.entities.utils;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.springframework.stereotype.Component;

/**
 * The class is just for the testing purposes to catch and log Hibernate cache 2nd level access.
 */
@Slf4j
@Component
public class CachesListener implements CacheEventListener<Object, Object> {
	
	@Override
	public void onEvent(CacheEvent<? extends Object, ? extends Object> cacheEvent) {
		log.trace("Ehcache event: {}", cacheEvent.getType());
	}
}
