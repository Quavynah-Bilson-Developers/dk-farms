/*
 * Copyright (c) 2018. Property of Dennis Kwabena Bilson. No unauthorized duplication of this material should be made without prior permission from the developer
 */

package io.peanutsdk.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.Dimension;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * A {@link RecyclerView.ItemDecoration} which draws dividers (along the right & bottom)
 * for {@link RecyclerView.ViewHolder}s which implement {@link Divided}.
 */
public class GridItemDividerDecoration extends RecyclerView.ItemDecoration {
	
	private final int dividerSize;
	private final Paint paint;
	
	public GridItemDividerDecoration(@Dimension int dividerSize,
	                                 @ColorInt int dividerColor) {
		this.dividerSize = dividerSize;
		paint = new Paint();
		paint.setColor(dividerColor);
		paint.setStyle(Paint.Style.FILL);
	}
	
	public GridItemDividerDecoration(@NonNull Context context,
	                                 @DimenRes int dividerSizeResId,
	                                 @ColorRes int dividerColorResId) {
		this(context.getResources().getDimensionPixelSize(dividerSizeResId),
				ContextCompat.getColor(context, dividerColorResId));
	}
	
	@Override
	public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
		if (parent.isAnimating()) return;
		
		int childCount = parent.getChildCount();
		RecyclerView.LayoutManager lm = parent.getLayoutManager();
		for (int i = 0; i < childCount; i++) {
			View child = parent.getChildAt(i);
			RecyclerView.ViewHolder viewHolder = parent.getChildViewHolder(child);
			
			if (viewHolder instanceof Divided) {
				int right = lm.getDecoratedRight(child);
				int bottom = lm.getDecoratedBottom(child);
				// draw the bottom divider
				c.drawRect(lm.getDecoratedLeft(child),
						bottom - dividerSize,
						right,
						bottom,
						paint);
				// draw the right edge divider
				c.drawRect(right - dividerSize,
						lm.getDecoratedTop(child),
						right,
						bottom - dividerSize,
						paint);
			}
		}
	}
	
}
