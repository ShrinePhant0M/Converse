package edu.lehigh.converse.conversationplayer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.lehigh.converse.DifficultyChooserActivity;
import edu.lehigh.converse.MainActivity;
import edu.lehigh.converse.R;
import edu.lehigh.converse.conversation.CNode;
import edu.lehigh.converse.conversation.Conversation;
import edu.lehigh.converse.conversation.Response;
import edu.lehigh.converse.util.DropboxUtils;
import edu.lehigh.converse.util.ListenableAsyncTask;
import edu.lehigh.converse.util.SpeechAnalysisTask;

public class ConversationPlayerActivity extends Activity
	{
		public static final String	TAG						= "TAG";
		public static final String	FILENAME				= "filename";
		public static final String	EXPECTED_RESPONSE		= "expectedResponse";
		public static final int		RC_VOICE_RECOGNITION	= 123;
		public static final int		RC_TTS_ENGINES			= 124;

		private String				filename;
		private Conversation		conversation;
		private CNode				currentNode;
		private ArrayList<String>	responseStrings;
		private PlayerItemAdapter	adapter;
		private ListView			list;

		private DbxFileSystem		dbxFS					= null;

		private TextToSpeech		tts;
		private String				expectedResponse;
		private int					chosenPosition;

		private ProgressDialog		pd;

		private double				difficulty;

		boolean						firstConversation		= true;

		private MediaPlayer			mp;

		private DbxFile				audioFile, imageFile;

		// =====================================
		// Overrides
		// =====================================

		@Override
		public void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_conversation_player);
				setVolumeControlStream(AudioManager.STREAM_MUSIC);

//				Debug.startMethodTracing();
				pd = ProgressDialog.show(this, getString(R.string.loading_resources), getString(R.string.please_wait_), true, false);
				difficulty = getIntent().getDoubleExtra("DIFFICULTY_LEVEL", 1);
				try
					{
						dbxFS = DbxFileSystem.forAccount(MainActivity.getDropboxManager().getLinkedAccount());
					}
				catch(Unauthorized e)
					{
						// This should not occur, since the app cannot be used unless there is a linked account
						e.printStackTrace();
					}

				this.filename = this.getIntent().getStringExtra(FILENAME);
				createTextToSpeech(this, null);
			}

		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				// Inflate the menu; this adds items to the action bar if it is present.
				getMenuInflater().inflate(R.menu.conversation_player_menu, menu);
				return true;
			}

		@SuppressWarnings("unchecked")
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data)
			{
				if(requestCode == RC_VOICE_RECOGNITION && resultCode == RESULT_OK)
					{
						ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

						Log.i(TAG, matches.toString());
						// Make sure all of the responses are lower case for proper matching
						for(int i = 0; i < matches.size(); i++)
							matches.set(i, cleanString(matches.get(i)));

						Log.i(TAG, "Expected: " + expectedResponse + " || Received: " + matches.toString());
						final ProgressDialog pd = new ProgressDialog(this);
						pd.setIndeterminate(true);
						pd.setCancelable(false);
						pd.setTitle(getString(R.string.analyzing_speech));
						pd.setMessage(getString(R.string.please_wait_while_the_speech_is_analyzed_));
						List<String> words;
						if(conversation.getLanguage().getLanguage().equals(Locale.CHINESE.getLanguage()))
							{
								words = Arrays.asList(expectedResponse.split("(?!^)"));
							}
						else
							{
								words = Arrays.asList(expectedResponse.split(" +"));
							}

						List<String> match;
						Log.w("MATCHES", matches.get(0));
						if(conversation.getLanguage().getLanguage().equals(Locale.CHINESE.getLanguage()))
							{
								int i = 0;
								while(i < matches.size() && !isChineseCharacter(Character.codePointAt(matches.get(i), 0)))
									{
										i += 1;
									}
								if(i >= matches.size())
									{
										runOnUiThread(new Runnable()
											{
												@Override
												public void run()
													{
														Toast.makeText(ConversationPlayerActivity.this, "Couldn't properly interpret spoken Chinese.", Toast.LENGTH_LONG).show();
													}
											});
										return;
									}
								match = Arrays.asList(matches.get(i).split("(?!^)"));
							}
						else
							{
								match = Arrays.asList(matches.get(0).split(" +"));
							}
						new SpeechAnalysisTask(this, chosenPosition, words).addTaskCompletedListener(new ListenableAsyncTask.OnTaskCompletedListener<Double>()
							{

								@Override
								public void onTaskCompleted(Double result)
									{
										Log.i(TAG, "Resultant percentage correct is: " + result);
										Log.i(TAG, "Difficulty is at: " + difficulty);
										if(result >= difficulty)
											step(chosenPosition);
										pd.dismiss();
									}

							}).execute(match);

//						if(matches.contains(expectedResponse))
//							step(chosenPosition);
					}
				else if(requestCode == RC_VOICE_RECOGNITION)
					{
					}
				else if(requestCode == RC_TTS_ENGINES && data != null)
					{
						List<String> voices = data.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES);
						for(String s : voices)
							{
								Log.w(TAG, s);
							}
						finish();
					}
				super.onActivityResult(requestCode, resultCode, data);
			}

		@Override
		protected void onStop()
			{
				if(mp != null)
					{
						mp.stop();
						mp.reset();
						mp.release();
					}
				tts.stop();
				tts.shutdown();
				super.onStop();
			}
		
		@Override
		protected void onDestroy()
			{
				if(audioFile != null)
					audioFile.close();
				if(imageFile != null)
					imageFile.close();
				super.onDestroy();
			}

		@Override
		protected void onResume()
			{
				super.onResume();
			}
		
		@Override
		protected void onStart()
			{
				super.onStart();
			}

		@Override
		protected void onRestart()
			{
				createTextToSpeech(this, null);
				super.onRestart();
			}

		// =====================================
		// Button Listeners
		// =====================================

		public void conversationCompleteClick(View v)
			{
				finish();
			}

		public void changeDifficultyClick(MenuItem item)
			{
				Log.w(TAG, "Difficulty before change is: " + difficulty);
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				final View view = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.activity_difficulty_chooser, null);
				((Button) view.findViewById(R.id.diffContinueButton)).setVisibility(View.GONE);
				final SeekBar sb = (SeekBar) view.findViewById(R.id.diffSeekBar);
