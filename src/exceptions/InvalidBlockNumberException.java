package exceptions;
/**
 * Exception for when the number of blocks is not valid
 * @author Israel J.Lopez Toledo
 *
 */
public class InvalidBlockNumberException extends RuntimeException {

	  /** 
	   * Constructs a new Invalid BlockNumber Exception with null as its detail message.
	  */
	public InvalidBlockNumberException() {
		// TODO Auto-generated constructor stub
	}

	  /** 
	   * Constructs a new Invalid BlockNumber Exception with the specified detail message.
	   * @param message message to be displayed
	  */
	public InvalidBlockNumberException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
