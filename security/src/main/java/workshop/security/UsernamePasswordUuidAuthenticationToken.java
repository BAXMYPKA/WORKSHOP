package workshop.security;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Objects;

/**
 * Special class for the newly-created {@link workshop.internal.entities.User}s who enter the Workshop site for the
 * first time with the connected {@link workshop.internal.entities.Uuid}.
 */
public class UsernamePasswordUuidAuthenticationToken extends UsernamePasswordAuthenticationToken {
	
	@Getter
	private String uuid;
	
	public UsernamePasswordUuidAuthenticationToken(Object principal, Object credentials, String uuid) {
		super(principal, credentials);
		this.uuid = Objects.requireNonNull(uuid, "Uuid cannot be null!");
	}
	
	public UsernamePasswordUuidAuthenticationToken(
		Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, String uuid) {
		super(principal, credentials, authorities);
		this.uuid = Objects.requireNonNull(uuid, "Uuid cannot be null!");
	}
}
