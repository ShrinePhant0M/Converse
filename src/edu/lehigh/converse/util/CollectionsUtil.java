package edu.lehigh.converse.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class CollectionsUtil
	{
		private CollectionsUtil()
			{
			}

		public static <K, V> String toString(Map<K, V> m)
			{
				Set<Entry<K, V>> entries = m.entrySet();
				StringBuilder sb = new StringBuilder();
				sb.append('[');
				boolean first = true;
				for(Entry<K, V> e : entries)
					{
						if(!first)
							sb.append("; ");
						sb.append(String.format("%s : %s", e.getKey().toString(), e.getValue().toString()));
						first = false;
					}
				sb.append(']');
				return sb.toString();
			}

		public static <K, V> Map<K, V> mapKeys(List<K> keys, List<V> values)
			{
				if(keys.size() != values.size())
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<K, V> map = new HashMap<K, V>();
				Iterator<K> keysIterator = keys.iterator();
				Iterator<V> valuesIterator = values.iterator();
				while(keysIterator.hasNext() && valuesIterator.hasNext())
					{
						map.put(keysIterator.next(), valuesIterator.next());
					}
				return map;
			}

		public static <K, V> Map<K, V> mapKeys(K[] keys, V[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<K, V> map = new HashMap<K, V>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <K> Map<K, Byte> mapKeys(K[] keys, byte[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<K, Byte> map = new HashMap<K, Byte>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <K> Map<K, Character> mapKeys(K[] keys, char[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<K, Character> map = new HashMap<K, Character>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <K> Map<K, Short> mapKeys(K[] keys, short[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<K, Short> map = new HashMap<K, Short>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <K> Map<K, Integer> mapKeys(K[] keys, int[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<K, Integer> map = new HashMap<K, Integer>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <K> Map<K, Long> mapKeys(K[] keys, long[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<K, Long> map = new HashMap<K, Long>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <K> Map<K, Float> mapKeys(K[] keys, float[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<K, Float> map = new HashMap<K, Float>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <K> Map<K, Double> mapKeys(K[] keys, double[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<K, Double> map = new HashMap<K, Double>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <V> Map<Boolean, V> mapKeys(boolean[] keys, V[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Boolean, V> map = new HashMap<Boolean, V>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <V> Map<Byte, V> mapKeys(byte[] keys, V[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Byte, V> map = new HashMap<Byte, V>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <V> Map<Character, V> mapKeys(char[] keys, V[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Character, V> map = new HashMap<Character, V>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <V> Map<Short, V> mapKeys(short[] keys, V[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Short, V> map = new HashMap<Short, V>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <V> Map<Integer, V> mapKeys(int[] keys, V[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Integer, V> map = new HashMap<Integer, V>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <V> Map<Long, V> mapKeys(long[] keys, V[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Long, V> map = new HashMap<Long, V>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <V> Map<Float, V> mapKeys(float[] keys, V[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Float, V> map = new HashMap<Float, V>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static <V> Map<Double, V> mapKeys(double[] keys, V[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Double, V> map = new HashMap<Double, V>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Boolean, Boolean> mapKeys(boolean[] keys, boolean[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Boolean, Boolean> map = new HashMap<Boolean, Boolean>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Boolean, Character> mapKeys(boolean[] keys, char[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Boolean, Character> map = new HashMap<Boolean, Character>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Boolean, Byte> mapKeys(boolean[] keys, byte[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Boolean, Byte> map = new HashMap<Boolean, Byte>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Boolean, Short> mapKeys(boolean[] keys, short[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Boolean, Short> map = new HashMap<Boolean, Short>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Boolean, Integer> mapKeys(boolean[] keys, int[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Boolean, Integer> map = new HashMap<Boolean, Integer>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Boolean, Long> mapKeys(boolean[] keys, long[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Boolean, Long> map = new HashMap<Boolean, Long>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Boolean, Float> mapKeys(boolean[] keys, float[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Boolean, Float> map = new HashMap<Boolean, Float>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Boolean, Double> mapKeys(boolean[] keys, double[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Boolean, Double> map = new HashMap<Boolean, Double>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Character, Character> mapKeys(char[] keys, char[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Character, Character> map = new HashMap<Character, Character>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Character, Byte> mapKeys(char[] keys, byte[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Character, Byte> map = new HashMap<Character, Byte>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Character, Short> mapKeys(char[] keys, short[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Character, Short> map = new HashMap<Character, Short>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Character, Integer> mapKeys(char[] keys, int[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Character, Integer> map = new HashMap<Character, Integer>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Character, Long> mapKeys(char[] keys, long[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Character, Long> map = new HashMap<Character, Long>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Character, Float> mapKeys(char[] keys, float[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Character, Float> map = new HashMap<Character, Float>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Character, Double> mapKeys(char[] keys, double[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Character, Double> map = new HashMap<Character, Double>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Byte, Byte> mapKeys(byte[] keys, byte[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Byte, Byte> map = new HashMap<Byte, Byte>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Byte, Short> mapKeys(byte[] keys, short[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Byte, Short> map = new HashMap<Byte, Short>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Byte, Integer> mapKeys(byte[] keys, int[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Byte, Integer> map = new HashMap<Byte, Integer>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Byte, Long> mapKeys(byte[] keys, long[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Byte, Long> map = new HashMap<Byte, Long>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Byte, Float> mapKeys(byte[] keys, float[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Byte, Float> map = new HashMap<Byte, Float>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Byte, Double> mapKeys(byte[] keys, double[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Byte, Double> map = new HashMap<Byte, Double>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Short, Short> mapKeys(short[] keys, short[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Short, Short> map = new HashMap<Short, Short>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Short, Integer> mapKeys(short[] keys, int[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Short, Integer> map = new HashMap<Short, Integer>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Short, Long> mapKeys(short[] keys, long[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Short, Long> map = new HashMap<Short, Long>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Short, Float> mapKeys(short[] keys, float[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Short, Float> map = new HashMap<Short, Float>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Short, Double> mapKeys(short[] keys, double[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Short, Double> map = new HashMap<Short, Double>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Integer, Integer> mapKeys(int[] keys, int[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Integer, Integer> map = new HashMap<Integer, Integer>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Integer, Long> mapKeys(int[] keys, long[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Integer, Long> map = new HashMap<Integer, Long>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Integer, Float> mapKeys(int[] keys, float[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Integer, Float> map = new HashMap<Integer, Float>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Integer, Double> mapKeys(int[] keys, double[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Integer, Double> map = new HashMap<Integer, Double>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Long, Long> mapKeys(long[] keys, long[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Long, Long> map = new HashMap<Long, Long>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Long, Float> mapKeys(long[] keys, float[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Long, Float> map = new HashMap<Long, Float>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Long, Double> mapKeys(long[] keys, double[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Long, Double> map = new HashMap<Long, Double>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Float, Float> mapKeys(float[] keys, float[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Float, Float> map = new HashMap<Float, Float>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Float, Double> mapKeys(float[] keys, double[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Float, Double> map = new HashMap<Float, Double>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

		public static Map<Double, Double> mapKeys(double[] keys, double[] values)
			{
				if(keys.length != values.length)
					throw new IllegalArgumentException("Number of keys does not equal number of values!");
				Map<Double, Double> map = new HashMap<Double, Double>();
				for(int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
				return map;
			}

	}
