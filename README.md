# RonImagePickerLibrary

>Step 1. Add the JitPack repository to your build file

```gradel
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  ```
  >Step 2. Add the dependency

 ```gradel
dependencies {
	        implementation 'com.github.ronil-gwalani:RonImagePicker:$VersonName' // here VersionName = 1.0.1
	}
  ```
  
  > Step 3. Implement ResultImage in your Activity/Fragment and Call RonImagePicker Exactly Like this
  ``` 
  class MainActivity : AppCompatActivity(), ResultImage  {
    private lateinit var imageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
            RonImagePicker(this, this).getImage(//Do not try to pass ResultImage callback in here it won't work just implement it the way it is in example
                crop = true,         // By default its true mark it false if you don't want that
                cameraOnly = true,   // if you only want image from camera pass cameraOnly=true
                galleyOnly = true,   // if you only want image from camera pass galleyOnly=true
                compress = true,     // by default compression is false if you need it pass compress = true
                compressionPercentage = 80,// only greater than 50 and less than 100 values are allowed for compression
                // & if you have chosen crop then no need to pass compression its automatically compressed
            )
      
        
        }
         
         
         
         
      override val startForResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imagePath = result.data?.getStringExtra(RESULT_IMAGE_PATH)
            val imageFile: File? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getSerializableExtra(
                    RESULT_IMAGE_FILE, File::class.java
                )
            } else {
                result.data?.getSerializableExtra(RESULT_IMAGE_FILE) as File
            }
            imageView.setImageURI(Uri.fromFile(imageFile))
//            here you got the image path and image file do whatever you want to do with it
//            Just check the null safety and file existence before using it
//            doSomeOperations()

        }
     }
        
 }
