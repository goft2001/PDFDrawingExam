package com.example.pdfdrawingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.pdfdrawingapp.R
import java.io.File
import java.util.*

class PDFAdapter(context: Context, var pdfList: ArrayList<File>) : ArrayAdapter<File?>(
    context, R.layout.adapter_pdf, pdfList as List<File?>
) {
    private var viewHolder: ViewHolder? = null
    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getViewTypeCount(): Int {
        return if (pdfList.size > 0) {
            pdfList.size
        } else {
            1
        }
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.adapter_pdf, parent, false)
            viewHolder = ViewHolder()
            viewHolder!!.fileNameTv = view.findViewById(R.id.tv_name)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }
        viewHolder!!.fileNameTv!!.text = pdfList[position].name
        return view!!
    }

    inner class ViewHolder {
        var fileNameTv: TextView? = null
    }
}