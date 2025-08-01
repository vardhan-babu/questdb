/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2024 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.griffin.engine.functions.array;

import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.arr.ArrayView;
import io.questdb.cairo.sql.Function;
import io.questdb.cairo.sql.Record;
import io.questdb.griffin.FunctionFactory;
import io.questdb.griffin.SqlException;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.std.IntList;
import io.questdb.std.ObjList;

public class DoubleArrayAvgFunctionFactory implements FunctionFactory {
    private static final String FUNCTION_NAME = "array_avg";

    @Override
    public String getSignature() {
        return FUNCTION_NAME + "(D[])";
    }

    @Override
    public Function newInstance(int position, ObjList<Function> args, IntList argPositions, CairoConfiguration configuration, SqlExecutionContext sqlExecutionContext) throws SqlException {
        return new Func(args.getQuick(0));
    }

    static class Func extends DoubleArraySumFunctionFactory.Func {
        private int count = 0;

        Func(Function arrayArg) {
            super(arrayArg);
        }

        @Override
        public void applyToElement(ArrayView view, int index) {
            double v = view.getDouble(index);
            if (!Double.isNaN(v)) {
                sum += v;
                count++;
            }
        }

        @Override
        public void applyToEntireVanillaArray(ArrayView view) {
            sum = view.flatView().avgDouble(view.getFlatViewOffset(), view.getFlatViewLength());
        }

        @Override
        public void applyToNullArray() {
            sum = Double.NaN;
        }

        @Override
        public double getDouble(Record rec) {
            ArrayView arr = arrayArg.getArray(rec);
            count = 0;
            sum = 0d;
            boolean vanilla = arr.isVanilla();
            calculate(arr);
            return vanilla ? sum : sum / count;
        }

        @Override
        public String getName() {
            return FUNCTION_NAME;
        }
    }
}
