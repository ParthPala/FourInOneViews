package multifunction.view.multifunctionview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.ListPopupWindow;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;

public class FourInOneViews extends AppCompatEditText {

   private static Typeface mTypeFace = null;
    private ListPopupWindow popupWindow;
    private  Drawable dropdownDrawable;
    private   ArrayAdapter adapter;
    private int mode;
    private int num = 0;
    private List initlist;
    private int mDropDownAnchorId;
    private static final long TIMEOUT_POPUP_DISMISS = 200l;
    private OnShowListener mOnShowListener;
    private boolean mIsEditable = true;
    private PopupWindow.OnDismissListener mOnDismissListener;
    private AdapterView.OnItemClickListener mItemClickListener;
    private long mLastDismissTime = 0l;
    private boolean mDropDownTouchedDown = false;
    private ItemConverter mItemConverter;
    private AttributeSet attrs;

    /**
     * constructor
     *
     * @param context
     */
    public FourInOneViews(Context context) {
        super(context);
    }

    /**
     * constructor
     *
     * @param context
     * @param attrs
     */
    public FourInOneViews(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
        init(context, attrs);
    }

    /**
     * constructor
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */

    public FourInOneViews(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.attrs = attrs;
        init(context, attrs);
    }


    /**
     * Enum for user, to pass parameter in selectView()
     */

    enum selectview {
        TextView,
        EditText,
        AutoCompleteTextView,
        Spinner;
    }

