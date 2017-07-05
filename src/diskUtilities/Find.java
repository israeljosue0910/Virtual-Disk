package diskUtilities;

import java.util.ArrayList;


/**
 * Manages the necessary methods to find every instance of the name in the disk
 * @author Israel J.Lopez Toledo
 *
 */
public class Find {

	public Find() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Travels recursively through every non empty directory. First it verifies if content of directory is 
	 * file or directory. If it is a directory it verifies it verifies if name matches target name, if it matches it adds the path name to list. 
	 * Then it verifies if it is empty, if it is empty it simply, goes on to the next element if it is not empty 
	 * the method calls itself. If it is a file it simply verifies it verifies if name matches target name, 
	 * if it matches it adds the path name to list and goes on to next element (uses stacks to move through directory).
	 * @return a list of every instance of the name it is looking for
	 * @param files an array list with the content of the current directory the method is verifying
	 * @param d an instance of the current disk 
	 * @param currentDir inode index of the previous directory 
	 * @param name target name method is looking for
	 * @param oldName previous path name
	 * @param names list of every instance of the name
	 *   
	**/
	public ArrayList<String> recFiles(ArrayList<String> files, DiskUnit d, int currentDir, String name, String oldName, ArrayList<String> names) {
    	oldName=oldName+"/";
    	String fileLocation="";
		for (String file : files) {
	        if (isDirectory(file, d, currentDir)) {
	        	if(file.equals(name)){
	        		fileLocation=oldName+file;
	        		names.add(fileLocation);
	        	}
	        	int inode = this.findFile(file, d, currentDir);
	        	if(d.getInodeSize(inode)>1){
		    		d.pushDir(currentDir);
		        	d.setDirectory(inode);
		        	if(oldName.charAt(oldName.length()-1)!='/'){
		        		oldName=oldName+"/"+file;
		        	}
		        	else{
		        		oldName=oldName+file;
		        	}
		            recFiles(this.createList(inode, d),d, inode, name, oldName, names); // Calls same method again.
		    		int oldDir = d.popDir();
		    		d.setDirectory(oldDir);
	        	}
	        } else {
	        	if(file.equals(name)){
	        		fileLocation=oldName+file;
	        		names.add(fileLocation);
	        	}
	        }
			
	    }
		return names;
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
	
	/** 
	 * Finds target directory or file inode index
     * @return target directory or file inode index, return -1 if file is not found
	 * @param name name of the target to find
	 * @param d an instance of the current disk
	 * @param current inode index of current directory 
	**/
	public int findFile(String name, DiskUnit d, int current){
		
		int bn = d.getInodeFBlock(current);
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
	 * Verifies if target is file or directory
     * @return true if file is directory, false otherwise
	 * @param name name of the target to determine if it is directory or file
	 * @param d an instance of the current disk 
	 * @param current inode index of current directory 
	**/
	public boolean isDirectory(String name, DiskUnit d, int current){
		int inodeIndex=this.findFile(name, d, current);
		if(d.getInodeType(inodeIndex)==0){
			return true;
		}
		return false;
	}
}
