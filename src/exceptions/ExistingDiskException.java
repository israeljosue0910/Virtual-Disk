package exceptions;
/**
 * Exception for when there is already a disk with specified name
 * @author Israel J.Lopez Toledo
 *
 */
public class ExistingDiskException extends RuntimeException {
	
	
	  /** 
	   * Constructs a new Existing Disk Exception with null as its detail message.
	  */
	public ExistingDiskException() {
		// TODO Auto-generated constructor stub
	}

	  /** 
	   * Constructs a new Existing Disk Exception with the specified detail message.
	   * @param arg0 message to be displayed
	  */
	public ExistingDiskException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
