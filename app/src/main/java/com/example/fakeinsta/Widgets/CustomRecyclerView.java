package com.example.fakeinsta.Widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomRecyclerView extends RecyclerView {

    List<View> showOnEmpty = new ArrayList<>();
    List<View> showOnNonEmpty = new ArrayList<>();

    public CustomRecyclerView(@NonNull Context context) {
        super(context);
    }

    public CustomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private AdapterDataObserver mObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            toggleViews();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            toggleViews();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            toggleViews();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            toggleViews();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            toggleViews();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            toggleViews();
        }
    };

    private void toggleViews() {
        if(getAdapter() != null) {
            if (getAdapter().getItemCount() == 0) {

                // Show the views on empty
                for (View view : showOnEmpty) {
                    view.setVisibility(View.VISIBLE);
                }

                // Hide recycler view
                setVisibility(GONE);

                // Hide items not to be shown
                for (View view : showOnNonEmpty) {
                    view.setVisibility(GONE);
                }
            } else {
                for (View view : showOnEmpty) {
                    view.setVisibility(GONE);
                }

                setVisibility(VISIBLE);

                for (View view : showOnNonEmpty) {
                    view.setVisibility(VISIBLE);
                }
            }
        }
    }

    public void hideIfEmpty(View... views) {
        showOnNonEmpty = Arrays.asList(views);
    }

    public void showIfEmpty(View... views) {
        showOnEmpty = Arrays.asList(views);
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);

        if(adapter != null) {
            adapter.registerAdapterDataObserver(mObserver);
        }
        mObserver.onChanged();
    }
}
