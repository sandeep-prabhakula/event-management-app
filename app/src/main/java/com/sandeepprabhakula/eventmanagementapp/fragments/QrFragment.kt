package com.sandeepprabhakula.eventmanagementapp.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.sandeepprabhakula.eventmanagementapp.databinding.FragmentQrBinding

class QrFragment : Fragment() {
    private var _binding:FragmentQrBinding? =  null
    private val binding get() = _binding!!
    private val mAuth = FirebaseAuth.getInstance()
    private val currentUser = mAuth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrBinding.inflate(layoutInflater,container,false)
        val uid:String = currentUser?.uid.toString()
        val writer = QRCodeWriter()
        Glide.with(requireContext()).load(currentUser?.photoUrl).circleCrop().into(binding.imageView2)
        binding.userNameInQR.text = currentUser?.displayName
        try{
            val bitMatrix = writer.encode(uid,BarcodeFormat.QR_CODE,512,512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565)
            for(x in 0 until width){
                for(y in 0 until height){
                    bmp.setPixel(x,y,if(bitMatrix[x,y]) Color.BLACK else Color.WHITE)
                }
            }
            binding.qrCode.setImageBitmap(bmp)
        }catch (e:WriterException){

        }
        return binding.root
    }
}