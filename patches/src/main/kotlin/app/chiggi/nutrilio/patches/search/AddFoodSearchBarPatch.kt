package app.chiggi.nutrilio.patches.search

import app.chiggi.nutrilio.patches.shared.Constants.COMPATIBILITY_NUTRILIO
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val addFoodSearchBarPatch = bytecodePatch(
    name = "Add food search bar",
    description = "Adds a live search box below each meal-time header in the day/meal form. Typing " +
        "filters that meal's food chips as you type; filtering only hides chips, so items you have " +
        "already added stay added. The hardest patch to keep working — it hooks the obfuscated " +
        "form adapter and will likely need re-fingerprinting after Nutrilio updates.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_NUTRILIO)

    // Pulls in the search-box layout and the MealTimeSearchFilter extension class.
    dependsOn(foodSearchBarLayoutPatch)
    extendWith("extensions/extension.mpe")

    execute {
        // il.r0.onAttachedToRecyclerView(rv) -> MealTimeSearchFilter.install(rv).
        // p1 is the form's RecyclerView (an android.view.ViewGroup).
        FormAdapterAttachedFingerprint.method.addInstructions(
            0,
            "invoke-static { p1 }, " +
                "Lapp/chiggi/nutrilio/extension/MealTimeSearchFilter;->install(Landroid/view/ViewGroup;)V",
        )
    }
}
