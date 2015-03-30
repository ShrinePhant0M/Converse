package edu.lehigh.converse.conversation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;

import com.dropbox.sync.android.DbxFile;

public class Conversation implements Serializable
	{
		private static final long	serialVersionUID	= 7750003663437734114L;

		private ArrayList<CNode>	nodes;
		private CNode				startingNode;
		private boolean				compFirst;
		private String language;
//		private String				name;

		private Map<CNode, DbxFile>	imageFiles;
		private Map<CNode, DbxFile>	audioFiles;

//	private transient static DbxFileSystem fs;
//
//	
//	static
//	{
//		try
//			{
//				fs = DbxFileSystem.forAccount(MainActivity.getDropboxManager().getLinkedAccount());
//			}
//		catch(Unauthorized e)
//			{
//				e.printStackTrace();
//			}
//	}

		public Conversation()
			{
				this(true);
			}

		public Conversation(boolean compFirst)
			{
				nodes = new ArrayList<CNode>();
				this.compFirst = compFirst;
				imageFiles = new IdentityHashMap<CNode, DbxFile>();
				audioFiles = new IdentityHashMap<CNode, DbxFile>();
			}

//		public void setName(String name)
//			{
//				this.name = name;
//			}
//		public String getName()
//			{
//				return name;
//			}

		public void addNode(CNode node)
			{
				nodes.add(node);
				imageFiles.put(node, null);
				audioFiles.put(node, null);

				// If we don't have a starting node, make it this item just so we have one
				if(startingNode == null)
					startingNode = node;
			}

		public void setStartingNode(CNode node)
			{
				this.startingNode = node;
			}

		public CNode getStartingNode()
			{
				return this.startingNode;
			}

		public boolean removeNode(CNode node)
			{
				return nodes.remove(node);
			}

		public CNode getNode(int position)
			{
				if(position >= size())
					{
						return null;
					}
				return nodes.get(position);
			}

		public CNode getNodeWithId(int id)
			{
				for(CNode n : nodes)
					if(n.getId() == id)
						return n;

				return null;
			}

		public int size()
			{
				return nodes.size();
			}

		public int getNextNodeId()
			{
				int highest = 0;
				for(CNode n : nodes)
					if(n.getId() > highest)
						highest = n.getId();
				return highest + 1;
			}

		/**
		 * Gives a reference to the list of nodes this conversation holds.
		 * 
		 * @return Returns a REFERENCE to this conversations node list. Changes made to the reference WILL be reflected
		 *         in the conversation's copy.
		 */
		public ArrayList<CNode> getNodes()
			{
				return nodes;
			}

		public boolean isCompFirst()
			{
				return this.compFirst;
			}
		
		public void setLanguage(Locale locale)
			{
				this.language = locale.getLanguage();
			}
		
		public Locale getLanguage()
			{
				return new Locale(language);
			}

	}
