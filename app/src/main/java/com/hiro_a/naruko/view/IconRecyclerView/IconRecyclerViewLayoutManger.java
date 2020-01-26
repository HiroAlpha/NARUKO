package com.hiro_a.naruko.view.IconRecyclerView;

import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class IconRecyclerViewLayoutManger extends RecyclerView.LayoutManager {

    String TAG = "NARUKO_DEBUG";

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        final View lastTopView = getChildCount()>0?getChildAt(0):null;
        final int lastTop = lastTopView != null? lastTopView.getTop():getPaddingTop();
        final int firstPosition = lastTopView != null ? getPosition(lastTopView):0;

        final View lastLeftView = getChildCount()>0?getChildAt(0):null;
        final int lastLeft = lastLeftView != null? lastLeftView.getLeft():getPaddingLeft();

        //再利用
        detachAndScrapAttachedViews(recycler);

        //子ビューの配置位置
        int top = lastTop;
        int offsetTop;
        int bottom;
        int left = lastLeft;
        int right;
        final int parentLeft = getPaddingLeft() + getPaddingRight();
        final int parentRight = getWidth() - getPaddingRight();
        final int parentBottom = getHeight() - getPaddingBottom();

        final int screenWidth = getWidth();
        final int screenHeight = getHeight();
        final int itemSpace = screenWidth/3;

        //ビューサイズ計測
        View exView = recycler.getViewForPosition(0);
        measureChildWithMargins(exView, 0, 0);
        int viewWidth = getDecoratedMeasuredWidth(exView);
        int viewHeight = getDecoratedMeasuredHeight(exView);

        final int count = state.getItemCount();
        int lineCounter = 1;
        for (int i=0;firstPosition+i<count && top-(viewHeight/2)*i<parentBottom;i++, top=bottom){
            //positionを指定してアイテム用ビュー取得
            View view = recycler.getViewForPosition(firstPosition+i);
            addView(view, i);

            //個々に計測
            measureChildWithMargins(view, 0, 0);
            viewWidth = getDecoratedMeasuredWidth(view);
            viewHeight = getDecoratedMeasuredHeight(view);

            offsetTop = top-(viewHeight/2)*i;//上辺
            switch (lineCounter){
                default:
                    right = left + viewWidth;//右辺
                    bottom = top + viewHeight;//下辺
                    break;
                case 1:
                    left = ((itemSpace*2)+itemSpace/2) - viewWidth/2;//左辺
                    right = left + viewWidth;//右辺
                    bottom = top + viewHeight;//下辺
                    lineCounter++;
                    break;
                case 2:
                    left = (itemSpace+itemSpace/2) - viewWidth/2;//左辺
                    right = left + viewWidth;//右辺
                    bottom = top + viewHeight;//下辺
                    lineCounter++;
                    break;
                case 3:
                    left = itemSpace/2 - viewWidth/2;//左辺
                    right = left + viewWidth;//右辺
                    bottom = top + viewHeight;//下辺
                    lineCounter=1;
                    break;

            }

            //ビューの配置位置指定
            layoutDecorated(view, left, offsetTop, right, bottom);
//            Log.d("DEBUG", "DecoratedWidth:"+viewWidth);
//            Log.d("DEBUG", "DecoratedHeight:"+viewHeight);
//            Log.d("DEBUG", "------------------");
//            Log.d("DEBUG", "Left:"+left);
//            Log.d("DEBUG", "Top:"+offsetTop);
//            Log.d("DEBUG", "Right:"+right);
//            Log.d("DEBUG", "Bottom:"+bottom);
//            Log.d("DEBUG", "------------------");
        }
//        Log.d(TAG, "Screen Width:"+screenWidth);
//        Log.d(TAG, "Screen Height:"+screenHeight);
//        Log.d(TAG, "Item Width:"+viewWidth);
//        Log.d(TAG, "Item Height:"+viewHeight);
    }
/*
    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount()==0){
            return 0;
        }

        int scrolled = 0;
        final int parentLeft = getPaddingLeft();
        final int parentRight = getWidth() - getPaddingRight();

        //上スクロール
        if (dy < 0){
            while (scrolled > dy){
                final View topView = getChildAt(0);
                //topViewのはみ出ている量
                final int hangingTop = Math.max(-getDecoratedTop(topView), 0);
                //スクロール幅
                final int scrollBy = Math.min(scrolled - dy, hangingTop);
                scrolled -= scrollBy;

                //縦方向にItemをずらす
                offsetChildrenVertical(scrollBy);
                //上にまだviewがあるなら上に追加
                int mFirstPosition = getPosition(topView);
                if (mFirstPosition > 0 && scrolled > dy){
                    mFirstPosition--;
                    //position指定でview取得
                    View view = recycler.getViewForPosition(mFirstPosition);
                    addView(view, 0);
                    //ビューサイズ計算
                    measureChildWithMargins(view, 0, 0);
                    final int bottom = getDecoratedTop(topView);
                    final int top = bottom - getDecoratedMeasuredHeight(view);
                }else {
                    break;
                }
            }
        }
        //下スクロール
        else if (dy > 0){
            while (scrolled < dy){
                final View bottomView = getChildAt(0);
                //topViewのはみ出ている量
                final int hangingBottom = Math.max(-getDecoratedBottom(bottomView), 0);
                //スクロール幅
                final int scrollBy = Math.min(scrolled - dy, hangingBottom);
                scrolled -= scrollBy;

                //縦方向にItemをずらす
                offsetChildrenVertical(scrollBy);
                //上にまだviewがあるなら上に追加
                int mLastPosition = getPosition(bottomView);
                if (mLastPosition < 0 && scrolled < dy){
                    mLastPosition--;
                    //position指定でview取得
                    View view = recycler.getViewForPosition(mLastPosition);
                    addView(view, 0);
                    //ビューサイズ計算
                    measureChildWithMargins(view, 0, 0);
                    final int top = getDecoratedBottom(bottomView);
                    final int bottom = top - getDecoratedMeasuredHeight(view);
                }else {
                    break;
                }
            }
        }

        final int childCount = getChildCount();
        final int parentWidth = getWidth();
        final int parentHeight = getHeight();
        boolean foundFirst = false;
        int first = 0;
        int last = 0;
        for (int i=0;i<childCount;i++){
            final View view = getChildAt(i);
            if (view.hasFocus() || (getDecoratedRight(view) >= 0 &&
                    getDecoratedLeft(view) <= parentWidth &&
                    getDecoratedBottom(view) >= 0 &&
                    getDecoratedTop(view) <= parentHeight)){
                if (!foundFirst){
                    first = i;
                    foundFirst = true;
                }
                last = i;
            }
        }
        for (int i=childCount-1;i>last;i--){
            removeAndRecycleViewAt(i, recycler);
        }
        for (int i=first-1;i>=0;i--){
            removeAndRecycleViewAt(i, recycler);
        }

        return scrolled;
    }
*/
}
