package internal.httpSecurity;

import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {
	
	public JwtUtils jwtUtils;
	public Security security;
	public SimpleGrantedAuthority grantedAuthority;
	public Authentication authentication;
	
	@BeforeEach
	public void overallPreparation() {
		security = new Security("WORKSHOP", SignatureAlgorithm.HS256);
		SimpleGrantedAuthority grantedAuthorityAdmin = new SimpleGrantedAuthority("Administrator");
		SimpleGrantedAuthority grantedAuthorityUser = new SimpleGrantedAuthority("User");
		authentication = new UsernamePasswordAuthenticationToken(
			"TestUser", "", Arrays.asList(grantedAuthorityAdmin, grantedAuthorityUser));
		jwtUtils = new JwtUtils();
	}
	
	@Test
	@DisplayName("Has to throw IllegalArgumentException with the distinct message")
	public void generateJwt_with_null_Authentication_Parameter() {
		//GIVEN
		String nullAuthenticationMessage = "Authentication cannot be null!";
		authentication = null;
		
		//WHEN
		IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
			() -> jwtUtils.generateJwt(authentication),
			"With null Authentication will throw IllegalArgumentsExceprion");
		
		//THEN
		assertEquals(illegalArgumentException.getClass(), IllegalArgumentException.class);
	}
	
	@Test
	@DisplayName("Has to throw BadCredentialsException with the distinct message")
	public void generateJwt_with_empty_Authentication_parameter() {
		//GIVEN
		String emptyAuthenticationMessage = "Username is null or empty!";
		authentication = new UsernamePasswordAuthenticationToken("", "", null);
		
		//WHEN
		BadCredentialsException badCredentialsException = assertThrows(BadCredentialsException.class,
			() -> jwtUtils.generateJwt(authentication));
		
		//THEN
		assertEquals(BadCredentialsException.class, badCredentialsException.getClass());
		assertEquals(emptyAuthenticationMessage, badCredentialsException.getMessage());
	}
}