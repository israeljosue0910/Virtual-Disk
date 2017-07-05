package theSystem;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import diskUtilities.Directory;
import diskUtilities.DiskUnit;
import diskUtilities.Find;
import diskUtilities.TextEditor;
import diskUtilities.Utils;
import diskUtilities.VirtualDiskBlock;
import diskUtilities.iNode;
import diskUtilities.iNodeList;
import exceptions.EmptyStackException;
import exceptions.ExistingDiskException;
import exceptions.FullDiskException;
import exceptions.InvalidBlockNumberException;
import exceptions.NonExistingDiskException;
import operandHandlers.OperandValidatorUtils;
import listsManagementClasses.ListsManager;
import systemGeneralClasses.Command;
import systemGeneralClasses.CommandActionHandler;
import systemGeneralClasses.CommandProcessor;
import systemGeneralClasses.FixedLengthCommand;
import systemGeneralClasses.SystemCommand;
import systemGeneralClasses.VariableLengthCommand;
import stack.IntStack;


/**
 * Controls users available commands
 * @author Israel J.Lopez Toledo
 *
 */
public class SystemCommandsProcessor extends CommandProcessor { 
	
	
	//NOTE: The HelpProcessor is inherited...

	// To initially place all lines for the output produced after a 
	// command is entered. The results depend on the particular command. 
	private ArrayList<String> resultsList; 
	
	SystemCommand attemptedSC; 
	// The system command that looks like the one the user is
	// trying to execute. 

	boolean stopExecution; 
	// This field is false whenever the system is in execution
	// Is set to true when in the "administrator" state the command
	// "shutdown" is given to the system.
	
	////////////////////////////////////////////////////////////////
	// The following are references to objects needed for management 
	// of data as required by the particular octions of the command-set..
	// The following represents the object that will be capable of
	// managing the different lists that are created by the system
	// to be implemented as a lab exercise. 
	private ListsManager listsManager = new ListsManager();
	
	private iNodeList list;
	
	private int current;
	
	private boolean mounted;
	
	private TextEditor text = new TextEditor();
	

	
	DiskUnit d, D;


