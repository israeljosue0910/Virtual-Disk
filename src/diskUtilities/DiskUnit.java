package diskUtilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Stack;

import exceptions.EmptyStackException;
import exceptions.ExistingDiskException;
import exceptions.FullDiskException;
import exceptions.InvalidBlockException;
import exceptions.InvalidBlockNumberException;
import exceptions.NonExistingDiskException;

/**
 * Manages the virtual disk, takes care of its creation, parameters, free block, directory and inode.
 * @author Israel J.Lopez Toledo
 *
 */
public class DiskUnit {
	  private static final int 
	    DEFAULT_CAPACITY = 1024;  // default number of blocks 	
	  private static final int
	    DEFAULT_BLOCK_SIZE = 256; // default number of bytes per block

	  private int capacity;     	// number of blocks of current disk instance
	  private int blockSizes; 	// size of each block of current disk instance
	  private int numNodes;
	  private int firstFLB;
	  private int firstFLBPos;
	  private int nextFreeNode;
	  private String diskName;
	  private int currentDir;
	  private String currentName;


	  Stack directoryStack = new Stack();
	  Stack <String> nameStack = new Stack<String>();
	  private static iNodeList listNodes;
	  
		
	  // the file representing the simulated  disk, where all the disk blocks
	  // are stored
	  private static RandomAccessFile disk;

	  // the constructor -- PRIVATE
	  /**
	    @param name is the name of the disk
	  **/
	  public DiskUnit(String name) {
	     try {
	         disk = new RandomAccessFile("DiskUnits/"+name, "rw");
	         diskName=name;
	     }
	     catch (IOException e) {
	         System.err.println ("Unable to start the disk");
	         System.exit(1);
	     }
	  }

	  /** Simulates shutting-off the disk. Just closes the corresponding RAF 
	   * and saves important disk parameters in block 0. If program is closed 
	   * and this method was not executed the disk is compromised and should be deleted.
	  **/
	  public void shutdown() {
		  try {
			  disk.seek(0);
			  disk.writeInt(capacity);  
			  disk.writeInt(blockSizes);
			  disk.writeInt(firstFLB);
			  disk.writeInt(firstFLBPos);
		      disk.writeInt(nextFreeNode);
			  disk.writeInt(numNodes);
		  } catch (IOException e) {
			  e.printStackTrace();
		  } 	
		  	
	    try {
	       disk.close();
	    } catch (IOException e) {
	       e.printStackTrace();
	    }
	    
	  }


	  // methods corresponding to disk operations, as well as the class 
	  // static methods as specified …

	  /**
	   * Turns on an existing disk unit whose name is given. If successful, it makes
	   * the particular disk unit available for operations suitable for a disk unit.
	   * And retrieves current disk parameters
	   * @param name the name of the disk unit to activate
	   * @return the corresponding DiskUnit object
	   * @throws NonExistingDiskException whenever no
	   *    ¨disk¨ with the specified name is found.
	  */
	  public static DiskUnit mount(String name)
	  throws NonExistingDiskException
	  {
	     File file=new File("DiskUnits/"+name);
	     if (!file.exists())
	         throw new NonExistingDiskException("No disk has name : " + name);
	    
	     DiskUnit dUnit = new DiskUnit(name);
	     	
	     // get the capacity and the block size of the disk from the file
	     // representing the disk
	     try {
	    	 dUnit.disk.seek(0);
	         dUnit.capacity = dUnit.disk.readInt();
	         dUnit.blockSizes = dUnit.disk.readInt();
	         dUnit.firstFLB = dUnit.disk.readInt();
	         dUnit.firstFLBPos = dUnit.disk.readInt();
	         dUnit.nextFreeNode = dUnit.disk.readInt();
	         dUnit.numNodes=dUnit.disk.readInt();
	     } catch (IOException e) {
	    	 e.printStackTrace();
	     }
	     return dUnit;     	
	  }
	     
