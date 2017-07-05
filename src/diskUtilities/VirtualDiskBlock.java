package diskUtilities;

/**
 * Object from this class creates a virtual disk block
 * @author Israel J.Lopez Toledo
 *
 */
public class VirtualDiskBlock {
	static int size; //number of bytes per block
	byte [] block;   
	
	 /**
	  * creates a block of size equal to 256 bytes.
	 */
	public VirtualDiskBlock(){
		size = 256;
		block = new byte[size]; 
	}

	 /**
	  * creates a block of size equal to blockCapacity
	  * @param blockCapacity is the desired bytes per block
	 */
	public VirtualDiskBlock(int blockCapacity){
		size=blockCapacity;
		block = new byte[size];
	}
	
	 /**
	  * @return returns a positive integer value that corresponds to the capacity 
	  * of the current instance of block
	 */ 
	public int getCapacity(){
		return size;
	}
	
	 /**
	  * Changes the content in the current disk block instance at a desired position 
	  * to a new element specified in the parameters
	  * @param index position of the element to be changed
	  * @param nuevo element that will be set at the specified position
	 */ 
	public void setElement(int index, byte nuevo){
		block[index]= nuevo;
	}
	
	 /**
	  * Returns a copy of the element at the specified position
	  * @param index position of the element that will be returned 
	  * @return element at the desired position
	 */
	public byte getElement(int index){
		return block[index];
	}
}
