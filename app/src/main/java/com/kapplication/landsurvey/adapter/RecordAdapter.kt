package com.kapplication.landsurvey.adapter

import android.app.Activity
import android.app.DialogFragment
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.kapplication.landsurvey.R
import com.kapplication.landsurvey.fragments.DetailDrawerFragment
import com.kapplication.landsurvey.fragments.SaveDialogFragment
import com.kapplication.landsurvey.fragments.SaveDialogFragment.SaveDialogListener
import com.kapplication.landsurvey.model.Record
import com.kapplication.landsurvey.utils.Utils

private const val TAG = "RecordAdapter"

class RecordAdapter(private val mRecordList: List<Record>) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>(), SaveDialogListener {
    class RecordViewHolder(val item: ConstraintLayout) : RecyclerView.ViewHolder(item)

    private var mCurrentRecord: Record? = null
    private var mContext: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_record, parent, false) as ConstraintLayout
        mContext = parent.context
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.item.findViewById<TextView>(R.id.textView_name).text = mRecordList[position].name
        holder.item.findViewById<TextView>(R.id.textView_perimeter).text = "Perimeter: ${String.format("%.2f m", mRecordList[position].perimeter) }"
        holder.item.findViewById<TextView>(R.id.textView_area).text = "Area: ${Utils.convertArea(mContext!!, mRecordList[position].area, 2)}"
        holder.item.findViewById<TextView>(R.id.textView_time).text = "Save Time: ${mRecordList[position].endTime}"
        holder.item.setBackgroundResource(if (position.rem(2) == 1) R.color.list_item_background else android.R.color.white)

        holder.itemView.setOnClickListener{
            mCurrentRecord = mRecordList[holder.adapterPosition]
            val detail = DetailDrawerFragment.newInstance(mRecordList[position])
            (holder.itemView.context as AppCompatActivity).supportFragmentManager.beginTransaction().replace(R.id.drawer_fragment, detail, "DetailDrawerFragment")
                    .addToBackStack(null).commit()
        }

        holder.itemView.setOnLongClickListener{
            mCurrentRecord = mRecordList[holder.adapterPosition]
            val dialog = SaveDialogFragment()
            dialog.setOnSaveDialogListener(this@RecordAdapter)
            dialog.show((it.context as Activity).fragmentManager, "RenameDialogFragment")
            true
        }

    }

    override fun getItemCount(): Int {
        return mRecordList.size
    }

    override fun onSaveClick(dialog: DialogFragment, fileName: String) {
        val recordFile = mCurrentRecord?.getRecordFile()
        if (recordFile?.delete()!!) {
            mCurrentRecord?.name = fileName
            if (mCurrentRecord?.writeToFile()!!) {
                notifyDataSetChanged()
                dialog.dismiss()
            } else {
                Toast.makeText(dialog.context, "The file name is invalidate.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(dialog.context, "Rename failed.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    override fun onCancelClick(dialog: DialogFragment) {
    }

}