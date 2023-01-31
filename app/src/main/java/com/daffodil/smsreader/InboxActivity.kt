package com.daffodil.smsreader

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.security.Permission

class InboxActivity : AppCompatActivity(), OnItemClickListener {
    var smsMessagesList = ArrayList<String>()
    var smsListView: ListView? = null
    lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var smsMessages: Array<String>
    var address: String? = null
    var smsMessage = ""
    var smsMessageStr: String? = null
    val context: Context = this
    val PERMISSION_REQUEST = 100
    public override fun onStart() {
        super.onStart()
        inst = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)
        smsListView = findViewById<View>(R.id.SMSList) as ListView
        arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, smsMessagesList)
        smsListView!!.setAdapter(arrayAdapter)
        smsListView!!.setOnItemClickListener(this)
        checkPermissions()

    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_SMS)==PackageManager.PERMISSION_GRANTED){
            refreshSmsInbox()
        }else{
            requestPermissions(arrayOf( Manifest.permission.READ_SMS), PERMISSION_REQUEST)
        }
    }

    fun refreshSmsInbox() {
        val contentResolver = contentResolver
        val smsInboxCursor: Cursor =
            contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)!!
        val indexBody: Int = smsInboxCursor.getColumnIndex("body")
        val indexAddress: Int = smsInboxCursor.getColumnIndex("address")
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return
        arrayAdapter.clear()
        do {
            val str = """
                SMS From: ${smsInboxCursor.getString(indexAddress).toString()}
                ${smsInboxCursor.getString(indexBody).toString()}
                
                """.trimIndent()
            arrayAdapter.add(str)
        } while (smsInboxCursor.moveToNext())
    }

    fun updateList(smsMessage: String?) {
        arrayAdapter.insert(smsMessage, 0)
        arrayAdapter.notifyDataSetChanged()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        try {
            smsMessage = ""
            smsMessages = smsMessagesList[pos].split("\n").toTypedArray()
            address = smsMessages[0]
            for (i in 1 until smsMessages.size) {
                smsMessage += smsMessages[i]
            }
            smsMessageStr = """
                $address
                
                """.trimIndent()
            smsMessageStr += smsMessage
            // Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
            dialoge()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dialoge() {
        // custom dialog
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog)
        dialog.setTitle("Message")
        // set the custom dialog components - text, image and button
        val text = dialog.findViewById(R.id.text) as TextView
        text.text = "" + smsMessageStr
        val dialogButton: Button = dialog.findViewById(R.id.dialogButtonOK) as Button
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                dialog.dismiss()
            }
        })
        dialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
            refreshSmsInbox()
        }
    }

    companion object {
        private var inst: InboxActivity? = null
        fun instance(): InboxActivity? {
            return inst
        }
    }
}