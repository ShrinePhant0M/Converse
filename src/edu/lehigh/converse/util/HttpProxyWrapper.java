package edu.lehigh.converse.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Random;

import javax.net.SocketFactory;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.Uri;
import android.util.Log;

public class HttpProxyWrapper
	{
		private static final String	SOCKET_NAME	= "/test/matt/testSocket";
		private LocalSocket			ls;
		private LocalServerSocket	lss;
		private Random r = new Random();
		private static final int RETRIES = 15;
		private int port = 0;
		private Socket socket = null;

		public HttpProxyWrapper(InputStream is)
			{
				try
					{
						for(int i = 0; i < RETRIES && (socket == null || !socket.isConnected()); i++)
							{
								port = r.nextInt(65535);
								socket = SocketFactory.getDefault().createSocket("127.0.0.1", port);
							}
						Log.w("HTTPProxyWrapper", "Successfully connected to localhost on port: " + port);
						final OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
						final BufferedReader br = new BufferedReader(new InputStreamReader(is));
						new Thread(new Runnable()
							{
								@Override
								public void run()
									{
										try
											{
												char[] buf = new char[512];
												while(br.ready())
													{
														br.read(buf);
														osw.write(buf);
													}
											}
										catch(IOException e)
											{
												e.printStackTrace();
											}
										finally
										{
											try
												{
													br.close();
													osw.close();
												}
											catch(IOException e)
												{
													e.printStackTrace();
												}
										}
									}
							}).start();
					}
				catch(UnknownHostException e2)
					{
						e2.printStackTrace();
					}
				catch(IOException e2)
					{
						e2.printStackTrace();
					}
				
				

				/*try
					{

						final LocalServerSocket lss = new LocalServerSocket(SOCKET_NAME);
						this.lss = lss;
						ls = new LocalSocket();
						new Thread(new Runnable()
							{
								@Override
								public void run()
									{
										try
											{
												lss.accept();
											}
										catch(IOException e)
											{
												e.printStackTrace();
											}
									}
							}).start();
						ls.connect(lss.getLocalSocketAddress());
					}
				catch(IOException e)
					{
						e.printStackTrace();
					}
				Log.w("MYTAG", lss.getFileDescriptor().toString());
				Log.w("MYTAG", lss.getLocalSocketAddress().getName());
				InputStreamReader br = new InputStreamReader(is);
				OutputStreamWriter writer = null;
				try
					{
						writer = new OutputStreamWriter(ls.getOutputStream());
						char[] buf = new char[1024];
						while(br.ready())
							{
								int read = br.read(buf);
								writer.write(buf, 0, read);
							}
						writer.close();
						br.close();
					}
				catch(IOException e1)
					{
						e1.printStackTrace();
					}
				InputStreamReader mir = null;
				try
					{
						mir = new InputStreamReader(ls.getInputStream());
					}
				catch(IOException e)
					{
						e.printStackTrace();
					}
				try
					{
						StringBuilder sb = new StringBuilder();
						while(mir.ready())
							{
								sb.append((char) mir.read());
							}
						Log.w("MYTAG", sb.toString());
					}
				catch(IOException e)
					{
						e.printStackTrace();
					}
*/
			}

		public Uri getUri()
			{
				Uri uri = Uri.parse(Uri.encode("http://127.0.0.1:" + port));
				return uri;
			}
		public URL getURL()
			{
				try
					{
						return new URL(String.format("http://127.0.0.1:%d", port));
					}
				catch(MalformedURLException e)
					{
						e.printStackTrace();
					}
				return null;
			}
		
		public int getPort()
			{
				return port;
			}

		public static InputStream getStream(final CharSequence charSequence)
			{
				return new InputStream()
					{
						int	index	= 0;
						int	length	= charSequence.length();

						@Override
						public int read() throws IOException
							{
								return index >= length ? -1 : charSequence.charAt(index++);
							}
					};
			}

	}
