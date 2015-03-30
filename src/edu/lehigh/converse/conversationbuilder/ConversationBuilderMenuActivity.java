package edu.lehigh.converse.conversationbuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;
import com.google.gson.Gson;

import edu.lehigh.converse.DropboxFileHolder;
import edu.lehigh.converse.MainActivity;
import edu.lehigh.converse.R;
import edu.lehigh.converse.conversation.Conversation;
import edu.lehigh.converse.util.DropboxUtils;

public class ConversationBuilderMenuActivity extends Activity
	{
		private DbxFileSystem	dbxFS;
		private ListView		listConvo;
		private int				pressedIndex;

		// =====================================
		// Overrides
		// =====================================

		@Override
		public void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_conversation_builder_menu);
				setVolumeControlStream(AudioManager.STREAM_MUSIC);
				try
					{
						dbxFS = DbxFileSystem.forAccount(MainActivity.getDropboxManager().getLinkedAccount());
					}
				catch(Unauthorized e)
					{
						e.printStackTrace();
					}
				listConvo = (ListView) findViewById(R.id.listConvo);
				initConversationList();
				registerForContextMenu(listConvo);
				listConvo.setOnItemClickListener(new ItemClick());
				listConvo.setOnItemLongClickListener(new ItemLongClick());
			}

		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				getMenuInflater().inflate(R.menu.menu_conversation_builder_menu, menu);
				return true;
			}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
			{
				if(v.getId() == R.id.listConvo)
					{
						menu.setHeaderTitle(R.string.options);
						String[] menuItems = getResources().getStringArray(R.array.convo_selector_values);
						for(int i = 0; i < menuItems.length; i++)
							menu.add(Menu.NONE, i, i, menuItems[i]);
					}
			}

		@Override
		public boolean onContextItemSelected(MenuItem item)
			{
//				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
				int menuItemIndex = item.getItemId();

				switch(menuItemIndex)
					{
						case 0:
							deleteConversationFile(pressedIndex);
							break;
						case 1:
							renameConversationFile(pressedIndex);
							break;
						case 2:
							duplicateConversationFile(pressedIndex);
					}
				return true;
			}

		@Override
		public void onRestart()
			{
				super.onRestart();
				initConversationList();
			}

		@Override
		public void onResume()
			{
				super.onResume();
				initConversationList();
			}

		@Override
		public void onBackPressed()
			{
				super.onBackPressed();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}

		// =====================================
		// Button Presses
		// =====================================

		public void addConversationClick(MenuItem v)
			{
				LayoutInflater inflater = getLayoutInflater();
				final View dialogLayout = inflater.inflate(R.layout.alert_new_conversation, null);

				AlertDialog alertDialog = new AlertDialog.Builder(this).create();
				alertDialog.setView(dialogLayout);
				alertDialog.setTitle(getString(R.string.new_conversation));

				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
							{
								EditText fileText = (EditText) dialogLayout.findViewById(R.id.textNewFilename);
								Spinner convoType = (Spinner) dialogLayout.findViewById(R.id.spinnerConvoType);
								Spinner language = (Spinner) dialogLayout.findViewById(R.id.spinnerLanguage);
								Locale languageLocale;
								String[] languages = getResources().getStringArray(R.array.supported_languages);
								if(language.getSelectedItem().equals(languages[0]))
									{
										languageLocale = Locale.ENGLISH;
									}
								else if(language.getSelectedItem().equals(languages[1]))
									{
										languageLocale = Locale.CHINESE;
									}
								else
									{
										throw new IllegalStateException("No support for selected language has been implemented.");
									}
								boolean compFirst = convoType.getSelectedItemPosition() == 0 ? true : false;
								startConversation(fileText.getText().toString() + (compFirst ? getString(R.string._computer_first_) : getString(R.string._player_first_)), true, compFirst, languageLocale);
							}
					});

				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
							{
								// Do Nothing
							}
					});

				alertDialog.show();
			}

		private class ItemClick implements OnItemClickListener
			{

				@Override
				public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
					{
						DropboxFileHolder holder = (DropboxFileHolder) adapter.getItemAtPosition(position);
						startConversation(holder.getFile().getName(), false);

					}
			}

		private class ItemLongClick implements OnItemLongClickListener
			{

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3)
					{
						pressedIndex = position;
						return false;
					}
			}

		// =====================================
		// Convenience Methods
		// =====================================

		private void initConversationList()
			{
				// File dir = getFilesDir();
				// File[] files = dir.listFiles();

				List<DbxFileInfo> fileInfo = DropboxUtils.findAllFiles(dbxFS, DbxPath.ROOT, ".*", false, false);
//				DropboxFileHolder[] holders = new DropboxFileHolder[fileInfo.size()];
				List<DropboxFileHolder> holders = new ArrayList<DropboxFileHolder>(fileInfo.size());
				for(DbxFileInfo info : fileInfo)
					{
						String filename = info.path.toString().substring(info.path.toString().lastIndexOf("/") + 1);
//						DbxPath audio = null, image = null;
//						try
//							{
//								if(dbxFS.exists(new DbxPath(info.path.toString() + "_data/")))
//									{
//										for(DbxFileInfo sInfo : dbxFS.listFolder(new DbxPath(info.path.toString() + "_data/")))
//											{
//												if(!sInfo.isFolder && sInfo.path.getName().matches("audio\\..+"))
//													{
//														audio = sInfo.path;
//													}
//												else if(!sInfo.isFolder && sInfo.path.getName().matches("image\\..+"))
//													{
//														image = sInfo.path;
//													}
//											}
//									}
//							}
//						catch(InvalidPathException e)
//							{
//								e.printStackTrace();
//							}
//						catch(DbxException e)
//							{
//								e.printStackTrace();
//							}
						holders.add(new DropboxFileHolder(info.path, filename));
					}

				ArrayAdapter<DropboxFileHolder> listAdapter = new ArrayAdapter<DropboxFileHolder>(this, android.R.layout.simple_list_item_1, android.R.id.text1, holders);
				listConvo.setAdapter(listAdapter);
			}

		private void startConversation(String filename, boolean isNew)
			{
				startConversation(filename, isNew, true, Locale.ENGLISH);
			}

		private void startConversation(String filename, boolean isNew, boolean compFirst, Locale language)
			{
				Intent i = new Intent(this, ConversationBuilderActivity.class);
				i.putExtra(ConversationBuilderActivity.FILENAME, filename);
				i.putExtra(ConversationBuilderActivity.IS_NEW, isNew);
				i.putExtra(ConversationBuilderActivity.COMP_FIRST, compFirst);
				i.putExtra(ConversationBuilderActivity.LANGUAGE, language.getLanguage());
				startActivity(i);
				overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			}

		private void deleteConversationFile(final int index)
			{
				AlertDialog alertDialog = new AlertDialog.Builder(this).create();
				alertDialog.setTitle(getString(R.string.confirm_delete));
				alertDialog.setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_conversation_this_action_cannot_be_undone_));

				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
					{
						@SuppressWarnings("unchecked")
						@Override
						public void onClick(DialogInterface dialog, int which)
							{
								ArrayAdapter<DropboxFileHolder> adapter = (ArrayAdapter<DropboxFileHolder>) listConvo.getAdapter();
								DbxPath path = adapter.getItem(index).getFile();
								try
									{
										dbxFS.delete(path);
									}
								catch(DbxException e)
									{
										e.printStackTrace();
									}
								// deleteFile(path.getName());
								initConversationList();
							}
					});

				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.no), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
							{
								// Do Nothing
							}
					});

				alertDialog.show();
			}

		private void renameConversationFile(final int index)
			{
				LayoutInflater inflater = getLayoutInflater();
				final View dialogLayout = inflater.inflate(R.layout.alert_rename_conversation, null);

				AlertDialog alertDialog = new AlertDialog.Builder(this).create();
				alertDialog.setView(dialogLayout);
				alertDialog.setTitle(getString(R.string.rename_conversation));

				final EditText renameBox = (EditText) dialogLayout.findViewById(R.id.textRenameFilename);
				final DropboxFileHolder holder = (DropboxFileHolder) listConvo.getAdapter().getItem(index);
				String filename = holder.getFilename();
				renameBox.setText(filename);

				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
							{
								// try
								// {
								// Gson gson = new Gson();
								//
								// // Read file in
								// BufferedReader br = new BufferedReader(new
								// InputStreamReader(openFileInput(holder.getFile().getName())));
								// Conversation conversation = (Conversation)
								// gson.fromJson(br, Conversation.class);
								//
								// // Write file out with new name
								// PrintStream ps = new
								// PrintStream(openFileOutput(renameBox.getText().toString(),
								// Context.MODE_PRIVATE));
								// ps.print(gson.toJson(conversation));
								// ps.close();
								// }
								// catch(FileNotFoundException e)
								// {
								// // TODO Auto-generated catch block
								// e.printStackTrace();
								// }
								// catch(IOException e)
								// {
								// // TODO Auto-generated catch block
								// e.printStackTrace();
								// }
								// deleteFile(holder.getFile().getName());
								try
									{
										dbxFS.move(holder.getFile(), new DbxPath("/" + renameBox.getText().toString()));
									}
								catch(DbxException e)
									{
										e.printStackTrace();
									}
								initConversationList();
							}
					});

				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
							{
								// Do Nothing
							}
					});

				alertDialog.show();
			}

		private void duplicateConversationFile(final int index)
			{
				final DropboxFileHolder holder = (DropboxFileHolder) listConvo.getAdapter().getItem(index);
				String filename = holder.getFilename();
				int i = 1;
				String newFilename = filename + " (" + i + ")";
				// while((f = new File(getFilesDir(), newFilename)).exists())
				// {
				// i++;
				// newFilename = filename + " (" + i + ")";
				// }
				try
					{
						while(dbxFS.exists(new DbxPath("/" + newFilename)))
							{
								i++;
								newFilename = filename + " (" + i + ")";
							}
					}
				catch(InvalidPathException e1)
					{
						e1.printStackTrace();
					}
				catch(DbxException e1)
					{
						e1.printStackTrace();
					}

				// Write duplicate
				// try
				// {
				// Gson gson = new Gson();
				//
				// // Read file in
				// BufferedReader br = new BufferedReader(new
				// InputStreamReader(openFileInput(holder.getFile().getName())));
				// Conversation conversation = (Conversation) gson.fromJson(br,
				// Conversation.class);
				//
				// // Write file out with new name
				// PrintStream ps = new PrintStream(openFileOutput(newFilename,
				// Context.MODE_PRIVATE));
				// ps.print(gson.toJson(conversation));
				// ps.close();
				// }
				// catch(FileNotFoundException e)
				// {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				try
					{
						DbxFile originalFile = dbxFS.open(holder.getFile());
						DbxFile newFile = dbxFS.create(new DbxPath("/" + newFilename));
						Gson gson = new Gson();

						// Read file in
						try
							{
								BufferedReader br = new BufferedReader(new InputStreamReader(originalFile.getReadStream()));
								Conversation conversation = (Conversation) gson.fromJson(br, Conversation.class);
								PrintStream ps = new PrintStream(newFile.getWriteStream());
								ps.print(gson.toJson(conversation));
								ps.close();
							}
						catch(IOException e)
							{
								e.printStackTrace();
							}
						finally
							{
								originalFile.close();
								newFile.close();
							}

					}
				catch(InvalidPathException e)
					{
						e.printStackTrace();
					}
				catch(DbxException e)
					{
						e.printStackTrace();
					}

				initConversationList();
			}
	}
