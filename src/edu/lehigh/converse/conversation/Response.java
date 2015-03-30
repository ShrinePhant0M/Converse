package edu.lehigh.converse.conversation;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class Response implements Serializable, Parcelable
{
	private static final long serialVersionUID = -1679078314938375949L;
	
	private String response;
	private int id;
	
	public Response(String response, int id) 
	{
		this.response = response;
		this.id = id;
	}

	public String getResponse() { return response; }
	
	public int getId() { return id; }
	
	@Override
	public String toString()
	{
		return "Response: " + response + " :: " + id;
	}

	@Override
	public int describeContents()
		{
			return 0;
		}

	@Override
	public void writeToParcel(Parcel parcel, int i)
		{
			parcel.writeString(response);
			parcel.writeInt(id);
		}
	public static final Parcelable.Creator<Response> CREATOR = new Parcelable.Creator<Response>()
				{

					@Override
					public Response createFromParcel(Parcel parcel)
						{
							return new Response(parcel);
						}

					@Override
					public Response[] newArray(int i)
						{
							return new Response[i];
						}
					
				};
		{
		};
		private Response(Parcel p)
			{
				response = p.readString();
				id = p.readInt();
			}
}
