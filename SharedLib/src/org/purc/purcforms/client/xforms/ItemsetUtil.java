package org.purc.purcforms.client.xforms;


/**
 * Utility methods used when manipulating dynamic options portions
 * of xforms documents, which are essentially itemsets.
 * 
 * @author daniel
 *
 */
public class ItemsetUtil {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private ItemsetUtil(){

	}
	
	
	/**
	 * Gets the dynamic option definition object child's instance id from its nodeset.
	 * E.g for: 
	 * <xf:itemset nodeset="instance('question4')/item[@parent=instance('newform1')/question3]">
	 * will return question4
	 * 
	 * @param nodeset the nodeset.
	 * @return the child instance id.
	 */
	public static String getChildInstanceId(String nodeset){
		if(nodeset == null)
			return null;

		//Get the position of the first ' character.
		int pos1 = nodeset.indexOf('\'');
		if(pos1 < 0)
			return null;

		//Get the position of the next ' character.
		int pos2 = nodeset.indexOf('\'', pos1 + 1);
		if(pos2 < 0 || (pos1 == pos2))
			return null;

		//Return the value between the two ' characters.
		return nodeset.substring(pos1 + 1, pos2);
	}

	
	/**
	 * Gets the dynamic option definition object parent question's bind id from its nodeset.
	 * E.g for: 
	 * <xf:itemset nodeset="instance('question4')/item[@parent=instance('newform1')/question3]">
	 * will return question3
	 * 
	 * @param nodeset the nodeset.
	 * @return the parent instance id.
	 */
	public static String getParentQuestionBindId(String nodeset){
		if(nodeset == null)
			return null;

		//Get the position of the last / character.
		int pos1 = nodeset.lastIndexOf('/');
		if(pos1 < 0)
			return null;

		//Get the position of the first ] character.
		int pos2 = nodeset.lastIndexOf(']');
		if(pos2 < 0 || (pos1 == pos2))
			return null;

		//Return the value between the / and ] characters.
		return nodeset.substring(pos1 + 1, pos2);
	}

	/**
	 * Gets the dynamic option definition object's form instance id from its nodeset.
	 * E.g for:
	 * <xf:itemset nodeset="instance('question4')/item[@parent=instance('newform1')/question3]">
	 * will return newform1
	 * 
	 * @param nodeset the nodeset.
	 * @return the instance id.
	 */
	public static String getFormInstanceId(String nodeset){
		if(nodeset == null)
			return null;

		//Get the position of the ')/item[@parent=instance(' token
		String token = "\')/item[@parent=instance(\'";
		int pos1 = nodeset.indexOf(token);
		if(pos1 < 0)
			return null;

		//Get the position of the ')/ token after the above token.
		int pos2 = nodeset.indexOf("\')/", pos1 + token.length());
		if(pos2 < 0 || (pos1 == pos2))
			return null;

		//Return the value between the two above tokens.
		return nodeset.substring(pos1+token.length(), pos2);
	}
}
