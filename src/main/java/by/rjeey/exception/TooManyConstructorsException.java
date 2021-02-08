package by.rjeey.exception;

public class TooManyConstructorsException extends Exception {

    @Override
    public String getMessage() {
        return "There should be only one constructor with @Inject annotation \n" + super.getMessage();
    }
}
