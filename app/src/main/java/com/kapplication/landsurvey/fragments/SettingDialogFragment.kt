package com.kapplication.landsurvey.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.RadioButton
import com.kapplication.landsurvey.R
import com.kapplication.landsurvey.model.Units


private const val TAG = "SettingDialogFragment"

class SettingDialogFragment : DialogFragment() {

    private var mCurrentUnit: Units = Units.ACRE

    init {
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity).apply {
            setView(initView())
        }.create()
    }

    private fun initView(): View {
        val view = activity.layoutInflater.inflate(R.layout.dialog_setting, null)
        view.findViewById<Button>(R.id.button_cancel).setOnClickListener {
            dismiss()
        }

        val sharedPre = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        val unit = sharedPre.getInt("UNIT", Units.ACRE.ordinal)
        when (unit) {
            Units.ACRE.ordinal -> view.findViewById<RadioButton>(R.id.radio_acre).isChecked = true
            Units.SQUARE_MILE.ordinal -> view.findViewById<RadioButton>(R.id.radio_sqmi).isChecked = true
            Units.SQUARE_FOOT.ordinal -> view.findViewById<RadioButton>(R.id.radio_sqft).isChecked = true
        }

        view.findViewById<Button>(R.id.button_save).setOnClickListener {
            val sharedPref = this@SettingDialogFragment.activity.getSharedPreferences("setting", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putInt("UNIT", mCurrentUnit.ordinal)
                apply()
            }
            dismiss()
        }

        view.findViewById<RadioButton>(R.id.radio_acre).setOnClickListener {
            mCurrentUnit = Units.ACRE
        }
        view.findViewById<RadioButton>(R.id.radio_sqmi).setOnClickListener {
            mCurrentUnit = Units.SQUARE_MILE
        }
        view.findViewById<RadioButton>(R.id.radio_sqft).setOnClickListener {
            mCurrentUnit = Units.SQUARE_FOOT
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        val params = dialog.window!!.attributes
        params.width = 480
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window!!.attributes = params as WindowManager.LayoutParams
    }
}