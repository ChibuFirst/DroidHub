package com.example.droidhub.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.droidhub.R
import com.example.droidhub.model.File
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FileRecyclerAdapter(private val activity: FragmentActivity, private val fileList: ArrayList<File>, private val fileKeys: ArrayList<String>) :
        RecyclerView.Adapter<FileRecyclerAdapter.FileViewHolder>() {

    companion object {
        private const val STORAGE_REQUEST_CODE: Int = 1000
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        return FileViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.files_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = fileList[position]
        val key = fileKeys[position]
        holder.textFile.text = file.fileName
        holder.cardView.setOnClickListener {
            openSelectedFile(file)
        }
        holder.itemView.setOnCreateContextMenuListener { contextMenu, view, _ ->
            contextMenu.add(0, view.id, 0, "View").setOnMenuItemClickListener {
                openSelectedFile(file)
                true
            }
            contextMenu.add(0, view.id, 0, "Delete").setOnMenuItemClickListener {
                deleteSelectedFile(file.fileName, key)
                true
            }
            contextMenu.add(0, view.id, 0, "Download").setOnMenuItemClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        activity.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE)
                    } else {
                        downloadSelectedFile(file.fileName, file.fileUri)
                    }
                } else {
                    downloadSelectedFile(file.fileName, file.fileUri)
                }
                true
            }
            contextMenu.add(0, view.id, 0, "Share").setOnMenuItemClickListener {
                shareSelectedFile(file.fileName, file.fileUri)
                true
            }
        }
    }

    private fun shareSelectedFile(fileName: String, fileUri: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/*"
            putExtra(Intent.EXTRA_TEXT, "Here is the link to the $fileName: \n\n$fileUri")
        }
        activity.startActivity(shareIntent)
    }

    private fun downloadSelectedFile(name: String, fileUri: String) {
        val dialogBuilder = MaterialAlertDialogBuilder(activity).apply {
            setTitle("Download this file?")
            setMessage("File name: $name")
            setPositiveButton("Continue") { _, _ ->
                val request = DownloadManager.Request(Uri.parse(fileUri)).apply {
                    setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    setTitle("Download")
                    setDescription("The file is downloading...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${System.currentTimeMillis()}")
                }

                val manager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                manager.enqueue(request)
                Toast.makeText(activity, "Selected file downloading", Toast.LENGTH_SHORT).show()
            }
            setNegativeButton("Cancel") { p0, _ -> p0!!.dismiss() }
        }
        dialogBuilder.create().show()
    }

    private fun deleteSelectedFile(name: String, key: String) {
        val auth = FirebaseAuth.getInstance()
        val databaseReference = FirebaseDatabase.getInstance().getReference("files").child(auth.currentUser!!.uid)

        val dialogBuilder = MaterialAlertDialogBuilder(activity).apply {
            setTitle("Delete this file?")
            setMessage("File name: $name \nThis is permanent and can't be undone")
            setPositiveButton("Delete") { _, _ ->
                databaseReference.child(key).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(activity, "Selected file deleted.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(activity, "Unable to delete file", Toast.LENGTH_SHORT).show()
                            }
                        }
            }
            setNegativeButton("Cancel") { p0, _ -> p0!!.dismiss() }
        }
        dialogBuilder.create().show()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openSelectedFile(file: File) {
        val extension = MimeTypeMap.getFileExtensionFromUrl(file.fileUri)
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(Uri.parse(file.fileUri), getTypeFromExtension(extension))
        }
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        } else {
            Toast.makeText(activity, "No app(s) found to open \".$extension\" file.", Toast.LENGTH_SHORT).show()
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