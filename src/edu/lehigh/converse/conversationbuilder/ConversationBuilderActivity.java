package edu.lehigh.converse.conversationbuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;
import com.google.gson.Gson;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import edu.lehigh.converse.MainActivity;
import edu.lehigh.converse.R;
import edu.lehigh.converse.conversation.CNode;
import edu.lehigh.converse.conversation.Conversation;
import edu.lehigh.converse.conversation.Response;

public class ConversationBuilderActivity extends Activity
	{
		public static final String	FILENAME		= "filename";
		public static final String	IS_NEW			= "isNew";
		public static final String	COMP_FIRST		= "compFirst";
		public static final String	LANGUAGE	= "language";

		private static final int	SELECT_IMAGE	= 100;
		private static final int	SELECT_AUDIO	= 101;
		

		private int					responseCount;
		private Conversation		conversation;
		private CNode				currentNode;
		private String				filename;
		private boolean				compFirst;
		private DbxFileSystem		dbxFS;

		private DragSortListView	list;
		private CNodeAdapter		adapter;

		// =====================================
		// Overrides
		// =====================================

		@Override
		public void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_conversation_builder);
				setVolumeControlStream(AudioManager.STREAM_MUSIC);

				try
					{
						dbxFS = DbxFileSystem.forAccount(MainActivity.getDropboxManager().getLinkedAccount());
					}
				catch(Unauthorized e)
					{
						// This should not occur, since the app cannot be used
						// unless there is a linked account
						e.printStackTrace();
					}

				filename = this.getIntent().getStringExtra(FILENAME);
				String language = getIntent().getStringExtra(LANGUAGE);
				boolean isNew = this.getIntent().getBooleanExtra(IS_NEW, true);

				compFirst = this.getIntent().getBooleanExtra(COMP_FIRST, true);

				initList();

				if(!isNew)
					readData();
				else
					{
						conversation = new Conversation(compFirst);
						conversation.setLanguage(language == null ? Locale.getDefault() : new Locale(language));
						// Build a special CNode for the first question
						if(!conversation.isCompFirst())
							{
								// Make node with null prompt and add to
								// conversation
								CNode node = new CNode(1, null);
								responseCount++;
								conversation.addNode(node);
							}
					}

				if(!conversation.isCompFirst())
					{
						// Show the linear layout that holds the root node
						LinearLayout root = (LinearLayout) findViewById(R.id.rootBox);
						root.setVisibility(LinearLayout.VISIBLE);
						updateRootNode();
					}
			}

		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				getMenuInflater().inflate(R.menu.menu_conversation_builder, menu);
				return true;
			}

		private void initList()
			{
				// Setup controller
				list = (DragSortListView) findViewById(R.id.nodeList);
				DragSortController controller = new DragSortController(list);
				controller.setDragHandleId(R.id.dragHandle);
				controller.setDragInitMode(DragSortController.ON_DOWN);
				list.setDragEnabled(true);
				list.setFloatViewManager(controller);
				list.setOnTouchListener(controller);
				list.setDropListener(onDrop);
				list.setRemoveListener(onRemove);

				// Make it clickable
				list.setOnItemClickListener(new ItemClick());
			}

		@Override
		public void onBackPressed()
			{
				// Use a cool animation when navigating between activities
				super.onBackPressed();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}

		// =====================================
		// Button Presses
		// =====================================

		public void addResponseClick(View v)
			{
				addTableRow();
			}

		public void saveClick(View v)
			{
				saveNode(currentNode);

				updateList();
				dismissKeyboard(v);
				hidePanel();

				saveData();
			}

		public void cancelClick(View v)
			{
				hidePanel();
				dismissKeyboard(v);
				readData();
			}

		public void addNodeClick(MenuItem v)
			{
				CNode n1 = new CNode(conversation.getNextNodeId(), "");

				conversation.addNode(n1);
				updateList();

				showPanel();
				buildBottomPanel(n1);

				currentNode = n1;
			}

		public void deleteNodeClick(MenuItem v)
			{
				if(currentNode != null)
					{
						conversation.removeNode(currentNode);
						sweepResponses(currentNode);
						saveData();
						updateList();
					}
				currentNode = null;
				hidePanel();
			}

		public void rootBoxClick(View v)
			{
				if(conversation.isCompFirst())
					return;

				currentNode = conversation.getNode(0);
				buildBottomPanelRoot(currentNode);
				showPanel();
			}

		private class ItemClick implements OnItemClickListener
			{

				@Override
				public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
					{
						// Add to the position if id:1 is not included in the
						// list
						if(!conversation.isCompFirst())
							position++;

						currentNode = conversation.getNode(position);
						buildBottomPanel(currentNode);
						showPanel();
					}
			}

		private class RemoveRowListener implements OnClickListener
			{
				private TableRow	row;
				private Response	response;

				public RemoveRowListener(TableRow row, Response r)
					{
						this.row = row;
						this.response = r;
					}

				@Override
				public void onClick(View arg0)
					{
						TableLayout tl = (TableLayout) findViewById(R.id.editTable);
						tl.removeView(row);

						currentNode.getResponses().remove(response);
					}
			}

		// =====================================
		// Drag Listeners
		// =====================================

		private DragSortListView.DropListener	onDrop		= new DragSortListView.DropListener()
																{
																	@Override
																	public void drop(int from, int to)
																		{
																			// If
																			// the
																			// player
																			// goes
																			// first,
																			// we
																			// have
																			// that
																			// root
																			// node
																			// that
																			// isn't
																			// in
																			// the
																			// ListView,
																			// but
																			// IS
																			// in
																			// the
																			// Conversation.
																			// Therefore,
																			// we
																			// have
																			// to
																			// skew
																			// the
																			// locations
																			// by
																			// 1
																			if(!conversation.isCompFirst())
																				{
																					from++;
																					to++;
																				}
																			if(from != to)
																				{
																					ArrayList<CNode> nodes = conversation.getNodes();
																					CNode item = nodes.get(from);
																					nodes.remove(item);
																					nodes.add(to, item);
																					saveData();
																					updateList();
																				}
																		}
																};

		private DragSortListView.RemoveListener	onRemove	= new DragSortListView.RemoveListener()
																{
																	@Override
																	public void remove(int which)
																		{
																			adapter.remove(adapter.getItem(which));
																			updateList();
																		}
																};

		// =====================================
		// Convenience
		// =====================================

		private void saveNode(CNode n)
			{
				String prompt = ((EditText) findViewById(R.id.promptText)).getText().toString();
				n.setPrompt(prompt);

				TableLayout tl = (TableLayout) findViewById(R.id.editTable);
				n.clearResponses();
				for(int i = 1; i < tl.getChildCount(); i++)
					{
						TableRow tr = (TableRow) tl.getChildAt(i);

						String responseText = ((EditText) tr.getChildAt(1)).getText().toString();
						int goesTo = Integer.parseInt(((Spinner) tr.getChildAt(3)).getSelectedItem().toString());
						n.addResponse(new Response(responseText, goesTo));
					}
			}

		private int getNextNodeId()
			{
				if(conversation.size() > 0)
					return conversation.getNode(conversation.size() - 1).getId() + 1;
				else
					return 1;
			}

		private void updateList()
			{
				ListView list = (ListView) findViewById(R.id.nodeList);
				ArrayList<CNode> nodes = getListableNodes();

				adapter = new CNodeAdapter(this, android.R.id.text1, nodes);
				list.setAdapter(adapter);

				if(!conversation.isCompFirst())
					updateRootNode();
			}

		private void updateRootNode()
			{
				TextView txtId = (TextView) findViewById(R.id.txtIDRoot);
				TextView txtPrompt = (TextView) findViewById(R.id.txtPromptRoot);
				TextView txtResponse = (TextView) findViewById(R.id.txtResponseRoot);
				CNode root = conversation.getNode(0);

				txtId.setText("" + root.getId());
				txtPrompt.setText(R.string.starting_prompts);
				txtResponse.setText(root.getResponses().size() + R.string._possible_prompts_);

			}

		private void sweepResponses(CNode node)
			{
				for(CNode n : conversation.getNodes())
					{
						// TODO go through and change all invalid responses
					}
			}

		private void addTableRow()
			{
				addTableRow(null);
			}

		private void addTableRow(Response r)
			{
				addTableRow(r, getString(R.string.response));
			}

		private void addTableRow(Response r, String labelText)
			{
				String response = "";
				int id = -1;
				if(r != null)
					{
						response = r.getResponse();
						id = r.getId();
					}

				// Create row
				TableLayout tl = (TableLayout) findViewById(R.id.editTable);
				TableRow tr = new TableRow(this);

				// Make label
				TextView t = new TextView(this);
				t.setText(labelText + " " + ++responseCount + ": ");
				tr.addView(t);

				// Text for placing response
				EditText et = new EditText(this);
				et.setText(response);
				et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
				tr.addView(et);

				// "GOTO" label
				TextView t2 = new TextView(this);
				t2.setText(R.string.goes_to_);
				tr.addView(t2);

				// Spinner for selecting GOTO
				Spinner spin = new Spinner(this);
				ArrayList<String> options = new ArrayList<String>();
				for(CNode n : conversation.getNodes())
					options.add(n.getId() + "");

				// Create adapter
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, options);
				adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
				spin.setAdapter(adapter);

				// If given a valid id, set the spinner to that id
				if(id > 0)
					spin.setSelection(id - 1);
				tr.addView(spin);

				// Remove button
				Button removeBtn = new Button(this);
				removeBtn.setText(R.string.remove);
				removeBtn.setOnClickListener(new RemoveRowListener(tr, r));
				tr.addView(removeBtn);

				// Add the row to the table
				tl.addView(tr);
			}

		private void buildBottomPanel(final CNode node)
			{
				// Grab the table layout and clear added rows
				TableLayout tl = (TableLayout) findViewById(R.id.editTable);
				while(tl.getChildCount() > 1)
					tl.removeViewAt(1);

				// Assign the title
				TextView title = (TextView) findViewById(R.id.panelTitleText);
				title.setText(R.string.currently_editing_node_ + node.getId());

				// Setup audio and image buttons
				ImageView audio = (ImageView) findViewById(R.id.audioSelector);
				audio.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								Intent audioPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
								audioPickerIntent.setType("audio/*");
								startActivityForResult(audioPickerIntent, SELECT_AUDIO);
							}

					});
				audio.setOnLongClickListener(new OnLongClickListener()
					{

						@Override
						public boolean onLongClick(View v)
							{
								new Thread(new Runnable()
									{
										@Override
										public void run()
											{
												DbxFile audioBase = null;
												try
													{
														final DbxFile audio = dbxFS.open(new DbxPath(currentNode.getAudioPath()));
														audioBase = audio;
														final MediaPlayer mp = new MediaPlayer();
														mp.setDataSource(audio.getReadStream().getFD());
														mp.setOnCompletionListener(new OnCompletionListener()
															{

																@Override
																public void onCompletion(MediaPlayer mediaplayer)
																	{
																		mp.release();
																		audio.close();
																	}

															});
														mp.prepare();
														mp.start();

													}
												catch(InvalidPathException e)
													{
														e.printStackTrace();
													}
												catch(AssertionError r)
													{
														runOnUiThread(new Runnable()
															{
																@Override
																public void run()
																	{
																		Toast.makeText(ConversationBuilderActivity.this, R.string.no_audio_file_set_for_this_node_, Toast.LENGTH_LONG).show();
																	}
															});
													}
												catch(DbxException.NotFound e)
													{
														runOnUiThread(new Runnable()
															{
																@Override
																public void run()
																	{
																		Toast.makeText(ConversationBuilderActivity.this, R.string.no_audio_file_set_for_this_node_, Toast.LENGTH_LONG).show();
																	}
															});
													}
												catch(DbxException e)
													{
														e.printStackTrace();
													}
												catch(IllegalArgumentException e)
													{
														e.printStackTrace();
													}
												catch(IllegalStateException e)
													{
														e.printStackTrace();
													}
												catch(IOException e)
													{
														e.printStackTrace();
													}
												finally
													{
														if(audioBase != null)
															audioBase.close();
													}

											}
									}).start();
								return true;
							}

					});
				ImageView image = (ImageView) findViewById(R.id.imageSelector);
				image.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View arg0)
							{
								Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
								photoPickerIntent.setType("image/*");
//								photoPickerIntent.putExtra("edu.lehigh.converse.NODE", (Parcelable) node);
								startActivityForResult(photoPickerIntent, SELECT_IMAGE);
							}

					});
				image.setOnLongClickListener(new OnLongClickListener()
					{

						@Override
						public boolean onLongClick(View arg0)
							{
//								Intent photoShowerIntent = new Intent(Intent.ACTION_VIEW);
//								photoShowerIntent.setType("image/*");
//								ProgressDialog pd = ProgressDialog.show(ConversationBuilderActivity.this, "Woo", "Test");
//								pd.setIndeterminate(true);
//								pd.show();
								new Thread(new Runnable()
									{
										@Override
										public void run()
											{
												DbxFile imageBase = null;
												try
													{
														final DbxFile image = dbxFS.open(new DbxPath(currentNode.getImagePath()));
														imageBase = image;
														final Bitmap bmp = BitmapFactory.decodeStream(image.getReadStream());
														runOnUiThread(new Runnable()
															{
																@Override
																public void run()
																	{
																		ImageView iv = new ImageView(ConversationBuilderActivity.this);
																		iv.setImageBitmap(bmp);
																		final AlertDialog ad = new AlertDialog.Builder(ConversationBuilderActivity.this).setPositiveButton(R.string.done, new Dialog.OnClickListener()
																			{

																				@Override
																				public void onClick(DialogInterface dialog, int which)
																					{
																						dialog.dismiss();
																					}

																			}).setView(iv).create();
																		ad.setOnDismissListener(new OnDismissListener()
																			{

																				@Override
																				public void onDismiss(DialogInterface arg0)
																					{
																						image.close();
																					}

																			});
																		ad.show();
																	}
															});

//													File f = File.createTempFile("image", "tmp");
//													BufferedReader br = new BufferedReader(new InputStreamReader(image.getReadStream()));
//													PrintWriter pw = new PrintWriter(new FileOutputStream(f));
//													while(br.ready())
//														pw.write(br.readLine());
//													pw.close();
//													br.close();
//													photoShowerIntent.setData(Uri.parse("image://" + f.getAbsolutePath()));
//													pd.dismiss();

													}
												catch(AssertionError r)
													{
														runOnUiThread(new Runnable()
															{
																@Override
																public void run()
																	{
																		Toast.makeText(ConversationBuilderActivity.this, R.string.no_image_file_set_for_this_node_, Toast.LENGTH_LONG).show();
																	}
															});
													}
												catch(DbxException e)
													{
														runOnUiThread(new Runnable()
															{
																@Override
																public void run()
																	{
																		Toast.makeText(ConversationBuilderActivity.this, R.string.no_audio_file_set_for_this_node_, Toast.LENGTH_LONG).show();
																	}
															});
													}
												catch(IOException e)
													{
														e.printStackTrace();
													}
												finally
												{
													if(imageBase != null)
														imageBase.close();
												}
											}
									}).start();

//								startActivityForResult(photoShowerIntent, 102);
								return true;
							}

					});
