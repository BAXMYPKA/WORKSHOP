package workshop.security;

import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

@Slf4j
@NoArgsConstructor
@Component
class SecurityUtils {
	/**
	 * Requires that you use a secret key that is at least 32 bytes long
	 * Can by set only at compilation time
	 */
	private String secretWord = "WORKSHOP-SECRET-WORD";
	@Getter
	private SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
	@Getter
	private Key key;
	private KeyGenerator keyGenerator;
	
	public SecurityUtils(String secretWord, SignatureAlgorithm signatureAlgorithm) {
		this.secretWord = secretWord;
		this.signatureAlgorithm = signatureAlgorithm;
		try {
			keyGenerator = KeyGenerator.getInstance(signatureAlgorithm.getJcaName());
			keyGenerator.init(256);
		} catch (NoSuchAlgorithmException e) {
			log.error("Fatal security initialization!", e);
		}
		key = keyGenerator.generateKey();
		
	}
}
