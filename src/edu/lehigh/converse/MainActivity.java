package edu.lehigh.converse;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.lehigh.converse.conversation.Conversation;
import edu.lehigh.converse.conversationbuilder.ConversationBuilderMenuActivity;
import edu.lehigh.converse.util.DropboxUtils;

public class MainActivity extends Activity
	{
		private static DbxAccountManager	dropboxMgr;
		private static DbxFileSystem		dbxFS;
		private static final int			dbxRequestCode	= 1865;

		@Override
		public void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_main);
				setVolumeControlStream(AudioManager.STREAM_MUSIC);
				firstRunInstall();
				final AtomicReference<AlertDialog> dialog = new AtomicReference<AlertDialog>();
				LinearLayout choices = new LinearLayout(this);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.setMargins(50, 50, 50, 50);
				choices.setOrientation(LinearLayout.VERTICAL);
				choices.setLayoutParams(params);
				Button english = new Button(this);
				english.setText("English (英语)");
				english.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								Locale.setDefault(Locale.ENGLISH);
								Configuration config = new Configuration();
								config.locale = Locale.ENGLISH;
								getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
								AlertDialog ad = dialog.getAndSet(null);
								if(ad != null)
									ad.dismiss();
								Intent refresh = new Intent(MainActivity.this, MainActivity.class);
								refresh.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								refresh.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								refresh.putExtra("hasSetLanguage", true);