	  /***
	   * Creates a new disk unit with the given name. The disk is formatted
	   * as having default capacity (number of blocks), each of default
	   * size (number of bytes). Those values are: DEFAULT_CAPACITY and
	   * DEFAULT_BLOCK_SIZE. The created disk is left as in off mode.
	   * @param name the name of the file that is to represent the disk.
	   * @throws ExistingDiskException whenever the name attempted is
	   * already in use.
	  */
	  public static void createDiskUnit(String name)
	  throws ExistingDiskException
	  {
	      createDiskUnit(name, DEFAULT_CAPACITY, DEFAULT_BLOCK_SIZE);
	  }
	     
	  /**
	   * Creates a new disk unit with the given name. The disk is formatted
	   * as with the specified capacity (number of blocks), each of specified
	   * size (number of bytes).  The created disk is left as in off mode.
	   * @param name the name of the file that is to represent the disk.
	   * @param capacity number of blocks in the new disk
	   * @param blockSize size per block in the new disk
	   * @throws ExistingDiskException whenever the name attempted is
	   * already in use.
	   * @throws InvalidParameterException whenever the values for capacity
	   *  or blockSize are not valid according to the specifications
	  */
	  public static void createDiskUnit(String name, int capacity, int blockSize)
	  throws ExistingDiskException, InvalidParameterException
	  {
	      File file=new File("DiskUnits/"+name);
	      if (file.exists())
	         throw new ExistingDiskException("Disk name is already used: " + name);
	     	
	      RandomAccessFile disk = null;
	      if (capacity < 0 || blockSize < 8 ||
	           !Utils.powerOf2(capacity) || !Utils.powerOf2(blockSize))
	         throw new InvalidParameterException("Invalid values: " +
	     		   " capacity = " + capacity + " block size = " +
	     		   blockSize);
	      // disk parameters are valid... hence create the file to represent the
	      // disk unit.
	      try {
	          disk = new RandomAccessFile("DiskUnits/"+name, "rw");
	      }
	      catch (IOException e) {
	          System.err.println ("Unable to start the disk");
	          System.exit(1);
	      }
	      
	      reserveDiskSpace(disk, capacity, blockSize);
	      
	      writeNodes(disk, capacity, blockSize);
	      
	      
	      // after creation, just leave it in shutdown mode - just
	      // close the corresponding file
	      try {
	          disk.close();
	      } catch (IOException e) {
	          e.printStackTrace();
	      }
	      
		  DiskUnit dUnit = mount(name);
		  for(int i = dUnit.firstFLB; i < dUnit.capacity; i++) {
			  dUnit.registerFB(i);
		  }
		  dUnit.shutdown();
	  }
	  
	  /**
	   * Sets the amount of available space for the new disk as specified by the capacity 
	   * and the block size. Afterwards the disk parameters are written in block 0
	   * @param disk the new disk unit to set its available space
	   * @param capacity number of blocks in the new disk
	   * @param blockSize size per block in the new disk
	  */ 
	  private static void reserveDiskSpace(RandomAccessFile disk, int capacity,
              int blockSize)
	  {
		  try {
			  disk.setLength(blockSize * capacity);
		  } catch (IOException e) {
			  e.printStackTrace();
		  }
		  int numNode = (int) (capacity*blockSize*(.01));
		  int numBlock = (numNode/(blockSize/9))+2;
		  int iNodeBlock = (numNode/(blockSize/9))+1;
		  int temp=numBlock+1;
		  // write disk parameters (number of blocks, bytes per block, etc) in
		  // block 0 of disk space
		  try {
			  disk.seek(0);
			  disk.writeInt(capacity);  
			  disk.writeInt(blockSize);
			  disk.writeInt(numBlock);     //First free block for data
			  disk.writeInt(0);            //FirstFLB Pos
			  disk.writeInt(1);            //First free iNode
			  disk.writeInt(numNode);      //Number of iNodes
		  } catch (IOException e) {
			  e.printStackTrace();
		  }
	  }
	  
