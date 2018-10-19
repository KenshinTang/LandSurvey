package com.kapplication.landsurvey.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.kapplication.landsurvey.R


class SaveDialogFragment : DialogFragment() {
    private val TAG = "SaveDialogFragment"

    interface SaveDialogListener {
        fun onSaveClick(dialog: DialogFragment, fileName: String)
        fun onCancelClick(dialog: DialogFragment)
    }

    private var mSaveDialogListener: SaveDialogListener? = null
    private var mFileNameEditText: EditText? = null
    private var mSaveButton: Button? = null
    private var mCancelButton: Button? = null

    init {
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var view = initView()
        val builder = AlertDialog.Builder(activity)
        with(builder) {
            setCancelable(false)
            setView(view)
        }
        return builder.create()
    }

    private fun initView(): View {
        val view = activity.layoutInflater.inflate(R.layout.dialog_save, null)
        mFileNameEditText = view.findViewById(R.id.editText_fileName)
        mSaveButton = view.findViewById(R.id.button_save)
        mCancelButton = view.findViewById(R.id.button_cancel)

        mSaveButton?.setOnClickListener {
            val fileName = mFileNameEditText?.text
            if (fileName.isNullOrEmpty()) {
                Toast.makeText(activity, "You must enter a file name.", Toast.LENGTH_SHORT).show()
            } else {
                mSaveDialogListener?.onSaveClick(this, fileName.toString())
                dismiss()
            }
        }
        mCancelButton?.setOnClickListener {
            mSaveDialogListener?.onCancelClick(this)
            dismiss()
        }
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mSaveDialogListener = context as SaveDialogListener
    }
}