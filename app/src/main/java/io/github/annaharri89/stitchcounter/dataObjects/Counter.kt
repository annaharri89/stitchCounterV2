package io.github.annaharri89.stitchcounter.dataObjects

data class Counter(val id: Int,
                   val type: String,
                   val name: String,
                   val stitchCounterNumber: Int,
                   val stitchAdjustment: Int,
                   val rowCounterNumber: Int,
                   val rowAdjustment: Int,
                   val totalRows: Int)