	  /**
	   * Writes the content in the given block instance directly into the specified 
	   * disk block.
	   * @param blockNum index of the specific block in the disk where the content
	   * of the given block will be written
	   * @param b block instance with the content to be written in the disk
	   * @throws InvalidBlockNumberException whenever the block number attempted is
	   * not a valid one.
	   * @throws InvalidBlockException whenever the block instance attempted does not match the 
	   * block size of the current disk instance
	  */	  
	 public void write(int blockNum, VirtualDiskBlock b) 
			 throws InvalidBlockNumberException, InvalidBlockException{
		 if(b == null)
			 throw new InvalidBlockException("Target block is empty");
		 if(blockNum<0 || blockNum>this.capacity-1)
			 throw new InvalidBlockNumberException("The block number attempted is not a valid one");
		 if(b.getCapacity()!=this.blockSizes)
			 throw new InvalidBlockException("Block instance does not match the block size of the current disk instance");
		 
		  try {
			  disk.seek(blockNum*this.blockSizes);
			  disk.write(b.block);
		  } catch (IOException e) {
			  e.printStackTrace();
		  } 	
	 }
	 
	  /**
	   * Reads a given block from the disk. The content of the specified disk block
	   * is copied as the new content of the current instance block.
	   * @param blockNum index of the specific block in the disk that contains the data to be read 
	   * @param b block instance where the content to be read will be copied and read from
	   * @throws InvalidBlockNumberException whenever the block number attempted is
	   * not a valid one.
	   * @throws InvalidBlockException whenever the block instance attempted does not match the 
	   * block size of the current disk instance
	  */	 
	 public void read(int blockNum, VirtualDiskBlock b) 
			 throws InvalidBlockNumberException, InvalidBlockException{
		 if(blockNum<0 || blockNum>this.capacity-1)
			 throw new InvalidBlockNumberException("The Block Number is not a valid one");
		 if(b.getCapacity()!=this.blockSizes)
			 throw new InvalidBlockException("Block instance does not match the block size of the current disk instance");

		 try {
			 disk.seek(blockNum*this.blockSizes);
			 disk.read(b.block);
		 } catch (IOException e) {
			 e.printStackTrace();
		 } 	
		 
	 }
	 
	  /**
	   * @return a nonnegative integer value corresponding to the number of 
	   * valid blocks that the current disk instance has.
	  */ 
	 public int getCapacity(){
		 return this.capacity;
	 }
	 
	  /**
	   * @return nonnegative integer value which corresponds to the size of 
	   * a block in the current disk instance. 
	  */ 
	 public int getBlockSize(){
		 return this.blockSizes;
	 }
	 
	  /** 
	   * Formats the disk. This operation visits every “physical block”, 
	   * except block 0, in the disk and fills with zeroes all those blocks.
	  */
	 public void lowLevelFormat(){
		 for(int i=this.blockSizes; i<this.capacity*this.blockSizes; i++){
			 try {
				 disk.seek(i);
				 disk.write(0);
			 } catch (IOException e) {
				 System.err.println("Format was not successful");
				 e.printStackTrace();
			 } 	
		 }
	 }
	 
	  /** 
	   * Creates a list of iNodes, every iNode is initialized with 1, 
	   * they are later modified at the time of use.
	   * @param disk current disk
	   * @param numNode number of inodes
	   * @param b block size
	   * @return list of new inodes
	  */
	 private static iNodeList createNodes(RandomAccessFile disk, int numNode, int b){
		 iNodeList list = new iNodeList(numNode);
		 iNode nodeDirectory = new iNode(b, 1, 1, 0);
		 list.add(nodeDirectory);
		 for(int i = 1; i<numNode; i++){
			 iNode node = new iNode(b, 1, 1, 1); //This changes at file creation
			 list.add(node);
		 }
		 return list;
	 }
	 
