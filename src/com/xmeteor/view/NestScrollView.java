package com.xmeteor.view;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;
import android.widget.ScrollView;

/**
 * Created by xs on 15/1/15.
 */
public class NestScrollView extends ScrollView {

    private int mTouchSlop;

    private int mLastMotionY;

    private boolean mIsBeingDragged = false;

    // disable current touch target
    private boolean mDisable = false;

    // the interupt touch flag
    private boolean mInterupt = true;

    // remeber the current touch flag
    private boolean mOriInterupt = true;


    private boolean mImitateClicked = false;

    // a sign now is imitate touch
    private boolean mImitateTouch = true;

    private ListView mListView;

    public NestScrollView(Context context) {
        this(context, null);
    }

    public NestScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    public void setListView(ListView listView) {
        mListView = listView;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mImitateTouch = false;
                mIsBeingDragged = false;
                mLastMotionY = (int) ev.getY();
                mOriInterupt = mInterupt;
                final float offsetX = getScrollX() - mListView.getLeft();
                final float offsetY = getScrollY() - mListView.getTop();

                // click is inside of listview and scrollview is still in screen
                if ((int)(offsetX+ev.getX()) >= 0 && (int)(offsetY+ev.getY()) >= 0 && canScrollDown(1)) {
                    imitateTouchEvent(ev, mListView);
                    mImitateTouch = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final int y = (int) ev.getY();
                int deltaY = mLastMotionY - y;
                boolean isSrollDown;
                if (deltaY > 0) {
                    isSrollDown = true;
                } else {
                    isSrollDown = false;
                }

                if (!mIsBeingDragged) {
                    if (Math.abs(deltaY) > mTouchSlop) {
                        mIsBeingDragged = true;
                        deltaY = deltaY > 0 ? deltaY-mTouchSlop : deltaY+mTouchSlop;
                    } else {
                        break;
                    }
                }

                if (mImitateTouch) {
                    imitateTouchEvent(MotionEvent.ACTION_CANCEL, ev.getX(), ev.getY(), mListView);
                    mImitateTouch = false;
                }

                if (!isSrollDown) {
                    // pull from top to bottom and listview can not scroll any more
                     if (!canScrollList(-1, mListView)) {
                         mInterupt = true;
                         if (!mOriInterupt) {
                             mDisable = true;
                             scrollBy(0, deltaY);
                         } else {
                             mDisable = false;
                         }
                     } else {
                         mDisable = true;
                         if (mImitateClicked) {
                             imitateTouchEvent(ev, mListView);
                         }
                     }
                } else {
                    // pull from bottom to top and scrollview can not scroll any more
                    if (!canScrollDown(1)) {
                        // move from scrollview to listview
                        if (mOriInterupt && mInterupt) {
                            imitateTouchEvent(MotionEvent.ACTION_DOWN, ev.getX(), ev.getY(), mListView);
                            mImitateClicked = true;
                        }
                        if (mOriInterupt) {
                            imitateTouchEvent(ev, mListView);
                        }
                        mInterupt = false;
                        mDisable = true;
                    } else {
                        mInterupt = true;
                        if (canScrollDown(-1) && !mOriInterupt) {
                            mDisable = true;
                            scrollBy(0, deltaY);
                        } else  {
                            mDisable = false;
                        }
                    }
                }
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_UP:
                mOriInterupt = mInterupt;
                if (mImitateTouch || mImitateClicked) {
                    imitateTouchEvent(ev, mListView);
                    mImitateTouch = mImitateClicked = false;
                }
                break;

        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mInterupt;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mDisable) {
            return true;
        } else {
            return super.onTouchEvent(ev);
        }
    }

    private void imitateTouchEvent(int action, float x, float y, View child) {
        MotionEvent transformedEvent = doTransformedTouchEvent(action, x, y, child);
        child.dispatchTouchEvent(transformedEvent);
        transformedEvent.recycle();
    }

    private void imitateTouchEvent(MotionEvent event, View child) {
        MotionEvent transformedEvent = doTransformedTouchEvent(event, child, true);
        child.dispatchTouchEvent(transformedEvent);
        transformedEvent.recycle();
    }

    private MotionEvent doTransformedTouchEvent(int action, float x, float y, View child) {
        MotionEvent event = imitateEvent(action, x, y);
        return doTransformedTouchEvent(event, child, false);
    }

    private MotionEvent doTransformedTouchEvent(MotionEvent event, View child, boolean rebuild) {
        if (rebuild) {
            event = MotionEvent.obtain(event);
        }
        final float offsetX = getScrollX() - child.getLeft();
        final float offsetY = getScrollY() - child.getTop();
        event.offsetLocation(offsetX, offsetY);
        return event;
    }

    private MotionEvent imitateEvent(int action, float x, float y) {
        MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), action, x, y, 0);
        return event;
    }

    // is scrollview can scroll
    private boolean canScrollDown(int direction) {
        final int offset = computeVerticalScrollOffset();
        final int range = computeVerticalScrollRange() - computeVerticalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset >= 0;
        } else {
            return offset < range;
        }
    }

    /**
     * Check if the items in the list can be scrolled in a certain direction.
     *
     * @param direction Negative to check scrolling up, positive to check
     *            scrolling down.
     * @return true if the list can be scrolled in the specified direction,
     *         false otherwise.
     */
    public boolean canScrollList(int direction, ListView listView) {
        if (listView == null) {
            return false;
        }

        final int childCount = listView.getChildCount();
        if (childCount == 0) {
            return false;
        }

        final int firstPosition = listView.getFirstVisiblePosition();
        if (direction > 0) {
            final int lastBottom = listView.getChildAt(childCount - 1).getBottom();
            return lastBottom < listView.getAdapter().getCount() || lastBottom > getHeight()-listView.getListPaddingBottom();
        } else {
            final int firstTop = listView.getChildAt(0).getTop();
            return firstPosition > 0 || firstTop < listView.getListPaddingTop();
        }
    }
}
