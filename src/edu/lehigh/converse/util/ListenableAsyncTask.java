package edu.lehigh.converse.util;

import java.util.concurrent.CopyOnWriteArraySet;

import android.os.AsyncTask;

public abstract class ListenableAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
	{
		private final CopyOnWriteArraySet<OnTaskCompletedListener<Result>> listeners = new CopyOnWriteArraySet<OnTaskCompletedListener<Result>>();
		public final ListenableAsyncTask<Params, Progress, Result> addTaskCompletedListener(OnTaskCompletedListener<Result> listener)
			{
				listeners.add(listener);
				return this;
			}
		@Override
		protected final void onPostExecute(Result result)
			{
				super.onPostExecute(result);
				for(OnTaskCompletedListener<Result> l : listeners)
					{
						l.onTaskCompleted(result);
					}
				onPostExecution(result);
			}
		
		public void onPostExecution(Result result)
			{
				
			}
		
		public interface OnTaskCompletedListener<Result>
		{
			public void onTaskCompleted(Result result);
		}
		
	}