	  /** 
	   * Writes every node into the disk at creation.
	   * @param disk current disk
	   * @param capacity disk capacity
	   * @param blockSize block size
	  */
	 private static void writeNodes(RandomAccessFile disk, int capacity, int blockSize){
	    int blockSizes = blockSize;
	    int numNode = (int) (capacity*blockSize*(.01));  //number of inodes
	    int numBlock = numNode/(blockSize/9);           //number of blocks for inodes
	    int nodesPerBlock= blockSize/9;                //nodes per block 
	    iNode node;
		listNodes = createNodes(disk, numNode, blockSize); //receives list of nodes
		 int index2 = 0;
		 outerloop:
		 for(int i=1; i<=numBlock+1; i++){
			 for(int j=0; j<nodesPerBlock; j++){
				 if(index2>=listNodes.length)
					 break outerloop;
				 try {
					 disk.seek((i*blockSize)+(9*(j)));
					 node=listNodes.getNode(index2);
					 disk.write(node.getType());
					 disk.writeInt(node.getSize());
					 disk.writeInt(node.getfirstBlock());
				 } catch (IOException e) {
					 e.printStackTrace();
				 } 
				 index2++;
			 }
		 }
	 }
	 
	  /** 
	   * Modifies specified iNode. This method is used at file creation.
	   * @param index iNode index
	   * @param size size of file
	   * @param firstBlock posistion of the file's first block
	  */
	 public void setInode(int index, int size, int firstBlock){
		int node = this.blockSizes + (9*index);
		 try {
			 disk.seek(node);
			 disk.write(1);
			 disk.writeInt(size);
			 disk.writeInt(firstBlock);
		 } catch (IOException e) {
			 e.printStackTrace();
		 }
	 }
	 
	 
	  /** 
	   * Modifies the file size the i node is holding
	   * @param index inode's index
	   * @param size size of file
	  */
	 public void setInode(int index, int size){
		int node = this.blockSizes + (9*index);  //finde position of node on disk
		 try {
			 disk.seek(node);
			 disk.write(1);
			 disk.writeInt(size);
		 } catch (IOException e) {
			 e.printStackTrace();
		 }
	 }
	 
	  /** 
	   * Sets specified iNode to directory iNode
	   * @param index iNode index
	  */
	 public void setInodeToDirectory(int index){
		int node = this.blockSizes + (9*index);
		 try {
			 disk.seek(node);
			 disk.write(0);
		 } catch (IOException e) {
			 e.printStackTrace();
		 }
	 }
	 
	  /** 
	   * Sets desired directory as current directory
	   * @param dir iNode index of the directory to be set as current directory
	  */
	 public void setDirectory(int dir){
		 currentDir=dir;
	 }
	 
	  /** 
	   * @return returns inode's type
	   * @param index inode's index
	  */
	 public int getInodeType(int index){
		 int node = this.blockSizes + (9*index);
		 int type=0;
		 try {
			 disk.seek(node);
			 type=disk.readByte();
		 } catch (IOException e) {
			 e.printStackTrace();
		 }
		 return type;
	 }
	 
	  /** 
	   * @return size of file that inode is holding
	   * @param index inode's index
	  */
	 public int getInodeSize(int index){
		 int node = this.blockSizes + (9*index);
		 int size=0;
		 try {
			 disk.seek(node);
			 disk.readByte();
			 size=disk.readInt();
		 } catch (IOException e) {
			 e.printStackTrace();
		 }
		 
		 return size;
	 }
	 
	  /** 
	   * @return location of the first block corresponding to this inodes file
	   * @param index inode's index
	  */
	 public int getInodeFBlock(int index){
		 int node = this.blockSizes + (9*index);
		 int block=0;
		 try {
			 disk.seek(node);
			 disk.readByte();
			 disk.readInt();
			 block=disk.readInt();
		 } catch (IOException e) {
			 e.printStackTrace();
		 }
		 return block;
	 }	 

