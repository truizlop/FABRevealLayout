/*
 * Copyright (C) 2015 Tomás Ruiz-López.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.truizlop.fabreveallayout;

import android.animation.TypeEvaluator;

/**
 * Created by tomas on 11/08/15.
 */
public class CurvedPathEvaluator implements TypeEvaluator<Point> {

    @Override
    public Point evaluate(float t, Point startValue, Point endValue) {
        float x, y;

        float oneMinusT = 1 - t;
        x = oneMinusT * oneMinusT * oneMinusT * startValue.x +
                3 * oneMinusT * oneMinusT * t * endValue.control0X +
                3 * oneMinusT * t * t * endValue.control1X +
                t * t * t * endValue.x;
        y = oneMinusT * oneMinusT * oneMinusT * startValue.y +
                3 * oneMinusT * oneMinusT * t * endValue.y +
                3 * oneMinusT * t * t * endValue.y +
                t * t * t * endValue.y;

        return new Point(x,y);
    }
}
