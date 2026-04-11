package com.zhan_dui.sync;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.asyncclient.EvernoteCallback;
import com.evernote.edam.error.EDAMErrorCode;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteCollectionCounts;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.User;
import com.evernote.thrift.TException;
import com.evernote.thrift.transport.TTransportException;
import com.zhan_dui.data.MemoEntity;
import com.zhan_dui.evermemo.BuildConfig;
import com.zhan_dui.repository.MemoRepository;
import com.zhan_dui.utils.Logger;
import com.zhan_dui.utils.SPManager;

import java.util.List;
import java.util.Map;

public class Evernote {

	public String LogTag = "EverNote";
	public Activity mContext;
	private static final String CONSUMER_KEY = BuildConfig.EVERNOTE_CONSUMER_KEY;
	private static final String CONSUMER_SECRET = BuildConfig.EVERNOTE_CONSUMER_SECRET;
	private static final String NOTEBOOK_NAME = "EverMemo";
	public static final String EVERNOTE_TOKEN = "Evernote_Token";
	public static final String EVERNOTE_TOKEN_TIME = "Evernote_Token_Time";
	public static final String EVERNOTE_USER_NAME = "Evernote_User_Name";
	public static final String EVERNOTE_USER_EMAIL = "Evernote_User_Email";
	public static final String EVERNOTE_NOTEBOOK_GUID = "Evenote_Note_Guid";
	public static final String LAST_SYNC_DOWN = "LAST_SYNC_DOWN";
	public static boolean SyncingUp = false;
	public static boolean SyncingDown = false;

	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.PRODUCTION;
//	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;
    /*
     * Set this to true if you want to allow linked notebooks for accounts that
     * can only access a single notebook.
     */
    private static final boolean SUPPORT_APP_LINKED_NOTEBOOKS = true;
	private EvernoteSession mEvernoteSession;
//	private SharedPreferences mSharedPreferences;
//	private ContentResolver mContentResolver;
	private MemoRepository memoRepository;
	private EvernoteLoginCallback mEvernoteLoginCallback;

	public Evernote(Activity context) {
		mContext = context;
//		mContentResolver = context.getContentResolver();
        memoRepository = new MemoRepository(context);
//		mSharedPreferences = PreferenceManager
//				.getDefaultSharedPreferences(context);
//		mEvernoteSession = EvernoteSession.getInstance(mContext, CONSUMER_KEY,
//				CONSUMER_SECRET, EVERNOTE_SERVICE);
        mEvernoteSession = new EvernoteSession.Builder(context)
                .setEvernoteService(EVERNOTE_SERVICE)
                .setSupportAppLinkedNotebooks(SUPPORT_APP_LINKED_NOTEBOOKS)
                .setForceAuthenticationInThirdPartyApp(true)
//                .setLocale(Locale.SIMPLIFIED_CHINESE)
                .build(CONSUMER_KEY, CONSUMER_SECRET)
                .asSingleton();
	}

	public Evernote(Activity context, EvernoteLoginCallback l) {
		this(context);
		mEvernoteLoginCallback = l;
	}

	public interface EvernoteLoginCallback {
		public void onLoginResult(Boolean result);

		public void onUserinfo(Boolean result, User user);

		public void onLogout(Boolean reuslt);
	}

	public boolean isLogin() {
		return mEvernoteSession.isLoggedIn();
	}

	public void auth() {
		mEvernoteSession.authenticate(mContext);
	}

	public void onAuthFinish(int resultCode) {
		if (resultCode == Activity.RESULT_OK) {
//			mSharedPreferences.edit()
//					.putString(EVERNOTE_TOKEN, mEvernoteSession.getAuthToken())
//					.putLong(EVERNOTE_TOKEN_TIME, System.currentTimeMillis())
//					.commit();
            SPManager.getInstance().putString(EVERNOTE_TOKEN, mEvernoteSession.getAuthToken());
            SPManager.getInstance().putLong(EVERNOTE_TOKEN_TIME, System.currentTimeMillis());
			getUserInfo();
			if (mEvernoteLoginCallback != null) {
				mEvernoteLoginCallback.onLoginResult(true);
			}
			sync(true, true, null);
		} else {
			if (mEvernoteLoginCallback != null) {
				mEvernoteLoginCallback.onLoginResult(false);
			}
		}
	}

	public String getUsername() {
//		return mSharedPreferences.getString(EVERNOTE_USER_NAME, null);
        return SPManager.getInstance().getString(EVERNOTE_USER_NAME, null);
	}