//				final String userID = MainActivity.getDropboxManager().getLinkedAccount().getUserId();
				sb.setProgress((int) Math.round(difficulty * 100)); //TODO
				sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
					{
						final TextView	difficulty	= (TextView) view.findViewById(R.id.diffSeekBarText);
						final TextView	level		= (TextView) view.findViewById(R.id.diffSeekBarValue);

						@Override
						public void onStopTrackingTouch(SeekBar seekBar)
							{
							}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar)
							{
							}

						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
							{
								if(fromUser)
									{
//										seekBar.setProgress(progress);
										difficulty.setText(DifficultyChooserActivity.rm.get(((double) progress) / 100));
										level.setText(String.format("%d%%", progress));
									}
							}
					});
				((TextView) view.findViewById(R.id.diffSeekBarText)).setText(DifficultyChooserActivity.rm.get(((double) sb.getProgress()) / 100));
				((TextView) view.findViewById(R.id.diffSeekBarValue)).setText(String.format("%d%%", sb.getProgress()));
				builder.setView(view);
				builder.setTitle(R.string.change_difficulty_level);
				builder.setPositiveButton(R.string.change, new Dialog.OnClickListener()
					{

						@Override
						public void onClick(DialogInterface dialog, int which)
							{
								difficulty = ((double) sb.getProgress()) / 100.0;
								Log.w(TAG, "Difficulty after change is: " + difficulty);
								dialog.dismiss();
							}

					});
				builder.setNeutralButton(R.string.cancel, new Dialog.OnClickListener()
					{

						@Override
						public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
							}

					});
				builder.show();
			}

		// =====================================
		// Initialization
		// =====================================

		private void initialize()
			{
				readData();
				initList();
				setupImageAndAudio();
//				Debug.stopMethodTracing();
			}

		private void initList()
			{
				this.list = (ListView) findViewById(R.id.listResponse);
				responseStrings = new ArrayList<String>();
				//TODO
//				adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, responseStrings);
				adapter = new PlayerItemAdapter(responseStrings);
				list.setAdapter(adapter);

				currentNode = conversation.getNode(0);

				if(currentNode != null)
					{
						copyFieldsToScreen();
					}
				if(!(conversation.isCompFirst() || currentNode.getPrompt() == null || currentNode.getPrompt().trim().length() <= 0))
					{
						TextView promptText = (TextView) findViewById(R.id.txtPromptPlay);
						promptText.setText(R.string.begin_by_selecting_a_prompt_from_below_);
					}

				list.setOnItemClickListener(new OnItemClickListener()
					{
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3)
							{
								chosenPosition = pos;
								expectedResponse = cleanString(adapter.getItem(pos));
								try
									{
										if(mp != null)
											{
												mp.stop();
											}
									}
								catch(IllegalStateException e)
									{

									}
								tts.stop();
								invokeSpeechToText(adapter.getItem(pos));
							}
					});

				// Read prompt
