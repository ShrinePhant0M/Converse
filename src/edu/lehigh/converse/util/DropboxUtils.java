package edu.lehigh.converse.util;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.google.common.collect.Queues;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public final class DropboxUtils
	{
		private static ListeningExecutorService	service	= MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

		private DropboxUtils()
			{
//				Ints.tryParse("5");
			}

		/**
		 * Returns all files with names matching {@code regex} recursively starting in the root directory. This method
		 * should not be called on the UI thread or it may block while syncing. This is a convenience method for
		 * {@code findAllFiles(filesystem, DbxPath.ROOT, regex, false, true)}.
		 * 
		 * @param filesystem
		 *            The {@code DbxFileSystem} on which the files will be found.
		 * @param regex
		 *            The regular expression against which to match filenames.
		 * @return A {@code List} containing references to all the files whose names match {@code regex}
		 */
		public static List<DbxFileInfo> findAllFiles(DbxFileSystem filesystem, String regex)
			{
				return findAllFiles(filesystem, DbxPath.ROOT, regex, false, true);
			}

		/**
		 * Returns a {@code ListenableFuture<List<DbxFileInfo>>} representing a list of {@code DbxFileInfo} objects
		 * whose names match {@code regex} recursively starting in the root directory. This method runs asynchronously
		 * and can be safely called from the UI thread. This is a convenience method for
		 * {@code findAllFiles(filesystem, DbxPath.ROOT, regex, false, true)}.
		 * 
		 * @param filesystem
		 *            The {@code DbxFileSystem} on which the files will be found.
		 * @param regex
		 *            The regular expression against which to match filenames.
		 * @return A {@code ListenableFuture} object used to represent a list of {@code DbxFileInfo} objects
		 *         representing all the files whose names match {@code regex}
		 */
		public static ListenableFuture<List<DbxFileInfo>> findAllFilesAsync(final DbxFileSystem filesystem, final String regex)
			{
				return service.submit(new Callable<List<DbxFileInfo>>()
					{

						@Override
						public List<DbxFileInfo> call()
							{
								try
									{
										filesystem.syncNowAndWait();
									}
								catch(DbxException e)
									{
									}
								return findAllFiles(filesystem, regex);
							}

					});
			}

		/**
		 * Returns all files with names matching {@code regex}. If {@code recursive} is {@code true}, returns all files
		 * in the {@code basePath} directory and deeper, including folders if {@code matchFolders} is {@code true}. This
		 * method should not be called on the event thread or it may block while syncing.
		 * 
		 * @param filesystem
		 *            The {@code DbxFileSystem} on which the files will be found.
		 * @param basePath
		 *            The base path from which files will be found. This must be a path to a folder. If {@code basePath}
		 *            is {@code null}, it is assumed to be the root directory.
		 * @param regex
		 *            The regular expression against which to match filenames. This regular expression must match the
		 *            filename exactly; if the regular expression only partially matches a filename, that file will not
		 *            be included in the returned {@code List}.
		 * @param matchFolders
		 *            A flag indicating whether or not folders should be included in the results
		 * @param recursive
		 *            A flag indicating whether or not folders should be searched recursively
		 * @return A {@code List} of {@code DbxFileInfo} objects containing references to all the files whose names
		 *         match {@code regex}
		 * 
		 * @throws IllegalArgumentException
		 *             if {@code filesystem == null}, {@code regex == null} or basePath is not a folder
		 */
		public static List<DbxFileInfo> findAllFiles(DbxFileSystem filesystem, DbxPath basePath, String regex, boolean matchFolders, boolean recursive)
			{
				if(filesystem == null)
					throw new IllegalArgumentException("filesystem must not be null.");
				if(regex == null)
					throw new IllegalArgumentException("regex must not be null.");
				if(basePath == null)
					basePath = DbxPath.ROOT;
				else
					try
						{
							if(!filesystem.isFolder(basePath))
								{
									throw new IllegalArgumentException("basePath must be the DbxPath to a folder!");
								}
						}
					catch(DbxException e1)
						{
							e1.printStackTrace();
						}

				List<DbxFileInfo> matches = new LinkedList<DbxFileInfo>();
				try
					{
						Queue<DbxFileInfo> queue = new LinkedList<DbxFileInfo>(filesystem.listFolder(basePath));
						while(!queue.isEmpty())
							{
								DbxFileInfo info = queue.poll();
								if(recursive && info.isFolder)
									queue.addAll(filesystem.listFolder(info.path));
								if((!info.isFolder || matchFolders) && info.path.getName().matches(regex))
									matches.add(info);
							}
					}
				catch(DbxException e)
					{
						e.printStackTrace();
					}

				return matches;
			}

		/**
		 * 
		 * @param filesystem
		 * @param basePath
		 * @param regex
		 * @param matchFolders
		 * @param recursive
		 * @return
		 */
		public static ListenableFuture<List<DbxFileInfo>> findAllFilesAsync(final DbxFileSystem filesystem, final DbxPath basePath, final String regex, final boolean matchFolders, final boolean recursive)
			{
				return service.submit(new Callable<List<DbxFileInfo>>()
					{

						@Override
						public List<DbxFileInfo> call()
							{
								try
									{
										filesystem.syncNowAndWait();
									}
								catch(DbxException e)
									{
									}
								return findAllFiles(filesystem, basePath, regex, matchFolders, recursive);
							}

					});
			}

		public static Future<List<DbxFileInfo>> findAllFilesAsync(final DbxFileSystem filesystem, final DbxPath basePath, final String regex, final boolean matchFolders, final boolean recursive, final ExecutorService executor)
			{
				return executor.submit(new Callable<List<DbxFileInfo>>()
					{
						@Override
						public List<DbxFileInfo> call()
							{
								try
									{
										filesystem.syncNowAndWait();
									}
								catch(DbxException e)
									{
									}
								return findAllFiles(filesystem, basePath, regex, matchFolders, recursive);
							}
					});
			}

		/**
		 * Determines whether a file whose name matches {@code regex} exists in {@code filesystem}. This is a
		 * convenience method for {@code fileExistsMatching(filesystem, DbxPath.ROOT, regex, matchFolders, true)}
		 * 
		 * @param filesystem
		 *            The {@code DbxFileSystem} on which the files will be found.
		 * @param regex
		 *            The regular expression against which to match filenames. This regular expression must match the
		 *            filename exactly; if the regular expression only partially matches a filename, that filename will
		 *            not be registered as having been found.
		 * @param matchFolders
		 *            A flag indicating whether or not folders should be included in the search
		 * @return
		 */
		public static boolean fileExistsMatching(DbxFileSystem filesystem, String regex, boolean matchFolders)
			{
				return fileExistsMatching(filesystem, DbxPath.ROOT, regex, matchFolders, true);
			}

		/**
		 * Determines whether a file whose name matches {@code regex} exists in {@code filesystem} at {@code basePath},
		 * including folders if {@code matchFolders} is {@code true} and searching recursively if {@code recursive} is
		 * {@code true}
		 * 
		 * @param filesystem
		 *            The {@code DbxFileSystem} on which the files will be found.
		 * @param basePath
		 *            The base path from which files will be found. This must be a path to a folder. If {@code basePath}
		 *            is {@code null}, it is assumed to be the root directory.
		 * @param regex
		 *            The regular expression against which to match filenames. This regular expression must match the
		 *            filename exactly; if the regular expression only partially matches a filename, that filename will
		 *            not be registered as having been found.
		 * @param matchFolders
		 *            A flag indicating whether or not folders should be included in the search
		 * @param recursive
		 *            A flag indicating whether or not folders should be searched recursively
		 * @return {@code true} if at least one file (or folder) exists matching {@code regex}; {@code false} otherwise
		 */
		public static boolean fileExistsMatching(DbxFileSystem filesystem, DbxPath basePath, String regex, boolean matchFolders, boolean recursive)
			{
				try
					{
						Queue<DbxFileInfo> queue = new LinkedList<DbxFileInfo>(filesystem.listFolder(basePath));
						while(!queue.isEmpty())
							{
								DbxFileInfo info = queue.poll();
								if(info.isFolder && recursive)
									queue.addAll(filesystem.listFolder(info.path));
								if((!info.isFolder || matchFolders) && info.path.getName().matches(regex))
									return true;

							}
					}
				catch(DbxException.NotFound e)
					{
						return false;
					}
				catch(DbxException e)
					{
						e.printStackTrace();
					}
				return false;
			}

		public static DbxFileInfo firstFileMatching(DbxFileSystem filesystem, DbxPath basePath, String regex, boolean matchFolders, boolean recursive)
			{
				if(filesystem == null)
					throw new IllegalArgumentException("filesystem must not be null.");
				if(regex == null)
					throw new IllegalArgumentException("regex must not be null.");
				if(basePath == null)
					basePath = DbxPath.ROOT;
				else
					try
						{
							if(!filesystem.exists(basePath))
								return null;
							if(!filesystem.isFolder(basePath))
								{
									throw new IllegalArgumentException("basePath must be the DbxPath to a folder!");
								}
						}
					catch(DbxException e1)
						{
							e1.printStackTrace();
						}

				try
					{
						Queue<DbxFileInfo> queue = new LinkedList<DbxFileInfo>(filesystem.listFolder(basePath));
						while(!queue.isEmpty())
							{
								DbxFileInfo info = queue.poll();
								if(recursive && info.isFolder)
									queue.addAll(filesystem.listFolder(info.path));
								if((!info.isFolder || matchFolders) && info.path.getName().matches(regex))
									return info;
							}
					}
				catch(DbxException e)
					{
						e.printStackTrace();
					}
				return null;
			}

		public static UnmodifiableIterator<DbxFileInfo> findAllFilesLazily(final DbxFileSystem filesystem, final DbxPath basePath, final String regex, final boolean matchFolders, final boolean recursive)
			{
				if(filesystem == null)
					throw new IllegalArgumentException("filesystem must not be null.");
				if(regex == null)
					throw new IllegalArgumentException("regex must not be null.");
				if(basePath != null)
					{
						try
							{
								if(!filesystem.isFolder(basePath))
									{
										throw new IllegalArgumentException("basePath must be the DbxPath to a folder!");
									}
							}
						catch(DbxException e1)
							{
								e1.printStackTrace();
							}
					}
				UnmodifiableIterator<DbxFileInfo> iter = null;
				try
					{
						iter = new UnmodifiableIterator<DbxFileInfo>()
							{
								Queue<DbxFileInfo>	queue			= new LinkedList<DbxFileInfo>(filesystem.listFolder(basePath == null ? DbxPath.ROOT : basePath));
//										DbxFileInfo match = null;
								Queue<DbxFileInfo>	matchSingleton	= Queues.newLinkedBlockingQueue(1);

								@Override
								public boolean hasNext()
									{
										if(matchSingleton.peek() != null)
											return true;
										while(!queue.isEmpty())
											{
												DbxFileInfo info = queue.remove();
												if(recursive && info.isFolder)
													try
														{
															queue.addAll(filesystem.listFolder(info.path));
														}
													catch(DbxException e)
														{
															e.printStackTrace();
														}
												if((!info.isFolder || matchFolders) && info.path.getName().matches(regex))
													{
														matchSingleton.add(info);
														return true;
													}
											}
										return false;
									}

								@Override
								public DbxFileInfo next()
									{
										if(hasNext())
											return matchSingleton.remove();
										throw new NoSuchElementException();
									}

							};
					}
				catch(DbxException e)
					{
						e.printStackTrace();
					}
				return iter;
			}

		public static List<DbxFileInfo> subfiles(DbxFileSystem filesystem, DbxPath basePath, boolean includeFolders, boolean recursive)
			{
				return findAllFiles(filesystem, basePath, ".*", includeFolders, recursive);
			}

		@SuppressWarnings("unused")
		private static final boolean verifyArguments(DbxFileSystem filesystem, DbxPath basePath, String regex) throws IllegalArgumentException
			{
				boolean check = false;
				check |= filesystem == null | basePath == null;
				if(filesystem.isShutDown())
					throw new IllegalArgumentException();
				try
					{
						Pattern.compile(regex);
					}
				catch(PatternSyntaxException e)
					{
						throw new IllegalArgumentException(e);
					}
				return !check;
			}

		public static boolean isConnectionAvailable(Context context)
			{
				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo info = cm.getActiveNetworkInfo();
				return info != null && info.isConnected();
			}

	}
