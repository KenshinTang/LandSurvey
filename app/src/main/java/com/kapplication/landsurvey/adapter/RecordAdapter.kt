package com.kapplication.landsurvey.adapter

import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.kapplication.landsurvey.R
import com.kapplication.landsurvey.fragments.DetailDrawerFragment
import com.kapplication.landsurvey.model.Record

private const val TAG = "RecordAdapter"

class RecordAdapter(private val mRecordList: ArrayList<Record>) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {
    class RecordViewHolder(val item: ConstraintLayout) : RecyclerView.ViewHolder(item)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_record, parent, false) as ConstraintLayout
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.item.findViewById<TextView>(R.id.textView_name).text = mRecordList[position].name
        holder.item.findViewById<TextView>(R.id.textView_perimeter).text = "Length: ${mRecordList[position].perimeter}m"
        holder.item.findViewById<TextView>(R.id.textView_area).text = "Area: ${mRecordList[position].area}㎡"
        holder.item.findViewById<TextView>(R.id.textView_time).text = "Save Time: ${mRecordList[position].endTime}"
        holder.item.setBackgroundResource(if (position.rem(2) == 1) R.color.list_item_background else android.R.color.white)

        holder.itemView.setOnClickListener{
            val detail = DetailDrawerFragment.newInstance(mRecordList[position])
            (holder.itemView.context as AppCompatActivity).supportFragmentManager.beginTransaction().replace(R.id.drawer_fragment, detail)
                    .addToBackStack(null).commit()
        }

    }

    override fun getItemCount(): Int {
        return mRecordList.size
    }
}