package app.chiggi.nutrilio.extension;

import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adds a live, per-meal-time food search to Nutrilio's day/meal form.
 *
 * <p>The form is a single RecyclerView (the obfuscated adapter hooked by the patch) with mixed row
 * types. Each meal-time header row (identified by the {@code chip_meal_time} view) gets a search box
 * injected below its chip by the companion resource patch. Typing filters the food chips of every
 * tag-group row in that meal-time section by toggling each chip's
 * {@link View#setVisibility(int) visibility}. This is purely visual: hiding a chip never un-adds it,
 * so already-logged items survive filtering exactly as confirmed by the user.
 *
 * <p>Driven from a single bytecode hook on the form adapter's
 * {@code onAttachedToRecyclerView(RecyclerView)} which calls {@link #install(ViewGroup)}; recycling
 * is handled with a {@link ViewGroup.OnHierarchyChangeListener} so off-screen rows are re-filtered
 * as they scroll in. No androidx types are referenced (the RecyclerView is treated as a ViewGroup),
 * and resource ids are resolved by name at runtime, so the helper is not pinned to one app version.
 */
public final class MealTimeSearchFilter {

    /** Meal-time name -> current query text. Survives row recycling. */
    private static final Map<String, String> QUERIES = new HashMap<>();
    /** Last meal-time name seen top-down; fallback for tag rows whose header scrolled off-screen. */
    private static String sLastSectionName = null;

    // Resource ids resolved by name once (stable names across app versions).
    private static boolean sResolved;
    private static int sIdChipMealTime; // present only on meal-time header rows
    private static int sIdChipText;     // meal-time name TextView inside the chip
    private static int sIdContainerTags; // InterceptFlowLayout holding a tag group's chips
    private static int sIdName;          // the food-name TextView inside a chip
    private static int sIdIconCross;     // the X / clear button in the search box

    private MealTimeSearchFilter() {
    }

    /** Entry point invoked from the form adapter's onAttachedToRecyclerView(RecyclerView). */
    public static void install(final ViewGroup recyclerView) {
        if (recyclerView == null) {
            return;
        }
        resolveIds(recyclerView);
        if (sIdChipMealTime == 0 || sIdChipText == 0 || sIdContainerTags == 0) {
            return; // app layout ids not found on this version; do nothing rather than misbehave
        }
        if (Boolean.TRUE.equals(recyclerView.getTag(sIdChipMealTime))) {
            return;
        }
        recyclerView.setTag(sIdChipMealTime, Boolean.TRUE);

        recyclerView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                post(recyclerView);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });
        post(recyclerView);
    }

    private static void resolveIds(View view) {
        if (sResolved) {
            return;
        }
        Resources res = view.getResources();
        String pkg = view.getContext().getPackageName();
        sIdChipMealTime = res.getIdentifier("chip_meal_time", "id", pkg);
        sIdChipText = res.getIdentifier("chip_text", "id", pkg);
        sIdContainerTags = res.getIdentifier("container_tags", "id", pkg);
        sIdName = res.getIdentifier("name", "id", pkg);
        sIdIconCross = res.getIdentifier("icon_cross", "id", pkg);
        sResolved = true;
    }

    private static void post(final ViewGroup recyclerView) {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                applyAll(recyclerView);
            }
        });
    }

    /** Wires any newly bound header boxes and re-filters every attached tag-group row. */
    private static void applyAll(ViewGroup recyclerView) {
        int count = recyclerView.getChildCount();
        List<View> rows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rows.add(recyclerView.getChildAt(i));
        }
        // RecyclerView children are not guaranteed to be in visual order; sort top-to-bottom so a
        // tag-group row inherits the query of the nearest meal-time header above it.
        Collections.sort(rows, new Comparator<View>() {
            @Override
            public int compare(View a, View b) {
                return Float.compare(a.getY(), b.getY());
            }
        });

        String current = sLastSectionName;
        for (View row : rows) {
            if (row.findViewById(sIdChipMealTime) != null) {
                String name = wireHeader(recyclerView, row);
                if (name != null) {
                    current = name;
                    sLastSectionName = name;
                }
            }
            View container = row.findViewById(sIdContainerTags);
            if (container instanceof ViewGroup) {
                String query = current == null ? "" : QUERIES.get(current);
                filter((ViewGroup) container, query == null ? "" : query);
            }
        }
    }

    /** Ensures the header's injected search box is wired to its meal-time; returns the meal name. */
    private static String wireHeader(final ViewGroup recyclerView, View headerRow) {
        View chipText = headerRow.findViewById(sIdChipText);
        if (!(chipText instanceof TextView)) {
            return null;
        }
        final String name = ((TextView) chipText).getText().toString();
        final EditText search = firstEditText(headerRow);
        if (search == null) {
            return name;
        }
        if (name.equals(search.getTag(sIdChipText))) {
            return name; // already wired for this meal time
        }

        // Recycled to a different meal time: drop the previous watcher before re-wiring.
        Object previous = search.getTag(sIdContainerTags);
        if (previous instanceof TextWatcher) {
            search.removeTextChangedListener((TextWatcher) previous);
        }
        search.setTag(sIdChipText, name);

        // Wire the X / clear button to empty the field (its TextWatcher then re-shows every chip).
        if (sIdIconCross != 0) {
            View cross = headerRow.findViewById(sIdIconCross);
            if (cross != null) {
                cross.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        search.setText("");
                    }
                });
            }
        }

        String saved = QUERIES.get(name);
        if (saved == null) {
            saved = "";
        }
        if (!saved.equals(search.getText().toString())) {
            search.setText(saved);
        }

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                QUERIES.put(name, s.toString());
                post(recyclerView);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        search.addTextChangedListener(watcher);
        search.setTag(sIdContainerTags, watcher);
        return name;
    }

    /** Shows chips whose food name contains the query (case-insensitive); hides the rest. */
    private static void filter(ViewGroup container, String query) {
        String needle = query.trim().toLowerCase(Locale.getDefault());
        int count = container.getChildCount();
        for (int i = 0; i < count; i++) {
            View chip = container.getChildAt(i);
            chip.setVisibility(needle.isEmpty() || chipMatches(chip, needle) ? View.VISIBLE : View.GONE);
        }
    }

    private static boolean chipMatches(View chip, String needle) {
        // Prefer the dedicated food-name label (R.id.name); a chip also carries a quantity/diff
        // number, and matching the first TextView found would test that number instead of the name.
        if (sIdName != 0) {
            View name = chip.findViewById(sIdName);
            if (name instanceof TextView) {
                return ((TextView) name).getText().toString().toLowerCase(Locale.getDefault()).contains(needle);
            }
        }
        // Fallback: any TextView descendant whose text contains the query.
        return anyTextViewContains(chip, needle);
    }

    private static boolean anyTextViewContains(View view, String needle) {
        if (view instanceof EditText) {
            return false;
        }
        if (view instanceof TextView) {
            return ((TextView) view).getText().toString().toLowerCase(Locale.getDefault()).contains(needle);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                if (anyTextViewContains(group.getChildAt(i), needle)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static EditText firstEditText(View view) {
        if (view instanceof EditText) {
            return (EditText) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                EditText found = firstEditText(group.getChildAt(i));
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
