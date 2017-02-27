package com.comp.iitb.vialogue.helpers;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.comp.iitb.vialogue.R;
import com.comp.iitb.vialogue.coordinators.SharedRuntimeContent;

import java.util.List;

/**
 * Created by shubh on 20-02-2017.
 */

public class DeleteActionMode implements ActionMode.Callback {

    private final Activity mActivity;
    private final int mIndex;
    private final List mList;
    private final RecyclerView.Adapter mAdapter;

    public DeleteActionMode(Activity activity, List list, int index, RecyclerView.Adapter adapter) {
        mActivity = activity;
        mList = list;
        mIndex = index;
        mAdapter = adapter;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.create_project_action, menu);
        MenuItem item = menu.findItem(R.id.delete_option);
        SharedRuntimeContent.selectedPosition = mIndex;
        SharedRuntimeContent.isSelected = true;
        SharedRuntimeContent.updateAdapterView();
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedRuntimeContent.deleteSlide(mIndex);
                return true;
            }
        });
        if (mActivity instanceof AppCompatActivity)
            ((AppCompatActivity) mActivity).getSupportActionBar().hide();
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        mList.remove(mIndex);
        mAdapter.notifyItemRemoved(mIndex);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        SharedRuntimeContent.isSelected = false;
        SharedRuntimeContent.updateAdapterView();
        if (mActivity instanceof AppCompatActivity)
            ((AppCompatActivity) mActivity).getSupportActionBar().show();
    }
}
