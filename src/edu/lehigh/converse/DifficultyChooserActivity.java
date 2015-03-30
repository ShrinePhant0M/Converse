package edu.lehigh.converse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import edu.lehigh.converse.conversationplayer.ConversationPlayerMenuActivity;

public class DifficultyChooserActivity extends Activity
	{

//		private static final String[]					levels			= new String[] { "Very Easy", "Easy", "Medium", "Hard", "Very Hard" };
		private static String[]							levels;
		private static final double[]					percentCorrect	= new double[] { 0, 0.5, 0.625, 0.75, 0.875, 1 };
//		private static final Map<String, Double>	levelsMap;
		private ListView								lv;
		private SeekBar									sb;
		public final static RangeMap<Double, String>	rm				= TreeRangeMap.create();

		private final static String						DIFFICULTY		= "DIFFICULTY";
		private String									userID;

		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_difficulty_chooser);
				if(levels == null)
					{
						levels = getResources().getStringArray(R.array.difficulty_levels);
						for(int i = 0; i < levels.length - 1; i++)
							{
								rm.put(Range.closedOpen(percentCorrect[i], percentCorrect[i + 1]), levels[i]);
							}
						rm.put(Range.closed(percentCorrect[percentCorrect.length - 2], percentCorrect[percentCorrect.length - 1]), levels[levels.length - 1]);
					}
				setVolumeControlStream(AudioManager.STREAM_MUSIC);
				lv = (ListView) findViewById(R.id.diffListView);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, levels);
				lv.setOnItemClickListener(new DifficultyClickListener());
				lv.setAdapter(adapter);

				sb = (SeekBar) findViewById(R.id.diffSeekBar);
				userID = MainActivity.getDropboxManager().getLinkedAccount().getUserId();
				sb.setProgress(getSharedPreferences(userID, MODE_PRIVATE).getInt(DIFFICULTY, 85));
				sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
					{
						final TextView	difficulty	= (TextView) findViewById(R.id.diffSeekBarText);
						final TextView	level		= (TextView) findViewById(R.id.diffSeekBarValue);

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
										difficulty.setText(rm.get(((double) progress) / 100));
										level.setText(String.format("%d%%", progress));
									}
							}
					});
				((TextView) findViewById(R.id.diffSeekBarText)).setText(rm.get(((double) sb.getProgress()) / 100));
				((TextView) findViewById(R.id.diffSeekBarValue)).setText(String.format("%d%%", sb.getProgress()));

			}

		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				// Inflate the menu; this adds items to the action bar if it is present.
				getMenuInflater().inflate(R.menu.difficulty_chooser, menu);
				return true;
			}

		@Override
		public void onBackPressed()
			{
				super.onBackPressed();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}

		public void continueClick(View v)
			{
				final Intent i = new Intent(this, ConversationPlayerMenuActivity.class);
				getSharedPreferences(userID, MODE_PRIVATE).edit().putInt(DIFFICULTY, sb.getProgress()).commit();
				i.putExtra("DIFFICULTY_LEVEL", ((double) sb.getProgress()) / 100.0);
				if((double) sb.getProgress() == 0)
					{
						new AlertDialog.Builder(this).setTitle(R.string.difficulty_at_0_).setMessage(getResources().getString(R.string.difficulty_zero_warning)).setPositiveButton(R.string.continue_text, new Dialog.OnClickListener()
							{

								@Override
								public void onClick(DialogInterface dialog, int which)
									{
										dialog.dismiss();
										startActivity(i);
										overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
									}

							}).setNegativeButton(R.string.cancel, new Dialog.OnClickListener()
							{

								@Override
								public void onClick(DialogInterface dialog, int which)
									{
										dialog.dismiss();
									}

							}).show();
					}
				else
					{
						startActivity(i);
						overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
					}

			}

		private class DifficultyClickListener implements OnItemClickListener
			{

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
					{
						Intent i = new Intent(DifficultyChooserActivity.this, ConversationPlayerMenuActivity.class);
						i.putExtra("DIFFICULTY_LEVEL", (double) sb.getProgress());
						startActivity(i);
						overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
					}

			}

	}
