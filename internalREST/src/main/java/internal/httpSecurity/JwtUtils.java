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
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;

@Slf4j
@Getter
@Setter
@Component
public class JwtUtils {
	
	@Autowired
	private Security security;
	
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
//		Header like {"alg": "HS256",	"typ": "JWT"} is automatically added by builder
//		.setHeader() will overwrite the existing one!
		String jwtoken = Jwts.builder()
			.setIssuedAt(Date.valueOf(LocalDate.now()))
			.setExpiration(Date.valueOf(LocalDate.now().plus(Duration.ofMinutes(30))))
			.setIssuer("workshop.pro")
			.setAudience("workshop.pro/internal")
			.setSubject(usernameAuthenticationToken.getPrincipal().toString())
			.claim("scope", Arrays.deepToString(usernameAuthenticationToken.getAuthorities().toArray()))
			.signWith(security.getKey(), security.getSignatureAlgorithm())
			.compact();
		
		return jwtoken;
	}
	
	public void parseJwt(String jwt) {
		try {
			Jwt<Header, Claims> claimsJwt = Jwts.parser().setSigningKey(security.getKey()).parseClaimsJwt(jwt);
		} catch (JwtException e) {
			log.error("Jwt parsing failure!", e);
		}
		return;
	}
}