	  /** 
	   * @return next free inode
	  */
	 public int getNextFreeNode(){
		 int current = nextFreeNode;
		 nextFreeNode++;
		 if(nextFreeNode>(numNodes-1))
			 nextFreeNode=0;
		 return current;
	 }
	 
	  /** 
	   * @return current free block
	  */
	 public int getfirstFLB(){
		 return this.firstFLB;
	 }
	 
	  /** 
	   * @return current directories inode index
	  */
	 public int getcurrentDir(){
		 return this.currentDir;
	 }
	 
	  /** 
	   * @return get name of current directory path
	  */
	 public String getCurrentName(){
		 return this.currentName;
	 }
	 
	  /** 
	   * Sets name of current directory path
	   * @param name name of current directory path
	  */
	 public void setCurrentName(String name){
		  this.currentName=name;
	 }
	 
	  /** 
	   * push current directory inode to stack, this is to preserve the directories order
	   * while moving through directories
	   * @param inodeIndex inode index of current directory
	  */
	 public void pushDir(int inodeIndex){ 
		 this.directoryStack.push(new Integer(inodeIndex));
	 }
	 
	  /** 
	   * push current directory path name, this is to preserve the directories path name order
	   * while moving forward through directories
	   * @param name current directory path name
	  */
	 public void pushName(String name){ 
		 this.nameStack.push(name);
	 }
	 
	  /** 
	   * pop stack to obtain previous directory inode index, this is to preserve the directories order
	   * while moving backwards through directories
	   * @return previous directory inode index, returns -1 if stack is empty
	  */
	 public int popDir() throws EmptyStackException{
		 int prevDir;
		 if(this.directoryStack.isEmpty()){
			 prevDir=-1;
			 return prevDir;
		 }
		 prevDir = (int) this.directoryStack.pop();
	
		 return prevDir;
	 }
	 
	  /** 
	   * @return get name of current disk
	  */
	 public String getDiskName(){
		 return this.diskName;
	 }
	 
	  /** 
	   * pop stack to obtain previous directory path name, this is to preserve the directories path name order
	   * while moving backwards through directories
	   * @return previous directory path name, returns null if stack is empty
	  */
	 public String popName() throws EmptyStackException{
		 String prevName;
		 if(this.nameStack.isEmpty()){
			 prevName=null;
			 return prevName;
		 }
		 prevName = this.nameStack.pop();
	
		 return prevName;
	 }
	 
	 /** 
	   * Attempts to delete specified disk.
	   * @param names file's name
	   * @return true or false depending on file deletion success
	  */
	 public static boolean deleteDisk(String names){
		 File file = new File("DiskUnits/"+names);
		 return file.delete();		 
	 }
	 
	  /** 
	   * @param i files block index
	   * @return specified file block
	  */
	 public FileBlock getFileBlock(int i) {
		 VirtualDiskBlock vdb = new VirtualDiskBlock(blockSizes);
		 this.read(i, vdb);
		 FileBlock fb = new FileBlock();
		 fb.setData(Utils.getStringFromBlock(vdb, blockSizes - 4));
		 fb.setNext(Utils.getIntFromBlock(vdb, vdb.getCapacity() - 4));
		 return fb;
	 }
	
	  /** 
	   * Manages the distribution of available free blocks
	   * @return index of next freeblock
	  */
	 public int getFreeBN() throws FullDiskException { 
		 int bn; 
		 if (firstFLB == 0) 
	      throw new FullDiskException("Disk is full.");
	   
		 VirtualDiskBlock vdb = new VirtualDiskBlock(blockSizes);
		 try{
			 this.read(firstFLB, vdb);
		 } catch(InvalidBlockNumberException e){
			 throw new FullDiskException("Disk is full.");
		 }
		   
		 if (firstFLBPos != 0) { 
			bn = Utils.getIntFromBlock(vdb, firstFLBPos * 4);
		    firstFLBPos--; 
		 }   
		 else {                                  
		    bn = firstFLB; 
		    firstFLB = Utils.getIntFromBlock(vdb, 0);  
		    firstFLBPos = (blockSizes/4) - 1;               
		 } 
		 return bn;     
	 }
	
