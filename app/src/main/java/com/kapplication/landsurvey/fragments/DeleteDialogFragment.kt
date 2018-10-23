package com.kapplication.landsurvey.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.model.Marker
import com.kapplication.landsurvey.R

private const val TAG = "DeleteDialogFragment"

class DeleteDialogFragment : DialogFragment() {

    interface OnDeleteDialogListener {
        fun onDeleteClick(dialog: DialogFragment, marker: Marker?)
    }

    private var mOnDeleteDialogListener: OnDeleteDialogListener? = null
    private var mDeleteButton: ImageView? = null
    private var mHitTextView: TextView? = null

    private var mMarker: Marker? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity).apply {
            setView(initView())
        }.create()
    }

    private fun initView(): View {
        val view = activity.layoutInflater.inflate(R.layout.dialog_delete, null)
        mDeleteButton = view.findViewById(R.id.button_delete)
        mHitTextView = view.findViewById(R.id.textView_hint)

        mDeleteButton?.setOnClickListener {
            mOnDeleteDialogListener?.onDeleteClick(this, mMarker)
        }
        mHitTextView?.text = "Point(${mMarker?.position?.latitude}, ${mMarker?.position?.longitude}) will be deleted."

        mDeleteButton?.setOnClickListener {
            mOnDeleteDialogListener?.onDeleteClick(this, mMarker)
        }

        return view
    }

    fun setMarker(marker: Marker?) {
        mMarker = marker
    }

    fun setOnDeleteDialogListener(listener: OnDeleteDialogListener) {
        mOnDeleteDialogListener = listener
    }
}