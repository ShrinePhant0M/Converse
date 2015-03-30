package edu.lehigh.converse;

import com.dropbox.sync.android.DbxPath;

public class DropboxFileHolder
	{

		private final DbxPath file;
		private final String filename;
//		private final DbxPath audio, image;
		
		public DropboxFileHolder(DbxPath file, String filename)
		{
			this.file = file;
			this.filename = filename;
//			this.audio = audio;
//			this.image = image;
		}
		
		public DbxPath getFile()
		{
			 return this.file;
		}
		
		public String getFilename()
		{
			return this.filename;
		}
		
//		public DbxPath getAudioFile()
//			{
//				return audio;
//			}
//		public DbxPath getImageFile()
//			{
//				return image;
//			}
		
		@Override
		public String toString()
		{
			return this.filename;
		}

	}