	  /** 
	   * Registers blocks that are no longer in use as free blocks
	   * @param bn free block index
	  */
	 public void registerFB(int bn) { 
		 VirtualDiskBlock vdb = new VirtualDiskBlock(blockSizes);
		 if (firstFLB == 0)  { 
		 	 firstFLB = bn; 
			 Utils.copyIntToBlock(vdb, 0, 0);
			 firstFLBPos = 0; 
			 this.write(firstFLB, vdb);
		 }  else if (firstFLBPos == (blockSizes/4) - 1) {  
			 Utils.copyIntToBlock(vdb, 0, firstFLB);
			 firstFLBPos = 0;
			 firstFLB = bn; 
			 this.write(firstFLB, vdb);
		 }  else { 
			 this.read(firstFLB, vdb);
			 firstFLBPos++; 
			 Utils.copyIntToBlock(vdb, firstFLBPos * 4, bn);
			 this.write(firstFLB, vdb);
		 } 
	 }
		
	  /** 
	   * Adds file information to directory and connects every directory block
	   * so that it can be read later as a single file
	   * @param name files name
	   * @param sizeD directory current size
	   * @param Node files inode index to write on block
	  */
	public void addTodirectory(int sizeD, String name, int Node){
		int chunkSize = this.blockSizes - 4; 
		byte[] barr = new byte[this.blockSizes];
		String newName = name+" ";
		VirtualDiskBlock vb = new VirtualDiskBlock(this.blockSizes);
		Utils util = new Utils();	
		int block = this.getFreeBN();
		if(sizeD==1){                     //if directory is empty
			this.setInode(this.getcurrentDir(), (this.blockSizes), block);
			this.setInodeToDirectory(this.getcurrentDir());
		}
		else if(sizeD==this.blockSizes){  //if directory has 1 file, writes bock index of second file on first file
			int prevBlock = this.getInodeFBlock(this.getcurrentDir());
			 try {
				 disk.seek(((this.blockSizes)*(prevBlock+1))-4);
				 disk.writeInt(block);
			 } catch (IOException e) {
				 e.printStackTrace();
			 } 		 
			 this.setInode(this.getcurrentDir(), (this.blockSizes)*2);
			 this.setInodeToDirectory(this.getcurrentDir());
		}	
		else{  //rest of the cases, writes directories next block index on previous block
			int temp = this.getInodeFBlock(this.getcurrentDir());
			int ind=1;
			int prev=1;
			while(temp!=0){
				try {
					disk.seek(((this.blockSizes)*(temp+1))-4);
					temp=disk.readInt();
					if(temp!=0){
						prev=temp;
					}
				 } catch (IOException e) {
					 e.printStackTrace();
				 }
				 ind++;
			}
			try {
				disk.seek(((blockSizes)*(prev+1))-4);
				disk.writeInt(block);
				} catch (IOException e) {
					e.printStackTrace();
				}
				 
			this.setInode(this.getcurrentDir(), (this.blockSizes)*ind);
			this.setInodeToDirectory(this.getcurrentDir());
		}
		//writes new file information on directory block
		int chn = 0;
		int b = 0; 
		for (int i=0; i<chunkSize && chn < newName.length(); i++) { 
			barr[i] = (byte) newName.charAt(chn); 
			chn++; 
			b++; 
		}	
		for (int i=0; i<chunkSize; i++)  
			vb.setElement(i, barr[i]);

		util.copyNodeToBlock(vb, Node); //writes files node index in directory
		write(block, vb);
	}
}

