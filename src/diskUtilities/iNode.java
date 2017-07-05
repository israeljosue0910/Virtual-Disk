package diskUtilities;
/**
 * Objects from this class represents an inode on the disk. Inodes carry information about the files.
 * @author Israel J.Lopez Toledo  
 * 
 */
public class iNode {

	
	private byte types;
	private int fileStart;
	private int fileSize;
	int bsize;
	
	/** 
	 * @param b current block size
	 * @param blocknum file first block index
	 * @param s file size
	 * @param type type of node
	*/	
	public iNode(int b, int blocknum, int s, int type){
		bsize=b;
		if(type == 0)
			types = 0; //type
		if(type == 1)
			types = 1;
		
		fileStart = (bsize * blocknum); //begining of file
		fileSize = s; //file size
	}
	
	/** 
	 * @return file size
	*/	
	public int getSize() {
		return this.fileSize;
	}
	
	/** 
	 * Set file size
	 * @param size file size
	*/	
	public void setSize(int size) {
		this.fileSize = size;
	}
	
	/** 
	 * @return node type
	*/
	public byte getType(){
		return this.types;
	}
	
	/** 
	 * Set node type
	 * @param type 1 for data and 0 for directory 
	*/	
	public void setType(int type){
		if(type!= 0 || type != 1)
			System.out.println("Not a valid type");
		this.types = (byte) type;
	}
	
	/** 
	 * @return file first block
	*/
	public int getfirstBlock(){
		return this.fileStart;
	}
	
	/** 
	 * Set file first block
	 * @param num file first block
	*/	
	public void setfirstBlock(int num){
		this.fileStart = (byte) (bsize * num);
	}
	
}
