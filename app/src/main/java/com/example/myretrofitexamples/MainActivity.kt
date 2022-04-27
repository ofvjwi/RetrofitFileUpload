package com.example.myretrofitexamples

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.example.myretrofitexamples.networking.retrofit.RetrofitHttp
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG: String = "MainActivity"
        private const val MY_PERMISSION_REQUEST = 100
        private const val PICK_IMAGE_FROM_GALLERY_REQUEST = 1
    }

    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
    }

    private fun initViews() {

        permission()

        button = findViewById(R.id.button_upload)
        button.setOnClickListener {
            callGallery()
        }
    }

    private fun callGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select picture"),
            PICK_IMAGE_FROM_GALLERY_REQUEST
        )
    }

    private fun permission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSION_REQUEST
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_GALLERY_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val url = data.data

            Log.d(TAG, "onActivityResult: ${url?.path}")
            uploadImage(url!!)
            //  uploadFile(url!!)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
            return
        }
    }


    private fun uploadFile(url: Uri) {

        val descriptionPart: RequestBody =
            RequestBody.create(MultipartBody.FORM, "descriptionImage")

        // val original = FileUtils.getFile(this, url)
        val original = File(url.toFile().toURI())
//         val original = File(URI(url.path))

        val filePart: RequestBody =
            RequestBody.create(MediaType.parse(contentResolver.getType(url)!!), original)

        val file: MultipartBody.Part =
            MultipartBody.Part.createFormData("photo", original.name, filePart)


        val call: Call<ResponseBody> = RetrofitHttp.apiService.uploadPhoto(file)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("TAG", "onFailure: $call")
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("TAG", "onResponse: $call")
            }
        })
    }

    private fun uploadImage(url: Uri) {
        val file = File(url.path.toString())
        val requestBody: RequestBody = RequestBody.create(MediaType.parse("image/*"), file)
        val part: MultipartBody.Part =
            MultipartBody.Part.createFormData("newImage", file.name, requestBody)

        Log.d(TAG, "uploadImage: ${file.absolutePath}")

        imageExecutor(part)

//        val call = RetrofitHttp.apiService.uploadPhoto(part)
//        call.enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                Log.d(TAG, "onResponse: ${response.isSuccessful}")
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                Log.d(TAG, "onFailure: ${t.message}\n ${t.localizedMessage}\n ${t.stackTrace}\n ${t.cause}\n")
//            }
//        })
    }

    private fun imageExecutor(multipartBody: MultipartBody.Part) {
        val executor = Executors.newSingleThreadExecutor() //in background
        val handler = Handler(Looper.getMainLooper()) //in UI
        executor.execute {
            val call = RetrofitHttp.apiService.uploadPhoto(multipartBody)
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    Log.d(TAG, "onResponse: ${response.isSuccessful}")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d(
                        TAG,
                        "onFailure: ${t.message}\n ${t.localizedMessage}\n ${t.stackTrace}\n ${t.cause}\n"
                    )
                }
            })
            handler.post {
                // UI
            }
        }
    }
}