//								finish();
								startActivity(refresh);
								finish();
							}
					});
				Button chinese = new Button(this);
				chinese.setText("汉语 (Chinese)");
				chinese.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
								Configuration config = new Configuration();
								config.locale = Locale.SIMPLIFIED_CHINESE;
								getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
								AlertDialog ad = dialog.getAndSet(null);
								if(ad != null)
									ad.dismiss();
								Intent refresh = new Intent(MainActivity.this, MainActivity.class);
								refresh.putExtra("hasSetLanguage", true);
								refresh.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								refresh.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(refresh);
								finish();
							}
					});
				choices.addView(english);
				choices.addView(chinese);
				if(!getIntent().hasExtra("hasSetLanguage"))
					{
						dialog.set(new AlertDialog.Builder(this).setTitle("Select a language (选语言)").setView(choices).setCancelable(false).show());
					}
			}

		public void playConversationClick(View v)
			{
				Intent i = new Intent(this, DifficultyChooserActivity.class);
				this.startActivity(i);
				overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			}

		public void conversationBuilderClick(View v)
			{
				Intent i = new Intent(this, ConversationBuilderMenuActivity.class);
				this.startActivity(i);
				overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			}

		private void firstRunInstall()
			{
				SharedPreferences prefs = this.getSharedPreferences("ConversePrefs", 0);
				dropboxMgr = DbxAccountManager.getInstance(getApplicationContext(), "weur18iwxph2io4", "hmpab3kjyiw0gz0");
				if(dropboxMgr.hasLinkedAccount())
					{
						((Button) findViewById(R.id.button0)).setText(R.string.change_linked_dropbox_account);
						((Button) findViewById(R.id.button1)).setEnabled(true);
						((Button) findViewById(R.id.button2)).setEnabled(true);

					}
				// if(prefs.getBoolean("firstRun", true))
				// {
				// copyDefaultFiles();
				// }
				// else
				// {
				SharedPreferences.Editor e = prefs.edit();
				e.putBoolean("firstRun", false);
				e.commit();
				// }
			}

		private void copyDefaultFiles()
			{
				GsonBuilder builder = new GsonBuilder();
				builder.addSerializationExclusionStrategy(new ExclusionStrategy()
					{
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
					});
				builder.addDeserializationExclusionStrategy(new ExclusionStrategy()
					{
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
					});
				Gson gson = builder.create();
				int[] ids;
//				if(Locale.getDefault().equals(Locale.SIMPLIFIED_CHINESE))
//					{
//						ids = new int[] { R.raw.telephone_computer_starts_zh, R.raw.telephone_player_starts_zh };
//					}
//				else
//					{
//						ids = new int[] { R.raw.telephone_computer_starts, R.raw.telephone_player_starts };
//					}
				ids = new int[]{R.raw.telephone_computer_starts, R.raw.telephone_player_starts, R.raw.telephone_computer_starts_zh, R.raw.telephone_player_starts_zh};

				String[] names = { "Telephone (Computer Starts)", "Telephone (Player Starts)", "电话 (玩家先开始)", "电话 (电脑先开始)" };
				
				if(!DropboxUtils.isConnectionAvailable(this))
					{
						Toast.makeText(this, "This app requires an active internet connection. Please try again.", Toast.LENGTH_LONG).show();
						return;
					}
				try
					{
						dbxFS = DbxFileSystem.forAccount(getDropboxManager().getLinkedAccount());
						dbxFS.awaitFirstSync();
					}
				catch(Unauthorized e)
					{
						e.printStackTrace();
					}
				catch(DbxException e)
					{
						e.printStackTrace();
					}
				for(int i = 0; i < ids.length; i++)
					{
						DbxPath dataFilePath = new DbxPath("/" + names[i]);
						DbxFile dataFile = null;
						try
							{
								if(!dbxFS.exists(dataFilePath))
									{
										try
											{
												dataFile = dbxFS.create(dataFilePath);
//												DbxPath dataFolder = new DbxPath(dataFilePath.toString());
												// Read file in
												BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(ids[i])));
												Conversation conversation = (Conversation) gson.fromJson(br, Conversation.class);
												conversation.getNode(0).setAudioFile(dataFilePath, dbxFS, getResources().openRawResource(R.raw.telephone_ring));
												conversation.getNode(0).setImageFile(dataFilePath, dbxFS, BitmapFactory.decodeStream(getResources().openRawResource(R.raw.telephone_icon)));
												// Write file out with new name
												PrintStream ps;
												// ps = newPrintStream(openFileOutput(names[id],Context.MODE_PRIVATE));
												ps = new PrintStream(dataFile.getWriteStream());
												ps.print(gson.toJson(conversation));
//												while(br.ready())
//													ps.print(br.readLine());
												ps.close();
											}
										catch(FileNotFoundException e)
											{
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										catch(IOException e)
											{
												e.printStackTrace();
											}
										finally
											{
												if(dataFile != null)
													dataFile.close();
											}
									}
							}
						catch(DbxException e)
							{
								e.printStackTrace();
							}
					}
				try
					{
						dbxFS.syncNowAndWait();
					}
				catch(DbxException e)
					{
						e.printStackTrace();
					}

				// for(int i = 0; i < ids.length; i++)
				// {
				//
				// }

			}

		public void setupDropbox(View view)
			{
				if(!dropboxMgr.hasLinkedAccount())
					{
						dropboxMgr.startLink(MainActivity.this, dbxRequestCode);
					}
				else
					{
						dropboxMgr.unlink();
						dropboxMgr.startLink(MainActivity.this, dbxRequestCode);
						// account = dropboxMgr.getLinkedAccount();
					}

			}

		public static DbxAccountManager getDropboxManager()
			{
				return dropboxMgr;
			}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent intent)
			{
				if(requestCode == dbxRequestCode)
					{
						// Toast.makeText(this, "requestCode is correct",
						// Toast.LENGTH_LONG).show();
						if(resultCode == Activity.RESULT_OK)
							{
								final ProgressDialog pd = ProgressDialog.show(this, getString(R.string.loading_resources), getString(R.string.please_wait_), true, false);
								new Thread(new Runnable()
									{

										@Override
										public void run()
											{
												copyDefaultFiles();
												runOnUiThread(new Runnable()
													{
														@Override
														public void run()
															{
																((Button) findViewById(R.id.button0)).setText(R.string.change_linked_dropbox_account);
																((Button) findViewById(R.id.button1)).setEnabled(true);
																((Button) findViewById(R.id.button2)).setEnabled(true);
																pd.dismiss();
															}
													});
											}

									}).start();

							}
						else
							{
								Toast.makeText(this, R.string.app_requires_dropbox, Toast.LENGTH_LONG).show();
								((Button) findViewById(R.id.button0)).setText(getString(R.string.btn_dropbox));
								((Button) findViewById(R.id.button1)).setEnabled(false);
								((Button) findViewById(R.id.button2)).setEnabled(false);
							}
					}
				else
					{
						super.onActivityResult(requestCode, resultCode, intent);
					}
			}

		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.activity_main_menu, menu);
				return true;
			}
	}
