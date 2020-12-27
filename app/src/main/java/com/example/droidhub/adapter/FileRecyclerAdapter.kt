package com.example.droidhub.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.droidhub.R
import com.example.droidhub.model.File
import com.google.android.material.card.MaterialCardView

class FileRecyclerAdapter(private val fileList: List<File>) :
        RecyclerView.Adapter<FileRecyclerAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        return FileViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.files_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = fileList[position]
        holder.textFile.text = file.fileName
        holder.cardView.setOnClickListener {
            val extension = MimeTypeMap.getFileExtensionFromUrl(file.fileUri)
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                setDataAndType(Uri.parse(file.fileUri), getTypeFromExtension(extension))
            }
            if (intent.resolveActivity(holder.itemView.context.packageManager) != null) {
                holder.itemView.context.startActivity(intent)
            } else {
                Toast.makeText(holder.itemView.context, "No app(s) found to open \".$extension\" file.", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun getTypeFromExtension(extension: String?): String {
        return if (extension.toString().contains("doc") || extension.toString().contains("docx")) {
            // Word document
            "application/msword"
        } else if (extension.toString().contains("pdf")) {
            // PDF file
            "application/pdf"
        } else if (extension.toString().contains("ppt") || extension.toString().contains("pptx")) {
            // Powerpoint file
            "application/vnd.ms-powerpoint"
        } else if (extension.toString().contains("xls") || extension.toString().contains("xlsx")) {
            // Excel file
            "application/vnd.ms-excel"
        } else if (extension.toString().contains("zip") || extension.toString().contains("rar")) {
            // WAV audio file
            "application/x-wav"
        } else if (extension.toString().contains("rtf")) {
            // RTF file
            "application/rtf"
        } else if (extension.toString().contains("wav") || extension.toString().contains("mp3")) {
            // WAV audio file
            "audio/*"
        } else if (extension.toString().contains("jpg") || extension.toString().contains("jpeg") || extension.toString().contains("png") || extension.toString().contains("gif")) {
            // JPG file
            "image/*"
        } else if (extension.toString().contains("txt")) {
            // Text file
            "text/plain"
        } else if (extension.toString().contains("3gp") || extension.toString().contains("mpg") || extension.toString().contains("mpeg") || extension.toString().contains("mpe") || extension.toString().contains("mp4") || extension.toString().contains("avi")) {
            // Video files
            "video/*"
        } else {
            // Other files
            "*/*"
        }
    }

    override fun getItemCount(): Int = fileList.size

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textFile: TextView = itemView.findViewById(R.id.text_file)
        val cardView: MaterialCardView = itemView.findViewById(R.id.card_view)

    }
}