package exceptions;
/**
 * Exception for when the disk does not exist
 * @author Israel J.Lopez Toledo
 *
 */
public class NonExistingDiskException extends RuntimeException {
	
	  /** 
	   * Constructs a new NonExisting Disk Exception with null as its detail message.
	  */
	public NonExistingDiskException() {
		// TODO Auto-generated constructor stub
	}

	  /** 
	   * Constructs a new NonExisting Disk Exception with the specified detail message.
	   * @param arg0 message to be displayed
	  */
	public NonExistingDiskException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
}
