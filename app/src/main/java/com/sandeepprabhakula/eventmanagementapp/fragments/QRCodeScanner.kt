package com.sandeepprabhakula.eventmanagementapp.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.util.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.sandeepprabhakula.eventmanagementapp.R
import com.sandeepprabhakula.eventmanagementapp.daos.EventsDao
import com.sandeepprabhakula.eventmanagementapp.daos.UserDao
import com.sandeepprabhakula.eventmanagementapp.data.User
import com.sandeepprabhakula.eventmanagementapp.databinding.FragmentQRCodeScannerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class QRCodeScanner : Fragment() {
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector
    private val args by navArgs<QRCodeScannerArgs>()
    private var _binding: FragmentQRCodeScannerBinding? = null
    private val binding get() = _binding!!
    private val userDao = UserDao()
    private val eventDao = EventsDao()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQRCodeScannerBinding.inflate(layoutInflater, container, false)
        requestPermissions()
        setUpControls()
        binding.scannerEventName.text = args.eventName
        binding.registerEvent.setOnClickListener {
            if (!TextUtils.isEmpty(binding.UserEmailFromQR.text) &&
                !TextUtils.isEmpty(binding.UserNameFromQR.text)
            ){
                eventDao.addParticipantToEvent(binding.UserEmailFromQR.text.toString(),args.eventName)
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setView(R.layout.registration_success)
                alertDialog.setPositiveButton("OK"){_,_->

                }
                alertDialog.show()

            }else{
                Toast.makeText(context,"No User Scanned",Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    private fun setUpControls() {
        detector = BarcodeDetector.Builder(context).build()
        cameraSource = CameraSource.Builder(context, detector)
            .setAutoFocusEnabled(true)
            .build()
        binding.cameraSurfaceView.holder.addCallback(surfaceCallBack)
        detector.setProcessor(processor)
    }

    private val processor = object : Detector.Processor<Barcode> {
        override fun release() {

        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>) {
            if (detections != null && detections.detectedItems.isNotEmpty()) {
                val qrCode: SparseArray<Barcode> = detections.detectedItems
                val code = qrCode.valueAt(0)
                val uid = code.displayValue
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d("uid", uid)
                    val user = userDao.getUserById(uid).await().toObject(User::class.java)
                    Log.d("user", user.toString())
                    withContext(Dispatchers.Main) {
                        binding.UserEmailFromQR.text = user?.userEmail
                        binding.UserNameFromQR.text = user?.userName
                        Glide.with(requireContext()).load(user?.userImageURL).circleCrop()
                            .into(binding.dpFromQR)
                    }
                }
            }
        }

    }
    private val surfaceCallBack = object : SurfaceHolder.Callback {
        override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
            try {
                cameraSource.start(surfaceHolder)
            } catch (e: Exception) {
                Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show()
            }
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            cameraSource.stop()
        }

    }

    private fun hasCameraPermission() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        val permissionToRequest = mutableListOf<String>()
        if (!hasCameraPermission()) {
            permissionToRequest.add(Manifest.permission.CAMERA)
        }
        if (permissionToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionToRequest.toTypedArray(),
                200
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (i == PackageManager.PERMISSION_GRANTED) {
                    Log.d("requestedPermission", "${permissions[i]} granted")
                }
            }
        }
    }
}