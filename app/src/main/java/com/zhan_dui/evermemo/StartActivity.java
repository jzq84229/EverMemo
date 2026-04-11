package com.zhan_dui.evermemo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.lifecycle.ViewModelProvider;

import com.evernote.client.android.EvernoteSession;
import com.huewu.pla.lib.MultiColumnListView;
import com.zhan_dui.adapters.MemoListAdapter;
import com.zhan_dui.data.MemoEntity;
import com.zhan_dui.sync.Evernote;
import com.zhan_dui.utils.Logger;
import com.zhan_dui.utils.MarginAnimation;
import com.zhan_dui.utils.SPManager;
import com.zhan_dui.viewmodel.MemoViewModel;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends AppCompatActivity implements
        OnClickListener, MemoListAdapter.ItemLongPressedListener,
        MemoListAdapter.OnItemSelectListener, MemoListAdapter.OnDeleteMemosListener {

    private MultiColumnListView mMemosGrid;
    private Context mContext;
    private MemoListAdapter mMemosAdapter;
    private LinearLayout mBindEvernotePanel;
//    private SharedPreferences mSharedPreferences;
    private Button mBindEvernote;
    private int mBindEvernotePandelHeight;
    public static Evernote mEvernote;
    public static String sShownRate = "ShownRate";
    public static String sStartCount = "StartCount";
    private Menu mMenu;
    private MemoViewModel mMemoViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setLogo(R.drawable.ab_logo);
        mContext = this;
        mEvernote = new Evernote(this);
//		MobclickAgent.onError(this);
        setContentView(R.layout.activity_start);
        mMemosGrid = (MultiColumnListView) findViewById(R.id.memos);
        mBindEvernotePanel = (LinearLayout) findViewById(R.id.evernote_panel);
        mBindEvernote = (Button) findViewById(R.id.bind_evernote);
        mBindEvernotePandelHeight = mBindEvernotePanel.getLayoutParams().height;

        mMemosAdapter = new MemoListAdapter(mContext, this, this, this);
        mMemosGrid.setAdapter(mMemosAdapter);

        mMemoViewModel = new ViewModelProvider(this).get(MemoViewModel.class);
        mMemoViewModel.getAllMemos().observe(this, memos -> {
            // Update adapter with new data
            updateMemosAdapter(memos);
        });

//        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);


        if (SPManager.getInstance().getInt(sStartCount, 1) == 1) {
            mBindEvernotePanel.startAnimation(new MarginAnimation(mBindEvernotePanel, 0, 0, 0, 0, 600));
            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {

                    StartActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mBindEvernotePanel.startAnimation(new MarginAnimation(mBindEvernotePanel, 0, 0, 0, -mBindEvernotePandelHeight));
                        }
                    });
                }
            }, 5000);
//            mSharedPreferences
//                    .edit()
//                    .putInt(sStartCount, mSharedPreferences.getInt(sStartCount, 1) + 1)
//                    .commit();
            SPManager.getInstance().putInt(sStartCount, SPManager.getInstance().getInt(sStartCount, 1) + 1);
            mBindEvernote.setOnClickListener(this);
        }

        if (SPManager.getInstance().getBoolean(
                SettingActivity.OPEN_MEMO_WHEN_START_UP, false)) {
            startActivity(new Intent(this, MemoActivity.class));
        }

        mEvernote.sync(true, true, null);
//		UmengUpdateAgent.update(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bind_evernote) {
            mEvernote.auth();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
//            case EvernoteSession.REQUEST_CODE_OAUTH:
            case EvernoteSession.REQUEST_CODE_LOGIN:
                mEvernote.onAuthFinish(resultCode);
                break;
        }
    }

    private Timer mSyncTimer;

    @Override
    protected void onResume() {
        super.onResume();
//		MobclickAgent.onResume(this);

        if (mMenu != null) {
            MenuItem syncItem = mMenu.findItem(R.id.sync);
            Logger.i("====isLoggedIn: " + mEvernote.isLogin());
            if (!mEvernote.isLogin()) {
                syncItem.setTitle(R.string.menu_bind);
            } else {
                syncItem.setTitle(R.string.menu_sync);
            }
        }

        if (SPManager.getInstance().getInt(MemoActivity.sEditCount, 0) == 5
                && SPManager.getInstance().getBoolean(sShownRate, false) == false) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(R.string.rate_for_evernote)
                    .setPositiveButton(R.string.rate_rate,
                            (dialog, which) -> {
                                Uri uri = Uri.parse("market://details?id=" + mContext.getPackageName());
                                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                try {
                                    startActivity(goToMarket);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(mContext, R.string.can_not_open_market, Toast.LENGTH_SHORT).show();
                                }
                            })
                    .setNegativeButton(R.string.rate_feedback,
                            (dialog, which) -> {
                                Intent Email = new Intent(Intent.ACTION_SEND);
                                Email.setType("text/email");
                                Email.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.team_email)});
                                Email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback));
                                Email.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_title));
                                startActivity(Intent.createChooser(Email, getString(R.string.email_chooser)));
                            }).create().show();
