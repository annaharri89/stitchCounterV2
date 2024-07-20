/*
   Copyright 2017 Anna Harrison

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package io.github.annaharri89.stitchcounter;

import android.provider.BaseColumns;

/**
 * Created by ETASpare on 6/15/2017.
 */

public final class StitchCounterContract {
    // To prevent someone from accidentally instantiating the contract class,
    // the constructor is private.
    private StitchCounterContract() {
    }

    /* Inner class that defines the table contents */
    public static class CounterEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_STITCH_COUNTER_NUM = "stitch_counter_number";
        public static final String COLUMN_STITCH_ADJUSTMENT = "stitch_adjustment";
        public static final String COLUMN_ROW_COUNTER_NUM = "row_counter_number";
        public static final String COLUMN_ROW_ADJUSTMENT = "row_adjustment";
        public static final String COLUMN_TOTAL_ROWS = "total_rows";
    }
}

