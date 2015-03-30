package edu.lehigh.converse;
/**
 * Aids in the building of XML files.
 * Has methods to add start tags, end tags,
 * and attributes.
 * 
 * Note: This class was created by me last semester
 * 		 for Professor Spear's Android class.
 * 
 * @author Greyson Parrelli (gkp214)
 * @version Java 1.7, Windows 7
 */


public class XMLBuilder
{
	StringBuilder output;

	/**
	 * Creates a new XML file with the XML declaration.
	 */
	public XMLBuilder()
	{
		this("");
	}

	/**
	 * Creates a new XML file with the previous contents of an existing file.
	 * This will not create a new header tag.
	 */
	public XMLBuilder(String prevContents)
	{
		this(prevContents, false);
	}

	/**
	 * Creates a new XML file with the previous contents specified. If desired,
	 * it will append a new header tag if there are no previous contents.
	 * @param prevContents
	 * 			The previous contents of a started XML file.
	 * @param header
	 * 			Whether or not you want to add an XML header tag.
	 */
	public XMLBuilder(String prevContents, boolean header)
	{
		// Initialize our output StringBuilder
		output = new StringBuilder();

		// If we have no previous contents, add a header.
		// Otherwise, add the contents and assume the header was present
		if (header && (prevContents == null || prevContents.equals("")))
			output.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		else
			output.append(prevContents);
	}

	/**
	 * Adds an item to the XML file that has no children.
	 * 
	 * @param indent
	 *            How many indents to add before the item.
	 * @param tag
	 *            The name of the tag
	 * @param contents
	 *            What should appear between the tags.
	 * @param attributes
	 *            A list of String[2]'s. The [0] is the name of the attribute
	 *            and the [1] is the value of the attribute.
	 */
	public void addChildlessItem(int indent, String tag, String contents, String[]... attributes)
	{
		// We should have a new line before every start tag
		newLine();

		// Add the appropriate number of indents
		for (int i = 0; i < indent; i++)
			output.append("\t");

		// Start out tag
		output.append("<" + tag);

		// Add all of the listed attributes
		for (String[] a : attributes)
		{
			output.append(" " + a[0] + "=\"" + a[1] + "\"");
		}

		// Close the start tag, add our contents, add the end tag
		// if (contents == null || contents.equals(""))
		// output.append(" />");
		// else
		if (contents == null)
			contents = "";
		output.append(">" + contents + "</" + tag + ">");
	}

	/**
	 * Starts an XML tag
	 * 
	 * @param indent
	 *            How many indents to add before the item.
	 * @param tag
	 *            The name of the tag
	 * @param attributes
	 *            A list of String[2]'s. The [0] is the name of the attribute
	 *            and the [1] is the value of the attribute.
	 */
	public void startTag(int indent, String tag, String[]... attributes)
	{
		// We should have a new line before every start tag
		newLine();

		// Add the appropriate number of indents
		for (int i = 0; i < indent; i++)
			output.append("\t");

		// Start out tag
		output.append("<" + tag);

		// Add all of the listed attributes
		for (String[] a : attributes)
		{
			output.append(" " + a[0] + "=\"" + a[1] + "\"");
		}

		// Close the start tag
		output.append(">");
	}

	/**
	 * Ends an XML tag.
	 * 
	 * @param indent
	 *            How many indents to add before the item.
	 * @param tag
	 *            The name of the tag.
	 */
	public void endTag(int indent, String tag)
	{
		// We should have a new line before every start tag
		newLine();

		// Add the appropriate number of indents
		for (int i = 0; i < indent; i++)
			output.append("\t");

		// Append the tag
		output.append("</" + tag + ">");
	}

	/**
	 * Short-cut to add a new line to the XML file
	 */
	public void newLine()
	{
		output.append("\n");
	}

	@Override
	public String toString()
	{
		return output.toString();
	}

}
