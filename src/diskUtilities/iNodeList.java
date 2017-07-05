package diskUtilities;
/**
 * Objects from this class hold a list of inodes.
 * @author Israel J.Lopez Toledo
 *
 */
public class iNodeList {
	
	iNode[] nodeList;
	int length;
	int size=0;
	
	/** 
	 * Initializes the list of inode
	 * @param numNode number of inodes
	*/
	public iNodeList( int numNode){
		length = numNode;
		nodeList = new iNode[length];
	}
	
	/**
	 * adds node to list
	 * @param node adds node to list
	 */
	public void add(iNode node){
		if(size<0 || size >= length)
			System.out.println("No iNode available");
		nodeList[size] = node;
		size++;
	}
	
	/**
	 * returns specified node
	 * @param index index of node
	 * @return specified node
	 */
	public iNode getNode(int index){
		return nodeList[index];
	}
	
	/**
	 * @return returns complete list
	 */
	public iNode[] getList(){
		return nodeList;
	}
}
