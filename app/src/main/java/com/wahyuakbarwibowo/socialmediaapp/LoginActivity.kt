package com.wahyuakbarwibowo.socialmediaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    //deklarasi variable untuk Firebase dkk
    private var mAuth: FirebaseAuth? = null
    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //get instance
        mAuth = FirebaseAuth.getInstance()
        // onclick untuk daftar
        tvDaftar.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    //event onClick Login
    fun btnLoginPage(view: View){
        LoginToFireBase(
            etEmail.text.toString(),
            etPassword.text.toString()
        )
    }

    override fun onStart() {
        super.onStart()
        LoadPost()
    }

    //LoadPost berdasarkan email dan uid(Unik ID)
    fun LoadPost(){
        var currentUser = mAuth!!.currentUser
        if (currentUser != null){
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("email", currentUser.email)
            intent.putExtra("uid", currentUser.uid)
            startActivity(intent)
        }
    }
    //login ke Firebase
    fun LoginToFireBase(email: String, password: String){
        // firebase login dengan email dan password
        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                //jika suskes
                if (task.isSuccessful){
                    var currentUser = mAuth!!.currentUser
                    Toast.makeText(applicationContext,
                    "Suskes Login",
                    Toast.LENGTH_LONG).show()
                    //save data ke firebase berdasarkan input pada editext
                    myRef.child("Users").child(currentUser!!.uid).child("email")
                        .setValue(currentUser.email)
                    LoadPost()
                }
                // jika gagal
                else {
                    Toast.makeText(applicationContext,
                    "Gagal Login",
                    Toast.LENGTH_LONG).show()
                }
            }
    }
}