	/**
	 *  Initializes the list of possible commands for each of the
	 *  states the system can be in. 
	 */
	public SystemCommandsProcessor() {
		
		// stack of states
		currentState = new IntStack(); 
		
		// The system may need to manage different states. For the moment, we
		// just assume one state: the general state. The top of the stack
		// "currentState" will always be the current state the system is at...
		currentState.push(GENERALSTATE); 

		// Maximum number of states for the moment is assumed to be 1
		// this may change depending on the types of commands the system
		// accepts in other instances...... 
		createCommandList(1);    // only 1 state -- GENERALSTATE

	
		// the following are for the different commands that are accepted by
		// the shell-like system that manage lists of integers
		
		// the command to create a new list is treated here as a command of variable length
		// as in the case of command testoutput, it is done so just to illustrate... And
		// again, all commands can be treated as of variable length command... 
		// One need to make sure that the corresponding CommandActionHandler object
		// is also working (in execute method) accordingly. See the documentation inside
		// the CommandActionHandler class for testoutput command.

		add(GENERALSTATE, SystemCommand.getFLSC("ls", new ListDirectoryProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("find name", new FindProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("cd return", new  ChangeDirectoryProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("mkdir dir_name", new  CreateDirectoryProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("drmdir dir_name", new RecursiveRemoveDirectoryProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("rmdir dir_name", new RemoveDirectoryProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("rm file_name_1", new RemoveFileProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("append ext_file_name file_name", new AppendFileProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("cp file_name_1 file_name_2", new CopyFileProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("loadfile file_name ext_file_name", new LoadFileProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("createdisk disk_name nblocks bsize", new CreateDiskProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("unmount", new UnmountDiskProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("mount disk_name", new MountDiskProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("cat file_name", new ShowFileProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("showdisks", new ShowDiskProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("exit", new ShutDownProcessor())); 
		add(GENERALSTATE, SystemCommand.getFLSC("deletedisk disk_name", new DeleteDiskProcessor())); 
				
		// set to execute....
		stopExecution = false; 

	}
		
	public ArrayList<String> getResultsList() { 
		return resultsList; 
	}
	
	// INNER CLASSES -- ONE FOR EACH VALID COMMAND --

	/**
	 * Class manages finding files locations
	 *
	 */
	private class FindProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) { 
			resultsList = new ArrayList<String>(); 
			if (d==null){
				resultsList.add("No disk mounted");
				return resultsList;
			}
			FixedLengthCommand fc = (FixedLengthCommand) c;
		    String name = fc.getOperand(1); 
		    
		    Find findOp = new Find();
		    ArrayList<String> list = new ArrayList<String>();   
		    ArrayList<String> names = new ArrayList<String>();
		    ArrayList<String> result = new ArrayList<String>();
		    
		    list=findOp.createList(0, d);              //creates list of the content in root
		    int currDir = d.getcurrentDir();          //saves current directory
		    result=findOp.recFiles(list, d, 0, name, d.getDiskName()+":/root", names);  //find all instances with recursion
		    if(result.isEmpty()){
				resultsList.add("No instance found");
				return resultsList;
		    }
		    for(int i=0; i<result.size(); i++){
		    	System.out.println(result.get(i));
		    }
		    d.setDirectory(currDir);                  // return directory to where it was before looking for the instances
			return resultsList;  
		}
	}
	
	/**
	 * Class manages removing non empty directories recursively
	 *
	 */
	private class RecursiveRemoveDirectoryProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) { 
			resultsList = new ArrayList<String>(); 
			if (d==null){
				resultsList.add("No disk mounted");
				return resultsList;
			}
			FixedLengthCommand fc = (FixedLengthCommand) c;
		    String name = fc.getOperand(1); 
		    
		    Directory dirOp = new Directory();
		    ArrayList<String> list = new ArrayList<String>();     
		    int inodeIndex =dirOp.findFile(name, d);			//finds inode of directory to remove
		    if(inodeIndex==-1){                              
				resultsList.add("Directory not found");
				return resultsList; 
		    }
		    
		    if(d.getInodeType(inodeIndex)!=0){
				resultsList.add("Name provided does not correspond to a directory");
				return resultsList; 
		    }
		    
		    if(d.getInodeSize(inodeIndex)<=1){
		    	dirOp.deleteDir(name, d);
				resultsList.add("Directory has been removed");
				return resultsList; 
		    }
		    
		    list=dirOp.createList(inodeIndex, d); //list of the content in directory to remove
		    dirOp.deleteFiles(list, d, inodeIndex, d.getcurrentDir());  //recursive remove
		    dirOp.deleteDir(name, d);                                   //after every conten is removed, target directory can now be removed
			resultsList.add("Directory has been removed");
			return resultsList; 
		}
	}
	
	
	/**
	 * Class manages removing empty directories
	 *
	 */
	private class RemoveDirectoryProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
			if (d==null){
				resultsList.add("No disk mounted");
				return resultsList;
			}
			FixedLengthCommand fc = (FixedLengthCommand) c;
		    String name = fc.getOperand(1); 
		    
		    String nme = "";
		    Utils util = new Utils();
		    int directoryFirst = d.getInodeFBlock(d.getcurrentDir());
		    
			VirtualDiskBlock vdm = new VirtualDiskBlock(d.getBlockSize());
		    VirtualDiskBlock vdb = new VirtualDiskBlock(d.getBlockSize());
			
			int inodeIndex=0;
			int prev=0;
			int next;
			int prevInode;
		    int sizeD= d.getInodeSize(d.getcurrentDir());
			int bn = d.getInodeFBlock(d.getcurrentDir()); 
			
			while (bn != 0) { //find target directory and removes from current directory, this removal is similar to a removal in a single linked list
				d.read(bn, vdb);  
				nme=util.stringFromVirtualDiskBlock(bn, vdb);
				next= util.getNextBNFromBlock(vdb);
				
				if(nme.equals(name) && bn==directoryFirst && next==0){  //if target directory is the only one in the directory
					inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
					if(d.getInodeType(inodeIndex)!=0){
						resultsList.add("Name provided does not correspond to a directory");
						return resultsList; 
					}
					if(d.getInodeSize(inodeIndex)>1){
						resultsList.add("Directory is not Empty");
						return resultsList; 
					}
					d.registerFB(bn);
					d.setInode(d.getcurrentDir(), 1, 0);
					break;
				}
				
				if(nme.equals(name)&& bn==directoryFirst){   //if target directory is first in the directory
					inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
					if(d.getInodeType(inodeIndex)!=0){
						resultsList.add("Name provided does not correspond to a directory");
						return resultsList; 
					}
					if(d.getInodeSize(inodeIndex)>1){
						resultsList.add("Directory is not Empty");
						return resultsList; 
					}
					d.registerFB(bn);
					bn = util.getNextBNFromBlock(vdb);
					d.setInode(d.getcurrentDir(), sizeD-d.getBlockSize(), bn);
					break;
				}
				
				if(nme.equals(name)&& next==0){  //if target directory is last in the directory
					inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
					if(d.getInodeType(inodeIndex)!=0){
						resultsList.add("Name provided does not correspond to a directory");
						return resultsList; 
					}
					if(d.getInodeSize(inodeIndex)>1){
						resultsList.add("Directory is not Empty");
						return resultsList; 
					}
					d.registerFB(bn);
					d.read(prev, vdb);
					util.copyNextBNToBlock(vdb, 0);
					d.write(prev, vdb);
					d.setInode(d.getcurrentDir(), sizeD-d.getBlockSize());
					break;
				}
				
				if(nme.equals(name)){  //rest of the cases
					inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
					if(d.getInodeType(inodeIndex)!=0){
						resultsList.add("Name provided does not correspond to a directory");
						return resultsList; 
					}
					if(d.getInodeSize(inodeIndex)>1){
						resultsList.add("Directory is not Empty");
						return resultsList; 
					}
					d.registerFB(bn);
					d.read(prev, vdb);
					util.copyNextBNToBlock(vdb, next);
					d.write(prev, vdb);
					d.setInode(d.getcurrentDir(), sizeD-d.getBlockSize());
					break;
				}
				prev=bn;
				bn = util.getNextBNFromBlock(vdb);
			}
			
			if(bn==0){
				resultsList.add("Directory not found");
				return resultsList; 
			}		
			
			d.setInode(inodeIndex, 1, 1);
			d.setInodeToDirectory(d.getcurrentDir());
			resultsList.add("Directory has been removed");
			return resultsList; 
		}
	}
	
	
	/**
	 * Class changes current directory
	 *
	 */
	private class ChangeDirectoryProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
			if (d==null){
				resultsList.add("No disk mounted");
				return resultsList;
			}
			FixedLengthCommand fc = (FixedLengthCommand) c;
		    String name = fc.getOperand(1);
		    
		    int prevDir;
		    String prevName;
		    
		    if(name.equals("..")){     
				
		    	prevDir=d.popDir();     //verifies if current directory is root 
		    	prevName=d.popName();
		    	if (prevDir == -1){
					resultsList.add(d.getCurrentName());
					return resultsList;
		    	}
		    	d.setDirectory(prevDir);    //goes to previous directory
		    	d.setCurrentName(prevName); //keeps a string of the location for user display
				resultsList.add(d.getCurrentName());
				return resultsList; 
		    }
		    String nme = "";
		    Utils util = new Utils();
			int n = d.getInodeSize(d.getcurrentDir());
			VirtualDiskBlock vdb = new VirtualDiskBlock(d.getBlockSize()); 
			int inodeIndex=0;
		    
			int bn = d.getInodeFBlock(d.getcurrentDir()); //verifies if target directory is on the current directory
			
			while (bn != 0) {
				d.read(bn, vdb); 
				nme=util.stringFromVirtualDiskBlock(bn, vdb);
				if(nme.equals(name)){
					inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
					break;
				}
				bn = util.getNextBNFromBlock(vdb);
			}
			
			if(bn==0){
				resultsList.add("File not found");
				return resultsList; 
			}
			
			if(d.getInodeType(inodeIndex)==1){
				resultsList.add("Name provided is not a directory");
				return resultsList; 
			}

			d.pushDir(d.getcurrentDir()); //pushes previous directory and current directory changes to target directory
			d.pushName(d.getCurrentName());
			d.setDirectory(inodeIndex);
			d.setCurrentName(d.getCurrentName()+"/"+name);
			
			resultsList.add(d.getCurrentName());
			return resultsList; 
		}
	}
	
	
	
	/**
	 * Class creates directory in current directory
	 *
	 */
	private class CreateDirectoryProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
			if (d==null){
				resultsList.add("No disk mounted");
				return resultsList;
			}
			FixedLengthCommand fc = (FixedLengthCommand) c;
		    String name = fc.getOperand(1); 
		    String nme = "";
		    Utils util = new Utils();
			int n = d.getInodeSize(d.getcurrentDir());
			VirtualDiskBlock vdb = new VirtualDiskBlock(d.getBlockSize()); 
			int inodeIndex=0;
		    
			if(d.getInodeSize(d.getcurrentDir())<=1){ //if current directory is empty
				int freeNode=d.getNextFreeNode();
				
				d.setInode(freeNode, 1, 1);
				d.setInodeToDirectory(freeNode);
				d.addTodirectory(n, name, freeNode); // creates directory in current directory
				d.setInodeToDirectory(d.getcurrentDir());
				
				resultsList.add("Directory has been created");
				return resultsList; 
			}
			
			
			int bn = d.getInodeFBlock(d.getcurrentDir()); //verifies if name is already used in directory
			
			while (bn != 0) {
				d.read(bn, vdb); 
				nme=util.stringFromVirtualDiskBlock(bn, vdb);
				if(nme.equals(name)){
					resultsList.add("Name is already in use");
					return resultsList;
				}
				bn = util.getNextBNFromBlock(vdb);
			}
			
			int freeNode=d.getNextFreeNode();
			
			d.setInode(freeNode, 1, 1);
			d.setInodeToDirectory(freeNode);
			d.addTodirectory(n, name, freeNode); //sets new directory inode and adds to directory
			d.setInodeToDirectory(d.getcurrentDir());
			
			resultsList.add("Directory has been created");
			return resultsList; 
		}
	}
	
	/**
	 * Class manages removing files
	 *
	 */
	private class RemoveFileProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
			if (d==null){
				resultsList.add("No disk mounted");
				return resultsList;
			}
			FixedLengthCommand fc = (FixedLengthCommand) c;
		    String name = fc.getOperand(1); 
		    
		    String nme = "";
		    Utils util = new Utils();
		    int directoryFirst = d.getInodeFBlock(d.getcurrentDir());
		    
			VirtualDiskBlock vdm = new VirtualDiskBlock(d.getBlockSize());
		    VirtualDiskBlock vdb = new VirtualDiskBlock(d.getBlockSize());
			
			int inodeIndex=0;
			int prev=0;
			int next;
			int prevInode;
		    int sizeD= d.getInodeSize(d.getcurrentDir());
			int bn = d.getInodeFBlock(d.getcurrentDir()); //finds file on directory
			
			while (bn != 0) { //find and erase from directory, removal is similar to that of a single linked list
				d.read(bn, vdb);         //all removals consist on registering old blocks and inodes as free
				nme=util.stringFromVirtualDiskBlock(bn, vdb);
				next= util.getNextBNFromBlock(vdb);
				
				if(nme.equals(name) && bn==directoryFirst && next==0){ //if file is the only one on the directory
					inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
					if(d.getInodeType(inodeIndex)==0){
						resultsList.add("Name provided is not a file");
						return resultsList; 
					}
					d.registerFB(bn);
					d.setInode(d.getcurrentDir(), 1, 0);
					break;
				}
				
				if(nme.equals(name)&& bn==directoryFirst){//if file is the first one on the directory
					inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
					if(d.getInodeType(inodeIndex)==0){
						resultsList.add("Name provided is not a file");
						return resultsList; 
					}
					d.registerFB(bn);
					bn = util.getNextBNFromBlock(vdb);
					d.setInode(d.getcurrentDir(), sizeD-d.getBlockSize(), bn);
					break;
				}
				
				if(nme.equals(name)&& next==0){////if file is the last one on the directory
					inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
					if(d.getInodeType(inodeIndex)==0){
						resultsList.add("Name provided is not a file");
						return resultsList; 
					}
					d.registerFB(bn);
					d.read(prev, vdb);
					util.copyNextBNToBlock(vdb, 0);
					d.write(prev, vdb);
					d.setInode(d.getcurrentDir(), sizeD-d.getBlockSize());
					break;
				}
				
				if(nme.equals(name)){//rest of the cases
					inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
					if(d.getInodeType(inodeIndex)==0){
						resultsList.add("Name provided is not a file");
						return resultsList; 
					}
					d.registerFB(bn);
					d.read(prev, vdb);
					util.copyNextBNToBlock(vdb, next);
					d.write(prev, vdb);
					d.setInode(d.getcurrentDir(), sizeD-d.getBlockSize());
					break;
				}
				prev=bn;
				bn = util.getNextBNFromBlock(vdb);
			}
			
			if(bn==0){
				resultsList.add("File not found");
				return resultsList; 
			}
			
			int fileStart = d.getInodeFBlock(inodeIndex); 
			int fileStart2 = fileStart;
			d.registerFB(fileStart);
			
			int counter=d.getBlockSize()-20;
			int b2=0;
			int b1=5;
			d.read(fileStart, vdm);
			
			while (fileStart2!=0) {//finds file on disk using inode and registers every block the file uses
				b2++;
				fileStart2 = util.getNextBNFromBlock2(vdm, counter);
				if(fileStart2!=0){
					d.registerFB(fileStart2);
				}
				counter+=4;
				if(b2>=b1-1){ //travels to the indirect block lists
					fileStart = util.getNextBNFromBlock2(vdm, counter);
					d.read(fileStart, vdm);
					if(fileStart!=0){
						d.registerFB(fileStart);
					}
					counter=0;
					b2=0;
					b1=(d.getBlockSize()/4);
				}
			}
			
			d.setInode(inodeIndex, 1, 1);
			d.setInodeToDirectory(d.getcurrentDir());
			resultsList.add("File has been removed");
			return resultsList; 
		}
	}
	
	/**
	 * Inner class that takes care of appending a external file to another file on disk
	 * 
	 */
	private class AppendFileProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
			if (d==null){
				resultsList.add("No disk mounted");
				return resultsList;
			}
			FixedLengthCommand fc = (FixedLengthCommand) c;
		    String name = fc.getOperand(1); 
		    String newName = fc.getOperand(2); 
		    
		    String nme = "";
		    Utils util = new Utils();
		    int directory = d.getInodeFBlock(d.getcurrentDir());
			VirtualDiskBlock vdb = new VirtualDiskBlock(d.getBlockSize());
			VirtualDiskBlock vdm = new VirtualDiskBlock(d.getBlockSize()); 
			int inodeIndex=0;	    
			int bn = d.getInodeFBlock(d.getcurrentDir()); 
			
			while (bn != 0) { //finds file in directory
				d.read(bn, vdb); 
				nme=util.stringFromVirtualDiskBlock(bn, vdb);
				if(nme.equals(name)){
					inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
					break;
				}
				bn = util.getNextBNFromBlock(vdb);
			}
			if(bn==0){
				resultsList.add("File not found");
				return resultsList; 
			}
			
			if(d.getInodeType(inodeIndex)==0){
				resultsList.add("Name provided is not a file");
				return resultsList; 
			}
			
			int fileStart = d.getInodeFBlock(inodeIndex);
			int fileStart2 = fileStart;
			int fileStart3 = fileStart;

			String file1="";
			int counter=d.getBlockSize()-20;
			int n1 = 20;
			int b2=0;
			int b1=5;
			d.read(fileStart, vdm);
			
			while (fileStart2!=0) {//finds file on disk using inode and puts it in string in file1
				b2++;
				d.read(fileStart2, vdb);
				file1=file1+util.stringFromVirtualDiskBlock2(vdb, n1);
				n1=0;
				fileStart2 = util.getNextBNFromBlock2(vdm, counter);
				if(fileStart2!=0){
					d.registerFB(fileStart2);
				}
				counter+=4;
				if(b2>=b1-1){
					fileStart = util.getNextBNFromBlock2(vdm, counter);
					d.read(fileStart, vdm);
					if(fileStart!=0){
						d.registerFB(fileStart);
					}
					counter=0;
					b2=0;
					b1=(d.getBlockSize()/4);
				}
			}
			file1=file1.substring(0, d.getInodeSize(inodeIndex));
			
		   
			try {
				String file2 = text.fileToSTring(newName);
				file1=file1+'\n'+file2;       //appends file1 and file2
			} catch (IOException e) {
				// TODO Auto-generated catch block
				resultsList.add("File not Found");
				return resultsList;
			}
								
			
			//writes file on disk

			
			int block=d.getBlockSize();
			VirtualDiskBlock vb = new VirtualDiskBlock(block);
			VirtualDiskBlock vbm = new VirtualDiskBlock(block);

			int blocknum = (file1.length()-(block-20))/block;

			byte[] barr = new byte[block];
			boolean done = false; 
			
			d.registerFB(fileStart3);
			int freeblock = d.getFreeBN();
			int chn = 0, bn1 = 0, index=0;
			int nextFreeBlock=0; 

			//sets file Inode

			d.setInode(inodeIndex, file1.length());
			
			for (int i=0; i<(block-20) && chn < file1.length(); i++) { //this series of steps before the while is to write 
			barr[i] = (byte) file1.charAt(chn);                        //the first N-20 bits
			chn++; 
			}

			for (int i=0; i<(block-20) && i<barr.length; i++){  
				vbm.setElement(i, barr[i]);
			}

			int counter1=block-20;
			
			int b=0;
			
			int b3=5;

			if(file1.length()<block-20){
				d.write(freeblock, vbm);
			}
			
			while(!done && file1.length()>block-20){ //once the N-20 bits have been filled, we continue to write on this block
				                                     //the block number where the other bits will be written
				nextFreeBlock=d.getFreeBN();
				
				Utils.copyIntToBlocks(vbm, counter1, nextFreeBlock);
				
				counter1+=4;
				b++;
				
				for (int i=0; i< block && chn < file1.length(); i++) { 
					barr[i] = (byte) file1.charAt(chn); 
					chn++; 
				}
				
				for (int i=0; i<block; i++)  
					vb.setElement(i, barr[i]);
				
				bn1++;

				if ((chn >= file1.length() || bn1 > blocknum+1)) {
					done = true;
					Utils.copyIntToBlocks(vbm, counter, 0);
					d.write(freeblock, vbm);
				}
				
				d.write(nextFreeBlock, vb);

				
				if(b>=b3-1){ //if necessary create indirect blocks to point to remaining bits

					int temp=d.getFreeBN();
					Utils.copyIntToBlocks(vbm, counter1, temp);
					d.write(freeblock, vbm);
					freeblock=temp;
					counter1=0;
					b=0;
					b3=(block/4);
				}
				

			}

			resultsList.add("Content has been appended to the file");
			
			return resultsList; 
		}
	}
	

	/**
	 * Inner class takes care of taking a file making a copy with a different name and copies it
	 * into disk.
	 */
	private class CopyFileProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
			if (d==null){
				resultsList.add("No disk mounted");
				return resultsList;
			}
			FixedLengthCommand fc = (FixedLengthCommand) c;
		    String name = fc.getOperand(1); 
		    String newName = fc.getOperand(2); 
		    
		    //finds the file and converts it to a string
		    
		    String nme = "";
		    Utils util = new Utils();
		    int directory = d.getInodeFBlock(d.getcurrentDir());
			VirtualDiskBlock vdb = new VirtualDiskBlock(d.getBlockSize());
			VirtualDiskBlock vdm = new VirtualDiskBlock(d.getBlockSize()); 
			int inodeIndex=0;	    
			int bn = d.getInodeFBlock(d.getcurrentDir()); 
			
			while (bn != 0) { //finds file in directory
				d.read(bn, vdb); 
				nme=util.stringFromVirtualDiskBlock(bn, vdb);
				if(nme.equals(name)){
					inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
					break;
				}
				bn = util.getNextBNFromBlock(vdb);
			}
			if(bn==0){
				resultsList.add("File not found");
				return resultsList; 
			}
			
			if(d.getInodeType(inodeIndex)==0){
				resultsList.add("Name provided is not a file");
				return resultsList; 
			}
			
			int fileStart = d.getInodeFBlock(inodeIndex);
			int fileStart2 = fileStart;

			String file1="";
			int counter=d.getBlockSize()-20;
			int n1 = 20;
			int b2=0;
			int b1=5;
			d.read(fileStart, vdm);
			
			while (fileStart2!=0) {//finds file on disk using inode and puts string in file1
				b2++;
				d.read(fileStart2, vdb);
				file1=file1+util.stringFromVirtualDiskBlock2(vdb, n1);
				n1=0;
				fileStart2 = util.getNextBNFromBlock2(vdm, counter);
				counter+=4;
				if(b2>=b1-1){
					fileStart = util.getNextBNFromBlock2(vdm, counter);
					d.read(fileStart, vdm);
					counter=0;
					b2=0;
					b1=(d.getBlockSize()/4);
				}
			}
			file1=file1.substring(0, d.getInodeSize(inodeIndex));
						
			
			//writes file on disk

			int n = d.getInodeSize(d.getcurrentDir());
			int freeNode = d.getNextFreeNode();
			
			int block=d.getBlockSize();
			VirtualDiskBlock vb = new VirtualDiskBlock(block);
			VirtualDiskBlock vbm = new VirtualDiskBlock(block);

			int blocknum = (file1.length()-(block-20))/block;

			byte[] barr = new byte[block];
			boolean done = false; 

			int freeblock;
			try{
				 freeblock = d.getFreeBN();  /////
			} catch(FullDiskException e){
				resultsList.add("Disk is full");
				return resultsList;
			}
			
			int chn = 0, bn1 = 0, index=0;
			int nextFreeBlock=0;
			if(freeNode==0){
				resultsList.add("Disk is full");
				return resultsList;
			}

			//sets file Inode

			d.setInode(freeNode, file1.length(), freeblock);
			
			for (int i=0; i<(block-20) && chn < file1.length(); i++) { //implementation of free blocks just like append and loadfile
			barr[i] = (byte) file1.charAt(chn); 
			chn++; 
			}

			for (int i=0; i<(block-20) && i<barr.length; i++){  
				vbm.setElement(i, barr[i]);
			}

			int counter1=block-20;
			
			int b=0;
			
			int b3=5;

			if(file1.length()<block-20){
				d.write(freeblock, vbm);
			}
			
			while(!done && file1.length()>block-20){
				
				nextFreeBlock=d.getFreeBN();
				
				
				Utils.copyIntToBlocks(vbm, counter1, nextFreeBlock);
				
				counter1+=4;
				b++;
				
				for (int i=0; i< block && chn < file1.length(); i++) { 
					barr[i] = (byte) file1.charAt(chn); 
					chn++; 
				}
				
				for (int i=0; i<block; i++)  
					vb.setElement(i, barr[i]);
				
				bn1++;

				if ((chn >= file1.length() || bn1 > blocknum+1)) {
					done = true;
					Utils.copyIntToBlocks(vbm, counter, 0);
					d.write(freeblock, vbm);
				}
				
				d.write(nextFreeBlock, vb);

				
				if(b>=b3-1){

					int temp=d.getFreeBN();
					Utils.copyIntToBlocks(vbm, counter1, temp);
					d.write(freeblock, vbm);
					freeblock=temp;
					counter1=0;
					b=0;
					b3=(block/4);
				}
				

			}

			d.addTodirectory(n, newName, freeNode); //adds file to directory
			
			resultsList.add("File has been copied");
			
			return resultsList; 
		}
	}
	
	
	
	/**
	 * Class manages showing desired file to user
	 *
	 */
	private class ShowFileProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
			if (d==null){
				resultsList.add("No disk mounted");
				return resultsList;
			}
			FixedLengthCommand fc = (FixedLengthCommand) c;
		    String name = fc.getOperand(1); 
		    String nme = "";
		    Utils util = new Utils();
		    int directory = d.getInodeFBlock(d.getcurrentDir());
			VirtualDiskBlock vdm = new VirtualDiskBlock(d.getBlockSize()); 
			VirtualDiskBlock vdb = new VirtualDiskBlock(d.getBlockSize()); 
			int inodeIndex=0;
		    
			int bn = d.getInodeFBlock(d.getcurrentDir()); //finds file on directory
			
			while (bn != 0) { 
				d.read(bn, vdb); 
				nme=util.stringFromVirtualDiskBlock(bn, vdb);
				if(nme.equals(name)){
					inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
					break;
				}
				bn = util.getNextBNFromBlock(vdb);
			}
			
			if(bn==0){
				resultsList.add("File not found");
				return resultsList; 
			}
			int a = d.getInodeType(inodeIndex);
			if(d.getInodeType(inodeIndex)==0){
				resultsList.add("Name provided is not a file");
				return resultsList; 
			}
	
			int fileStart = d.getInodeFBlock(inodeIndex);
			int fileStart2 = fileStart;

			String file1="";
			int counter=d.getBlockSize()-20;
			int n = 20;
			int b=0;
			int b1=5;
			d.read(fileStart, vdm);
			
			while (fileStart2!=0) {//finds file on disk using inode and prints it
				b++;
				d.read(fileStart2, vdb);
				file1=file1+util.stringFromVirtualDiskBlock2(vdb, n);
				n=0;
				fileStart2 = util.getNextBNFromBlock2(vdm, counter);
				counter+=4;
				if(b>=b1-1){ //if file uses indirect blocks
					fileStart = util.getNextBNFromBlock2(vdm, counter);
					d.read(fileStart, vdm);
					counter=0;
					b=0;
					b1=(d.getBlockSize()/4);
				}
			}
			//Display text
			if(file1.length()>=d.getInodeSize(inodeIndex)){
				file1=file1.substring(0, d.getInodeSize(inodeIndex));
			}
			int j;
			int k=0;
			for(int i=0; i<=(file1.length()/64)+1; i++){
				j=k+64;
				if(j>=file1.length()){
					System.out.println(file1.substring(k, file1.length()));
					break;
				}
				System.out.println(file1.substring(k, j));
				k+=64;

			}
		    
			return resultsList; 
		}
	}
	
	
	/**
	 * Displays the list of files in directory and displays size (number of blocks x block size)
	 *
	 */
	private class ListDirectoryProcessor implements CommandActionHandler { 
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
			if(d==null){
				resultsList.add("No disk mounted");
				return resultsList;
			}
			int sizeD = d.getInodeSize(d.getcurrentDir());
			
			if(sizeD==1){ //if directory is empty
				resultsList.add("Directory is Empty");
				return resultsList;
			}

			Utils util = new Utils();
			VirtualDiskBlock vdb = new VirtualDiskBlock(d.getBlockSize()); 
			int inodeIndex;
		
			System.out.println("Name - Size");
			
			int bn = d.getInodeFBlock(d.getcurrentDir()); //finds directory file and displays it
			while (bn != 0) { 
				d.read(bn, vdb); 
				util.showVirtualDiskBlock(bn, vdb);
				inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
				System.out.println(" "+d.getInodeSize(inodeIndex));//!!!!!
				bn = util.getNextBNFromBlock(vdb);			
			}
			
			return resultsList; 
		}
	}
	
	
	/**
	 * Loads a file to disk, adds it to directory and sets its Inode
	 *
	 */
	private class LoadFileProcessor implements CommandActionHandler { 
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
			if (d==null){
				resultsList.add("No disk mounted");
				return resultsList;
			}
			FixedLengthCommand fc = (FixedLengthCommand) c;
			String name = fc.getOperand(1);
			String newName = fc.getOperand(2);
			int n = d.getInodeSize(d.getcurrentDir());
			int freeNode = d.getNextFreeNode();
			
			
			int block=d.getBlockSize();
			VirtualDiskBlock vb = new VirtualDiskBlock(block);
			VirtualDiskBlock vbm = new VirtualDiskBlock(block);
			try {
				String file = text.fileToSTring(name); //converts file to string
				int blocknum = (file.length()-(block-20))/block;
				
				byte[] barr = new byte[block];
				boolean done = false; 
				Utils util = new Utils();
				int freeblock;
				try{
					 freeblock = d.getFreeBN();  
				} catch(FullDiskException e){
					resultsList.add("Disk is full");
					return resultsList;
				}
				int chn = 0, bn = 0, index=0;
				int nextFreeBlock=0;
				if(freeNode==0){
					resultsList.add("Disk is full");
					return resultsList;
				}

				d.setInode(freeNode, file.length(), freeblock);
				
				//writes file using new file block implementation 
				
				for (int i=0; i<(block-20) && chn < file.length(); i++) { //implementation of free blocks just like append and copy file
				barr[i] = (byte) file.charAt(chn); 
				chn++; 
			}
				for (int i=0; i<(block-20) && i<barr.length; i++){  
					vbm.setElement(i, barr[i]);
				}
				
				int counter=block-20;
				
				int b=0;
				
				int b1=5;
				
				if(file.length()<block-20){
					d.write(freeblock, vbm);
				}
				
				while(!done && file.length()>block-20){
					
					nextFreeBlock=d.getFreeBN();
		
					Utils.copyIntToBlocks(vbm, counter, nextFreeBlock);
					
					counter+=4;
					b++;
					
					for (int i=0; i< block && chn < file.length(); i++) { 
						barr[i] = (byte) file.charAt(chn); 
						chn++; 
					}
					
					for (int i=0; i<block; i++)  
						vb.setElement(i, barr[i]);
					
					bn++;

					if ((chn >= file.length() || bn > blocknum+1)) {
						done = true;
						Utils.copyIntToBlocks(vbm, counter, 0);
						d.write(freeblock, vbm);
					}
					try {
					d.write(nextFreeBlock, vb);
					} catch (InvalidBlockNumberException e){
						resultsList.add("Disk full");
						return resultsList;
					}
					
					if(b>=b1-1){

						int temp=d.getFreeBN();
						Utils.copyIntToBlocks(vbm, counter, temp);
						d.write(freeblock, vbm);
						freeblock=temp;
						counter=0;
						b=0;
						b1=(block/4);
					}
					

				}
				
			  d.addTodirectory(n, newName, freeNode);
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				resultsList.add("File not Found");
				return resultsList;
			}
			
			resultsList.add("File has been loaded");
			
			return resultsList; 
		}
	}
	
	/**
	 * Creates disk with a specified block size and capacity
	 *
	 */
	private class CreateDiskProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>();
			if(d!=null){
				resultsList.add("Please unmount first");
				return resultsList;
			}
			FixedLengthCommand fc = (FixedLengthCommand) c;
		    String name = fc.getOperand(1);
		    String diskSize = fc.getOperand(2);
		    String blockSize = fc.getOperand(3);
		    int bsize = Integer.parseInt(blockSize);
		    int dsize = Integer.parseInt(diskSize);
		    if(bsize<32){
		    	resultsList.add("Block Size is less than 32");
		    	return resultsList;
		    }
		    try{
		    	DiskUnit.createDiskUnit(name, dsize, bsize);
		    	TextEditor.writeNewText(name); //creates a text file that is going to keep the disks 
		    	resultsList.add("Disk "+name+" was created");
		    }
		    catch(ExistingDiskException e){
		    	resultsList.add("Disk already exists");
		    }
		    catch (InvalidParameterException e){
		    	resultsList.add("Disk parameters are not valid");
		    } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return resultsList; 
		}
	}
	/**
	 * Unmounts disk
	 *
	 */
	private class UnmountDiskProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
		    if(d!=null){
		    	d.shutdown(); //shutdowns disk, saves important parameters before closing
				resultsList.add("Disk has been unmounted");
				d=null;
		    }
		    else{
		    	resultsList.add("No available disk to unmount");
		    }

			return resultsList; 
		}
	}
	
	/**
	 * Mounts disk
	 *
	 */
	private class MountDiskProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>();
		    if(d!=null){
				resultsList.add("Unmount current disk first");
				return resultsList;
		    }
			FixedLengthCommand fc = (FixedLengthCommand) c;
		    String name = fc.getOperand(1); 
		    try{
				d=DiskUnit.mount(name); //mounts disk and loads important parameters
				d.setDirectory(0);
				
				d.setCurrentName(name+":/"+"root");
				resultsList.add("Disk has been mounted");
				current=0;
				mounted=true;
		    }
		    catch(NonExistingDiskException e){
		    	resultsList.add("Disk was not found");
		    }
			return resultsList; 
		}
	}
	
	/**
	 * Deletes disk
	 *
	 */
	private class DeleteDiskProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
			if(d!=null){
				resultsList.add("Please unmount first");
				return resultsList;
			}
			FixedLengthCommand fc = (FixedLengthCommand) c;
		    String name = fc.getOperand(1);
		    
		    if(DiskUnit.deleteDisk(name)){
		    	System.gc();
		    	try {
					text.remove(name); //removes disk from text file
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	resultsList.add("Disk has been deleted");
	    	}

	    	else{
		    	resultsList.add("Disk could not be deleted");
		    	}
		    
			return resultsList; 
		}
	}
	
	/**
	 * Ends program
	 *
	 */
	private class ShutDownProcessor implements CommandActionHandler { 
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>(); 
			if(d!=null){
		    	d.shutdown();
				resultsList.add("Disk has been unmounted");
				d=null;
			}

			resultsList.add("SYSTEM IS SHUTTING DOWN!!!!");
			stopExecution = true;
			return resultsList; 
		}
	}
	
	/**
	 * Shows a list of disks with its block size and capacity
	 *
	 */
	private class ShowDiskProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>();
			int blockSize;
			int capacity;
			if(d!=null){
				resultsList.add("Please unmount first");
				return resultsList;
			}
			try {
				ArrayList<String> list = TextEditor.currentDiskList();
				for(int i=0; i<list.size(); i++){
					D=DiskUnit.mount(list.get(i));
					blockSize=D.getBlockSize();
					capacity=D.getCapacity();
					resultsList.add(list.get(i)+" : BlockSize is "+blockSize+" Capacity is "+capacity);
					D.shutdown();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return resultsList;
		}
	}


	/**
	 * 
	 * @return
	 */
	public boolean inShutdownMode() {
		return stopExecution;
	}

}		