//            mSharedPreferences.edit().putBoolean(sShownRate, true).commit();
            SPManager.getInstance().putBoolean(sShownRate, true);
        }
        mSyncTimer = new Timer();
        Logger.e("启动自动更新任务");
        mSyncTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                mEvernote.sync(true, true, null);
            }
        }, 30000, 50000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSyncTimer != null) {
            Logger.e("结束定时同步任务");
            mSyncTimer.cancel();
        }
//		MobclickAgent.onPause(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.start, menu);
        mMenu = menu;
        MenuItem syncItem = menu.findItem(R.id.sync);
        if (!mEvernote.isLogin()) {
            syncItem.setTitle(R.string.menu_bind);
        } else {
            syncItem.setTitle(R.string.menu_sync);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.settiing) {
            Intent intent = new Intent(mContext, SettingActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.sync) {
            if (mEvernote.isLogin() == false) {
                mEvernote.auth();
            } else {
                mEvernote.sync(true, true, new SyncHandler());
            }
            return true;
        } else if (itemId == R.id.feedback) {
            Intent Email = new Intent(Intent.ACTION_SEND);
            Email.setType("text/email");
            Email.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.team_email)});
            Email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback));
            Email.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_title));
            startActivity(Intent.createChooser(Email, getString(R.string.email_chooser)));
            return true;
        }
        return false;
    }

    @SuppressLint("HandlerLeak")
    class SyncHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Evernote.SYNC_START:
                    findViewById(R.id.sync_progress).setVisibility(View.VISIBLE);
                    break;
                case Evernote.SYNC_END:
                    findViewById(R.id.sync_progress).setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            findViewById(R.id.more).performClick();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private Menu mContextMenu;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onActionItemClicked(ActionMode arg0, MenuItem menuItem) {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.delete) {
                if (mMemosAdapter.getSelectedCount() == 0) {
                    Toast.makeText(mContext, R.string.delete_select_nothing, Toast.LENGTH_SHORT).show();
                } else {
                    Builder builder = new Builder(mContext);
                    builder.setMessage(R.string.delete_all_confirm)
                            .setTitle(R.string.delete_title)
                            .setPositiveButton(R.string.delete_sure,
                                    (dialog, which) -> {
                                        mMemosAdapter.deleteSelectedMemos();
                                        if (mActionMode != null) {
                                            mActionMode.finish();
                                        }
                                    })
                            .setNegativeButton(R.string.delete_cancel, null)
                            .create().show();
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode arg0) {
            mActionMode = null;
            mContextMenu = null;
            mMemosAdapter.setCheckMode(false);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode arg0, Menu menu) {
            mContextMenu = menu;
            updateActionMode();
            return false;
        }

    };

    private ActionMode mActionMode;

    @Override
    public void startActionMode() {
        if (mActionMode != null) {
            return;
        }
        mActionMode = startSupportActionMode(mActionModeCallback);
    }

    public void updateActionMode() {
        if (mMemosAdapter.getSelectedCount() <= 1) {
            mContextMenu.findItem(R.id.selected_counts).setTitle(
                    mContext.getString(R.string.selected_one_count,
                            mMemosAdapter.getSelectedCount()));
        } else {
            mContextMenu.findItem(R.id.selected_counts).setTitle(
                    mContext.getString(R.string.selected_more_count,
                            mMemosAdapter.getSelectedCount()));
        }
    }

    @Override
    public void onSelect() {
        updateActionMode();
    }

    @Override
    public void onCancelSelect() {
        updateActionMode();
    }

    @Override
    public void onDeleteMemos(List<MemoEntity> memos) {
        for (MemoEntity memo : memos) {
            mMemoViewModel.deleteMemo(memo);
        }
        // Sync after deletion
        mEvernote.sync(true, false, null);
    }

    private void updateMemosAdapter(List<MemoEntity> memos) {
        if (memos == null) {
            return;
        }
        mMemosAdapter.setMemos(memos);
    }


}
