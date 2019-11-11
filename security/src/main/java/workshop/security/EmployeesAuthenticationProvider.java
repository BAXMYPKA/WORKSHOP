package workshop.security;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Objects;

@Slf4j
@Setter
public class EmployeesAuthenticationProvider implements AuthenticationProvider {
	
	@Autowired
	@Qualifier("employeesDetailsService")
	private EmployeesDetailsService employeesDetailsService;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	/**
	 * User is ready to use implementation of the UserDetails
	 * UsernamePasswordAuthenticationToken.getPrincipal() returns a username String
	 * UsernamePasswordAuthenticationToken.getCredentials() returns a password String
	 *
	 * @param authenticationToken Only a UsernameAuthenticationToken with a raw (non encrypted) username and password
	 * @return Fully verified (by email & password) and authenticated UsernamePasswordAuthenticationToken
	 * @throws AuthenticationException in case of authentication failure
	 */
	@Override
	public Authentication authenticate(Authentication authenticationToken) throws AuthenticationException {
		if (authenticationToken == null || authenticationToken.getPrincipal() == null ||
			authenticationToken.getPrincipal().toString().isEmpty()) {
			throw new BadCredentialsException("Authentication or Principal cannot be null or empty!");
		}
		log.trace("Provide authentication...");

		UserDetailsEmployee employee = employeesDetailsService.loadUserByUsername(authenticationToken.getPrincipal().toString());
		
		log.debug("Employee={} is found.", employee.getUsername());
		
		isEmployeeEnabled(employee);
		
		//The raw password must match an encoded one from the Employee with that email
		if (!passwordEncoder.matches((String) authenticationToken.getCredentials(), employee.getPassword())) {
			throw new BadCredentialsException("Username or Password is incorrect!");
		}
		
		return new UsernamePasswordAuthenticationToken(employee, "", employee.getAuthorities());
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		log.trace("Check is Authentication.class supported...");
		return authentication.isInstance(Authentication.class);
	}
	
	/**
	 * Just returns the Authentication with email and Authorities.
	 * Use this method when prerequisites (JWT or something else) are valid and checked!
	 * Also this method has to be supported by all the further implementations of custom Authentication providers
	 */
	public Authentication authenticateByEmail(String employeeEmail) {
		if (employeeEmail == null || employeeEmail.isEmpty()) {
			throw new BadCredentialsException("Email cannot be null or empty!");
		}
		log.trace("Trying to find Employee by email {}", employeeEmail);
		UserDetailsEmployee userDetailsEmployee = employeesDetailsService.loadUserByUsername(employeeEmail);
		log.trace("User={} is found", userDetailsEmployee.getUsername());
		
		isEmployeeEnabled(userDetailsEmployee);
		
		UsernamePasswordAuthenticationToken authenticatedToken =
			new UsernamePasswordAuthenticationToken(userDetailsEmployee,"", userDetailsEmployee.getAuthorities());
		authenticatedToken.setAuthenticated(true);
		return authenticatedToken;
	}
	
	private void isEmployeeEnabled(UserDetailsEmployee employee) {
		if (!Objects.requireNonNull(employee).isEnabled()) {
			throw new InsufficientAuthenticationException("User " + employee.getEmployee() + " is not enabled!");
		}
	}
	
}
