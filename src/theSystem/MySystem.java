/**
 * 
 */
package theSystem;

import java.io.File;
import java.io.IOException;

import systemGeneralClasses.SystemController;

/**
 * Main class 
 * @author Israel J.Lopez Toledo
 *
 */
public class MySystem {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException  {
		createTextFile();
		SystemController system = new SystemController(); 
		system.start(); 
		// the system is shutting down...
		System.out.println("+++++ SYSTEM SHUTDOWN +++++"); 
	}
	
	/**
	 * Creates text file and directory for the list of disks at start of program
	 * @throws IOException
	 */
	public static void createTextFile() throws IOException{
		File file1 = new File ("DiskUnits");
		file1.mkdir();
		File file = new File("DiskUnits/DiskNames.txt");
		//Create the file
		file.createNewFile();
	}
	

}
