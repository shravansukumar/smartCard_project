import java.security.NoSuchAlgorithmException;

public class AppExceptions extends Throwable {
    public AppExceptions(String errorMessage) {
        super(errorMessage);
    }    
}