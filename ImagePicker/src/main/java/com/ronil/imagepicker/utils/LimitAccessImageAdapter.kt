package com.ronil.imagepicker.utils

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ronil.imagepicker.R
import com.ronil.imagepicker.databinding.ImageItemBinding

class LimitAccessImageAdapter(private val imageUris: List<Uri>,private val onclick:(Uri)->Unit) : RecyclerView.Adapter<LimitAccessImageAdapter.ImageViewHolder>() {

        inner class ImageViewHolder(val binding: ImageItemBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {

            return ImageViewHolder(ImageItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val uri = imageUris[position]
            holder.binding.imageView.setImageURI(uri)
        holder.binding.root.setOnClickListener {
            onclick(uri)
        }// Load the image into the ImageView
        }

        override fun getItemCount(): Int = imageUris.size
    }

