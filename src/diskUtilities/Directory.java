package diskUtilities;

import java.io.File;
import java.util.ArrayList;

import exceptions.NonExistingDiskException;
import systemGeneralClasses.Command;
import systemGeneralClasses.CommandActionHandler;
import systemGeneralClasses.FixedLengthCommand;


/**
 * Manages the necessary methods to achieve recursive removal
 * @author Israel J.Lopez Toledo
 *
 */
public class Directory {
	
	
	public Directory() {
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * Travels recursively through every non empty directory. First it verifies if content of directory is 
	 * file or directory. If it is a directory it verifies if it is empty, if it is empty it deletes it, if it is not empty 
	 * the method calls itself. If it is a file it simply deletes it
	 * @param files an array list with the content of the current directory the method is verifying
	 * @param d an instance of the current disk 
	 * @param inodeDir inode index of the next directory the method is moving to
	 * @param currentDir inode index of the previous directory 
	 *   
	**/
	public void deleteFiles(ArrayList<String> files, DiskUnit d, int inodeDir, int currentDir) {
    	d.pushDir(currentDir);
    	d.setDirectory(inodeDir);
		for (String file : files) {
	        if (isDirectory(file, d)) {
	        	int inode = this.findFile(file, d);
	        	if(d.getInodeSize(inode)>1){
		            deleteFiles(this.createList(inode, d),d, inode, inodeDir); // Calls same method again.
	        	}
		        deleteDir(file, d);
	        } else {
	        	deleteFile(file, d);
	        }
	    }
		int oldDir = d.popDir();
		d.setDirectory(oldDir);
	}

	/**
	 * Removes target file from current directory and disk 
	 * (works exactly like the RemoveFileProcessor in the SystemCommandsProcessor class)
	 * @param name name of the target file to remove
	 * @param d an instance of the current disk 
	 *   
	**/
	public void deleteFile(String name, DiskUnit d){
	    
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
	
		while (bn != 0) { //find and erase from directory
			d.read(bn, vdb);  
			nme=util.stringFromVirtualDiskBlock(bn, vdb);
			next= util.getNextBNFromBlock(vdb);
			
			if(nme.equals(name) && bn==directoryFirst && next==0){
				inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
				d.registerFB(bn);
				d.setInode(d.getcurrentDir(), 1, 0);
				break;
			}
			
			if(nme.equals(name)&& bn==directoryFirst){
				inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
				d.registerFB(bn);
				bn = util.getNextBNFromBlock(vdb);
				d.setInode(d.getcurrentDir(), sizeD-d.getBlockSize(), bn);
				break;
			}
			
			if(nme.equals(name)&& next==0){
				inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
				d.registerFB(bn);
				d.read(prev, vdb);
				util.copyNextBNToBlock(vdb, 0);
				d.write(prev, vdb);
				d.setInode(d.getcurrentDir(), sizeD-d.getBlockSize());
				break;
			}
			
			if(nme.equals(name)){
				inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
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
		
		int fileStart = d.getInodeFBlock(inodeIndex); //!!!!!!!!
		int fileStart2 = fileStart;
		d.registerFB(fileStart);
		
		int counter=d.getBlockSize()-20;
		int b2=0;
		int b1=5;
		d.read(fileStart, vdm);
		
		while (fileStart2!=0) {//finds file on disk using inode
			b2++;
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
		
		d.setInode(inodeIndex, 1, 1);
		d.setInodeToDirectory(d.getcurrentDir());
	}
	
	/**
	 * Removes target directory from current directory and disk 
	 * (works exactly like the RemoveDirectoryProcessor in the SystemCommandsProcessor class)
	 * @param name name of the target directory to remove
	 * @param d an instance of the current disk 
	 *   
	**/
	public void deleteDir(String name, DiskUnit d){
		VirtualDiskBlock vdb = new VirtualDiskBlock(d.getBlockSize());
	    Utils util = new Utils();
		String nme = "";
		int directoryFirst = d.getInodeFBlock(d.getcurrentDir());
		int inodeIndex = this.findFile(name, d);
		int prev=0;
		int next;
		int prevInode;
	    int sizeD= d.getInodeSize(d.getcurrentDir());
		int bn = d.getInodeFBlock(d.getcurrentDir()); //finds file on directory
		
		while (bn != 0) { //find and erase from directory
			d.read(bn, vdb);  
			nme=util.stringFromVirtualDiskBlock(bn, vdb);
			next= util.getNextBNFromBlock(vdb);
			
			if(nme.equals(name) && bn==directoryFirst && next==0){
				inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
				d.registerFB(bn);
				d.setInode(d.getcurrentDir(), 1, 0);
				break;
			}
			
			if(nme.equals(name)&& bn==directoryFirst){
				inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
				d.registerFB(bn);
				bn = util.getNextBNFromBlock(vdb);
				d.setInode(d.getcurrentDir(), sizeD-d.getBlockSize(), bn);
				break;
			}
			
			if(nme.equals(name)&& next==0){
				inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
				d.registerFB(bn);
				d.read(prev, vdb);
				util.copyNextBNToBlock(vdb, 0);
				d.write(prev, vdb);
				d.setInode(d.getcurrentDir(), sizeD-d.getBlockSize());
				break;
			}
			
			if(nme.equals(name)){
				inodeIndex = util.getIntFromBlock(vdb, (d.getBlockSize()-8));
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
		
		d.setInode(inodeIndex, 1, 1);
		d.setInodeToDirectory(d.getcurrentDir());
	}
	
	/** 
	 * Verifies if target is file or directory
     * @return true if file is directory, false otherwise
	 * @param name name of the target to determine if it is directory or file
	 * @param d an instance of the current disk 
	**/
	public boolean isDirectory(String name, DiskUnit d){
		int inodeIndex=this.findFile(name, d);
		if(d.getInodeType(inodeIndex)==0){
			return true;
		}
		return false;
	}
	
	/** 
	 * Finds target directory or file inode index
     * @return target directory or file inode index, return -1 if file is not found
	 * @param name name of the target to find
	 * @param d an instance of the current disk 
	**/
	public int findFile(String name, DiskUnit d){
		
		int bn = d.getInodeFBlock(d.getcurrentDir());
		int inodeIndex=0;	
		VirtualDiskBlock vdb = new VirtualDiskBlock(d.getBlockSize());
	    String nme = "";
	    Utils util = new Utils();
	    
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
			return -1;
		}
		
		return inodeIndex;
	}
	
	/** 
	 * Creates an  array list with every name that the directory provided contains
     * @return array list with every content of the directory
	 * @param inodeIndex inode index of the directory from which the list will be created
	 * @param d an instance of the current disk 
	**/
	public ArrayList<String> createList(int inodeIndex,  DiskUnit d){
		
		ArrayList<String> list = new ArrayList<String>();
		VirtualDiskBlock vdb = new VirtualDiskBlock(d.getBlockSize());
		int bn = d.getInodeFBlock(inodeIndex);	
	    String nme = "";
	    Utils util = new Utils();
	    
		while (bn != 0) { //finds file in directory
			d.read(bn, vdb); 
			nme=util.stringFromVirtualDiskBlock(bn, vdb);
			list.add(nme);
			bn = util.getNextBNFromBlock(vdb);
		}
		return list;
	}

}
