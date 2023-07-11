package com.ronil.ronimagepicker

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ronil.ronimagepicker.databinding.ActivityCoppingBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class CroppingActivity : AppCompatActivity() {
    var path: String = ""

    lateinit var binding: ActivityCoppingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoppingBinding.inflate(layoutInflater)
        val outValue = TypedValue()
        path = intent.getStringExtra(FILE_PATH) ?: ""
        setContentView(binding.root)
        supportActionBar?.hide()
//        binding.imageView1.setImageURI(Uri.parse(path))
        setRotation()
        setListeners()
    }

    private fun setListeners() {
        binding.yesConfirm.setOnClickListener {
            cropImageClick()
        }
        binding.noChange.setOnClickListener {
            finish()
        }
    }

    private fun setRotation() {
        val tempBitmap = BitmapFactory.decodeFile(path)
        var scale = 0.0f
        val exif = ExifInterface(path)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            // No rotation needed
        }

        val bitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.width, tempBitmap.height, matrix, true)
//        binding.imageView2.setImageBitmap(bitmap)

        binding.rotation.setOnClickListener {
            val matrix = Matrix()
            matrix.postRotate(90 + scale)
            scale += 90
            val scaledBitmap =
                Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
            val rotatedBitmap = Bitmap.createBitmap(
                scaledBitmap,
                0,
                0,
                scaledBitmap.width,
                scaledBitmap.height,
                matrix,
                true
            )
            binding.cropimageview.setImageBitmap(rotatedBitmap)
        }
        binding.cropimageview.setImageBitmap(bitmap)
//        if (croppedImage.croppedImage == null) {
//            binding.rotation.performClick()
//        }

    }

    private fun cropImageClick() {
        try {
            val bitmap: Bitmap? = binding.cropimageview.croppedImage
            val resultIntent = Intent()
            if (bitmap == null) {
                Toast.makeText(
                    this,
                    "This Image is not Able to cropped try changing the rotation",
                    Toast.LENGTH_SHORT
                ).show()
                resultIntent.putExtra(RESULT_IMAGE_PATH, path);
                resultIntent.putExtra(RESULT_IMAGE_FILE, File(path))
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            val file1 = File.createTempFile(Date().time.toString() + "", ".jpg")
            var fileOutputStream: FileOutputStream? = null
            fileOutputStream = FileOutputStream(file1)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            val croppedPath = file1.path
            resultIntent.putExtra(RESULT_IMAGE_PATH, croppedPath);
            resultIntent.putExtra(RESULT_IMAGE_FILE, file1)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()

        } catch (e: IOException) {
            Toast.makeText(this, e.message + "", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }


}


