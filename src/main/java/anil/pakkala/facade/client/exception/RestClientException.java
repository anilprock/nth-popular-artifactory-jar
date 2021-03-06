package anil.pakkala.facade.client.exception;

/**
 * Exception throwed by a rest client request
 * @author anil pakkala
 *
 */
public class RestClientException extends Exception{

    private int statusCode;
    
    public RestClientException() {
        super();
    }
    
    public RestClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RestClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestClientException(String message) {
        super(message);
    }

    public RestClientException(Throwable cause) {
        super(cause);
    }
    
    public RestClientException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
    
        return statusCode;
    }

}
