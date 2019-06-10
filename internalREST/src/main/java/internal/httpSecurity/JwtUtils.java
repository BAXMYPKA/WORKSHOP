package internal.httpSecurity;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;

@Slf4j
@Getter
@Setter
@Component
public class JwtUtils {
	
	@Autowired
	private Security security;
	
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
			.signWith(security.getKey(), security.getSignatureAlgorithm())
			.compact();
		
		return jwtoken;
	}
	
	public boolean validateJwt(String jwt) throws JwtException {
		if (jwt == null || jwt.isEmpty()) {
			throw new IllegalArgumentException("Jwt cannot be null or empty!");
		}
		try {
			Jws<Claims> claimsJws = Jwts.parser().setSigningKey(security.getKey()).parseClaimsJws(jwt);
			Claims claims = claimsJws.getBody();
			return true;
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
		Jws<Claims> claimsJws = Jwts.parser().setSigningKey(security.getKey()).parseClaimsJws(jwt);
		Claims claims = claimsJws.getBody();
		return claims.getExpiration().before(new java.util.Date(System.currentTimeMillis()));
	}
}
