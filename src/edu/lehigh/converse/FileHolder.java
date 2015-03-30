package edu.lehigh.converse;

import java.io.File;

public class FileHolder
{
	private File file;
	private String filename;
	
	public FileHolder(File file, String filename)
	{
		this.file = file;
		this.filename = filename;
	}
	
	public File getFile()
	{
		 return this.file;
	}
	
	public String getFilename()
	{
		return this.filename;
	}
	
	@Override
	public String toString()
	{
		return this.filename;
	}

}
