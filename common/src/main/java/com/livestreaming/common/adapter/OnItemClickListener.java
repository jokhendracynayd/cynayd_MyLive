package com.livestreaming.common.adapter;

public interface OnItemClickListener<T> {
   public void notifyItemClickCallback(int position,T item);
}
