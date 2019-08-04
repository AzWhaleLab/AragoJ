package com.aragoj.mainscreen.menuitems

sealed class MenuItemAction {

    data class Click(val item: Item) : MenuItemAction()
    data class Check(val item: Item, val checked: Boolean) : MenuItemAction()

    enum class Item {
        // File menu
        NEW_SESSION, OPEN_SESSION, SAVE_SESSION, SAVE_SESSION_AS, IMPORT_IMAGES, EXPORT_CSV, EXIT,

        // View menu
        PRECISION_LINES_CHECK, IDENTIFIER_LINES_CHECK,

        // Calibration menu
        CALIBRATE_CAMERA, UNDISTORT_IMAGE,

        // Unit Conversion
        CONVERT_UNITS, CONVERT_RATIO_DF, CONVERT_SCALE
    }

}