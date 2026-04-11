package com.zhan_dui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhan_dui.data.MemoEntity;
import com.zhan_dui.evermemo.MemoActivity;
import com.zhan_dui.evermemo.R;
import com.zhan_dui.utils.DateHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Adapter for displaying memos in a list/grid without Cursor dependency.
 */
public class MemoListAdapter extends BaseAdapter implements View.OnClickListener, View.OnLongClickListener {

    public interface ItemLongPressedListener {
        void startActionMode();
    }

    public interface OnItemSelectListener {
        void onSelect();
        void onCancelSelect();
    }

    public interface OnDeleteMemosListener {
        void onDeleteMemos(List<MemoEntity> memoIds);
    }

    private final Context context;
    private final LayoutInflater layoutInflater;
    private final Typeface robotoThin;

    private List<MemoEntity> memos = new ArrayList<>();
    private boolean checkMode = false;
    private HashMap<Long, MemoEntity> checkedItems = new HashMap<>();

    private ItemLongPressedListener itemLongPressedLisener;
    private OnItemSelectListener onItemSelectLisener;
    private OnDeleteMemosListener onDeleteMemosListener;

    public MemoListAdapter(Context context, ItemLongPressedListener itemLongPressedLisener,
                          OnItemSelectListener selectLisener) {
        this(context, itemLongPressedLisener, selectLisener, null);
    }

