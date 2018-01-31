package com.hironytic.kiretan0a.view.util

sealed class UpdateHint {
    object Whole: UpdateHint()
    data class Partial(val changes: List<Change>): UpdateHint()
    object None: UpdateHint()

    sealed class Change {
        data class Deleted(val index: Int) : Change()
        data class Inserted(val index: Int) : Change()
        data class Moved(val oldIndex: Int, val newIndex: Int) : Change()
    }
}
