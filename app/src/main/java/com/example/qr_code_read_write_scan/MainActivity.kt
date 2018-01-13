package com.example.qr_code_read_write_scan

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.graphics.BitmapFactory
import android.view.View
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.Frame
import com.google.android.gms.common.api.CommonStatusCodes
import android.content.Intent
import android.util.Log
import com.example.qr_code_read_write_scan.barcode.BarcodeCaptureActivity
import com.google.zxing.WriterException
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix


class MainActivity : AppCompatActivity() {

    private val LOG_TAG = MainActivity::class.java.simpleName
    private val BARCODE_READER_REQUEST_CODE = 1

    val QRcodeWidth = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        btn_generate_qr.setOnClickListener({
            val textValue = et_qr_code_text.getText().toString()
            try {
                val bitmap = textToQRBitmap(textValue)
                iv_qr_code_image.setImageBitmap(bitmap)
            } catch (e: WriterException) {
                e.printStackTrace()
            }
        })


        btn_read_qr_code.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                // Load the Image
                val myBitmap = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.puppy)
                iv_qr_code_image.setImageBitmap(myBitmap)

                // Setup the Barcode Detector
                val detector = BarcodeDetector.Builder(applicationContext)
                        .setBarcodeFormats(Barcode.DATA_MATRIX or Barcode.QR_CODE)
                        .build()
                if (!detector.isOperational) {
                    tv_qr_code_text_content.setText("Could not set up the detector!")
                    return
                }

                // Detect the Barcode
                val frame = Frame.Builder().setBitmap(myBitmap).build()
                val barcodes = detector.detect(frame)

                // Decode the Barcode
                val thisCode = barcodes.valueAt(0)
                tv_qr_code_text_content.text = thisCode.rawValue
            }
        })





        btn_scan_qrcode.setOnClickListener(View.OnClickListener {
            val intent = Intent(applicationContext, BarcodeCaptureActivity::class.java)
            startActivityForResult(intent, BARCODE_READER_REQUEST_CODE)
        })

    }

    @Throws(WriterException::class)
    fun textToQRBitmap(Value: String): Bitmap? {
        val bitMatrix: BitMatrix
        try {
            bitMatrix = MultiFormatWriter().encode(Value, BarcodeFormat.QR_CODE, QRcodeWidth, QRcodeWidth, null)
        } catch (Illegalargumentexception: IllegalArgumentException) {
            return null
        }

        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height
        val pixels = IntArray(matrixWidth * matrixHeight)

        for (y in 0 until matrixHeight) {
            val offset = y * matrixWidth
            for (x in 0 until matrixWidth) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.RGB_565)
        bitmap.setPixels(pixels, 0, 500, 0, 0, matrixWidth, matrixHeight)
        return bitmap
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    val barcode : Barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject)
                    val p = barcode.cornerPoints
                    tv_qr_code_text_content.setText(barcode.displayValue)
                } else
                    tv_qr_code_text_content.setText(R.string.no_barcode_captured)
            } else
                Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format), CommonStatusCodes.getStatusCodeString(resultCode)))
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }
}
