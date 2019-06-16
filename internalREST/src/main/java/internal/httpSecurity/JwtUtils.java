package internal.httpSecurity;

import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;

@Slf4j
@Getter
@Setter
@Component
public class JwtUtils {
	
	@Autowired
	private SecurityUtils securityUtils;
	
	/**
	 * Jwt token expiration time in seconds. Default = 1800 (30minutes)
	 */
	private int expirationTime = 60 * 30;
	private String issuer = "workshop.pro";
	private String audience = "workshop.pro/internal";
	
	/**
	 * @param usernameAuthenticationToken where .getPrincipal = String(Username)
	 * @return signed ready to use JWT for inserting into HTTP header
	 */
	public String generateJwt(Authentication usernameAuthenticationToken)
		throws IllegalArgumentException, AuthenticationException {
		
		if (usernameAuthenticationToken == null) {
			throw new IllegalArgumentException("Authentication cannot be null!");
		} else if (usernameAuthenticationToken.getPrincipal() == null ||
			usernameAuthenticationToken.getPrincipal().toString().isEmpty()) {
			throw new BadCredentialsException("Username is null or empty!");
		}
		String scope;
		if (usernameAuthenticationToken.getCredentials() == null) {
			scope = "";
		} else {
			scope = Arrays.deepToString(usernameAuthenticationToken.getAuthorities().toArray());
		}
//		Header like {"alg": "HS256",	"typ": "JWT"} is automatically added by builder
//		JwtBuilder.setHeader() will overwrite the existing one!
		String jwtoken = Jwts.builder()
			.setIssuedAt(Date.valueOf(LocalDate.now()))
			.setExpiration(new Date(System.currentTimeMillis() + 1000 * expirationTime))
			.setIssuer(issuer)
			.setAudience(audience)
			.setSubject(usernameAuthenticationToken.getPrincipal().toString())
			.claim("scope", scope)
			.signWith(securityUtils.getKey(), securityUtils.getSignatureAlgorithm())
			.compact();
		
		return jwtoken;
	}
	
	/**
	 * @return true if JWT contains all the proprietary fields
	 * @throws JwtException if JWT contains incorrect sign or any other fatal problems. Particularly throws
	 *                      ExpiredJwtException if the JWT is expired.
	 */
	public boolean validateJwt(String jwt) throws JwtException {
		if (jwt == null || jwt.isEmpty()) {
			throw new IllegalArgumentException("Jwt cannot be null or empty!");
		}
		//TODO: to check 'scope' to match workshop.pro/internal
		try {
			Jws<Claims> claimsJws = Jwts.parser().setSigningKey(securityUtils.getKey()).parseClaimsJws(jwt);
			Claims claims = claimsJws.getBody();
			return audience.equals(claims.getAudience()) &&
				issuer.equals(claims.getIssuer()) &&
				(claims.getSubject() != null && !claims.getSubject().isEmpty()) &&
				claims.containsKey("scope");
		} catch (ExpiredJwtException exp) {
			log.trace(exp.getMessage());
			throw exp;
		} catch (JwtException e) {
			log.trace("Jwt parsing failure! Message:" + e.getMessage());
			return false;
		}
	}
	
	public boolean isJwtExpired(String jwt) throws IllegalArgumentException, JwtException {
		if (jwt == null || jwt.isEmpty()) {
			throw new IllegalArgumentException("Jwt cannot be null or empty!");
		}
		try {
			validateJwt(jwt);
		} catch (ExpiredJwtException exp) {
			log.trace(exp.getMessage());
			return true;
		}
		//Can be modified to set a time lag
		Jws<Claims> claimsJws = Jwts.parser().setSigningKey(securityUtils.getKey()).parseClaimsJws(jwt);
		Claims claims = claimsJws.getBody();
		return claims.getExpiration().before(new java.util.Date(System.currentTimeMillis()));
	}
	
	//TODO: to test
	public String getUsernameFromJwt(String jwt) throws IllegalArgumentException, JwtException {
		if (jwt == null || jwt.isEmpty()) {
			throw new IllegalArgumentException("Jwt cannot be null or empty!");
		}
		try {
			String subject = Jwts.parser().setSigningKey(securityUtils.getKey()).parseClaimsJws(jwt).getBody().getSubject();
			return subject;
		} catch (JwtException e) {
			log.trace(e.getMessage());
			throw  e;
		}
	}
	
//	public String getAuthiritiesFromJwt(String jwt) throws IllegalArgumentException, JwtException {
//		if (jwt == null || jwt.isEmpty()) {
//			throw new IllegalArgumentException("Jwt cannot be null or empty!");
//		}
//		try {
//			Jwts.parser().setSigningKey(securityUtils.getKey()).parseClaimsJws(jwt).getBody().get("scope")
//		}
//		return null;
//	}
}
