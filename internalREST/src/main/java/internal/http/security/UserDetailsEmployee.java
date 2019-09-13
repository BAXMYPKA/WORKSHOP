package internal.http.security;

import internal.entities.Employee;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * This implementation is intended to give an Employee object (eg for Trackable.setModified purpose, etc).
 */
@Setter
@Getter
public class UserDetailsEmployee implements UserDetails {
	
	private Employee employee;
	
	private String username;
	
	private String password;
	
	private Collection<GrantedAuthority> authorities;
	
	public UserDetailsEmployee(Employee employee) {
		this.employee = employee;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return employee.getPosition().getInternalAuthorities();
//		return Collections.singletonList(employee.getPosition());
	}
	
	@Override
	public String getPassword() {
		return employee.getPassword();
	}
	
	@Override
	public String getUsername() {
		return employee.getEmail();
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return employee.getIsEnabled();
	}
}
