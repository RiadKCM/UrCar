package fr.parisdauphine.exception;

public class CarAlreadyInCartException extends RuntimeException {
    public CarAlreadyInCartException(String message) {
        super(message);
    }
}
