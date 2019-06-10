package internal.httpSecurity;

import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeAll;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {
	
	public static JwtUtils jwtUtils;
	public static Security security;
	public SimpleGrantedAuthority grantedAuthority;
	public String administratorAuthorityName = "Administrator";
	public String userAuthorityName = "User";
	public SimpleGrantedAuthority grantedAuthorityAdmin = new SimpleGrantedAuthority(administratorAuthorityName);
	public SimpleGrantedAuthority grantedAuthorityUser = new SimpleGrantedAuthority(userAuthorityName);
	public Authentication authentication;
	
	@BeforeAll
	public static void overallPreparation() {
		security = new Security("WORKSHOP", SignatureAlgorithm.HS256);
		jwtUtils = new JwtUtils();
		jwtUtils.setSecurity(security);
		jwtUtils.setAudience("workshop.pro/internal");
		jwtUtils.setIssuer("workshop.pro");
	}
	
	@BeforeEach
	public void methodPreparation() {
		authentication = new UsernamePasswordAuthenticationToken(
			"TestUser", "", Arrays.asList(grantedAuthorityAdmin, grantedAuthorityUser));
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
		assertEquals(nullAuthenticationMessage, illegalArgumentException.getMessage());
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
	
	@Test
	public void null_or_empty_jwts_always_throws_IllegalArgumentsException() {
		//GIVEN null or empty String
		
		//WHEN
		IllegalArgumentException exceptionNullValidation = assertThrows(IllegalArgumentException.class,
			() -> jwtUtils.validateJwt(null));
		IllegalArgumentException exceptionEmptyValidation = assertThrows(IllegalArgumentException.class,
			() -> jwtUtils.validateJwt(""));
		IllegalArgumentException exceptionNullExpired = assertThrows(IllegalArgumentException.class,
			() -> jwtUtils.isJwtExpired(null));
		IllegalArgumentException exceptionEmptyExpired = assertThrows(IllegalArgumentException.class,
			() -> jwtUtils.isJwtExpired(""));
		
		//THEN
		System.out.println(exceptionEmptyExpired);
		System.out.println(exceptionNullExpired);
		
		assertEquals(IllegalArgumentException.class, exceptionNullValidation.getClass());
		assertEquals(IllegalArgumentException.class, exceptionEmptyValidation.getClass());
		assertEquals(IllegalArgumentException.class, exceptionNullExpired.getClass());
		assertEquals(IllegalArgumentException.class, exceptionEmptyExpired.getClass());
	}
	
	@Test
	public void check_non_expired_token() {
		//GIVEN generate non-expired token
		jwtUtils.setExpirationTime(1800); //30 minutes, default
		String thirtyMinutesToken = jwtUtils.generateJwt(authentication);
		
		//WHEN
		boolean nonExpired = jwtUtils.isJwtExpired(thirtyMinutesToken);
		
		//THEN
		assertFalse(nonExpired);
	}
	
	@Test
	public void check_expired_token() throws InterruptedException {
		//GIVEN
		jwtUtils.setExpirationTime(2);
		
		String expireAfterTwoSecondsToken = jwtUtils.generateJwt(authentication);
		
		//WHEN sleep 2sec to fake expiration
		Thread.sleep(2000);
		boolean expiredToken = jwtUtils.isJwtExpired(expireAfterTwoSecondsToken);
		
		//THEN
		assertTrue(expiredToken);
	}
	
	@Test
	public void unsigned_or_wrong_singed_tokens_always_return_false() {
		//GIVEN
		String jwt = jwtUtils.generateJwt(authentication);
		String spoiledJwt = jwt.substring(0, jwt.length() - 7).concat("spoiled");
		
		System.out.println("Original: " + jwt + "\n" + "Spoiled: " + spoiledJwt);
		
		//WHEN
		boolean isValid = jwtUtils.validateJwt(spoiledJwt);
		
		//THEN
		assertFalse(isValid);
	}
	
	
	@Test
	@DisplayName("Fully valid JWT has to be valid and non-expired")
	public void valid_JWT_return_true_with_non_expired() {
		//GIVEN
		String jwt = jwtUtils.generateJwt(authentication);
		
		//WHEN
		boolean validJwt = jwtUtils.validateJwt(jwt);
		boolean isExpired = jwtUtils.isJwtExpired(jwt);
		
		//THEN
		assertTrue(validJwt);
		assertFalse(isExpired);
	}
}