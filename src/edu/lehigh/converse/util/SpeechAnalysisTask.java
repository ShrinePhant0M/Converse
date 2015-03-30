package edu.lehigh.converse.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.apache.commons.codec.language.DoubleMetaphone;

import android.content.Context;
import android.util.Log;

public class SpeechAnalysisTask extends ListenableAsyncTask<List<String>, Double, Double>
	{
		private static final String							TAG			= "SpeechAnalysisTask";
		private final List<String>							expectedWords;
		private static final DoubleMetaphone				meta		= new DoubleMetaphone();
		private final Context								c;
		private final int									pos;

		private final List<OnTaskCompletedListener<Double>>	listeners	= new CopyOnWriteArrayList<OnTaskCompletedListener<Double>>();

		public SpeechAnalysisTask(Context c, int positionChosen, List<String> expectedWords)
			{
				if(expectedWords != null)
					this.expectedWords = new LinkedList<String>(expectedWords);
				else
					throw new IllegalArgumentException();
				this.c = c;
				this.pos = positionChosen;
			}

		@Override
		protected Double doInBackground(List<String>... params)
			{
				if(params.length != 1)
					throw new IllegalArgumentException("This method takes exactly one parameter");
				List<String[]> expected = getMetaphonics(expectedWords);
				List<String[]> spoken = getMetaphonics(params[0]);
				Log.i("SpeechAnalysisTask", expected.toString());
				Log.i("SpeechAnalysisTask", spoken.toString());
				StringBuilder esb = new StringBuilder();
				StringBuilder ssb = new StringBuilder();
				if(Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage()))
					{
						Log.w("EXPECTED", Arrays.deepToString(expected.toArray()));
						for(String[] s : expected)
							esb.append(s[0]).append(' ');
						esb.deleteCharAt(esb.length() - 1);
						Log.w("SPOKEN", Arrays.deepToString(spoken.toArray()));

						for(String[] s : spoken)
							ssb.append(s[0]).append(' ');
					}
				else
					{
						for(String[] s : expected)
							esb.append(s[0]).append(' ');
						esb.deleteCharAt(esb.length() - 1);
						for(String[] s : spoken)
							ssb.append(s[0]).append(' ');
					}
				ssb.deleteCharAt(ssb.length() - 1);
				double distance = computeLevenshteinDistance(esb.toString(), ssb.toString());
				Log.i("SpeechAnalysisTask", "Levenshtein distance is: " + distance);
				return Math.max(0, 1.0 - (distance / (double) esb.toString().length()));
			}

		private static List<String[]> getMetaphonics(List<String> words)
			{
				List<String[]> newWords = new LinkedList<String[]>();
				if(Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage()))
					{
						HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
						format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
						format.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);
						format.setVCharType(HanyuPinyinVCharType.WITH_U_AND_COLON);
						for(String s : words)
							{
								if(s.length() != 1)
									throw new IllegalArgumentException("Each string must be a single Chinese character!");
								try
									{
										String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(s.charAt(0), format);
										if(pinyin == null)
											pinyin = new String[]{s};
										newWords.add(pinyin);
									}
								catch(BadHanyuPinyinOutputFormatCombination e)
									{
										e.printStackTrace();
									}
							}
					}
				else
					{
						for(String s : words)
							{
								newWords.add(new String[] { meta.doubleMetaphone(s) });
							}
					}
				return newWords;
			}
		

		private static int levenshteinDistance(String s1, String s2)
			{
				if(s1.equals(s2))
					return 0;
				if(s1.length() == 0)
					return s2.length();
				if(s2.length() == 0)
					return s1.length();

				int[] v0 = new int[s2.length()];
				int[] v1 = new int[s2.length()];

				for(int i = 0; i < v0.length; i++)
					v0[i] = i;

				for(int i = 0; i < s1.length(); i++)
					{
						v1[0] = i + 1;

						for(int j = 0; j < s2.length(); j++)
							{
								int cost = (s1.charAt(i) == s2.charAt(i)) ? 0 : 1;
								v1[j + 1] = min(v1[j] + 1, v0[j + 1] + 1, v0[j] + cost);
							}

						for(int j = 0; j < v0.length; j++)
							v0[j] = v1[j];
					}
				return v1[s2.length()];
			}

		private static int minimum(int a, int b, int c)
			{
				return Math.min(Math.min(a, b), c);
			}

		public static int computeLevenshteinDistance(String str1, String str2)
			{
				int[][] distance = new int[str1.length() + 1][str2.length() + 1];

				for(int i = 0; i <= str1.length(); i++)
					distance[i][0] = i;
				for(int j = 1; j <= str2.length(); j++)
					distance[0][j] = j;

				for(int i = 1; i <= str1.length(); i++)
					for(int j = 1; j <= str2.length(); j++)
						distance[i][j] = minimum(distance[i - 1][j] + 1, distance[i][j - 1] + 1, distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));

				return distance[str1.length()][str2.length()];
			}

		private static final int min(int... ints)
			{
				int min = Integer.MAX_VALUE;
				for(int i : ints)
					{
						if(i < min)
							min = i;
					}
				return min;
			}

	}
