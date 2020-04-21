package com.wahyuakbarwibowo.socialmediaapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    //deklarasi variable untuk Firebase dkk
    private var mAuth: FirebaseAuth? = null
    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        //get instance
        mAuth = FirebaseAuth.getInstance()

        ivImageAkun.setOnClickListener {
            checkPermission()
        }
    }

    val READIMAGE: Int = 253
    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    READIMAGE
                )
                return
            }
        }
        loadImage()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READIMAGE -> {
                if (grantResults[0] ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    loadImage()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Gambar tidak dapat diakses",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    val PICK_IMAGE_CODE = 123

    //load gambar
    fun loadImage() {
        var intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_CODE && data != null &&
            resultCode == RESULT_OK
        ) {
            // Set Foto Profil
            val selectedImage = data.data
            val filePathColum = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = selectedImage?.let {
                contentResolver.query(
                    it,
                    filePathColum, null, null, null
                )
            }
            if (cursor != null) {
                cursor.moveToFirst()
            }
            val columIndex = cursor?.getColumnIndex(filePathColum[0])
            val picturePath = columIndex?.let { cursor?.getString(it) }
            cursor?.close()
            ivImageAkun.setImageBitmap(BitmapFactory.decodeFile(picturePath))

        }
    }

    //simpan gambar ke firebase
    fun saveImageInFirebase() {
        //memberi nama gambar yang akan kita save pada firebase
        var currentUser = mAuth!!.currentUser
        val email: String = currentUser!!.email.toString()
        val storage = FirebaseStorage.getInstance()
        // link dari firebase storage
        val storageRef = storage.getReferenceFromUrl("gs://social-media-app-db92e.appspot.com")
        val df = SimpleDateFormat("ddMMyyHHmmss")
        val dataObj = Date()
        val imagePath = SplitString(email) + "." + df.format(dataObj) + "/jpg"
        val ImageRef = storageRef.child("gambar/" + imagePath)
        ivImageAkun.isDrawingCacheEnabled = true
        ivImageAkun.buildDrawingCache()
        //merubah format dari gambar yang akan kita save
        val ivDrawable = ivImageAkun.drawable as BitmapDrawable
        val bitmap = ivDrawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = ImageRef.putBytes(data)
        var addss =
            FirebaseStorage.getInstance().equals("downloadTokens")
        var DownloadURLz =
            "https:firebasestorage.googleapis.com/v0/b/" +
                    "social-media-app-db92e.appspot.com/o/gambra%2F" +
                    SplitString(currentUser.email.toString()) + "." + df
                .format(dataObj) + ".jpg" + "?alt=media&token=" +
                    addss.toString()
        myRef.child("Users").child(currentUser.uid)
            .child("email").setValue(currentUser.email)
        myRef.child("Users").child(currentUser.uid)
//            .child("ProfileImage").setSampler.Value(DownloadURLz)
        LoadPost()
    }

    //untuk me rename edit text
    fun SplitString(email: String): String {
        val split = email.split("@")
        return split[0]
    }

    override fun onStart() {
        super.onStart()
        LoadPost()
    }

    fun LoadPost() {
        var currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            // intent ke mainActivity. Pada mainActivity kita akan
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("email", currentUser.email)
            intent.putExtra("uid", currentUser.uid)
            startActivity(intent)
            finish()
        }
    }

    //button login
    fun btnDaftar(view: View) {
        //jika email tidak di isi
        if (etEmailRegister.text.isEmpty()) {
            Toast.makeText(applicationContext, "Email tidak boleh kosong", Toast.LENGTH_LONG).show()
        } else if (etPasswordRegister.text.isEmpty()) {
            Toast.makeText(applicationContext, "Password tidak boelh kosong", Toast.LENGTH_LONG)
                .show()
            // Jika sudah benar
        } else {
            LoginToFireBase(etEmailRegister.text.toString(), etPasswordRegister.text.toString())
        }
    }

    fun LoginToFireBase(email: String, password: String) {
        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Sukses Login", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, "Gagal Login", Toast.LENGTH_LONG).show()
                }
            }
    }
}

