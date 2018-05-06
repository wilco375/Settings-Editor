package com.wilco375.settingseditor.layout;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatAutoCompleteTextView;

/**
 * Custom View extending the AutoCompleteTextView that instantly shows the autocomplete items
 */
public class InstantAutoComplete extends AppCompatAutoCompleteTextView {

    public InstantAutoComplete(Context context) {
        super(context);
    }

    public InstantAutoComplete(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public InstantAutoComplete(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
    }

    /**
     * Make sure autocomplete dropdown always shows
     * @return true
     */
    @Override
    public boolean enoughToFilter() {
        return true;
    }

    /**
     * Show autocomplete dropdown on focus
     */
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            performFiltering(getText(), 0);
        }
    }

}