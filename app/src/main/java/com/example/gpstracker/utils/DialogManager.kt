package com.example.gpstracker.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Toast
import com.example.gpstracker.R
import com.example.gpstracker.databinding.SaveDialogBinding
import com.example.gpstracker.db.TrackItem

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

    fun showSaveDialog(context: Context, item: TrackItem, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        // Кастомная разметка для диалога
        val binding = SaveDialogBinding.inflate(LayoutInflater.from(context), null, false)
        builder.setView(binding.root)
        val dialog = builder.create()

        binding.apply {
            tvTimeDialog.text = item.time
            tvSpeedDialog.text = item.velocity
            tvDistanceDialog.text = item.distance
            bSave.setOnClickListener {
                listener.onClick()
                dialog.dismiss()
            }
            bCancel.setOnClickListener {
                dialog.dismiss()
            }
        }
        // Прозрачный фон диалогового окна (иначе не видны скругления карточки диалога)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    interface Listener {
        fun onClick()
    }
}