    public MemoListAdapter(Context context, ItemLongPressedListener itemLongPressedLisener,
                          OnItemSelectListener selectLisener, OnDeleteMemosListener deleteListener) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.robotoThin = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
        this.itemLongPressedLisener = itemLongPressedLisener;
        this.onItemSelectLisener = selectLisener;
        this.onDeleteMemosListener = deleteListener;
    }

    /**
     * Update the adapter with new memo data
     */
    public void setMemos(List<MemoEntity> memos) {
        if (memos == null) {
            this.memos = new ArrayList<>();
        } else {
            this.memos = memos;
        }
        notifyDataSetChanged();
    }

    /**
     * Add a special "add memo" item at position 0
     */
    private int getItemCountWithAdd() {
        return memos.size() + 1; // +1 for the "add" item
    }

    @Override
    public int getCount() {
        return getItemCountWithAdd();
    }

    @Override
    public Object getItem(int position) {
        if (position == 0) {
            return null; // "add" item
        }
        int memoIndex = position - 1;
        if (memoIndex >= 0 && memoIndex < memos.size()) {
            return memos.get(memoIndex);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return 0; // "add" item ID
        }
        int memoIndex = position - 1;
        if (memoIndex >= 0 && memoIndex < memos.size()) {
            return memos.get(memoIndex)._id;
        }
        return -1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1; // 0 = add item, 1 = memo item
    }

    @Override
    public int getViewTypeCount() {
        return 2; // add item and memo item
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);

        if (viewType == 0) {
            // "Add memo" item
            return createAddItemView(convertView, parent);
        } else {
            // Memo item
            int memoIndex = position - 1;
            if (memoIndex >= 0 && memoIndex < memos.size()) {
                MemoEntity memo = memos.get(memoIndex);
                return createMemoItemView(memoIndex, memo, convertView, parent);
            }
        }

        return convertView != null ? convertView : createEmptyView(parent);
    }

    private View createAddItemView(View convertView, ViewGroup parent) {
        if (convertView != null && convertView.getTag(R.string.memo_first) != null &&
            (Boolean) convertView.getTag(R.string.memo_first)) {
            // Reuse the existing view
            return convertView;
        }

        View view = layoutInflater.inflate(R.layout.memo_add, parent, false);
        view.setTag(R.string.memo_first, true);
        TextView plusTextView = view.findViewById(R.id.plus);
        if (plusTextView != null) {
            plusTextView.setTypeface(robotoThin);
        }
        view.setOnClickListener(this);
        return view;
    }

    private View createMemoItemView(int position, MemoEntity memo, View convertView, ViewGroup parent) {
        View view;
        boolean isFirst = false; // Not used in this adapter, but kept for compatibility

        if (convertView != null && convertView.getTag(R.string.memo_first) != null) {
            isFirst = (Boolean) convertView.getTag(R.string.memo_first);
            if (isFirst && position != 0) {
                view = layoutInflater.inflate(R.layout.memo_item, parent, false);
            } else if (!isFirst && position == 0) {
                view = layoutInflater.inflate(R.layout.memo_item, parent, false);
            } else {
                view = convertView;
            }
        } else {
            view = layoutInflater.inflate(R.layout.memo_item, parent, false);
        }

        view.setTag(R.string.memo_first, position == 0);

        // Bind data
        TextView contentTextView = view.findViewById(R.id.content);
        TextView dateTextView = view.findViewById(R.id.date);
        View hoverView = view.findViewById(R.id.hover);
        View uploadView = view.findViewById(R.id.uploading);

        if (contentTextView != null) {
            contentTextView.setText(Html.fromHtml(memo.content));
        }

        if (dateTextView != null) {
            dateTextView.setText(DateHelper.getGridDate(context, memo.createdTime));
        }

        if (hoverView != null) {
            hoverView.setTag(R.string.memo_data, memo);
            hoverView.setTag(R.string.memo_id, memo._id);
            hoverView.setTag(R.string.memo_position, position + 1); // +1 because position 0 is "add" item
            hoverView.setOnClickListener(this);
            hoverView.setOnLongClickListener(this);

            // Set background based on check mode
            if (checkMode) {
                if (isChecked(memo._id)) {
                    hoverView.setBackgroundResource(R.drawable.hover_multi_background_normal);
                } else {
                    hoverView.setBackgroundResource(R.drawable.hover_border_normal);
                }
            } else {
                hoverView.setBackgroundResource(R.drawable.hover_background);
            }
        }

        if (uploadView != null) {
            uploadView.setVisibility(memo.isSyncingUp() ? View.VISIBLE : View.INVISIBLE);
        }

        return view;
    }

    private View createEmptyView(ViewGroup parent) {
        return layoutInflater.inflate(R.layout.memo_item, parent, false);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(R.string.memo_first) != null && (Boolean) v.getTag(R.string.memo_first)) {
            // "Add memo" item clicked
            context.startActivity(new Intent(context, MemoActivity.class));
        } else {
            if (v.getId() == R.id.hover) {
                MemoEntity memo = (MemoEntity) v.getTag(R.string.memo_data);
                if (checkMode) {
                    toggleCheckedId(memo._id, memo, v);
                } else {
                    Intent intent = new Intent(context, MemoActivity.class);
                    intent.putExtra("memo", memo);
                    context.startActivity(intent);
                }
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (!checkMode) {
            if (itemLongPressedLisener != null) {
                itemLongPressedLisener.startActionMode();
            }
            setCheckMode(true);
        }
        MemoEntity memo = (MemoEntity) v.getTag(R.string.memo_data);
        toggleCheckedId(memo._id, memo, v);
        return true;
    }

    // Check mode management
    public void setCheckMode(boolean check) {
        checkMode = check;
        if (!checkMode) {
            checkedItems.clear();
        }
        notifyDataSetChanged();
    }

    public void toggleCheckedId(Long id, MemoEntity memo, View v) {
        if (checkedItems.containsKey(id)) {
            checkedItems.remove(id);
            if (onItemSelectLisener != null) {
                onItemSelectLisener.onCancelSelect();
            }
        } else {
            if (checkedItems.isEmpty()) {
                checkedItems = new HashMap<>();
            }
            checkedItems.put(id, memo);
            if (onItemSelectLisener != null) {
                onItemSelectLisener.onSelect();
            }
        }
        notifyDataSetChanged();
    }

    public boolean isChecked(Long id) {
        return checkedItems.containsKey(id);
    }

    public int getSelectedCount() {
        return checkedItems.size();
    }

    public void deleteSelectedMemos() {
        if (checkedItems.isEmpty()) {
            return;
        }

        // Collect IDs to delete
        List<MemoEntity> idsToDelete = new ArrayList<>(checkedItems.values());

        // Notify listener to delete from database
        if (onDeleteMemosListener != null) {
            onDeleteMemosListener.onDeleteMemos(idsToDelete);
        }

        // Clear selection
        checkedItems.clear();

        if (onItemSelectLisener != null) {
            onItemSelectLisener.onCancelSelect();
        }

        // Note: The LiveData from ViewModel will automatically update the adapter
        // so we don't need to modify the local list here
        notifyDataSetChanged();
    }

}