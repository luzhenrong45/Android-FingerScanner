/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mobi.thinkchange.android.fingerscannercn.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.shapes.Shape;

/**
 * Defines a triangle shape.
 * The rectangle can be drawn to a Canvas with its own draw() method,
 * but more graphical control is available if you instead pass
 * the TriangleShape to a {@link android.graphics.drawable.ShapeDrawable}.
 */
public class TriangleShape extends Shape {
    private RectF mRect = new RectF();
    private Path mTrianglePath = new Path();

    public TriangleShape() {
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        // 构建triangle path
        mTrianglePath.moveTo(0, 0);
        mTrianglePath.lineTo(mRect.width(), 0);
        mTrianglePath.lineTo(0, mRect.height());
        mTrianglePath.lineTo(0, 0);
        mTrianglePath.close();

        canvas.drawPath(mTrianglePath, paint);
    }

    @Override
    protected void onResize(float width, float height) {
        mRect.set(0, 0, width, height);
    }

    /**
     * Returns the RectF that defines this rectangle's bounds.
     */
    protected final RectF rect() {
        return mRect;
    }

    @Override
    public TriangleShape clone() throws CloneNotSupportedException {
        final TriangleShape shape = (TriangleShape) super.clone();
        shape.mRect = new RectF(mRect);
        return shape;
    }
}
