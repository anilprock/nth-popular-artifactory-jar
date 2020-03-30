package anil.pakkala.facade.client.exception;

/**
 * Exception throwed by the artifactory client
 * @author anil pakkala
 *
 */
public class ArtifactoryClientException extends Exception {

    public ArtifactoryClientException() {
        super();
    }

    public ArtifactoryClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ArtifactoryClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArtifactoryClientException(String message) {
        super(message);
    }

    public ArtifactoryClientException(Throwable cause) {
        super(cause);
    }
    
}
