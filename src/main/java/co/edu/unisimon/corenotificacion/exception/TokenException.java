package co.edu.unisimon.corenotificacion.exception;

import org.springframework.security.core.AuthenticationException;

public class TokenException extends AuthenticationException {
	private static final long serialVersionUID = 1L;

	public TokenException(String msg) {
		super(msg);
	}
}
