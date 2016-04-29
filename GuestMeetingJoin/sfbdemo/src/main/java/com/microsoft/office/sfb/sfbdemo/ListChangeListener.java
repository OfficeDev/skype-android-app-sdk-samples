/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 */

package com.microsoft.office.sfb.sfbdemo;

import android.support.v7.widget.RecyclerView;

import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.ObservableList;

/**
 * Generic Listener class to communicate list changes from the SDK to the RecyclerView Adapter.
 * The class listens for changes to the list and calls the appropriate adapter method.
 */
class ListChangeListener<T,V extends RecyclerView.ViewHolder> extends ObservableList.OnListChangedCallback<T> {

    RecyclerView.Adapter<V> adapter = null;

    public ListChangeListener(RecyclerView.Adapter<V> adapter) {
        this.adapter = adapter;
    }

    /**
     * Called whenever a change of unknown type has occurred, such as the entire list being set to new values.
     *
     * @param sender ObservableList instance
     */
    @Override
    public void onChanged(T sender) {
        this.adapter.notifyDataSetChanged();
    }

    /**
     * Called whenever one or more items in the list have changed.
     *
     * @param sender        ObservableList instance
     * @param positionStart starting index of the changed items.
     * @param itemCount     number of items that have changed.
     */
    @Override
    public void onItemRangeChanged(T sender, int positionStart, int itemCount) {
        this.adapter.notifyItemRangeChanged(positionStart, itemCount);
    }

    /**
     * Called whenever items have been inserted into the list.
     *
     * @param sender        ObservableList instance
     * @param positionStart starting index of the inserted items.
     * @param itemCount     number of items that have changed
     */
    @Override
    public void onItemRangeInserted(T sender, int positionStart, int itemCount) {
        this.adapter.notifyItemRangeInserted(positionStart, itemCount);
    }

    /**
     * Called whenever items in the list have been moved.
     *
     * @param sender       ObservableList instance
     * @param fromPosition starting index of the moved items.
     * @param toPosition   destination index of the moved items.
     * @param itemCount    number of items that have moved.
     */
    @Override
    public void onItemRangeMoved(T sender, int fromPosition, int toPosition, int itemCount) {
        for (int i = 0; i < itemCount; ++i) {
            this.adapter.notifyItemMoved(fromPosition + i, toPosition + i);
        }
    }

    /**
     * Called whenever items in the list have been deleted.
     *
     * @param sender        ObservableList instance
     * @param positionStart starting index of the moved items.
     * @param itemCount     number of items that have been removed.
     */
    @Override
    public void onItemRangeRemoved(T sender, int positionStart, int itemCount) {
        this.adapter.notifyItemRangeRemoved(positionStart, itemCount);
    }
}