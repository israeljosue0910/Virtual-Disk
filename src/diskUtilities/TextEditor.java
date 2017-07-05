package diskUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Methods for file manipulation
 * @author Israel J.Lopez Toledo
 *
 */
public class TextEditor {
	public TextEditor(){
		
	}
	
	/**
	 * Converts a file to string
	 * @param name name of file
	 * @return returns an string of the specified file
	 * @throws IOException
	 */
	public static String fileToSTring(String name) throws IOException{
		BufferedReader text = new BufferedReader(new FileReader("Files/"+name));
		String file = "", line;
		while ((line = text.readLine()) != null) {
			file += line;
		}
		text.close();
		return file;
	}
	/**
	 * Writes strings to a text file, used to maintain the list of disks available
	 * @param name files name
	 * @throws IOException
	 */
	public static void writeNewText(String name) throws IOException{
		ArrayList<String> list = currentDiskList();
		list.add(name);
		try{
		    PrintWriter writer = new PrintWriter("DiskUnits/DiskNames.txt", "UTF-8");
		    for(int i=0; i<list.size(); i++){
		    	writer.write(list.get(i)+"\n");
		    }
		    writer.close();
		    list=currentDiskList();
		} catch (IOException e) {
		   // do something
		}
	}
	
	/**
	 * @return returns a list of current available disks extracted from the text file DisksNames and 
	 * maintains this list
	 * @throws IOException
	 */
	public static ArrayList<String> currentDiskList() throws IOException{
		BufferedReader in = new BufferedReader(new FileReader("DiskUnits/DiskNames.txt"));
		String str;
		ArrayList<String> list = new ArrayList<String>();
		while((str = in.readLine()) != null){
		    list.add(str);
		}
		in.close();
		return list;
	 }
	
	/**
	 * Removes a disk from the file and maintains the current list of disks
	 * @param name name of the disk to remove
	 * @throws IOException
	 */
	public static void remove(String name) throws IOException{
		ArrayList<String> newlist = currentDiskList();
		String etr=name;
		for(int i=0; i<newlist.size();i++){
			if(etr.equals(newlist.get(i))){
				newlist.remove(i);
			}
		}
		try{
		    PrintWriter writer = new PrintWriter("DiskUnits/DiskNames.txt", "UTF-8");
		    for(int i=0; i<newlist.size(); i++){
		    	writer.write(newlist.get(i)+"\n");
		    }
		    writer.close();
		    newlist=currentDiskList();

		} catch (IOException e) {
		   // do something
		}
		
	}
}
