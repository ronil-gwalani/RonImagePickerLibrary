package com.ronil.ronimagepicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.ronil.ronimagepicker.databinding.ImagPickerDialogueBinding
import java.io.File
import java.io.FileOutputStream
import java.util.*


class ImagePickerMainActivity : AppCompatActivity() {
    private var from = ""
    private var compressPercentage = 100
    private var wantCrop: Boolean = false
    private var wantCompress: Boolean = false
    private var cameraImageUri: Uri? = null
    private lateinit var dialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker_main)
        supportActionBar?.hide()
        title = ""
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        wantCrop = intent.getBooleanExtra(WANT_CROP, false)
        wantCompress = intent.getBooleanExtra(WANT_COMPRESSION, false)
        compressPercentage = intent.getIntExtra(COMPRESSION_PERCENTAGE, 100)
        when (intent.getStringExtra(SELECTION_TYPE) ?: "") {
            CAMERA -> {
                checkCameraPermission()
            }
            GALLERY -> {
                checkGalleryPermission()
            }
            else -> {
                openSelectionDialogue()
            }
        }

    }

    private fun openSelectionDialogue() {
        val binding = ImagPickerDialogueBinding.inflate(layoutInflater)
        dialog = AlertDialog.Builder(this).setView(binding.root)
            .setOnCancelListener {
                finish()
            }.create().also {
                it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.window?.setWindowAnimations(R.style.DialogAnimation)
                it.setCanceledOnTouchOutside(true)
            }
        dialog.show()
        binding.camera.setOnClickListener {
            checkCameraPermission()
            dialog.dismiss()
        }
        binding.gallery.setOnClickListener {
            checkGalleryPermission()
            dialog.dismiss()
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.data
            if (data != null) {
                val uri1: Uri = data
                val filepath = arrayOf(MediaStore.Images.Media.DATA)
                val cursor: Cursor =
                    contentResolver?.query(uri1, filepath, null, null, null)!!
                cursor.moveToFirst()
                val columIndex = cursor.getColumnIndex(filepath[0])
                val path = cursor.getString(columIndex)

                val file = File(path)
                if (file.exists()) {
                    if (wantCrop) {
                        cropImage(path)
                    } else {
                        val resultIntent = Intent()
                        if (wantCompress && compressPercentage > 50 && compressPercentage < 100) {
                            try {
                                val compressedFile =
                                    compress(
                                        compressPercentage,
                                        BitmapFactory.decodeFile(path)
                                    )
                                if (compressedFile.exists()) {
                                    setResultAndFinish(
                                        Uri.fromFile(compressedFile).path,
                                        compressedFile,
                                        resultIntent
                                    )
                                } else {
                                    setResultAndFinish(path, file, resultIntent)

                                }
                            } catch (e: Exception) {
                                setResultAndFinish(path, file, resultIntent)
                            }
                        } else {
                            setResultAndFinish(path, file, resultIntent)
                        }
                    }
                }
            }
        } else {
            dialog.dismiss()
            finish()
        }
    }

    private fun checkGalleryPermission() {
        from = GALLERY
        val readImagePermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
        if (checkCallingOrSelfPermission(readImagePermission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                readImagePermission
            )

        } else {
            openGallery()
        }
    }

    private fun checkCameraPermission() {
        from = CAMERA
        if (checkCallingOrSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        } else {
            openCamera()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun openCamera() {
        val file = createImageFile()
        this.cameraImageUri =
            FileProvider.getUriForFile(this, "com.ronil.ronimagepicker.fileprovider", file)
        cameraLauncher.launch(this.cameraImageUri)

    }


    private fun cropImage(filePath: String) {
        startForResult.launch(Intent(this, CroppingActivity::class.java).also {
            it.putExtra(FILE_PATH, filePath)
        })
//
    }

    private val startForResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imagePath = result.data?.getStringExtra(RESULT_IMAGE_PATH)
            val imageFile: File? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getSerializableExtra(
                    RESULT_IMAGE_FILE, File::class.java
                )
            } else {
                result.data?.getSerializableExtra(RESULT_IMAGE_FILE) as File
            }
            val resultIntent = Intent()
            resultIntent.putExtra(RESULT_IMAGE_PATH, imagePath)
            resultIntent.putExtra(RESULT_IMAGE_FILE, imageFile)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            dialog.dismiss()
            finish()
        }
    }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->

            if (isGranted) {
                if (from == GALLERY) {
                    openGallery()
                } else if (from == CAMERA) {
                    openCamera()
                }
            } else {
                val permissionMessage = if (from == GALLERY) {
                    "Storage Permission is not Provided yet Please Provide the Permission so that we can Proceed further"
                } else {
                    "Camera Permission is not Provided yet Please Provide the Permission so that we can Proceed further"
                }
                android.app.AlertDialog.Builder(this).setTitle("Alert!!")
                    .setMessage(permissionMessage)
                    .setPositiveButton("Okay") { _, _ -> finish() }.show()
            }
        }

    private var cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success == true) {
                val path = Environment.getExternalStoragePublicDirectory(
                    cameraImageUri?.path.toString().replace("/external_files", "")
                ).toString()
                val file = File(
                    path
                )
                if (file.exists()) {
                    if (wantCrop) {
                        cropImage(path)
                    } else {
                        val resultIntent = Intent()
                        setResultAndFinish(cameraImageUri?.path, file, resultIntent)
                    }
                }

            } else {
                dialog.dismiss()
                finish()
            }
        }

    private fun setResultAndFinish(path: String?, file: File, resultIntent: Intent) {
        resultIntent.putExtra(RESULT_IMAGE_PATH, path)
        resultIntent.putExtra(RESULT_IMAGE_FILE, file)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun createImageFile(): File {
        return File.createTempFile(
            Date().time.toString(),
            "jpg",
            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        )
    }


    private fun compress(percentage: Int, bitmap: Bitmap?): File {
        val file1 = File.createTempFile(Date().time.toString(), ".jpg")
        val fileOutputStream = FileOutputStream(file1)
        bitmap?.compress(Bitmap.CompressFormat.JPEG, percentage, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        return file1
    }


}