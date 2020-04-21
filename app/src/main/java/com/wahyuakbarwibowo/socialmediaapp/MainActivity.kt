package com.wahyuakbarwibowo.socialmediaapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_post.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_list.view.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference
    private var mAuth: FirebaseAuth? = null
    private var firebaseStorage: FirebaseStorage? = null

    //deklrasi variable List Post
    var ListPost = ArrayList<DataPostingan>()

    //variabele inner class adapter
    var adapter: MyPostAdapter? = null

    //deklarasi info user
    var myemail: String? = null
    var UserUID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        var b: Bundle? = intent.extras
        //myemail = email pada firebase database
        if (b != null) {
            myemail = b.getString("email")
        }
        //id user
        UserUID = b?.getString("uid")
        //tambahkan postingan baru berdasarkan class DAta Postingan
        ListPost.add(DataPostingan("0", "him", "url", "add"))
        //set Adapter
        adapter = MyPostAdapter(this, ListPost)
        lvTweets.adapter = adapter
        //load post yang sudah ada
        LoadPost()
    }

    //inner class adapter
    inner class MyPostAdapter : BaseAdapter {
        var listNotesAdapter = ArrayList<DataPostingan>()
        var context: Context

        constructor(
            context: Context,
            listNotesAdapter: ArrayList<DataPostingan>
        ) : super() {
            this.listNotesAdapter = listNotesAdapter
            this.context = context
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var mypost = listNotesAdapter[position]
            if (mypost.postPersonUID.equals("add")) {
                //code untuk tambahkan postingan
                var myView = layoutInflater.inflate(
                    R.layout.activity_add_post,
                    null
                )
                //button pilih gambar
                myView.iv_gambar.setOnClickListener {
                    loadImage()
                }
                //button upload gambar
                myView.iv_post.setOnClickListener {
                    //upload server
                    myRef.child("posts").push().setValue(
                        InfoPostingan(
                            UserUID!!,
                            myView.etPost.text.toString(),
                            DownloadURL!!
                        )
                    )
                    myView.etPost.setText("")
                }
                return myView
                // tampilkan loading ketika upload gambar
            } else if (mypost.postPersonUID.equals("loading")) {
                val myView = layoutInflater.inflate(R.layout.loading_ticket, null)
                return myView
            }
            // tampilkan Welcom to bla bla
            else if (mypost.postPersonUID.equals("ads")) {
                val myView = layoutInflater.inflate(R.layout.layout_welcome, null)
                return myView
            }
            // tampilkan postingan
            else {
                // layout post (item list)
                val myView = layoutInflater.inflate(R.layout.item_list, null)
                // isi dari postingan
                myView.txt_detail_postingan.text = mypost.postText
                // tampilkan gambar
                Glide.with(context).load(mypost.postImageURL)
                    //place holder untuk tampilkan ketika gambar masih loading
                    .placeholder(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(myView.gambar_postingan)
                //tampilkan username dan poto user
                myRef.child("Users").child(mypost.postPersonUID!!)
                    .addValueEventListener(object :
                        ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            try {
                                //poto user
                                var td = dataSnapshot.value as HashMap<String, Any>
                                for (key in td.keys) {
                                    var userInfo = td[key] as String
                                    if (key.equals("ProfileImage")) {
                                        Glide.with(context)
                                            .load(userInfo)
                                            .placeholder(R.mipmap.ic_launcher)
                                            .into(myView.poto_user)
                                    } else {
                                        myView.txtUsername.text =
                                            SplitString(userInfo)
                                    }
                                }
                            } catch (ex: Exception) {

                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                return myView
            }
        }

        // tangkap data dari item
        override fun getItem(position: Int): Any {
            return listNotesAdapter[position]
        }

        // tangkap id item
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        // get jumlah item
        override fun getCount(): Int {
            return listNotesAdapter.size
        }
    }

    // Load Image
    val PICK_IMAGE_CODE = 123
    fun loadImage() {
        //intent ke galeri atua apliaksi footo lainnya
        var intent = Intent(
            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == PICK_IMAGE_CODE && data != null && resultCode == RESULT_OK) {
            val selectedImage = data.data
            val filePathColum = arrayOf(MediaStore.Images.Media.DATA)
            val cursor =
                selectedImage?.let { contentResolver.query(it, filePathColum, null, null, null) }
            if (cursor != null) {
                cursor.moveToFirst()
            }
            val columIndex = cursor?.getColumnIndex(filePathColum[0])
            val picturePath = columIndex?.let { cursor.getString(it) }
            if (cursor != null) {
                cursor.close()
            }
            // upload gambar dengan format bitmap
            UploadImage(BitmapFactory.decodeFile(picturePath))
        }
    }

    // download url gambar
    var DownloadURL: String? = ""

    //upload gambar
    fun UploadImage(bitmap: Bitmap) {
        ListPost.add(0, DataPostingan("0", "him", "url", "loading"))
        adapter!!.notifyDataSetChanged()
        //upload ke firebase storage dengan format
        val storage = FirebaseStorage.getInstance()
        //link firebase storage
        val storageRef = storage.getReferenceFromUrl(
            "gs://social-media-app-db92e.appspot.com"
        )
        // save nama gambar berdasarkan waktu upload (hari, bulan, tahun, menit, jam dan detik)
        val formattanggal = SimpleDateFormat("ddMMyyHHmmss")
        val dataobject = Date()
        //save sebagai .jpg
        var imagePath = SplitString(myemail!!) + "." +
                formattanggal.format(dataobject) + ".jpg"
        //save di folder imagePost
        var imageRef = storageRef.child("imagePost/" + imagePath)
        //reformat gambar menjadi bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val dataz = baos.toByteArray()
        val uploadTask = imageRef.putBytes(dataz)
        //upload gambar
        uploadTask.addOnSuccessListener { taskSnapshot ->
            var tokens =
                FirebaseStorage.getInstance().equals("downloadTokens")
            DownloadURL = "https:firebasestorage.googleapis.com/v0/b/" +
                    "social-media-app-db92e.appspot.com/o/imagePost%2F" + SplitString(myemail!!) +
                    "." + formattanggal.format(dataobject) + ".jpg" + "?alt=media&token=" + tokens.toString()
            //tamplkan post
            ListPost.removeAt(0)
            adapter!!.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(applicationContext, "Fail to upload", Toast.LENGTH_LONG).show()
        }
    }

    // fun untuk menghapus "@gmail.com"
    fun SplitString(email: String): String {
        val split = email.split("@")
        return split[0]
    }

    // load postingan
    fun LoadPost() {
        myRef.child("posts")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        ListPost.clear()
                        ListPost.add(DataPostingan("0", "him", "url", "add"))
                        ListPost.add(DataPostingan("0", "him", "url", "add"))
                        var td = dataSnapshot!!.value as HashMap<String, Any>
                        for (key in td.keys) {
                            var post = td[key] as HashMap<String, Any>
                            ListPost.add(
                                DataPostingan(
                                    key,
                                    post["text"] as String,
                                    post["postImage"] as String,
                                    post["userUID"] as String
                                )
                            )
                        }
                        adapter!!.notifyDataSetChanged()
                    } catch (ex: Exception) {
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item != null) {
            when (item.itemId) {
                R.id.logout -> {
                    mAuth!!.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
