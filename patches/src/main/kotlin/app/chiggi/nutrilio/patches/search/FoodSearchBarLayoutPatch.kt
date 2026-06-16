package app.chiggi.nutrilio.patches.search

import app.chiggi.nutrilio.patches.shared.Constants.COMPATIBILITY_NUTRILIO
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Injects the app's own search box (layout_search_box) into the meal-time header row, directly below
 * the meal-time chip. The companion bytecode patch wires the box up at runtime. Kept as a separate
 * resource patch so the bytecode patch can depend on it.
 */
val foodSearchBarLayoutPatch = resourcePatch(
    name = "Food search bar layout",
    description = "Adds the search box view below each meal-time header in the day/meal form. " +
        "Enabled automatically by \"Add food search bar\".",
    default = false,
) {
    compatibleWith(COMPATIBILITY_NUTRILIO)

    execute {
        document("res/layout/list_item_form_meal_time_header.xml").use { document ->
            // Root is the RelativeLayout that holds the meal-time chip (@id/chip_meal_time).
            val root = document.documentElement

            // Anchor the chip to the top. By default it is layout_centerInParent, which re-centres it
            // over anything placed below it (the search box) once the row grows, overlapping it.
            val includes = document.getElementsByTagName("include")
            for (i in 0 until includes.length) {
                val element = includes.item(i) as Element
                if (element.getAttribute("android:id") == "@id/chip_meal_time") {
                    element.removeAttribute("android:layout_centerInParent")
                    element.setAttribute("android:layout_alignParentTop", "true")
                }
            }

            val searchBox = document.createElement("include").apply {
                setAttribute("layout", "@layout/layout_search_box")
                setAttribute("android:layout_width", "match_parent")
                setAttribute("android:layout_height", "wrap_content")
                setAttribute("android:layout_below", "@id/chip_meal_time")
                setAttribute("android:layout_marginLeft", "@dimen/card_side_margin")
                setAttribute("android:layout_marginRight", "@dimen/card_side_margin")
                setAttribute("android:layout_marginBottom", "@dimen/normal_margin")
            }
            root.appendChild(searchBox)
        }
    }
}
