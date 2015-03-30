package edu.lehigh.converse.conversation;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class CNode implements Serializable, Parcelable, ExclusionStrategy
	{
		private static final long	serialVersionUID	= -8407445654026763173L;

		private int					id;
		private String				prompt;
		private ArrayList<Response>	responses;
		private transient DbxFile	audio, image;
//		private transient DbxPath	baseFolder;
		private String				audioPath, imagePath;

		public CNode(int id, String prompt)
			{
				this.id = id;
				this.prompt = prompt;
				responses = new ArrayList<Response>();
			}

		public void copyValues(CNode node)
			{
				this.id = node.id;
				this.prompt = node.prompt;
				this.responses = node.responses;
			}

		public void addResponse(Response response)
			{
				responses.add(response);
			}

		public void addResponse(String response, int toId)
			{
				this.addResponse(new Response(response, toId));
			}

		public void setImageFile(DbxPath basePath, DbxFileSystem fs, Bitmap file)
			{
				try
					{
						String base = basePath.toString();
						if(!base.endsWith("_data/"))
							base += "_data/";
						else if(!base.endsWith("/"))
							base += "/";
//						baseFolder = new DbxPath(base);
//					BufferedReader br = new BufferedReader(new FileReader(file));
						if(image == null && fs.exists(new DbxPath(base + "image_" + id)))
							{
								image = fs.open(new DbxPath(base + "image_" + id));
							}
						else
							{
								image = fs.create(new DbxPath(base + "image_" + id));
							}
						FileOutputStream imageFOS = image.getWriteStream();
						PrintWriter pw = new PrintWriter(imageFOS);
//					while(br.ready())
//						pw.write(br.readLine());
						file.compress(Bitmap.CompressFormat.PNG, 85, imageFOS);
						pw.close();
//					br.close();
//					imagePath = image.getPath().toString();
						imagePath = new DbxPath(base + "image_" + id).toString();
						image.close();
					}
				catch(FileNotFoundException e)
					{
						e.printStackTrace();
					}
				catch(InvalidPathException e)
					{
						e.printStackTrace();
					}
				catch(DbxException e)
					{
						e.printStackTrace();
					}
				catch(IOException e)
					{
						e.printStackTrace();
					}
			}

		public void setAudioFile(DbxPath basePath, DbxFileSystem fs, InputStream selectedFile)
			{
				try
					{
						String base = basePath.toString();
						while(base.endsWith("/"))
							base = base.substring(0, base.length() - 1);
						if(!base.endsWith("_data/"))
							base += "_data/";
						if(!base.endsWith("/"))
							base += "/";
//						baseFolder = new DbxPath(base);
//						BufferedReader br = new BufferedReader(new InputStreamReader(selectedFile));
						if(audio == null && fs.exists(new DbxPath(base + "audio_" + id + ".wav")))
							{
								audio = fs.open(new DbxPath(base + "audio_" + id + ".wav"));
							}
						else
							{
								audio = fs.create(new DbxPath(base + "audio_" + id + ".wav"));
							}
						FileOutputStream audioFOS = audio.getWriteStream();
//						PrintWriter pw = new PrintWriter(audioFOS);
						byte[] buf = new byte[8092];
						int read = 0;
						while((read = selectedFile.read(buf)) != -1)
							audioFOS.write(buf, 0, read);
						audioFOS.close();
						selectedFile.close();
//						pw.close();
//						br.close();
						audioPath = audio.getPath().toString();
						audio.close();
					}
				catch(FileNotFoundException e)
					{
						e.printStackTrace();
					}
				catch(InvalidPathException e)
					{
						e.printStackTrace();
					}
				catch(DbxException e)
					{
						e.printStackTrace();
					}
				catch(IOException e)
					{
						e.printStackTrace();
					}
			}

//	public DbxPath getAudioFile()
//		{
//			return new DbxPath(audioPath);
//		}
//	public DbxPath getImageFile()
//		{
//			return new DbxPath(imagePath);
//		}
		public void setAudioPathWithinDropbox(DbxPath path)
			{
				this.audioPath = path.toString();
			}
		public void setImagePathWithinDropbox(DbxPath path)
			{
				this.imagePath = path.toString();
			}
		public String getAudioPath()
			{
				return audioPath;
			}

		public String getImagePath()
			{
				return imagePath;
			}

		public int getId()
			{
				return id;
			}

		public void setPrompt(String prompt)
			{
				this.prompt = prompt;
			}

		public String getPrompt()
			{
				return prompt;
			}

		public ArrayList<Response> getResponses()
			{
				return responses;
			}

		public Response getResponse(int index)
			{
				return responses.get(index);
			}

		public int numResponses()
			{
				return this.responses.size();
			}

		public void clearResponses()
			{
				responses.clear();
			}

		@Override
		public String toString()
			{
				return "ID: " + id + "  |  Prompt: " + prompt;
			}

		@Override
		public boolean equals(Object o)
			{
				if(o instanceof CNode)
					{
						CNode n = (CNode) o;
						return n.id == this.id && n.responses.size() == this.responses.size();
					}
				return false;
			}

		@Override
		public int describeContents()
			{
				return 0;
			}

		@Override
		public void writeToParcel(Parcel arg0, int arg1)
			{
				arg0.writeInt(id);
				arg0.writeString(prompt);
				arg0.writeTypedList(responses);
				arg0.writeString(audioPath);
				arg0.writeString(imagePath);
			}

		public static final Parcelable.Creator<CNode>	CREATOR	= new Parcelable.Creator<CNode>()
																	{

																		@Override
																		public CNode createFromParcel(Parcel source)
																			{
																				return new CNode(source);
																			}

																		@Override
																		public CNode[] newArray(int size)
																			{
																				return new CNode[size];
																			}

																	};
			{
			};

		private CNode(Parcel p)
			{
				id = p.readInt();
				prompt = p.readString();
				responses = new ArrayList<Response>();
				p.readTypedList(responses, Response.CREATOR);
//				responses = p.readArrayList(Response.class.getClassLoader());
				audioPath = p.readString();
				imagePath = p.readString();
			}

		@Override
		public boolean shouldSkipClass(Class<?> arg0)
			{
				if(arg0.equals(DbxFile.class) || arg0.equals(DbxPath.class))
					return true;
				return false;
			}

		@Override
		public boolean shouldSkipField(FieldAttributes f)
			{
				if(f.getClass().getName().matches("Dbx.+"))
					return true;
				return false;
			}

	}
