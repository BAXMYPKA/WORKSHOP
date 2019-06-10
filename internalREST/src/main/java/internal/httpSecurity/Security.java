package internal.httpSecurity;

import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Slf4j
@NoArgsConstructor
@Component
class Security {
	/**
	 * Requires that you use a secret key that is at least 32 bytes long
	 * Can by set only at compilation time
	 */
	private String secretWord = "WORKSHOP-SECRET-WORD";
	@Getter
	private SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
	@Getter
	private Key key;
	
	public Security(String secretWord, SignatureAlgorithm signatureAlgorithm) {
		this.secretWord = secretWord;
		this.signatureAlgorithm = signatureAlgorithm;
		key = new SecretKeySpec(secretWord.getBytes(StandardCharsets.UTF_8), signatureAlgorithm.getFamilyName());
	}
}
