package exceptions;
/**
 * Exception for when the block specification are not valid
 * @author Israel J.Lopez Toledo
 *
 */
public class InvalidBlockException extends RuntimeException {
	
	  /** 
	   * Constructs a new Invalid Block Exception with null as its detail message.
	  */
	public InvalidBlockException() {
		// TODO Auto-generated constructor stub
	}

	  /** 
	   * Constructs a new Invalid Block Exception with the specified detail message.
	   * @param message message to be displayed
	  */
	public InvalidBlockException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
