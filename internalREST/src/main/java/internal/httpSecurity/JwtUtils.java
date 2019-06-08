package internal.httpSecurity;

import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;

@Component
@Slf4j
@Getter
@Setter
public class JwtUtils {
	
	/**
	 * Requires that you use a secret key that is at least 32 bytes long
	 */
	private final String SECRET_WORD = "WORKSHOP-SECRET-WORD";
	private final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;
	private final Key key = new SecretKeySpec(SECRET_WORD.getBytes(StandardCharsets.UTF_8), SIGNATURE_ALGORITHM.getFamilyName());
	
	/**
	 * @param usernameAuthenticationToken
	 * @return signed ready to use JWT for inserting into HTTP header
	 */
	public String generateJwt(Authentication usernameAuthenticationToken) {

//		Requires that you use a secret key that is at least 32 bytes long
//		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
//		Key key = new SecretKeySpec(getSECRET_WORD().getBytes(StandardCharsets.UTF_8), signatureAlgorithm.getFamilyName());
		
//		Header like {"alg": "HS256",	"typ": "JWT"} is automatically added by builder
//		setHeader will overwrite existing one!
		String jwtoken = Jwts.builder()
			.setIssuedAt(Date.valueOf(LocalDate.now()))
			.setExpiration(Date.valueOf(LocalDate.now().plus(Duration.ofMinutes(30))))
			.setIssuer("workshop.pro")
			.setAudience("workshop.pro/internal")
			.setSubject(usernameAuthenticationToken.getPrincipal().toString())
			.claim("scope", Arrays.deepToString(usernameAuthenticationToken.getAuthorities().toArray()))
			.signWith(key, SIGNATURE_ALGORITHM)
			.compact();
		
		return jwtoken;
	}
	
	public void parseJwt(String jwt) {
		try {
			Jwt<Header, Claims> claimsJwt = Jwts.parser().setSigningKey(key).parseClaimsJwt(jwt);
		} catch (JwtException e) {
			log.error("Jwt parsing failure!", e);
		}
		return;
	}
}
