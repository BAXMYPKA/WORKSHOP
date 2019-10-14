package workshop.security;

import workshop.internal.entities.Department;
import workshop.internal.entities.Employee;
import workshop.internal.entities.Position;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {
	
	public static JwtUtils jwtUtils;
	public static SecurityUtils securityUtils;
	public String administratorAuthorityName = "Administrator";
	public String userAuthorityName = "User";
	public SimpleGrantedAuthority grantedAuthorityAdmin = new SimpleGrantedAuthority(administratorAuthorityName);
	public SimpleGrantedAuthority grantedAuthorityUser = new SimpleGrantedAuthority(userAuthorityName);
	public Authentication authentication;
	
/*
	@BeforeAll
	public static void overallPreparation() {
		securityUtils = new SecurityUtils("WORKSHOP", SignatureAlgorithm.HS256);
		jwtUtils = new JwtUtils();
		jwtUtils.setSecurityUtils(securityUtils);
		jwtUtils.setAudience("workshop.pro/internal");
		jwtUtils.setIssuer("workshop.pro");
		jwtUtils.setExpirationTime(30*60);
	}
*/
	
	@BeforeEach
	public void methodPreparation() {
		Position position = Position.builder().name("Position").department(new Department()).build();
		Employee employee = new Employee("TestUser", "ln", "ppppp", "TestUser@pro.pro",
			LocalDate.now().minusYears(17), position);
		authentication = new UsernamePasswordAuthenticationToken(
			employee, employee.getPassword(), Arrays.asList(grantedAuthorityAdmin, grantedAuthorityUser));
		
		securityUtils = new SecurityUtils("WORKSHOP", SignatureAlgorithm.HS256);
		jwtUtils = new JwtUtils();
		jwtUtils.setSecurityUtils(securityUtils);
		jwtUtils.setAudience("workshop.pro/internal");
		jwtUtils.setIssuer("workshop.pro");
		jwtUtils.setExpirationTime(30*60);
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
	@DisplayName("Authentication.Principal has to be either Employee or User." +
		"Otherwise a BadCredentialsException with the distinct message will be thrown")
	public void generateJwt_with_String_Authentication_parameter() {
		//GIVEN
		String emptyAuthenticationMessage = "Principal object in the AuthenticationToken neither Employee nor User!";
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
		
//		System.out.println("Original: " + jwt + "\n" + "Spoiled: " + spoiledJwt);
		
		//WHEN
		boolean isValid = jwtUtils.validateJwt(spoiledJwt);
		
		//THEN
		assertFalse(isValid);
	}
	
	@Test
	@DisplayName("Fully valid JWT has to be valid and non-expired")
	public void valid_JWT_return_true_and_wont_be_expired() {
		//GIVEN
		String jwt = jwtUtils.generateJwt(authentication);
		
		//WHEN
		boolean validJwt = jwtUtils.validateJwt(jwt);
		boolean isExpired = jwtUtils.isJwtExpired(jwt);
		
		//THEN
		assertTrue(validJwt);
		assertFalse(isExpired);
	}
	
	@Test
	@DisplayName("Max Cookie size is 4kb")
	public void jwt_Size_Doesnt_Exceed_Four_kb_Of_Cookies_Limitation_in_UTF() {
		//GIVEN a lot of huge strings for being stored into the JWT
		SimpleGrantedAuthority authority1 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoJWT");
		SimpleGrantedAuthority authority2 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoJWT");
		SimpleGrantedAuthority authority3 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoJWT");
		SimpleGrantedAuthority authority4 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoJWT");
		SimpleGrantedAuthority authority5 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoJWT");
		SimpleGrantedAuthority authority6 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoJWT");
		SimpleGrantedAuthority authority7 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoJWT");
		SimpleGrantedAuthority authority8 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoJWT");
		SimpleGrantedAuthority authority9 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoJWT");
		SimpleGrantedAuthority authority10 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoJWT");
		SimpleGrantedAuthority authority11 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoJWT");
		SimpleGrantedAuthority authority12 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoJWT");
		SimpleGrantedAuthority authority13 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoJWT");
		SimpleGrantedAuthority authority14 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoJWT");
		SimpleGrantedAuthority authority15 = new SimpleGrantedAuthority(
			"UnbearableHugeGrantedAuthorityNameToBeStoredIntoCookie");
		
		List<GrantedAuthority> authorities = Arrays.asList(authority1, authority2, authority3, authority4, authority5,
			authority6, authority7, authority8, authority9, authority10, authority11, authority12, authority13,
			authority14, authority15);
		
		Employee employee = new Employee(
			"FirstNameUnbearableStringForBeingStoredIntoThisFieldAndNotToBeExceededWith4KilobytesSize",
			"LastNameUnbearableStringForBeingStoredIntoThisFieldAndNotToBeExceededWith4KilobytesSize",
			"PasswordUnbearableStringForBeingStoredIntoThisFieldAndNotToBeExceededWith4KilobytesSize",
			"EmailUnbearableStringForBeingStoredIntoThisFieldAndNotToBeExceededWith4KilobytesSize@gmail.mail",
			LocalDate.now().minusYears(18), new Position(
			"NameUnbearableStringForBeingStoredIntoThisFieldAndNotToBeExceededWith4KilobytesSize",
			new Department()));
		
		Authentication bigSizeAuthentication = new UsernamePasswordAuthenticationToken(
			employee,
			employee.getPassword(),
			authorities);
		
		//WHEN generating big JWT. 1kb = 1024b
		String hugeJwt = jwtUtils.generateJwt(bigSizeAuthentication);
		int sizeInBytes = hugeJwt.getBytes(StandardCharsets.UTF_8).length;
		
		//THEN the JWT size doesn't exceed 4kb size
		assertTrue(sizeInBytes < 1024 * 4);
	}
}