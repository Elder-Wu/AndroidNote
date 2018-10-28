package com.wuzhanglao.androidnote.base;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiTypeAdapter<CB extends MultiTypeAdapter.Callback> extends RecyclerView.Adapter<MultiTypeAdapter.ViewHolder> {

    private Activity mActivity;
    private List<Data> mDataList = new ArrayList<>();
    private SparseArray<Manager> mManagerMap = new SparseArray<>();

    public MultiTypeAdapter(Activity activity) {
        mActivity = activity;
    }

    public void setDataList(List<Data> dataList) {
        if (dataList == null || dataList.size() == 0) {
            return;
        }
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public Data getItem(int position) {
        if (position < 0 || position >= mDataList.size()) {
            return null;
        }
        return mDataList.get(position);
    }

    public void register(Class<? extends Manager> clazz) {
        try {
            Manager manager = clazz.newInstance();
            manager.setActivity(mActivity);
            mManagerMap.put(manager.bindDataClass().getName().hashCode(), manager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position).getClass().getName().hashCode();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Manager manager = getManager(viewType);
        if (manager == null) {
            // can not return `null` here , RecyclerView may crash
            return createNullViewHolder(parent.getContext());
        }
        return manager.createViewHolder(parent);
    }

    private NullViewHolder createNullViewHolder(Context context) {
        return new NullViewHolder(new View(context));
    }

    private Manager getManager(int type) {
        return mManagerMap.get(type);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder instanceof NullViewHolder) {
            return;
        }

        int type = getItemViewType(position);
        Manager manager = getManager(type);
        if (manager == null || getItem(position) == null) {
            return;
        }
        manager.bindViewHolder(holder, getItem(position), position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void setCallback(CB callback) {
        for (int i = 0; i < mManagerMap.size(); i++) {
            Manager manager = mManagerMap.valueAt(i);
            if (manager != null) {
                manager.setCallback(callback);
            }
        }
    }

    public void reset() {
        mDataList.clear();
        for (int i = 0; i < mManagerMap.size(); i++) {
            Manager manager = mManagerMap.valueAt(i);
            if (manager != null) {
                manager.reset();
            }
        }
    }

    // this class only used to show some view without any interact,just display
    public static class EmptyViewHolder extends ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class NullViewHolder extends EmptyViewHolder {
        private NullViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface Callback {

    }

    public abstract static class Manager<D extends Data, VH extends ViewHolder, CB extends Callback> {

        private CB mCallback;
        private Activity mActivity;

        public Manager() {
        }

        private void setCallback(CB callback) {
            mCallback = callback;
        }

        private void setActivity(Activity activity) {
            mActivity = activity;
        }

        public final VH createViewHolder(@NonNull ViewGroup parent) {
            return onCreateViewHolder(parent);
        }

        public final void bindViewHolder(VH holder, D data, int position) {
            holder.setData(data);
            holder.render(data);
        }

        public abstract VH onCreateViewHolder(@NonNull ViewGroup parent);

        public Activity getActivity() {
            return mActivity;
        }

        public CB getCallback() {
            return mCallback;
        }

        public void reset() {
        }

        @NonNull
        public abstract Class<D> bindDataClass();
    }

    public abstract static class ViewHolder<D extends Data> extends RecyclerView.ViewHolder {

        private D data;

        public ViewHolder(final View itemView) {
            super(itemView);
        }

        protected void render(D data) {

        }

        protected final void setData(D data) {
            this.data = data;
        }

        public D getData() {
            return data;
        }
    }

    public static class Data {

    }
}
