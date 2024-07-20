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
package io.github.annaharri89.stitchcounter

import android.provider.BaseColumns

/**
 * Created by ETASpare on 6/15/2017.
 */
class StitchCounterContract  // To prevent someone from accidentally instantiating the contract class,
// the constructor is private.
private constructor() {
    /* Inner class that defines the table contents */
    object CounterEntry : BaseColumns {
        const val _ID: String = "_id"
        const val TABLE_NAME: String = "entry"
        const val COLUMN_TYPE: String = "type"
        const val COLUMN_TITLE: String = "title"
        const val COLUMN_STITCH_COUNTER_NUM: String = "stitch_counter_number"
        const val COLUMN_STITCH_ADJUSTMENT: String = "stitch_adjustment"
        const val COLUMN_ROW_COUNTER_NUM: String = "row_counter_number"
        const val COLUMN_ROW_ADJUSTMENT: String = "row_adjustment"
        const val COLUMN_TOTAL_ROWS: String = "total_rows"
    }
}

