package diskUtilities;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;

import exceptions.ExistingDiskException;
/**
 * Various useful methods
 * @author Israel J.Lopez Toledo
 *
 */
public class Utils {

	public Utils() {
		// TODO Auto-generated constructor stub
	}
	public static final int INTSIZE = 4; 
	  /**
	   * Verifies if the integer is a power of two by using the "and" function between
	   * the target number "n" and "n-1". If the number is indeed a power of 2 the result will
	   * be 0 (this is because the "and" function is actually applied to the binary representation 
	   * of "n" and "n-1" and if the number is a power of two it will always result in 0), 
	   * else it is not a power of two.
	   * @param n target number to verify if it is a power of two
	   * @return true if it is a power of two
	   * @return false if it is not a power of two
	  */
	public static boolean powerOf2 (int n){ 
		if((n & (n-1)) == 0) 
			return true;

		else 
			return false;
	}
	
	/**
	 * Writes the index of the next free block on the last four bytes of the block
	 * @param vdb virtual disk block object
	 * @param value index of next free block
	 */
	public static void copyNextBNToBlock(VirtualDiskBlock vdb, int value) { 
		int lastPos = vdb.getCapacity()-1;
		for (int index = 0; index < 4; index++) { 
			vdb.setElement(lastPos - index, (byte) (value & 0x000000ff)); 	
			value = value >> 8; 
		}
	}
	
	/**
	 * Writes the index of the specified inode on the block (capacity-5 to capacity-8)
	 * @param vdb virtual disk block object
	 * @param value index of the inode to write
	 */
	public static void copyNodeToBlock(VirtualDiskBlock vdb, int value) { 
		int lastPos = vdb.getCapacity()-1;
		for (int index = 0; index < 4; index++) { 
			vdb.setElement(lastPos - index-4, (byte) (value & 0x000000ff)); 	
			value = value >> 8; 
		}
	}
	
	/**
	 * Reads a number from block
	 * @param vdb virtual disk block object
	 * @param index location of number to read
	 * @return number read
	 */
	public static int getIntFromBlock(VirtualDiskBlock vdb, int index) {  
		int value = 0; 
		int lSB; 
		for (int i=0; i < INTSIZE; i++) { 
			value = value << 8; 
			lSB = 0x000000ff & vdb.getElement(index + i);
			value = value | lSB; 
		}
		return value; 
	}
	
	/**
	 * gets a string from block
	 * @param vdb virtual disk block object
	 * @param length length of string
	 * @return string read
	 */
	public static String getStringFromBlock(VirtualDiskBlock vdb, int length) {
		String result = "";
		
		for(int i = 0; i < length; i++) {
			char c = Utils.getCharFromBlock(vdb, i);
			if(c != ' ')
				result += c;
			else
				return result;
		}
		
		return result;
	}
	
	/**
	 * Gets a character from virtual disk block object
	 * @param vdb virtual disk block object
	 * @param index index of character 
	 * @return returns the character specified by index
	 */
	public static char getCharFromBlock(VirtualDiskBlock vdb, int index) { 
		return (char) vdb.getElement(index); 
	}
	
	/**
	 * Copies a number to virtual disk block object
	 * @param vdb virtual disk block object
	 * @param index block where the number will be written
	 * @param value number to be written
	 */
	public static void copyIntToBlock(VirtualDiskBlock vdb, int index, int value) { 
		for (int i = INTSIZE-1; i >= 0; i--) { 
			vdb.setElement(index+i, (byte) (value & 0x000000ff)); 	
			value = value >> 8; 
		}
	}
	
	/**
	 * Displays content of virtual disk block
	 * @param b index
	 * @param block virtual disk block object
	 */
	public static void showVirtualDiskBlock(int b, VirtualDiskBlock block) {
	    for (int i=0; i<block.getCapacity(); i++) {
	    	char c = (char) block.getElement(i); 
	    	if (Character.isLetterOrDigit(c))
	    		System.out.print(c);
	    	else break;
	    }
	}
	
	/**
	 * Displays content of virtual disk block but leaves out the last 4 bytes
	 * @param b index
	 * @param block virtual disk block object
	 */
	public static void showVirtualDiskBlock2(int b, VirtualDiskBlock block) {
	    for (int i=0; i<block.getCapacity()-4; i++) {
	    	char c = (char) block.getElement(i); 
	    	if (Character.isLetterOrDigit(c))
	    		System.out.print(c); 
	    	else
	    		System.out.print(' '); 
	    }
	    System.out.println(); 
	}
	
	
	/**
	 * Reads and returns a string from virtual disk block object
	 * @param b index
	 * @param block virtual disk block object
	 * @return string extracted from virtual diskblock
	 */
	public static String stringFromVirtualDiskBlock(int b, VirtualDiskBlock block) {
		String result="";
	    for (int i=0; i<block.getCapacity(); i++) {
	    	char c = (char) block.getElement(i); 
	    	if (Character.isLetterOrDigit(c))
	    		result = result+c;
	    	else
	    		break;
	    }
	    return result;
	}
	
	/**
	 * Reads and returns a string from virtual disk block object, but leaves out the last 4 bytes
	 * @param b index
	 * @param block virtual disk block object
	 * @return string extracted from virtual disk block
	 */
	public static String stringFromVirtualDiskBlock2(VirtualDiskBlock block, int n) {
		String result="";
	    for (int i=0; i<block.getCapacity()-n; i++) {
	    	char c = (char) block.getElement(i); 
	    	result = result+c;

	    }
	    return result;
	}
	
	/**
	 * Gets the next block written at the end of the block
	 * @param vdb virtual disk block object
	 * @return next block number
	 */
	public static int getNextBNFromBlock(VirtualDiskBlock vdb) { 
		int bsize = vdb.getCapacity(); 
		int value = 0; 
		int lSB; 
		for (int index = 3; index >= 0; index--) { 
			value = value << 8; 
			lSB = 0x000000ff & vdb.getElement(bsize-1-index);
			value = value | lSB; 
		}
		return value; 

	}
	

	/**
	 * Copies a number to virtual disk block object
	 * @param vdb virtual disk block object
	 * @param index block where the number will be written
	 * @param value number to be written
	 */
	public static void copyIntToBlocks(VirtualDiskBlock vdb, int index, int value) { 

		for (int i = INTSIZE-1; i >= 0; i--) { 
			vdb.setElement(index+i, (byte) (value & 0x000000ff)); 	
			value = value >> 8;
		}
		
	}
	
	/**
	 * Gets the next block written at the specified location
	 * @param vdb virtual disk block object
	 * @param counter specified location to start reading from
	 * @return next block number
	 */
	public static int getNextBNFromBlock2(VirtualDiskBlock vdb, int counter) { 
		int bsize = vdb.getCapacity(); 
		int value = 0; 
		int lSB; 
		for (int index = 0; index < 4; index++) {
			value = value << 8; 
			lSB = 0x000000ff & vdb.getElement(counter+index);
			value = value | lSB;
		}
		return value; 

	}
	
	

}
