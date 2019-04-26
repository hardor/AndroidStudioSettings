package ru.profapp.ranobe.customElements

import android.content.Context
import android.content.res.TypedArray
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.NumberPicker
import ru.profapp.ranobe.MyApp

/**
 * A [android.preference.Preference] that displays a number picker as a dialog.
 */
class NumberPickerPreference : DialogPreference {

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr)

    companion object {
        const val MAX_VALUE: Int = 30
        const val MIN_VALUE: Int = 6
    }

    private lateinit var picker: NumberPicker
    private var value: Int = 0
        set(value) {
            field = value
            persistInt(this.value)
        }

    override fun onCreateDialogView(): View {
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER

        picker = NumberPicker(context)
        picker.layoutParams = layoutParams

        val dialogView = FrameLayout(context)
        dialogView.addView(picker)

        return dialogView
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        picker.minValue = MIN_VALUE
        picker.maxValue = MAX_VALUE
        picker.wrapSelectorWheel = true
        picker.value = value
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            picker.clearFocus()
            val newValue = picker.value
            if (callChangeListener(newValue)) {
                value = newValue
                MyApp.preferencesManager.fontSize = newValue
            }
        }
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a?.getInt(index, 16) ?: 16
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        value = if (restorePersistedValue) getPersistedInt(MIN_VALUE) else defaultValue as Int
    }

}