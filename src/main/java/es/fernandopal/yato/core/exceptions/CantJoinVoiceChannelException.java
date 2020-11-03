package es.fernandopal.yato.core.exceptions;

@SuppressWarnings("serial")
public class CantJoinVoiceChannelException extends Exception {
	public CantJoinVoiceChannelException(String errorMessage) {
        super(errorMessage);
    }
}