//				if(currentNode.getImagePath() != null)
//					{
//						DbxFile f = null;
//						try
//							{
//								f = dbxFS.open(new DbxPath(currentNode.getImagePath()));
//								image.setImageBitmap(BitmapFactory.decodeStream(f.getReadStream()));
//							}
//						catch(InvalidPathException e)
//							{
//								Log.w(getLocalClassName(), "setting image in builder found an invalid path");
//							}
//						catch(DbxException e)
//							{
//								Log.w(getLocalClassName(), "DbxException...probably means image file doesn't exist in dropbox");
//							}
//						catch(IOException e)
//							{
//								Log.e(getLocalClassName(), "Error parsing image.");
//							}
//						finally
//							{
//								if(f != null)
//									f.close();
//							}
//					}

				// Ensure prompt row is visible
				TableRow rowPrompt = (TableRow) findViewById(R.id.rowPrompt);
				rowPrompt.setVisibility(TableRow.VISIBLE);

				// Assign the prompt text
				EditText promptText = (EditText) findViewById(R.id.promptText);
				promptText.setText(node.getPrompt());

				// Add all responses
				responseCount = 0;
				for(Response r : node.getResponses())
					{
						addTableRow(r);
					}
			}

		private void buildBottomPanelRoot(final CNode node)
			{
				// Grab the table layout and clear added rows
				TableLayout tl = (TableLayout) findViewById(R.id.editTable);
				while(tl.getChildCount() > 1)
					tl.removeViewAt(1);

				// Assign the title
				TextView title = (TextView) findViewById(R.id.panelTitleText);
				title.setText(R.string.currently_editing_starting_prompts);

				// Setup audio and image buttons
				ImageView audio = (ImageView) findViewById(R.id.audioSelector);
				audio.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
							{
								Intent audioPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
								audioPickerIntent.setType("audio/*");
								startActivityForResult(audioPickerIntent, SELECT_AUDIO);
							}

					});
				audio.setOnLongClickListener(new OnLongClickListener()
					{

						@Override
						public boolean onLongClick(View v)
							{
								new Thread(new Runnable()
									{
										@Override
										public void run()
											{
												DbxFile audioBase = null;
												try
													{
														final DbxFile audio = dbxFS.open(new DbxPath(currentNode.getAudioPath()));
														audioBase = audio;
														final MediaPlayer mp = new MediaPlayer();
														mp.setDataSource(audio.getReadStream().getFD());
														mp.setOnCompletionListener(new OnCompletionListener()
															{

																@Override
																public void onCompletion(MediaPlayer mediaplayer)
																	{
																		mp.release();
																		audio.close();
																	}

															});
														mp.prepare();
														mp.start();

													}
												catch(InvalidPathException e)
													{
														e.printStackTrace();
													}
												catch(AssertionError r)
													{
														runOnUiThread(new Runnable()
															{
																@Override
																public void run()
																	{
																		Toast.makeText(ConversationBuilderActivity.this, R.string.no_audio_file_set_for_this_node_, Toast.LENGTH_LONG).show();
																	}
															});
													}
												catch(DbxException.NotFound e)
													{
														runOnUiThread(new Runnable()
															{
																@Override
																public void run()
																	{
																		Toast.makeText(ConversationBuilderActivity.this, R.string.no_audio_file_set_for_this_node_, Toast.LENGTH_LONG).show();
																	}
															});
													}
												catch(DbxException e)
													{
														e.printStackTrace();
													}
												catch(IllegalArgumentException e)
													{
														e.printStackTrace();
													}
												catch(IllegalStateException e)
													{
														e.printStackTrace();
													}
												catch(IOException e)
													{
														e.printStackTrace();
													}
												finally
													{
														if(audioBase != null)
															audioBase.close();
													}

											}
									}).start();
								return true;
							}

					});
				ImageView image = (ImageView) findViewById(R.id.imageSelector);
				image.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View arg0)
							{
								Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
								photoPickerIntent.setType("image/*");
//								photoPickerIntent.putExtra("edu.lehigh.converse.NODE", (Parcelable) node);
								startActivityForResult(photoPickerIntent, SELECT_IMAGE);
							}

					});
				image.setOnLongClickListener(new OnLongClickListener()
					{

						@Override
						public boolean onLongClick(View arg0)
							{
//								Intent photoShowerIntent = new Intent(Intent.ACTION_VIEW);
//								photoShowerIntent.setType("image/*");
//								ProgressDialog pd = ProgressDialog.show(ConversationBuilderActivity.this, "Woo", "Test");
//								pd.setIndeterminate(true);
//								pd.show();
								new Thread(new Runnable()
									{
										@Override
										public void run()
											{
												DbxFile imageBase = null;
												try
													{
														final DbxFile image = dbxFS.open(new DbxPath(currentNode.getImagePath()));
														imageBase = image;
														final Bitmap bmp = BitmapFactory.decodeStream(image.getReadStream());
														runOnUiThread(new Runnable()
															{
																@Override
																public void run()
																	{
																		ImageView iv = new ImageView(ConversationBuilderActivity.this);
																		iv.setImageBitmap(bmp);
																		final AlertDialog ad = new AlertDialog.Builder(ConversationBuilderActivity.this).setPositiveButton(R.string.done, new Dialog.OnClickListener()
																			{

																				@Override
																				public void onClick(DialogInterface dialog, int which)
																					{
																						dialog.dismiss();
																					}

																			}).setView(iv).create();
																		ad.setOnDismissListener(new OnDismissListener()
																			{

																				@Override
																				public void onDismiss(DialogInterface arg0)
																					{
																						image.close();
																					}

																			});
																		ad.show();
																	}
															});

//													File f = File.createTempFile("image", "tmp");
//													BufferedReader br = new BufferedReader(new InputStreamReader(image.getReadStream()));
//													PrintWriter pw = new PrintWriter(new FileOutputStream(f));
//													while(br.ready())
//														pw.write(br.readLine());
//													pw.close();
//													br.close();
//													photoShowerIntent.setData(Uri.parse("image://" + f.getAbsolutePath()));
//													pd.dismiss();

													}
												catch(AssertionError r)
													{
														runOnUiThread(new Runnable()
															{
																@Override
																public void run()
																	{
																		Toast.makeText(ConversationBuilderActivity.this, R.string.no_image_file_set_for_this_node_, Toast.LENGTH_LONG).show();
																	}
															});
													}
												catch(DbxException e)
													{
														runOnUiThread(new Runnable()
															{
																@Override
																public void run()
																	{
																		Toast.makeText(ConversationBuilderActivity.this, R.string.no_audio_file_set_for_this_node_, Toast.LENGTH_LONG).show();
																	}
															});
													}
												catch(IOException e)
													{
														e.printStackTrace();
													}
												finally
												{
													if(imageBase != null)
														imageBase.close();
												}
											}
									}).start();

//								startActivityForResult(photoShowerIntent, 102);
								return true;
							}

					});
				// Make prompt row invisible
				TableRow rowPrompt = (TableRow) findViewById(R.id.rowPrompt);
				rowPrompt.setVisibility(TableRow.GONE);

				// Add all responses
				responseCount = 0;
				for(Response r : node.getResponses())
					{
						addTableRow(r, getString(R.string.prompt));
					}
			}

		/**
		 * Shows that bottom panel that has all of our node info.
		 */
		private void showPanel()
			{
				SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.slidingPanel);
				if(!drawer.isOpened())
					drawer.animateOpen();
			}

		/**
		 * Hides the bottom panel that contains all of our node info.
		 */
		private void hidePanel()
			{
				currentNode = null;
				SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.slidingPanel);
				if(drawer.isOpened())
					drawer.animateClose();
			}

		/**
		 * Dismisses the keyboard from view
		 * 
		 * @param v
		 *            The view that is active. Necessary so we can get the proper keyboard instance.
		 */
		private void dismissKeyboard(View v)
			{
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}

		private void saveData()
			{
				// PrintStream ps = new PrintStream(openFileOutput(filename,
				// Context.MODE_PRIVATE));
				Gson gson = new Gson();
				String jsonString = gson.toJson(conversation);
				// ps.print(jsonString);
				DbxFile dropboxFile = null;
				DbxPath dropboxPath = new DbxPath(filename);
				DbxPath dropboxDataPath = new DbxPath(filename + "_data");
				try
					{
						if(dbxFS.exists(dropboxPath))
							{
								dropboxFile = dbxFS.open(dropboxPath);
							}
						else
							{
								dropboxFile = dbxFS.create(dropboxPath);
							}
						if(!dbxFS.exists(dropboxDataPath))
							{
								dbxFS.createFolder(dropboxDataPath);
							}
						dropboxFile.writeString(jsonString);
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
				finally
					{
						if(dropboxFile != null)
							dropboxFile.close();
					}
				// ps.close();
				Log.i("TAG", "Saved Data: " + gson.toJson(conversation));
			}

		private void readData()
			{
				DbxPath dropboxDataPath = new DbxPath(filename);
				DbxFile dropboxDataFile = null;
				try
					{
						if(dbxFS.exists(dropboxDataPath))
							{
								dropboxDataFile = dbxFS.open(dropboxDataPath);
							}
						else
							{
								dropboxDataFile = dbxFS.create(dropboxDataPath);
							}

						BufferedReader br = new BufferedReader(new InputStreamReader(dropboxDataFile.getReadStream()));
						// BufferedReader br = new BufferedReader(new
						// InputStreamReader(openFileInput(filename)));
						Gson gson = new Gson();
						conversation = (Conversation) gson.fromJson(br, Conversation.class);
//						List<DbxFileInfo> infos = dbxFS.listFolder(new DbxPath(String.format("/%s/", conversation.getName())));
//						for(DbxFileInfo info : infos)
//							{
//								if(info.path.getName().matches("audio_[1-9]+[0-9]*"))
//									{
//										String id = info.path.getName().replaceAll("[^0-9]+", "");
//										conversation.getNodeWithId(Integer.valueOf(id)).setAudioPathWithinDropbox(info.path);
//									}
//								else if(info.path.getName().matches("image_[1-9]+[0-9]*"))
//									{
//										String id = info.path.getName().replaceAll("[^0-9]+", "");
//										conversation.getNodeWithId(Integer.valueOf(id)).setImagePathWithinDropbox(info.path);
//									}
//							}
						if(conversation == null)
							{
								conversation = new Conversation();
							}
						updateList();
						// Log.i("TAG", "Root: " +
						// conversation.getNode(0).getPrompt());
					}
				catch(FileNotFoundException e)
					{
						// TODO Auto-generated catch block
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
				finally
					{
						dropboxDataFile.close();
					}
			}

		private ArrayList<CNode> getListableNodes()
			{
				ArrayList<CNode> nodes = (ArrayList<CNode>) conversation.getNodes().clone();
				if(!conversation.isCompFirst())
					if(nodes.size() > 0)
						nodes.remove(0);
				return nodes;
			}

		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent)
			{

				switch(requestCode)
					{
						case SELECT_IMAGE:
							if(resultCode == RESULT_OK)
								{
									Uri selectedImage = returnedIntent.getData();
									InputStream imageStream = null;
									try
										{
											imageStream = getContentResolver().openInputStream(selectedImage);
										}
									catch(FileNotFoundException e)
										{
											e.printStackTrace();
										}
									Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
//									CNode node = (CNode) imageReturnedIntent.getParcelableExtra("edu.lehigh.converse.NODE");
//									Log.w("edu.lehigh.converse", String.valueOf(node == null));
									currentNode.setImageFile(new DbxPath(getIntent().getStringExtra(FILENAME)), dbxFS, yourSelectedImage);

								}
							break;
						case SELECT_AUDIO:
							if(resultCode == RESULT_OK)
								{
									Uri selectedFile = returnedIntent.getData();
									InputStream audioStream = null;
									try
										{
											Toast.makeText(ConversationBuilderActivity.this, getContentResolver().getType(selectedFile), Toast.LENGTH_LONG).show();
											audioStream = getContentResolver().openInputStream(selectedFile);
											currentNode.setAudioFile(new DbxPath(getIntent().getStringExtra(FILENAME)), dbxFS, audioStream);
//											BufferedReader br = new BufferedReader(new InputStreamReader(audioStream));
											audioStream.close();
										}
									catch(FileNotFoundException e)
										{
											e.printStackTrace();
										}
									catch(IOException e)
										{
											e.printStackTrace();
										}

								}
					}
				super.onActivityResult(requestCode, resultCode, returnedIntent);
			}

	}
