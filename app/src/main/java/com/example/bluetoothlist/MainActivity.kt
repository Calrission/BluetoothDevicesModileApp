package com.example.bluetoothlist

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_rec.view.*


class MainActivity : AppCompatActivity() {

    val ID_BLUETOOTH = 101
    val ID_ENABLE_BLUETOOTH = 102

    val INTERVAL_REFRESH_DATA_SCANNER_MS: Long =  60 * 1000 // минута

    lateinit var bluetoothScanner: BluetoothScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initScan()

        startInfinityRefreshScanner()

        rec.apply{
            adapter = AdapterDevices()
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun startInfinityRefreshScanner(){
        val handler = Handler()
        val runnable = object: Runnable{
            override fun run() {
                (rec.adapter as AdapterDevices).clearAllDevices()
                bluetoothScanner.initRefreshData()
                handler.postDelayed(this, INTERVAL_REFRESH_DATA_SCANNER_MS)
            }
        }
        handler.post(runnable)
    }

    private fun initScan(){
        bluetoothScanner = BluetoothScanner(this, object: CallbackBluetoothScan{
            override fun permissionsNotGranted(permissions: Array<String>) {
                requestPermissions(permissions, ID_BLUETOOTH)
            }

            @SuppressLint("MissingPermission")
            override fun bluetoothNotEnable() {
                Toast.makeText(this@MainActivity, "Включите bluetooth !", Toast.LENGTH_LONG).show()
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, ID_ENABLE_BLUETOOTH)
            }

            override fun deviceFound(device: BluetoothDevice) {
                (rec.adapter as AdapterDevices).addUniqueDevice(device)
            }

            override fun deviceNotSupportBluetooth() {
                Toast.makeText(this@MainActivity, "Устройство не поддерживает Bluetooth !", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ID_BLUETOOTH){
            bluetoothScanner.initRefreshData()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ID_ENABLE_BLUETOOTH){
            bluetoothScanner.initRefreshData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothScanner.onDestroy()
    }
}

class SimpleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
class AdapterDevices(private val data: MutableList<BluetoothDevice> = mutableListOf()): RecyclerView.Adapter<SimpleViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        return SimpleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_rec, parent, false))
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        holder.itemView.name_bt.text = data[position].name
        holder.itemView.address_bt.text = data[position].address
    }

    override fun getItemCount(): Int = data.size

    fun addUniqueDevice(device: BluetoothDevice){
        if (device !in data) {
            data.add(device)
            notifyDataSetChanged()
        }
    }

    fun extendDevice(devices: Collection<BluetoothDevice>){
        data.addAll(devices)
        notifyDataSetChanged()
    }

    fun clearAllDevices(){
        data.clear()
        notifyDataSetChanged()
    }
}