package com.ronil.imagepicker.activities

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ronil.imagepicker.R
import com.ronil.imagepicker.databinding.ImagPickerDialogueBinding
import com.ronil.imagepicker.databinding.LinitedImagesLayoutBinding
import com.ronil.imagepicker.utils.CAMERA
import com.ronil.imagepicker.utils.FILE_PATH
import com.ronil.imagepicker.utils.GALLERY
import com.ronil.imagepicker.utils.LimitAccessImageAdapter
import com.ronil.imagepicker.utils.RESULT_IMAGE_FILE
import com.ronil.imagepicker.utils.RESULT_IMAGE_PATH
import com.ronil.imagepicker.utils.SELECTION_TYPE
import com.ronil.imagepicker.utils.WANT_COMPRESSION
import com.ronil.imagepicker.utils.WANT_CROP
import com.ronil.imagepicker.utils.compreser.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import java.util.Objects


internal class ImagePickerMainActivity : AppCompatActivity() {
    private var from = ""
    private var wantCrop: Boolean = false
    private var wantCompress: Boolean = false
    private var cameraImageUri: Uri? = null
    private lateinit var dialog: AlertDialog
    private lateinit var binding: ImagPickerDialogueBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker_main)
        supportActionBar?.hide()
//        clearCache()
        binding = ImagPickerDialogueBinding.inflate(layoutInflater)
        dialog = AlertDialog.Builder(this).setView(binding.root).create()
        title = ""
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        wantCrop = intent.getBooleanExtra(WANT_CROP, false)
        wantCompress = intent.getBooleanExtra(WANT_COMPRESSION, false)
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
        dialog.setOnCancelListener {
            finish()
        }

        dialog.also {
            it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.window?.setWindowAnimations(R.style.DialogAnimation)
            it.setCanceledOnTouchOutside(true)
        }.show()
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
                    manageProcessing(path, file)
                }
            }
        } else {
            dialog.dismiss()
            finish()
        }
    }

    private fun manageProcessing(path: String, file: File) {
        if (wantCrop) {
            cropImage(path)
        } else {
            val resultIntent = Intent()
            if (wantCompress) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val compressedFile =
                            compress(
                                file
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
                }
            } else {
                setResultAndFinish(path, file, resultIntent)
            }
        }

    }

    private fun checkGalleryPermission() {
        from = GALLERY
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            if (
                ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES) == PERMISSION_GRANTED
            ) {
                // Full access on Android 13 (API level 33) or higher
                openGallery()
            } else if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(
                    this,
                    READ_MEDIA_VISUAL_USER_SELECTED
                ) == PERMISSION_GRANTED
            ) {
                openLimitedAccessImages()
            } else if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(
                    this,
                    READ_MEDIA_VISUAL_USER_SELECTED
                ) != PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(
                    arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VISUAL_USER_SELECTED)
                )
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(READ_MEDIA_IMAGES)
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    READ_EXTERNAL_STORAGE
                ) == PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        READ_EXTERNAL_STORAGE
                    )
                )
            }
        }

    }

    private fun checkCameraPermission() {
        from = CAMERA
        if (checkCallingOrSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        } else {
            openCamera()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun openCamera() {
        startCameraForResult.launch(Intent(this, CameraActivity::class.java))
    }

    private fun openCamera2() {
        val file = createImageFile()
        this.cameraImageUri =
            FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", file)

        cameraLauncher.launch(this.cameraImageUri)

    }


    private fun cropImage(filePath: String) {
        if (wantCompress) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val compressedFile =
                        compress(
                            File(filePath)
                        )
                    if (compressedFile.exists()) {
                        startForResult.launch(
                            Intent(
                                this@ImagePickerMainActivity,
                                CroppingActivity::class.java
                            ).also {
                                it.putExtra(FILE_PATH, compressedFile.absolutePath)
                            })
                    }
                } catch (_: Exception) {
                    startForResult.launch(
                        Intent(
                            this@ImagePickerMainActivity,
                            CroppingActivity::class.java
                        ).also {
                            it.putExtra(FILE_PATH, filePath)
                        })
                }
            }
        } else {
            startForResult.launch(Intent(this, CroppingActivity::class.java).also {
                it.putExtra(FILE_PATH, filePath)
            })
        }

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
            setResultAndFinish(imagePath, imageFile, resultIntent)
        } else {
            dialog.dismiss()
            finish()
        }
    }


    private val cameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                // Access denied
                val permissionMessage =
                    "Camera Permission is not Provided yet Please Provide the Permission so that we can Proceed further"

                android.app.AlertDialog.Builder(this).setTitle("Alert!!")
                    .setMessage(permissionMessage)
                    .setPositiveButton("Okay") { _, _ -> finish() }.show()
            }
        }
    private val requestPermissionLauncher =
        registerForActivityResult(
            RequestMultiplePermissions()
        ) { results ->
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES) == PERMISSION_GRANTED

            ) {
                // Full access on Android 13 (API level 33) or higher
                openGallery()

            } else if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(
                    this,
                    READ_MEDIA_VISUAL_USER_SELECTED
                ) == PERMISSION_GRANTED
            ) {

                openLimitedAccessImages()
                // Partial access on Android 14 (API level 34) or higher
            } else if (ContextCompat.checkSelfPermission(
                    this,
                    READ_EXTERNAL_STORAGE
                ) == PERMISSION_GRANTED
            ) {
                // Full access up to Android 12 (API level 32)
                openGallery()
            } else {
                // Access denied
                val permissionMessage =
                    "Storage Permission is not Provided yet Please Provide the Permission so that we can Proceed further"
                android.app.AlertDialog.Builder(this).setTitle("Alert!!")
                    .setMessage(permissionMessage)
                    .setPositiveButton("Okay") { _, _ -> finish() }.show()
            }

        }

    private fun openLimitedAccessImages() {
        CoroutineScope(Dispatchers.IO).launch {
            val images = getImages(contentResolver)
            withContext(Dispatchers.Main) {

                showSheet(images.map { it.uri }, {
                    val file = getFileFromUri(it)
                    if (file?.exists() == true) {
                        manageProcessing(file.path, file)
                    }
                }) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        requestPermissionLauncher.launch(
                            arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VISUAL_USER_SELECTED)
                        )
                    }
                }
            }
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
                        if (wantCompress) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val compressedFile =
                                        compress(
                                            file
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
                            }
                        } else {
                            setResultAndFinish(path, file, resultIntent)
                        }