	public void getUserInfo() {
		if (mEvernoteSession.isLoggedIn()) {
			try {
				mEvernoteSession.getEvernoteClientFactory().getUserStoreClient()
						.getUserAsync(new EvernoteCallback<User>() {

							@Override
							public void onSuccess(User user) {
//								mSharedPreferences
//										.edit()
//										.putString(EVERNOTE_USER_NAME, user.getUsername())
//										.putString(EVERNOTE_USER_EMAIL, user.getEmail())
//                                        .commit();
                                SPManager.getInstance().putString(EVERNOTE_USER_NAME, user.getUsername());
                                SPManager.getInstance().putString(EVERNOTE_USER_EMAIL, user.getEmail());
								if (mEvernoteLoginCallback != null) {
									mEvernoteLoginCallback.onUserinfo(true, user);
								}
							}

							@Override
							public void onException(Exception exception) {
								if (mEvernoteLoginCallback != null) {
									mEvernoteLoginCallback.onUserinfo(false, null);
								}
							}
						});
			} catch (IllegalStateException e) {
				e.printStackTrace();
				if (mEvernoteLoginCallback != null) {
					mEvernoteLoginCallback.onUserinfo(false, null);
				}
//			} catch (TTransportException e) {
//				e.printStackTrace();
//				if (mEvernoteLoginCallback != null) {
//					mEvernoteLoginCallback.onUserinfo(false, null);
//				}
			}
		}
	}

	public void Logout() {
//		try {
			mEvernoteSession.logOut();
//			mSharedPreferences.edit().remove(EVERNOTE_USER_NAME)
//					.remove(EVERNOTE_NOTEBOOK_GUID).remove(EVERNOTE_USER_EMAIL)
//					.commit();
        SPManager.getInstance().remove(EVERNOTE_USER_NAME);
        SPManager.getInstance().remove(EVERNOTE_NOTEBOOK_GUID);
        SPManager.getInstance().remove(EVERNOTE_USER_EMAIL);
			if (mEvernoteLoginCallback != null) {
				mEvernoteLoginCallback.onLogout(true);
			}

//		} catch (InvalidAuthenticationException e) {
//			if (mEvernoteLoginCallback != null) {
//				mEvernoteLoginCallback.onLogout(false);
//			}
//		}
	}

	public boolean isNotebookExsist(String guid, String name) throws Exception {
		boolean result = false;
		try {
//			Notebook notebook = mEvernoteSession.getClientFactory()
//					.createNoteStore()
//					.getNotebook(mEvernoteSession.getAuthToken(), guid);
            Notebook notebook = mEvernoteSession.getEvernoteClientFactory().getNoteStoreClient().getNotebook(guid);
			if (notebook.getName().equals(name)) {
				result = true;
				Logger.e(LogTag, guid + "笔记本存在");
//				mSharedPreferences.edit()
//						.putString(EVERNOTE_NOTEBOOK_GUID, notebook.getGuid())
//						.commit();
                SPManager.getInstance().putString(EVERNOTE_NOTEBOOK_GUID, notebook.getGuid());
			}
		} catch (EDAMNotFoundException e) {
			e.printStackTrace();
			if (e.getIdentifier().equals("Notebook.guid")) {
				result = false;
				Logger.e(LogTag, guid + "笔记本不存在");
			}
		}
		return result;
	}

	/**
	 * create a notebook by bookname
	 * 
	 * @param bookname
	 * @return
	 * @throws Exception
	 */
	public boolean createNotebook(String bookname) throws Exception {
		Notebook notebook = new Notebook();
		notebook.setDefaultNotebook(false);
		notebook.setName(bookname);
		boolean result = false;
		try {
			Notebook resultNotebook = mEvernoteSession.getEvernoteClientFactory()
                    .getNoteStoreClient()
					.createNotebook(notebook);
			result = true;
			Logger.e(LogTag, "Notebook" + bookname + "不存在，创建成功");
//			mSharedPreferences
//					.edit()
//					.putString(EVERNOTE_NOTEBOOK_GUID, resultNotebook.getGuid())
//					.commit();
            SPManager.getInstance().putString(EVERNOTE_NOTEBOOK_GUID, resultNotebook.getGuid());
		} catch (EDAMUserException e) {
			if (e.getErrorCode() == EDAMErrorCode.DATA_CONFLICT) {
				result = true;
				Logger.e(LogTag, "已经存在，无需创建");
			}
		} catch (Exception e) {
			Logger.e(LogTag, "传输出现错误");
			throw e;
		}
		return result;
	}

