package diskUtilities;
/**
 * Objects from this class represents a file block on disk, it holds the next block and the data on block. 
 * @author Israel J.Lopez Toledo 
 * 
 */
public class FileBlock{
	
	private int next;
	private String data;
	
	public FileBlock() {
		
	}
	/** 
	 * Sets file block next and data
	 * @param next next block
	 * @param data data on block
	*/
	public FileBlock(int next, String data) {
		next = next;
		data = data;

	}
	/** 
	 * @return next block
	*/
	public int getNext() {
		return next;
	}

	/** 
	 * Set next file block
	 * @param next next bloxk
	*/
	public void setNext(int next) {
		this.next = next;
	}
	/** 
	 * Return fileblock data
	 * @return data
	*/
	public String getData() {
		return data;
	}
	/** 
	 * Set fileblock data
	 * @param data data on block
	*/
	public void setData(String data) {
		this.data = data;
	}

}