//                        val resultIntent = Intent()
//                        setResultAndFinish(cameraImageUri?.path, file, resultIntent)
                    }
                }

            } else {
                dialog.dismiss()
                finish()
            }
        }

    private fun setResultAndFinish(path: String?, file: File?, resultIntent: Intent) {
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


    private suspend fun compress(bitmap: File): File {

        val job = CoroutineScope(Dispatchers.IO).async {
            Compressor.compress(this@ImagePickerMainActivity, bitmap)
        }
        return job.await().also {
            Log.d("compress", "yes the compressed worked ")
        }

//        val file1 = File.createTempFile(Date().time.toString(), ".jpg")
//        val fileOutputStream = FileOutputStream(file1)
//        bitmap?.compress(Bitmap.CompressFormat.JPEG, percentage, fileOutputStream)
//        fileOutputStream.flush()
//        fileOutputStream.close()
//        return bitmap
    }


    private val startCameraForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imagePath = result.data?.getStringExtra(RESULT_IMAGE_PATH)
            val imageFile: File? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getSerializableExtra(
                        RESULT_IMAGE_FILE, File::class.java
                    )
                } else {
                    result.data?.getSerializableExtra(RESULT_IMAGE_FILE) as File
                }
            if (imageFile?.exists() == true) {
                if (wantCrop) {
                    cropImage(imagePath ?: "")
                } else {
                    val resultIntent = Intent()
                    if (wantCompress) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val compressedFile =
                                    compress(
                                        imageFile
                                    )
                                if (compressedFile.exists()) {
                                    setResultAndFinish(
                                        Uri.fromFile(compressedFile).path,
                                        compressedFile,
                                        resultIntent
                                    )
                                } else {
                                    setResultAndFinish(imagePath, imageFile, resultIntent)

                                }
                            } catch (e: Exception) {
                                setResultAndFinish(imagePath, imageFile, resultIntent)
                            }
                        }
                    } else {
                        setResultAndFinish(imagePath, imageFile, resultIntent)
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.some_error_occur_try_again),
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
                finish()
            }
        } else {
            dialog.dismiss()
            finish()
        }
    }

    private fun clearCache() {
        try {
            val dir = cacheDir
            deleteDir(dir)
            val externalDir = externalCacheDir
            deleteDir(externalDir)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in Objects.requireNonNull(children).indices) {
                children?.get(i)?.let {
                    val success = deleteDir(File(dir, it))
                    if (!success) {
                        return false
                    }
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }


    private fun showSheet(imageUris: List<Uri>, onclick: (Uri) -> Unit, manageAccess: () -> Unit) {
        val sheetBinding: LinitedImagesLayoutBinding =
            LinitedImagesLayoutBinding.inflate(this.layoutInflater)
        val dialogue = BottomSheetDialog(this)
        dialogue.setOnCancelListener {
            finish()
        }
        val adapter = LimitAccessImageAdapter(imageUris) {
            dialogue.dismiss()
            onclick(it)
        }
        sheetBinding.recyclerView.adapter = adapter
        sheetBinding.btnManagePhotos.setOnClickListener {
            dialogue.dismiss()
            manageAccess()
        }
        dialogue.setContentView(sheetBinding.root)
        dialogue.show()
    }

    data class Media(
        val uri: Uri,
        val name: String,
        val size: Long,
        val mimeType: String,
    )

    // Run the querying logic in a coroutine outside of the main thread to keep the app responsive.
// Keep in mind that this code snippet is querying only images of the shared storage.
    private suspend fun getImages(contentResolver: ContentResolver): List<Media> =
        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.MIME_TYPE,
            )

            val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Query all the device storage volumes instead of the primary only
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val images = mutableListOf<Media>()

            contentResolver.query(
                collectionUri,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val uri = ContentUris.withAppendedId(collectionUri, cursor.getLong(idColumn))
                    val name = cursor.getString(displayNameColumn)
                    val size = cursor.getLong(sizeColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)

                    val image = Media(uri, name, size, mimeType)
                    images.add(image)
                }
            }

            return@withContext images
        }


    private fun getFileFromUri(uri: Uri): File? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val filePath = it.getString(columnIndex)
                return File(filePath)
            }
        }
        return null
    }
}