	private Note createNote(MemoEntity memo) throws Exception {
		try {
			Note note = memo.toNote();
			note.setNotebookGuid(SPManager.getInstance().getString(EVERNOTE_NOTEBOOK_GUID, null));
			Note responseNote = mEvernoteSession.getEvernoteClientFactory()
                    .getNoteStoreClient()
					.createNote(note);
			Logger.e(LogTag, "Note创建成功");
            memo.enid = responseNote.getGuid();
            memo.syncStatus = MemoEntity.NEED_NOTHING;
            memo.updatedTime = responseNote.getUpdated();
            memo.hash = responseNote.getContentHash();
            memoRepository.updateMemo(memo);
//			ContentValues values = new ContentValues();
//			values.put(MemoDB.ENID, responseNote.getGuid());
//			values.put(MemoDB.SYNCSTATUS, Memo.NEED_NOTHING);
//			values.put(MemoDB.UPDATEDTIME, responseNote.getUpdated());
//			values.put(MemoDB.HASH, responseNote.getContentHash());
//			mContentResolver.update(
//					ContentUris.withAppendedId(MemoProvider.MEMO_URI,
//							memo.getId()), values, null, null);
			return responseNote;
		} catch (EDAMUserException e) {
			throw new Exception("Note格式不合理");
		} catch (EDAMNotFoundException e) {
			throw new Exception("笔记本不存在");
		} catch (Exception e) {
			throw e;
		}
	}

	private boolean deleteNote(Note note) {
		if (note.getGuid() == null) {
			Logger.e(LogTag, "GUID是空，无需删除");
			return true;
		} else {
			try {
				mEvernoteSession.getEvernoteClientFactory()
                        .getNoteStoreClient()
						.deleteNote(note.getGuid());
				Logger.e(LogTag, "Note删除成功");
				return true;
			} catch (EDAMUserException e) {
				Logger.e(LogTag, "Note早已被删除，说明删除成功");
				return true;
			} catch (EDAMNotFoundException e) {
				Logger.e(LogTag, "Note未找到，说明无需删除");
				return true;
			} catch (Exception e) {
				Logger.e(LogTag, "传输失败，说明删除失败");
				return false;
			}
		}
	}

	private Note updateNote(MemoEntity memo) throws Exception {
		try {
			Note responseNote = mEvernoteSession.getEvernoteClientFactory()
                    .getNoteStoreClient()
					.updateNote(memo.toUpdateNote());
//			ContentValues values = new ContentValues();
//			values.put(MemoDB.SYNCSTATUS, Memo.NEED_NOTHING);
//			values.put(MemoDB.UPDATEDTIME, responseNote.getUpdated());
//			values.put(MemoDB.HASH, responseNote.getContentHash());
//			mContentResolver.update(
//					ContentUris.withAppendedId(MemoProvider.MEMO_URI,
//							memo.getId()), values, null, null);
            memo.syncStatus = MemoEntity.NEED_NOTHING;
            memo.updatedTime = responseNote.getUpdated();
            memo.hash = responseNote.getContentHash();
			Logger.e(LogTag, "Note更新成功");
			return responseNote;
		} catch (EDAMUserException e) {
			Logger.e(LogTag, "数据格式有误");
			throw new Exception(e.getCause());
		} catch (EDAMNotFoundException e) {
			Logger.e(LogTag, "Note根据GUID没有找到:" + e.getCause());
			throw new Exception("Note未找到");
		} catch (Exception e) {
			Logger.e(LogTag, "传输出现错误:" + e.getCause());
			throw new Exception("传输出现错误:" + e.getCause());
		}
	}

	private void makeSureNotebookExsits(String NotebookName) throws Exception {
		try {
//			if (mSharedPreferences.contains(EVERNOTE_NOTEBOOK_GUID)) {
			if (SPManager.getInstance().contains(EVERNOTE_NOTEBOOK_GUID)) {
				if (!isNotebookExsist(SPManager.getInstance().getString(EVERNOTE_NOTEBOOK_GUID, ""), NOTEBOOK_NAME)) {
					createNotebook(NOTEBOOK_NAME);
				}
			} else {
				List<Notebook> books = mEvernoteSession.getEvernoteClientFactory()
                        .getNoteStoreClient()
						.listNotebooks();
				int count = books.size();
				for (int i = 0; i < count; i++) {
					Notebook book = books.get(i);
					if (book.getName().equals(NotebookName)) {
//						mSharedPreferences.edit()
//								.putString(EVERNOTE_NOTEBOOK_GUID, book.getGuid())
//                                .commit();
                        SPManager.getInstance().putString(EVERNOTE_NOTEBOOK_GUID, book.getGuid());
						return;
					}
				}
				createNotebook(NOTEBOOK_NAME);
			}

		} catch (Exception e) {
			Logger.e(LogTag, "检查笔记本是否存和创建笔记本的时候出现异常");
			throw e;
		}
	}

