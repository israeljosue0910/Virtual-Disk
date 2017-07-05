package exceptions;
/**
 * Exception for when the disk is full
 * @author Israel J.Lopez Toledo
 *
 */
public class FullDiskException extends RuntimeException {

	public FullDiskException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 4693346754730484078L;
	

}
