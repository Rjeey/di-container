package by.rjeey.exception;

public class BindingNotFoundException extends Exception{

    @Override
    public String getMessage() {
        return "Binding for this class was not found in container \n" + super.getMessage();
    }
}
