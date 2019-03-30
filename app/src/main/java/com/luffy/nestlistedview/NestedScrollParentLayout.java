package com.luffy.nestlistedview;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class NestedScrollParentLayout extends RelativeLayout implements NestedScrollingParent,NestedScrollingChild {
    private NestedScrollingParentHelper mParentHelper;
    private int mTitleHeight;
    private View mTitleTabView;
    private Scroller mScroller;

    public NestedScrollParentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NestedScrollParentLayout(Context context) {
        super(context);
        init();
    }

    private void init() {
        mParentHelper = new NestedScrollingParentHelper(this);
        mScroller = new Scroller(this.getContext());
    }

    //获取子view
    @Override
    protected void onFinishInflate() {
        mTitleTabView = this.findViewById(R.id.title_container);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mTitleHeight = mTitleTabView.getMeasuredHeight();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec + mTitleHeight);
    }

    //接口实现--------------------------------------------------

    //在此可以判断参数target是哪一个子view以及滚动的方向，然后决定是否要配合其进行嵌套滚动
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        if (target instanceof NestedListView) {
            return true;
        }
        return false;
    }


    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        mParentHelper.onStopNestedScroll(target);
    }

    //先于child滚动
    //前3个为输入参数，最后一个是输出参数
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (dy > 0) {//手势向上滑动,如果小于可滑动标题的高度则全部消费掉滑动
            if (getScrollY() < mTitleHeight) {
                scrollBy(0, dy);//滚动
                consumed[1] = dy;//告诉child我消费了多少
            }
        } else if (dy < 0) {//手势向下滑动,如果有scroll过则恢复，全部消耗
            if (getScrollY() > 0) {
                scrollBy(0, dy);//滚动
                consumed[1] = dy;//告诉child我消费了多少
            }
        }
    }

    //后于child滚动
    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {

    }


    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }


    //scrollBy内部会调用scrollTo
    //限制滚动范围
    @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > mTitleHeight) {
            y = mTitleHeight;
        }

        super.scrollTo(x, y);
    }


    //返回值：是否消费了fling
    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        Log.e("TAG", "velocityY: " + velocityY);
        if (Math.abs(velocityY) > ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity() && getScrollY() < mTitleHeight && getScrollY() > 0) {
            flingWithNestedDispatch((int) velocityY);
            Log.e("TAG", "fling被父视图消耗返回true");
            return true;
        }
        return false;
    }

    //返回值：是否消费了fling
    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
//        if (!consumed) {
//            flingWithNestedDispatch((int) velocityY);
//            return true;
//        }
        return false;
    }

    private void flingWithNestedDispatch(int velocityY) {
        final int scrollY = getScrollY();
        Log.e("TAG", "scrollY: " + scrollY);
        fling(velocityY);
    }

    public void fling(int velocityY) {

        mScroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, 0, mTitleHeight);
        Log.e("TAG", "fling开始");
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //触发draw()
            postInvalidate();
        }
    }

}
