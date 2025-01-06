package com.example.gpstracker.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.example.gpstracker.R

object DialogManager {
    fun showLocEnableDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle(R.string.location_disabled)
        dialog.setMessage(context.getString(R.string.location_dialog_message))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { _, _ ->
            listener.onClick()
            Toast.makeText(context, "Yes", Toast.LENGTH_SHORT).show()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { _, _ ->
            dialog.dismiss()
            Toast.makeText(context, "No", Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }

    interface Listener {
        fun onClick()
    }
}