package com.zhan_dui.evermemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.zhan_dui.data.Memo;
import com.zhan_dui.sync.Evernote;
import com.zhan_dui.viewmodel.MemoViewModel;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MemoActivity extends AppCompatActivity implements OnClickListener,
		OnKeyListener {

	private EditText mContentEditText;
	private Memo memo;
	private boolean mCreateNew;
	private Context mContext;
	private MemoViewModel mMemoViewModel;

	private String mLastSaveContent;

	private Timer mTimer;
	private Evernote mEvernote;

	private boolean mTextChanged = false;

	private final String mBullet = " • ";
	private final String mNewLine = "\n";
	public static final String LogTag = "MemoActivity Log";
	public static String sEditCount = "EditCount";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		overridePendingTransition(R.anim.push_up, R.anim.push_down);
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setTitle(getString(R.string.app_memo));
		setContentView(R.layout.activity_memo);
		mContentEditText = (EditText) findViewById(R.id.content);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.getSerializable("memo") != null) {
			memo = (Memo) bundle.getSerializable("memo");
			mCreateNew = false;
			mLastSaveContent = memo.getContent();
		} else {
			memo = new Memo();
			mCreateNew = true;
		}

		mContentEditText.setText(Html.fromHtml(memo.getContent()));
		if (mCreateNew) {
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			mContentEditText.requestFocus();
//			MobclickAgent.onEvent(mContext, "new_memo");
		} else {
//			MobclickAgent.onEvent(mContext, "edit_memo");
		}

		mContentEditText.setOnKeyListener(this);
		mMemoViewModel = new ViewModelProvider(this).get(MemoViewModel.class);
		mEvernote = new Evernote(mContext);
		findViewById(R.id.edit_container).setOnClickListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			saveMemoAndLeave();
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.edit_container) {
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.toggleSoftInputFromWindow(
					findViewById(R.id.edit_container)
							.getApplicationWindowToken(),
					InputMethodManager.SHOW_FORCED, 0);
		}
	}

	private void share() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_TITLE,
				getText(R.string.share_title));
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				mContentEditText.getText());

		startActivity(Intent.createChooser(shareIntent,
				getText(R.string.share_via)));
	}

	private boolean clickEnter() {
		int currentPosition = mContentEditText.getSelectionStart();
		int newPosition = currentPosition;
		String currentText = mContentEditText.getText().toString();
		StringBuffer contentBuffer = new StringBuffer(currentText);
		int maxEnd = contentBuffer.length();
		int before3 = ((currentPosition - mBullet.length()) < 0) ? 0
				: (currentPosition - mBullet.length());
		int start = currentText.lastIndexOf(mNewLine, currentPosition - 1) + 1;
		start = (start == -1) ? 0 : start;
		int end = ((start + mBullet.length()) > maxEnd) ? maxEnd
				: (start + mBullet.length());

		if (contentBuffer.substring(start, end).equals(mBullet)) {
			if (maxEnd == end) {
				contentBuffer.replace(start, end, "\n");
				mContentEditText.setText(contentBuffer);
				mContentEditText.setSelection(contentBuffer.length());
				return true;
			} else if (contentBuffer.substring(before3, currentPosition)
					.equals(mBullet)) {
				contentBuffer.replace(start, end, "");
				newPosition = currentPosition - (end - start) + 1;
				mContentEditText.setText(contentBuffer);
				mContentEditText.setSelection(newPosition);
			} else {
				contentBuffer.insert(currentPosition, mNewLine + mBullet);
				mContentEditText.setText(contentBuffer);
				newPosition = ((currentPosition + mBullet.length() + mNewLine
						.length()) > contentBuffer.length()) ? contentBuffer
						.length()
						: (currentPosition + mBullet.length() + mNewLine
								.length());
				mContentEditText.setSelection(newPosition);
			}
			return true;
		}
		return false;

	}

	private void clickList() {
		int currentPosition = mContentEditText.getSelectionStart();
		int newPosition = currentPosition;
		String currentText = mContentEditText.getText().toString();
		StringBuffer contentBuffer = new StringBuffer(currentText);
		int maxEnd = contentBuffer.length();
		int start = currentText.lastIndexOf(mNewLine, currentPosition - 1) + 1;
		int end = ((start + mBullet.length()) > maxEnd) ? maxEnd
				: (start + mBullet.length());
		if (contentBuffer.substring(start, end).equals(mBullet)) {
			contentBuffer.replace(start, start + mBullet.length(), "");
			newPosition -= mBullet.length();
			newPosition = (newPosition < start) ? start : newPosition;
			newPosition = newPosition < 0 ? 0 : newPosition;
		} else {
			contentBuffer.insert(start, mBullet);
			if (currentPosition < currentPosition + mBullet.length()) {
				newPosition = currentPosition + mBullet.length();
			} else {
				newPosition += mBullet.length();
			}
		}
		mContentEditText.setText(contentBuffer);
		mContentEditText.setSelection(newPosition);
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {

		if (event.getAction() != KeyEvent.KEYCODE_BACK) {
			mTextChanged = true;
		}

		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_ENTER) {
			return clickEnter();
		}
		return false;
	}

	private void saveMemo(Boolean toLeave) {
		if (mCreateNew
				&& mContentEditText.getText().toString().trim().length() == 0) {
			return;
		}

		if (mLastSaveContent == null) {
			mLastSaveContent = new String(mContentEditText.getText().toString());
		} else {
			if (mLastSaveContent.equals(mContentEditText.getText().toString())) {
				return;
			}
		}
		memo.setContent(Html.toHtml(mContentEditText.getText()));
		memo.setCursorPosition(mContentEditText.getSelectionStart());
		memo.setUpdatedTime(System.currentTimeMillis());
		if (mCreateNew) {
			Uri resultUri = mMemoViewModel.createMemo(memo.getContent());
			if (resultUri != null) {
				mCreateNew = false;
				memo.setId(Integer.parseInt(resultUri.getLastPathSegment()));
			}
		} else {
			if (mContentEditText.getText().toString().trim().length() == 0) {
				// Empty content means delete
				mMemoViewModel.deleteMemo(memo.getId());
				mCreateNew = true;
			} else {
				mMemoViewModel.updateMemo(memo);
			}
		}
		if (toLeave && mTextChanged) {
			mEvernote.sync(true, false, null);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mTimer.cancel();
//		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
//		MobclickAgent.onResume(this);
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				saveMemo(false);
			}
		}, 5000, 10000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.memo, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			saveMemoAndLeave();
			return true;
		} else if (itemId == R.id.li) {
			clickList();
			return true;
		} else if (itemId == R.id.delete) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage(R.string.give_up_edit)
					.setTitle(R.string.give_up_title)
					.setPositiveButton(R.string.give_up_sure,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									deleteAndLeave();
//									MobclickAgent.onEvent(mContext, "delete_memo");
								}
							}).setNegativeButton(R.string.give_up_cancel, null)
					.create().show();
			return true;
		} else if (itemId == R.id.share_to) {
			share();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void saveMemoAndLeave() {
		saveMemo(true);
		int count = PreferenceManager.getDefaultSharedPreferences(mContext)
				.getInt(sEditCount, 0);
		if (count < 5) {
			count++;
			PreferenceManager.getDefaultSharedPreferences(mContext).edit()
					.putInt(sEditCount, count).commit();
		}
		mEvernote.sync(true, false, null);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		finish();
		overridePendingTransition(R.anim.out_push_up, R.anim.out_push_down);
	}

	private void deleteAndLeave() {
		if (memo.getId() != 0) {
			mMemoViewModel.deleteMemo(memo.getId());
			mEvernote.sync(true, false, null);
		}
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		finish();
		overridePendingTransition(R.anim.out_push_up, R.anim.out_push_down);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			findViewById(R.id.more).performClick();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

}
