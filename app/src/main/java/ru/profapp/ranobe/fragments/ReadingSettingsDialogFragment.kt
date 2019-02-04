package ru.profapp.ranobe.fragments

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import ru.profapp.ranobe.common.Constants
import top.defaults.colorpicker.ColorPickerPopup


public class ReadingSettingsDialogFragment : DialogFragment() {

    lateinit var mListener: DialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface DialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)
        // Get the layout inflater
        val inflater = activity!!.layoutInflater
        isCancelable = false

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        val view = inflater.inflate(R.layout.dialog_readingsettings, null)

        val fontSpinner: Spinner = view.findViewById(R.id.spinner_reading_font)


        val fontAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, enumValues<Constants.CustomFonts>().map { it.title }).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            fontSpinner.adapter = adapter
        }

        fontSpinner.setSelection(fontAdapter.getPosition(Constants.CustomFonts.getTitleByFile(MyApp.preferencesManager.font)))

        val chooseColorWebviewTextButton: Button = view.findViewById(R.id.btn_chooseColor_webviewText)
        val chooseColorWebviewTextEditText: EditText = view.findViewById(R.id.eT_chooseColor_webviewText)

        chooseColorWebviewTextButton.setBackgroundColor(MyApp.preferencesManager.textColor
                ?: resources.getColor(R.color.webViewText))
        chooseColorWebviewTextEditText.setText(String.format("%06X", 0xFFFFFF and (MyApp.preferencesManager.textColor
                ?: resources.getColor(R.color.webViewText))))


        chooseColorWebviewTextEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                try {
                    val editColor = Color.parseColor("#$s")
                    chooseColorWebviewTextButton.setBackgroundColor(editColor)
                } catch (e: IllegalArgumentException) {
                    chooseColorWebviewTextButton.setBackgroundColor(resources.getColor(R.color.webViewText))
                    chooseColorWebviewTextEditText.error = getString(R.string.incorrect_color)
                }

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }


            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }


        })

        chooseColorWebviewTextButton.setOnClickListener { v ->
            ColorPickerPopup.Builder(context).initialColor(MyApp.preferencesManager.textColor
                    ?: resources.getColor(R.color.webViewText)) // Set initial color
                    .enableBrightness(true) // Enable brightness slider or not
                    .enableAlpha(false) // Enable alpha slider or not
                    .okTitle("Choose").cancelTitle("Cancel").showIndicator(true).showValue(true).build().show(v, object : ColorPickerPopup.ColorPickerObserver() {
                        override fun onColorPicked(color: Int) {
                            v.setBackgroundColor(color)
                            chooseColorWebviewTextEditText.setText(String.format("%06X", 0xFFFFFF and color), TextView.BufferType.EDITABLE)
                        }
                    })

        }


        val chooseColorWebviewBackgroundButton: Button = view.findViewById(R.id.btn_chooseColor_webviewBackground)
        val chooseColorWebviewBackgroundEditText: EditText = view.findViewById(R.id.eT_chooseColor_webviewBackground)
        chooseColorWebviewBackgroundButton.setBackgroundColor(MyApp.preferencesManager.backgroundColor?:resources.getColor(R.color.webViewBackground))
        chooseColorWebviewBackgroundEditText.setText(String.format("%06X", 0xFFFFFF and (MyApp.preferencesManager.backgroundColor?:resources.getColor(R.color.webViewBackground))))


        chooseColorWebviewBackgroundEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                try {
                    val editColor = Color.parseColor("#$s")
                    chooseColorWebviewBackgroundButton.setBackgroundColor(editColor)
                } catch (e: IllegalArgumentException) {
                    chooseColorWebviewBackgroundButton.setBackgroundColor(resources.getColor(R.color.webViewBackground))
                    chooseColorWebviewBackgroundEditText.error = getString(R.string.incorrect_color)
                }

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }


            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }


        })

        chooseColorWebviewBackgroundButton.setOnClickListener { v ->
            ColorPickerPopup.Builder(context).initialColor(MyApp.preferencesManager.backgroundColor?:resources.getColor(R.color.webViewBackground)) // Set initial color
                    .enableBrightness(true) // Enable brightness slider or not
                    .enableAlpha(false) // Enable alpha slider or not
                    .okTitle("Choose").cancelTitle("Cancel").showIndicator(true).showValue(true).build().show(v, object : ColorPickerPopup.ColorPickerObserver() {
                        override fun onColorPicked(color: Int) {
                            v.setBackgroundColor(color)
                            chooseColorWebviewBackgroundEditText.setText(String.format("%06X", 0xFFFFFF and color), TextView.BufferType.EDITABLE)
                        }
                    })

        }


        val dayNightSwitch: Switch = view.findViewById(R.id.switch_reading_day_night)
        dayNightSwitch.isChecked = MyApp.preferencesManager.isDarkTheme

        val fontSeekBar: SeekBar = view.findViewById(R.id.seekBar_reading_fontsize)
        fontSeekBar.progress = MyApp.preferencesManager.fontSize - 6


        val fontSeekBarTextView: TextView = view.findViewById(R.id.textView_reading_fontsize)
        fontSeekBarTextView.text = MyApp.preferencesManager.fontSize.toString()

        fontSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                fontSeekBarTextView.text = (progress + 6).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    val fontSize = fontSeekBarTextView.text.toString().toIntOrNull()
                    if (fontSize != null) MyApp.preferencesManager.fontSize = fontSize

                    MyApp.preferencesManager.font = Constants.CustomFonts.getFileByTitle(fontSpinner.selectedItem as String)

                    MyApp.preferencesManager.isDarkTheme = dayNightSwitch.isChecked

                    try {
                        val editColor = Color.parseColor("#${chooseColorWebviewTextEditText.text}")
                        MyApp.preferencesManager.textColor = editColor
                    } catch (e: IllegalArgumentException) {
                        MyApp.preferencesManager.textColor = null
                    }


                    try {
                        val editColor = Color.parseColor("#${chooseColorWebviewBackgroundEditText.text}")
                        MyApp.preferencesManager.backgroundColor = editColor
                    } catch (e: IllegalArgumentException) {
                        MyApp.preferencesManager.backgroundColor = null
                    }

                    dialog.cancel()
                    mListener.onDialogPositiveClick(this@ReadingSettingsDialogFragment)
                }.setNegativeButton(R.string.cancel, { dialog, id ->
                    dialog.cancel()
                })
        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            // Instantiate the DialogListener so we can send events to the host
            mListener = activity as DialogListener

        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(activity!!.toString() + " must implement DialogListener")
        }

    }

}