	private void downloadNote(String guid) {
		Logger.e(LogTag, "准备添加:" + guid);
		try {
            Note note = mEvernoteSession.getEvernoteClientFactory()
                    .getNoteStoreClient()
                    .getNote(guid, true, false, false, false);
			Logger.e("获取到的文本：" + note.getContent());

//			ContentValues values = MemoEntity.buildInsertMemoFromNote(note)
//					.toInsertContentValues();
//			mContentResolver.insert(MemoProvider.MEMO_URI, values);
            MemoEntity memo = MemoEntity.buildInsertMemoFromNote(note);
            memoRepository.insertMemo(memo);
		} catch (TTransportException e) {
		} catch (EDAMUserException e) {
		} catch (EDAMSystemException e) {
		} catch (EDAMNotFoundException e) {
		} catch (TException e) {
		}
	}

	private void updateLocalNote(String guid, MemoEntity memo) {
		Logger.e(LogTag, "准备更新:" + guid);
		try {
			Note note = mEvernoteSession.getEvernoteClientFactory()
                    .getNoteStoreClient()
					.getNote(guid, true, false, false, false);
//			ContentValues contentValues = memo.toUpdateContentValues();
//			mContentResolver.update(
//					ContentUris.withAppendedId(MemoProvider.MEMO_URI, _id),
//					contentValues, null, null);
			memo.buildFromNote(note);
            memoRepository.updateMemo(memo);
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (EDAMUserException e) {
			e.printStackTrace();
		} catch (EDAMSystemException e) {
			e.printStackTrace();
		} catch (EDAMNotFoundException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}

	}

	private void syncDown() {
		if (SyncingDown) {
			return;
		}
		SyncingDown = true;
		NoteFilter noteFilter = new NoteFilter();
//		String guid = mSharedPreferences.getString(EVERNOTE_NOTEBOOK_GUID, "");
		String guid = SPManager.getInstance().getString(EVERNOTE_NOTEBOOK_GUID, "");
		noteFilter.setNotebookGuid(guid);
		NotesMetadataResultSpec notesMetadataResultSpec = new NotesMetadataResultSpec();
		notesMetadataResultSpec.setIncludeUpdated(true);
		try {
			NoteCollectionCounts noteCollectionCounts = mEvernoteSession.getEvernoteClientFactory()
                    .getNoteStoreClient()
					.findNoteCounts(noteFilter, false);
			Map<String, Integer> maps = noteCollectionCounts
					.getNotebookCounts();
			if (maps == null || maps.size() == 0)
				return;
			int maxcount = maps.get(guid);
			NotesMetadataList list = mEvernoteSession.getEvernoteClientFactory()
                    .getNoteStoreClient()
					.findNotesMetadata(noteFilter, 0, maxcount, notesMetadataResultSpec);

//			for (int i = 0; i < list.getNotes().size(); i++) {
//				NoteMetadata note = list.getNotes().get(i);
//				Cursor cursor = mContentResolver.query(MemoProvider.MEMO_URI,
//						new String[] { MemoDB.UPDATEDTIME, MemoDB.ID },
//						MemoDB.ENID + "=?", new String[] { note.getGuid() },
//						null);
//				if (cursor.getCount() != 0) {
//					cursor.moveToNext();
//					if (cursor.getLong(cursor.getColumnIndex(MemoDB.UPDATEDTIME)) != note.getUpdated()) {
//						// 更新数据
//						updateLocalNote(note.getGuid(), cursor.getInt(cursor.getColumnIndex(MemoDB.ID)));
//					}
//				} else {
//					// 添加数据
//					downloadNote(note.getGuid());
//				}
//				cursor.close();
//			}
			for (NoteMetadata note : list.getNotes()) {
				List<MemoEntity> memos = memoRepository.getMemoByEnid(note.getGuid());
				if (memos.size() > 0) {
					MemoEntity memo = memos.get(0);
					if (memo.updatedTime != note.getUpdated()) {
						//更新数据
						updateLocalNote(note.getGuid(), memo);
					}
				} else {
					downloadNote(note.getGuid());
				}
			}

		} catch (TTransportException e) {
		} catch (EDAMUserException e) {
		} catch (EDAMSystemException e) {
		} catch (EDAMNotFoundException e) {
		} catch (TException e) {
		} finally {
			SyncingDown = false;
		}
	}

	private void syncUp() {
		if (SyncingUp) {
			Logger.e(LogTag, "正在同步");
			return;
		}
		Logger.e(LogTag, "开始同步");
		SyncingUp = true;
//		Cursor cursor = mContentResolver.query(MemoProvider.ALL_MEMO_URI, null, null, null, null);
//		while (cursor.moveToNext()) {
//			Memo memo = new Memo(cursor);
//			if (memo.isNeedSyncDelete()) {
//				if (deleteNote(memo.toDeleteNote())) {
//					ContentValues values = new ContentValues();
//					values.put(MemoDB.SYNCSTATUS, Memo.NEED_NOTHING);
//					mContentResolver.update(ContentUris.withAppendedId(
//							MemoProvider.MEMO_URI, memo.getId()), values, null,
//							null);
//				}
//			} else {
//				if (memo.isNeedSyncUp()) {
//					if (memo.getEnid() != null && memo.getEnid().length() != 0) {
//						try {
//							updateNote(memo);
//						} catch (Exception e) {
//							Logger.e(LogTag, "尝试更新的时候出现错误:" + e.getCause());
//							continue;
//						}
//					} else {
//						try {
//							createNote(memo);
//						} catch (Exception e) {
//							Logger.e(LogTag,
//									"尝试创建新的Note的时候出现错误:" + e.getCause());
//							continue;
//						}
//					}
//				}
//			}
//		}
		List<MemoEntity> memos = memoRepository.getAllMemosWithDelete();
		for (MemoEntity memo : memos) {
			if (memo.isNeedSyncDelete()) {
				if (deleteNote(memo.toDeleteNote())) {
					memo.syncStatus = MemoEntity.NEED_NOTHING;
					memoRepository.updateMemo(memo);
				}
			} else {
				if (memo.isNeedSyncUp()) {
					if (memo.enid != null && memo.enid.length() != 0) {
						try {
							updateNote(memo);
						} catch (Exception e) {
							Logger.e(LogTag, "尝试更新的时候出现错误:" + e.getCause());
							continue;
						}
					} else {
						try {
							createNote(memo);
						} catch (Exception e) {
							Logger.e(LogTag, "尝试创建新的Note的时候出现错误:" + e.getCause());
							continue;
						}
					}
				}
			}
		}

		SyncingUp = false;
//		cursor.close();
	}

	public synchronized void sync(final boolean syncUp, final boolean syncDown,
			Handler hanler) {
		if (hanler != null) {
			hanler.sendEmptyMessage(SYNC_START);
		}
		toSync(syncUp, syncDown, hanler);
	}

	private synchronized void toSync(final boolean syncUp,
			final boolean syncDown, Handler handler) {
		new SyncTask(syncUp, syncDown, handler).execute();
	}

	public static final int SYNC_START = 1;
	public static final int SYNC_END = 10;
	public static final int SYNC_ERROR = 100;
	public static final int SYNC_SUCCESS = 1000;

	class SyncTask extends AsyncTask<Void, Integer, Void> {

		Handler mHandler;
		boolean mSyncUp;
		boolean mSyncDown;

		public SyncTask(Boolean syncUp, Boolean syncDown, Handler handler) {
			this(syncUp, syncDown);
			mHandler = handler;
		}

		private SyncTask(Boolean syncUp, Boolean syncDown) {
			mSyncUp = syncUp;
			mSyncDown = syncDown;
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (mSyncUp == false && mSyncDown == false) {
				return null;
			}
			if (mEvernoteSession.isLoggedIn() == false) {
				Logger.e(LogTag, "未登录");
				publishProgress(new Integer[] { SYNC_ERROR });
				return null;
			}
			publishProgress(new Integer[] { SYNC_START });
			try {
				makeSureNotebookExsits(NOTEBOOK_NAME);
				if (mSyncUp)
					syncUp();
				if (mSyncDown)
					syncDown();
				publishProgress(new Integer[] { SYNC_SUCCESS });
			} catch (Exception e) {
				publishProgress(new Integer[] { SYNC_ERROR });
				return null;
			} finally {
				publishProgress(new Integer[] { SYNC_END });
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (mHandler == null) {
				return;
			}
			switch (values[0]) {
			case SYNC_START:
				mHandler.sendEmptyMessage(SYNC_START);
				break;
			case SYNC_END:
				mHandler.sendEmptyMessage(SYNC_END);
				break;
			case SYNC_ERROR:
				mHandler.sendEmptyMessage(SYNC_ERROR);
				break;
			case SYNC_SUCCESS:
				mHandler.sendEmptyMessage(SYNC_SUCCESS);
				break;
			default:
				break;
			}
		}
	}
}