//				if(conversation.size() > 0 && conversation.isCompFirst())
//					speakLine(currentNode.getPrompt());
			}

		private void setupImageAndAudio()
			{
				try
					{
						DbxFileInfo info = null;
						if((info = DropboxUtils.firstFileMatching(dbxFS, new DbxPath(filename + "_data/"), "(?i)image_" + currentNode.getId() + "(\\.(jpg|jpeg|png|bmp|tiff|gif))?", false, false)) != null)
							{
								ImageView iv = (ImageView) findViewById(R.id.imageViewer);
								DbxFile imageFile = dbxFS.open(info.path);
//								this.imageFile = imageFile;
								Bitmap bm = BitmapFactory.decodeStream(imageFile.getReadStream());
								iv.setImageBitmap(bm);
								iv.setVisibility(View.VISIBLE);
								imageFile.close();
							}
						else if((info = DropboxUtils.firstFileMatching(dbxFS, new DbxPath(filename + "_data/"), "(?i)^image(\\.(jpg|jpeg|png|bmp|tiff|gif))?$", false, false)) != null)
							{
								ImageView iv = (ImageView) findViewById(R.id.imageViewer);
								DbxFile imageFile = dbxFS.open(info.path);
//								this.imageFile = imageFile;
								Bitmap bm = BitmapFactory.decodeStream(imageFile.getReadStream());
								iv.setImageBitmap(bm);
								iv.setVisibility(View.VISIBLE);
								imageFile.close();
							}
						else
							{
								ImageView iv = (ImageView) findViewById(R.id.imageViewer);
								iv.setVisibility(View.GONE);
							}
					}
				catch(DbxException e)
					{
//						 e.printStackTrace();
					}
				catch(IOException e)
					{
						e.printStackTrace();
					}
				try
					{
//						if(dbxFS.exists(new DbxPath(filename + "_data/audio_" + currentNode.getId() + ".wav")))
						if(firstConversation && DropboxUtils.fileExistsMatching(dbxFS, new DbxPath(filename + "_data/"), "^(?i)audio(\\.(wav|mp3|ogg))?$", false, false))
							{
								final DbxFile audioFile = dbxFS.open(DropboxUtils.firstFileMatching(dbxFS, new DbxPath(filename + "_data/"), "^audio(\\.(wav|mp3|ogg))?$", false, false).path);
								new AudioPlayerThread(audioFile).start();
							}
						else if(DropboxUtils.fileExistsMatching(dbxFS, new DbxPath(filename + "_data/"), "(?i)^audio_" + currentNode.getId() + "\\.(wav|mp3|ogg)$", false, false))
							{
//								final DbxFile audioFile = dbxFS.open(new DbxPath(filename + "_data/audio_" + currentNode.getId() + ".wav"));
								final DbxFile audioFile = dbxFS.open(DropboxUtils.firstFileMatching(dbxFS, new DbxPath(filename + "_data/"), "audio_" + currentNode.getId() + "\\.(wav|mp3|ogg)", false, false).path);
								this.audioFile = audioFile;

								new AudioPlayerThread(audioFile).start();

							}
						else
							{
								pd.dismiss();
								speakLine(currentNode.getPrompt());

								if(currentNode.getResponses().isEmpty())
									endGame();
							}

					}
				catch(DbxException e)
					{
						pd.dismiss();
					}
				pd.dismiss();
			}

		public static boolean isChineseCharacter(int codePoint)
			{
				TreeRangeSet<Integer> ranges = TreeRangeSet.create();
				ranges.add(Range.<Integer> closed(0x4e00, 0x9fcc));
				ranges.add(Range.<Integer> closed(0x3400, 0x4db5));
				ranges.add(Range.<Integer> closed(0x20000, 0x2a6d6));
				ranges.add(Range.<Integer> closed(0x2a700, 0x2b734));
				ranges.add(Range.<Integer> closed(0x2b740, 0x2b81d));
				return ranges.contains(codePoint);
			}

		public void createTextToSpeech(final Context context, final Locale locale)
			{
				tts = new TextToSpeech(context, new OnInitListener()
					{
						@Override
						public void onInit(int status)
							{
								if(status == TextToSpeech.SUCCESS)
									{
										Locale defaultOrPassedIn = locale;
										if(locale == null)
											defaultOrPassedIn = Locale.getDefault();

										// check if language is available
										switch(tts.isLanguageAvailable(defaultOrPassedIn))
											{
												case TextToSpeech.LANG_AVAILABLE:
												case TextToSpeech.LANG_COUNTRY_AVAILABLE:
												case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
													Log.d(TAG, "SUPPORTED");
													tts.setLanguage(defaultOrPassedIn);
													tts.setSpeechRate(0.9f);
													initialize();
													break;
												case TextToSpeech.LANG_MISSING_DATA:
													Log.d(TAG, "MISSING_DATA");
													Log.d(TAG, "require data...");
													Intent installIntent = new Intent();
													installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
													context.startActivity(installIntent);
													break;
												case TextToSpeech.LANG_NOT_SUPPORTED:
//													if(defaultOrPassedIn == Locale.CHINESE)
//														{
													pd.dismiss();
													new AlertDialog.Builder(ConversationPlayerActivity.this).setTitle("Language not supported").setMessage("Sorry, you must first download a Chinese TTS engine to use this app in Chinese.").setNegativeButton("Done", new OnClickListener()
														{

															@Override
															public void onClick(DialogInterface dialog, int which)
																{
																	dialog.dismiss();
																	final Intent ttsIntent = new Intent();
																	ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
																	startActivityForResult(ttsIntent, RC_TTS_ENGINES);
//																	finish();
																}
														}).show();
//													final Intent ttsIntent = new Intent();
//													ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//													startActivityForResult(ttsIntent, RC_TTS_ENGINES);
//														}
													break;
											}
									}
							}
					});
			}

		// =====================================
		// Convenience
		// =====================================

		private void invokeSpeechToText(String response)
			{
				Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
				i.putExtra(RecognizerIntent.EXTRA_PROMPT, response);
//				i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2500);
				i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
//				i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, conversation.getLanguage().getLanguage());
//				i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().getLanguage());
				i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, conversation.getLanguage().getLanguage());
				startActivityForResult(i, RC_VOICE_RECOGNITION);
			}

		public void step(int choice)
			{
				firstConversation = false;
				if(choice < 0 || choice > currentNode.numResponses())
					endGame();

				int choiceId = currentNode.getResponse(choice).getId();
				currentNode = conversation.getNodeWithId(choiceId);

				copyFieldsToScreen();
				updateList();

				// Show image and start sound
				setupImageAndAudio();
				// Read prompt

			}

		public void speakLine(String line)
			{
				if(tts != null)
					tts.speak(line, TextToSpeech.QUEUE_FLUSH, null);
			}

		private void endGame()
			{
				Button completeButton = (Button) findViewById(R.id.btnConvoComplete);
				completeButton.setVisibility(Button.VISIBLE);
			}

		private void copyFieldsToScreen()
			{
				Log.i("TAG", "Player: Current Node: " + currentNode.getId());
				// Update picture

				// Update prompt
				TextView promptText = (TextView) findViewById(R.id.txtPromptPlay);
				if(currentNode.getPrompt() != null && currentNode.getPrompt().trim().length() > 0)
					promptText.setText("\"" + currentNode.getPrompt() + "\"");
				else
					promptText.setText("");

				// Update response choices
				responseStrings = new ArrayList<String>();
				ArrayList<Response> responses = currentNode.getResponses();
				for(Response r : responses)
					responseStrings.add("\"" + r.getResponse() + "\"");

				updateList();
			}

		private void updateList()
			{
//				adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, responseStrings);
				adapter = new PlayerItemAdapter(responseStrings);
				list.setAdapter(adapter);
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
								Log.w(TAG, "dropboxDataFile exists already");
							}
						else
							{
								dropboxDataFile = dbxFS.create(dropboxDataPath);
							}

						BufferedReader br = new BufferedReader(new InputStreamReader(dropboxDataFile.getReadStream()));
						// BufferedReader br = new BufferedReader(new
						// InputStreamReader(openFileInput(filename)));
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
						conversation = (Conversation) gson.fromJson(br, Conversation.class);
						if(conversation == null)
							{
								conversation = new Conversation();
							}
					}
				catch(FileNotFoundException e)
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
						dropboxDataFile.close();
					}
			}

		/**
		 * Strips everything from a String except for letters and spaces. Purpose: Allow for more accurate String
		 * equality checks with Google's speech-to-text API
		 * 
		 * @param s
		 *            String to be cleaned.
		 * @return The String s with only letters and spaces.
		 */
		private static String cleanString(String s)
			{
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < s.length(); i++)
					{
						char c = s.charAt(i);
						if(Character.isLetter(c) || (!Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage()) && c == ' '))
							sb.append(c);
					}

				return sb.toString().toLowerCase(Locale.getDefault());
			}

		private class AudioPlayerThread extends Thread
			{
				private final DbxFile	audioFile;

				public AudioPlayerThread(DbxFile audioFile)
					{
						this.audioFile = audioFile;
					}

				@Override
				public void run()
					{
						try
							{
								FileInputStream fis = audioFile.getReadStream();
								mp = new MediaPlayer();
								try
									{
										mp.setDataSource(fis.getFD());
									}
								catch(IllegalArgumentException e1)
									{
										e1.printStackTrace();
									}
								catch(SecurityException e1)
									{
										e1.printStackTrace();
									}
								catch(IllegalStateException e1)
									{
										e1.printStackTrace();
									}
								catch(IOException e1)
									{
										e1.printStackTrace();
									}
								mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
									{

										@Override
										public void onCompletion(MediaPlayer mediaplayer)
											{
												audioFile.close();
//																		server1.stop();
												if(conversation.size() > 0 && (conversation.isCompFirst() || !currentNode.equals(conversation.getStartingNode())))
													speakLine(currentNode.getPrompt());

												if(currentNode.getResponses().isEmpty())
													endGame();
											}

									});
								mp.setOnErrorListener(new OnErrorListener()
									{

										@Override
										public boolean onError(MediaPlayer arg0, int arg1, int arg2)
											{
//																	Toast.makeText(getBaseContext(), arg1 + ',' + arg2, Toast.LENGTH_LONG).show();
//																		server1.stop();
												Log.e("MediaStuff", arg1 + "," + arg2);
												return false;
											}

									});
								mp.setOnPreparedListener(new OnPreparedListener()
									{

										@Override
										public void onPrepared(MediaPlayer mp)
											{
												pd.dismiss();
												mp.start();
											}

									});
								try
									{
										mp.prepare();
									}
								catch(IllegalStateException e)
									{
										e.printStackTrace();
									}
								catch(Exception e)
									{
										e.printStackTrace();
									}

							}
						catch(IllegalArgumentException e)
							{
								e.printStackTrace();
							}
						catch(SecurityException e)
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

					}
			}

		private class PlayerItemAdapter extends BaseAdapter
			{
				final List<String>						responses;
//				Picture									speakerIcon;
				private Drawable						speakerBitmapDrawable;
//				private Bitmap							speakerBitmap;
				private final ReentrantReadWriteLock	readWriteLock	= new ReentrantReadWriteLock();
				private final ReadLock					readLock		= readWriteLock.readLock();
				private final WriteLock					writeLock		= readWriteLock.writeLock();
				private final Object					mutex			= new Object();

				public PlayerItemAdapter(List<String> responses)
					{
						this.responses = new ArrayList<String>(responses);
						new Thread(new Runnable()
							{
								@Override
								public void run()
									{
										writeLock.lock();
										synchronized(mutex)
											{
												mutex.notify();
											}
										SVG icon = null;
										try
											{
												icon = SVG.getFromResource(getResources(), R.raw.speaker_icon);
												icon.setDocumentWidth("100%");
												icon.setDocumentHeight("100%");
											}
										catch(SVGParseException e)
											{
												e.printStackTrace();
											}

//										SVG icon = SVGParser.getSVGFromResource(getResources(), R.raw.speaker_icon);
//										int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
										int px = 48;
//										Picture speakerIcon = icon.resizePicture(48, 48);
										Picture picture = icon.renderToPicture(px, px);
										PictureDrawable pd = new PictureDrawable(picture);
//										PictureDrawable pd = new CustomPictureDrawable(icon.getPicture(), px, px);
										Bitmap tmpBitmap = Bitmap.createBitmap(pd.getIntrinsicWidth(), pd.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
										Canvas canvas = new Canvas(tmpBitmap);
										picture.draw(canvas);
//										canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, 10, new Paint());
//										speakerBitmap = tmpBitmap;
//										pd.draw(canvas);
//										speakerBitmapDrawable = pd;
										speakerBitmapDrawable = new BitmapDrawable(getResources(), tmpBitmap);
//										speakerBitmapDrawable = new BitmapDrawable(getResources(), tmpBitmap);
										writeLock.unlock();
									}
							}).start();

						synchronized(mutex)
							{
								try
									{
										mutex.wait();
									}
								catch(InterruptedException e)
									{
										e.printStackTrace();
									}
							}

					}

				@Override
				public int getCount()
					{
						return responses.size();
					}

				@Override
				public String getItem(int position)
					{
						return responses.get(position);
					}

				@Override
				public long getItemId(int position)
					{
						return 0;
					}

				@Override
				public View getView(final int position, View convertView, ViewGroup parent)
					{
						LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
						View root;
						if(convertView == null)
							{
								root = inflater.inflate(R.layout.player_response_item_layout, parent, false);
								final ImageView iv = (ImageView) root.findViewById(R.id.response_item_play_icon);
								final TextView tv = (TextView) root.findViewById(R.id.response_item_text);
								tv.setText(responses.get(position));
								new Thread(new Runnable()
									{
										@Override
										public void run()
											{
												final Object mutex = new Object();
												readLock.lock();
												runOnUiThread(new Runnable()
													{
														@Override
														public void run()
															{
																synchronized(mutex)
																	{
																		iv.setImageDrawable(speakerBitmapDrawable);
																		iv.setOnClickListener(new View.OnClickListener()
																			{

																				@Override
																				public void onClick(View v)
																					{
																						speakLine(responses.get(position).replace("\"", ""));
																					}

																			});
//																		iv.setImageBitmap(speakerBitmap);
																		mutex.notify();
																	}
															}
													});
												synchronized(mutex)
													{
														try
															{
																mutex.wait();
															}
														catch(InterruptedException e)
															{
															}
													}
												readLock.unlock();
											}
									}).start();

							}
						else
							{
								root = convertView;
								root.findViewById(R.id.response_item_play_icon).setOnClickListener(new View.OnClickListener()
									{

										@Override
										public void onClick(View v)
											{
												speakLine(responses.get(position).replace("\"", ""));
											}

									});
							}
						return root;
					}

			}
	}