    /**
     * For Setting the dropdown frame and showing dropdown for only spinner and autocomplete
     *
     * @param l
     * @param t
     * @param r
     * @param b
     * @return
     */
    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean result = super.setFrame(l, t, r, b);
        if (mode == 3 || mode == 2) {
            if (ispopupShowing()) {
                showDropDown();
            }
        }
        return result;
    }

    /**
     * To dismiss dropDown if focus has changed to some other view
     *
     * @param focused
     * @param direction
     * @param previouslyFocusedRect
     */
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (!focused) {
            dismissDropDown();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        dismissDropDown();
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (isInDropDownClickArea(event)) {
                    mDropDownTouchedDown = true;
                    return true;
                } else {
                    mDropDownTouchedDown = false;
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mDropDownTouchedDown && isInDropDownClickArea(event)) {
                    if (SystemClock.elapsedRealtime() - mLastDismissTime > TIMEOUT_POPUP_DISMISS) {
                        clearFocus();
                        showDropDown();
                        return true;
                    } else {
                        dismissDropDown();
                    }
                }
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && ispopupShowing()) {
            // special case for the back key, we do not even try to send it
            // to the drop down list but instead, consume it immediately
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    state.startTracking(event, this);
                }
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    state.handleUpEvent(event);
                }
                if (event.isTracking() && !event.isCanceled()) {
                    dismissDropDown();
                    return true;
                }
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (popupWindow != null) {
            boolean consumed = popupWindow.onKeyUp(keyCode, event);
            if (consumed) {
                switch (keyCode) {
                    // if the list accepts the key events and the key event
                    // was a click, the text view gets the selected item
                    // from the drop down as its content
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_TAB:
                        if (event.hasNoModifiers()) {
                            performCompletion();
                        }
                        return true;
                }
            }
            if (ispopupShowing() && keyCode == KeyEvent.KEYCODE_TAB && event.hasNoModifiers()) {
                performCompletion();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (popupWindow != null) {
            if (popupWindow.onKeyDown(keyCode, event)) {
                return true;
            }
        }
        if (ispopupShowing() && keyCode == KeyEvent.KEYCODE_TAB && event.hasNoModifiers()) {
            return true;
        }

        boolean handled = super.onKeyDown(keyCode, event);
        if (handled && ispopupShowing()) {
            clearListSelection();
        }
        return handled;
    }

    @Override
    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top,
                                     @Nullable Drawable right, @Nullable Drawable bottom) {
        super.setCompoundDrawables(left, top, dropdownDrawable != null ? dropdownDrawable : right, bottom);
    }

    /**
     * initialising views
     *
     * @param context
     * @param attrs
     */

    private void init(Context context, AttributeSet attrs) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FourInOneViews, 0, 0);
        mode = a.getInt(R.styleable.FourInOneViews_selectview, 0);
        switch (mode) {
            case 0:
                initTextView();
                break;
            case 1:
                initEditText();
                break;
            case 2:
                initAutoCompleteTextView(context, attrs, a);
                break;
            case 3:
                initSpinner(context, attrs, a);
                break;
        }
    }

    /**
     * initialising TextView
     */

    private void initTextView() {
        popupWindow = null;
        removedropdownDrawable();
        if (!isInEditMode() && mTypeFace == null) {
            mTypeFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/LATO-SEMIBOLD_0.TTF");
        }
        if (!isInEditMode()) {
            setTypeface(mTypeFace);
        }
        if (getBackground() == null)
            setBackground(null);
        else {
            setBackground(getBackground());
        }
        setEnabled(false);
        setShowSoftInputOnFocus(false);
        setCursorVisible(false);
        mIsEditable = false;
    }

    /**
     * initialising EditText
     */

    private void initEditText() {
        popupWindow = null;
        removedropdownDrawable();
        if (!isInEditMode() && mTypeFace == null) {
            mTypeFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/LATO-SEMIBOLD_0.TTF");
        }
        if (!isInEditMode()) {
            setTypeface(mTypeFace);
        }
        if (getBackground() == null)
            setBackground(null);
        else {
            setBackground(getBackground());
        }
        setEnabled(true);
        requestFocus();
        setShowSoftInputOnFocus(true);
        setCursorVisible(true);
        mIsEditable = true;
        removeTextChangedListener(new DoublePTextWatcher());
    }

    /**
     * initialising spinner view
     *
     * @param context
     * @param attrs
     * @param a
     */

    private void initSpinner(Context context, AttributeSet attrs, TypedArray a) {

        popupWindow = new ListPopupWindow(context, attrs);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW);

        Drawable selector = a.getDrawable(R.styleable.FourInOneViews_dropDownSelector);
        if (selector != null) {
            popupWindow.setListSelector(selector);
        }
        int dropdownAnimationResId = a.getResourceId(R.styleable.FourInOneViews_dropDownAnimStyle, -1);
        if (dropdownAnimationResId > 0) {
            setDropDownAnimationStyle(dropdownAnimationResId);
        }
        dropdownDrawable = a.getDrawable(R.styleable.FourInOneViews_dropDownDrawable);
        int dropdownDrawableScpacing = a.getDimensionPixelOffset(R.styleable.FourInOneViews_dropDownDrawableSpacing, 0);
        if (dropdownDrawable != null) {
            int dropDowndrawableWidth = a.getDimensionPixelOffset(R.styleable.FourInOneViews_dropDownDrawableWidth, -1);
            int dropDowndrawableHeight = a.getDimensionPixelOffset(R.styleable.FourInOneViews_dropDownDrawableHeight, -1);
            setDropDownDrawable(dropdownDrawable, dropDowndrawableWidth, dropDowndrawableHeight);
            setDropDownDrawableSpacing(dropdownDrawableScpacing);
        }
        mDropDownAnchorId = a.getResourceId(R.styleable.FourInOneViews_dropDownAnchor, View.NO_ID);
        popupWindow.setWidth(a.getLayoutDimension(R.styleable.FourInOneViews_dropDownDrawableWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        popupWindow.setHeight(a.getLayoutDimension(R.styleable.FourInOneViews_dropDownDrawableHeight, ViewGroup.LayoutParams.WRAP_CONTENT));
        popupWindow.setOnItemClickListener(new DropDownItemClickListener());
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mLastDismissTime = SystemClock.elapsedRealtime();
                if (mOnDismissListener != null) {
                    mOnDismissListener.onDismiss();
                }
            }
        });

        if (getBackground() == null)
            setBackground(null);
        else {
            setBackground(getBackground());
        }
        setTextIsSelectable(false);
        a.recycle();
        requestFocusFromTouch();
        setEnabled(true);
        setShowSoftInputOnFocus(false);
        setCursorVisible(false);
        mIsEditable = false;
        removeTextChangedListener(new DoublePTextWatcher());
    }

    /**
     * initialising AutoComplete view
     *
     * @param context
     * @param attrs
     * @param a
     */

    private void initAutoCompleteTextView(Context context, AttributeSet attrs, TypedArray a) {
        popupWindow = new ListPopupWindow(context, attrs);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW);
        Drawable selector = a.getDrawable(R.styleable.FourInOneViews_dropDownSelector);
        if (selector != null) {
            popupWindow.setListSelector(selector);
        }
        int dropdownAnimationResId = a.getResourceId(R.styleable.FourInOneViews_dropDownAnimStyle, -1);
        if (dropdownAnimationResId > 0) {
            setDropDownAnimationStyle(dropdownAnimationResId);
        }
        dropdownDrawable = a.getDrawable(R.styleable.FourInOneViews_dropDownDrawable);
        int dropdownDrawableScpacing = a.getDimensionPixelOffset(R.styleable.FourInOneViews_dropDownDrawableSpacing, 0);
        if (dropdownDrawable != null) {
            int dropDowndrawableWidth = a.getDimensionPixelOffset(R.styleable.FourInOneViews_dropDownDrawableWidth, -1);
            int dropDowndrawableHeight = a.getDimensionPixelOffset(R.styleable.FourInOneViews_dropDownDrawableHeight, -1);
            setDropDownDrawable(dropdownDrawable, dropDowndrawableWidth, dropDowndrawableHeight);
            setDropDownDrawableSpacing(dropdownDrawableScpacing);
        }
        mDropDownAnchorId = a.getResourceId(R.styleable.FourInOneViews_dropDownAnchor, View.NO_ID);
        popupWindow.setWidth(a.getLayoutDimension(R.styleable.FourInOneViews_dropDownDrawableWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        popupWindow.setHeight(a.getLayoutDimension(R.styleable.FourInOneViews_dropDownDrawableHeight, ViewGroup.LayoutParams.WRAP_CONTENT));
        popupWindow.setOnItemClickListener(new DropDownItemClickListener());
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mLastDismissTime = SystemClock.elapsedRealtime();
                if (mOnDismissListener != null) {
                    mOnDismissListener.onDismiss();
                }
            }
        });
        if (getBackground() == null)
            setBackground(null);
        else {
            setBackground(getBackground());
        }
        a.recycle();
        requestFocus();
        setCursorVisible(true);
        setShowSoftInputOnFocus(true);
        setEnabled(true);
        mIsEditable = true;
        initlist = new ArrayList();
        addTextChangedListener(new DoublePTextWatcher());
    }

    /**
     * For User to Set DropDown Anchor
     */

    public void setDropDownAnchor(int id) {
        mDropDownAnchorId = id;
        popupWindow.setAnchorView(null);
    }

    /**
     * To dismiss DropDown
     */

    public void dismissDropDown() {
        if (popupWindow != null) {
            if (popupWindow.isShowing())
                popupWindow.dismiss();
        }
    }

    /**
     * For user to get Dropdown Anchor
     *
     * @return
     */

    public int getDropDownAnchor() {
        return mDropDownAnchorId;
    }

    /**
     * To Show Drop Down
     */

    private void showDropDown() {
        if (popupWindow != null) {
            if (popupWindow.getAnchorView() == null) {
                if (mDropDownAnchorId != View.NO_ID) {
                    popupWindow.setAnchorView(getRootView().findViewById(mDropDownAnchorId));
                } else {
                    popupWindow.setAnchorView(this);
                }
            }

            if (!ispopupShowing()) {
                // Make sure the list does not obscure the IME when shown for the first time.
                popupWindow.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NEEDED);
            }
            if (popupWindow != null) {
                requestFocus();
                popupWindow.show();
                popupWindow.getListView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);
            }
            if (mOnShowListener != null) {
                mOnShowListener.onShow();
            }
        }
    }

    /**
     * For User to get Show Listener
     *
     * @param showListener
     */
    public void setOnShowListener(final OnShowListener showListener) {
        mOnShowListener = showListener;
    }

    /**
     * interface for show listener
     */
    private interface OnShowListener {
        void onShow();

    }

    /**
     * To check if the touch is in or within drop down area and returns accordingly
     *
     * @param event
     * @return
     */
    private boolean isInDropDownClickArea(MotionEvent event) {
        int areaLeft = mIsEditable ? getWidth() - getCompoundPaddingRight() : 0;
        int areaRight = getWidth();
        int areaTop = 0;
        int areaBottom = getHeight();

            return event.getX() > areaLeft && event.getX() < areaRight && event.getY() > areaTop && event.getY() < areaBottom;

    }

    /**
     * To Clear the List Selection
     */
    public void clearListSelection() {
        if (popupWindow != null)
            popupWindow.clearListSelection();
    }

    /**
     * User Can set Animation Style for DropDown
     *
     * @param animationStyle
     */
    private void setDropDownAnimationStyle(int animationStyle) {
        if (popupWindow != null)
            popupWindow.setAnimationStyle(animationStyle);
    }

    /**
     * User can set the drop down drawable icon
     *
     * @param drawable
     */
    public void setDropDownDrawable(Drawable drawable) {
        setDropDownDrawable(drawable, -1, -1);
    }

    /**
     * user can customise the height of dropdown
     *
     * @param height
     */
    public void setDropDownHeight(int height) {
        if (popupWindow != null)
            popupWindow.setHeight(height);
    }

    /**
     * User can customise the width of dropDown
     *
     * @param width
     */
    public void setDropDownWidth(int width) {
        if (popupWindow != null)
            popupWindow.setWidth(width);
    }

    /**
     * User can set drop down drawable with height and width customisation
     *
     * @param drawable
     * @param width
     * @param height
     */
    public void setDropDownDrawable(Drawable drawable, int width, int height) {
        dropdownDrawable = drawable;
        if (width >= 0 && height >= 0) {
            drawable.setBounds(new Rect(0, 0, width, height));
            setCompoundDrawables(null, null, drawable, null);
        } else
            setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }

    /**
     * User can set add some margin to dropdown drawable
     *
     * @param spacing
     */
    public void setDropDownDrawableSpacing(int spacing) {
        setCompoundDrawablePadding(spacing);
    }

    protected void performCompletion() {
        performCompletion(null, -1, -1);
    }

    /**
     * To perform text completetion in AutoComplete View
     *
     * @param view
     * @param pos
     * @param id
     */
    protected void performCompletion(View view, int pos, long id) {
        if (popupWindow != null) {
            if (ispopupShowing()) {
                Object selectedItem;
                if (pos < 0) {
                    selectedItem = popupWindow.getSelectedItem().toString();
                } else {
                    selectedItem = adapter.getItem(pos).toString();
                }
                if (selectedItem == null) {
                    if (true) {
                        Log.w("View", "performCompletion: no selected item");
                    }
                    return;
                }
                selectItem(selectedItem);
                if (mItemClickListener != null) {
                    final ListPopupWindow list = popupWindow;
                    if (view == null || pos < 0) {
                        view = list.getSelectedView();
                        pos = list.getSelectedItemPosition();
                        id = list.getSelectedItemId();
                    }
                    mItemClickListener.onItemClick(list.getListView(), view, pos, id);
                }
            }
        }
    }

    /**
     * passes the selected text to text replacement
     *
     * @param selectedItem
     */
    private void selectItem(Object selectedItem) {
        if (selectedItem != null) {
            replaceText(convertSelectionToString(selectedItem));
        }

    }

    /**
     * Converting Object type of Selection to String type
     *
     * @param selectedItem
     * @return
     */

    private CharSequence convertSelectionToString(Object selectedItem) {
        if (mItemConverter != null) {
            return mItemConverter.convertItemToString(selectedItem);
        } else {
            return selectedItem.toString();
        }
    }

    /**
     * Replace the text in View with user selected text
     *
     * @param text
     */

    private void replaceText(CharSequence text) {
        clearComposingText();
        setText(text);
        // make sure we keep the caret at the end of the text view
        Editable spannable = getText();
        Selection.setSelection(spannable, spannable.length());
    }

    /**
     * User can set the treshold(number of characters after the autoComplete textview to search among the list)
     *
     * @param num
     */
    public void setThreshold(int num) {
        if (num <= 0) {
            num = 1;
        }
        this.num = num;
    }

    /**
     * User to set Adapter in case for Spinner and AutoCompleteTextView only
     * Adapter Should be of ArrayAdapter Type
     *
     * @param madapter
     * @param <T>
     */
    public <T extends ArrayAdapter & Filterable> void setAdapter(T madapter) {
        if (popupWindow != null) {
            if (madapter != null) {
                this.adapter = madapter;
                popupWindow.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                if (mode == 2) {
                    if (adapter != null) {
                        for (int i = 0; i < adapter.getCount(); i++) {
                            initlist.add(adapter.getItem(i));
                        }
                    }
                } else {
                    initlist.clear();
                }
            } else {
                Log.e("CustView", "Adapter is null");
            }
        }
    }

    /**
     * For User to change the view on RunTime where the user can pass the values from enum
     *
     * @param view
     */
    public void changeView(selectview view) {

        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FourInOneViews, 0, 0);

        switch (view) {
            case TextView:
                mode = 0;
                initTextView();
                break;
            case EditText:
                mode = 1;
                initEditText();
                break;
            case AutoCompleteTextView:
                mode = 2;
                initAutoCompleteTextView(getContext(), attrs, a);
                break;
            case Spinner:
                mode = 3;
                initSpinner(getContext(), attrs, a);

                break;
        }
    }

    /**
     * To check whether the popup is showing
     *
     * @return
     */
    private boolean ispopupShowing() {
        if (popupWindow != null) {
            return popupWindow.isShowing();
        } else {
            return false;
        }
    }

    /**
     * User can set on Item Selected Listener for Spinner and AutoCompleteTextview Only
     *
     * @param l
     */
    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        if (mode == 2 || mode == 3) {
            if (mItemClickListener != null)
                mItemClickListener = l;
        }
    }

    public void removedropdownDrawable()
    {
        setDropDownDrawable(null);
    }

    /**
     * returns the view mode
     * 0 indicates TextView
     * 1 indicates EditText
     * 2 indicates AutoCompleteTextView
     * 3 indicates Spinner
     *
     * @return
     */
    public int getCurrentViewMode() {
        return mode;
    }

    /**
     * Interface for Conversion of Selected Item To String
     */
    public interface ItemConverter {
        String convertItemToString(Object selectedItem);
    }

    /**
     * If the user clicks on item in the list and set that text in Field
     */
    private class DropDownItemClickListner implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            performCompletion(view, position, id);
        }
    }

    /**
     * Class for getting the filtered list results for AutoCompleteTextView
     */
    private class GetList implements Filterable {

        List sortedlist = new ArrayList();
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    sortedlist.clear();
                    for (int i = 0; i < initlist.size(); i++) {
                        if (initlist.get(i).toString().contains(constraint.toString())) {
                            if (sortedlist!=null)
                                sortedlist.add(constraint.toString());
                        }
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = sortedlist;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (!TextUtils.isEmpty(results.values.toString()) && !results.values.toString().equalsIgnoreCase("[]")) {
                        showDropDown();
                    } else {
                        dismissDropDown();
                    }
                }
            };
        }

    }

    /**
     * Custom TextWatcher for list filtering in AutoComplete Layout
     */

    protected class DoublePTextWatcher implements TextWatcher {


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
        @Override
        public void afterTextChanged(Editable s) {
            if (mode == 2) {
                if (num < s.length()) {
                    adapter.getFilter().filter(s.toString());
                    adapter.notifyDataSetChanged();
                    showDropDown();
                } else {
                    dismissDropDown();
                }
            }
        }

    }
    /**
     * ItemClick Listener for spinner and AutoCompleteText view
     */

    protected class DropDownItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            performCompletion(v, position, id);
            dismissDropDown();
        }
    }
}
