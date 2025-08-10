package com.vibeshift.robotcontroller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vibeshift.robotcontroller.databinding.ActivityMainBinding
import com.vibeshift.robotcontroller.fragments.AutoFragment
import com.vibeshift.robotcontroller.fragments.ManualFragment
import com.vibeshift.robotcontroller.fragments.SensorFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var robotViewModel: RobotViewModel
    private lateinit var bluetoothService: BluetoothService
    private var bluetoothAdapter: BluetoothAdapter? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            initializeBluetooth()
        } else {
            Toast.makeText(this, "Bluetooth permissions required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        robotViewModel = ViewModelProvider(this)[RobotViewModel::class.java]

        // Setup Bluetooth
        setupBluetooth()

        // Setup Navigation
        setupBottomNavigation()

        // Load initial fragment
        if (savedInstanceState == null) {
            loadFragment(ManualFragment())
        }

        // Setup connection button
        binding.btnConnect.setOnClickListener {
            if (bluetoothService.isConnected()) {
                bluetoothService.disconnect()
            } else {
                connectToRobot()
            }
        }

        // Observe connection status
        robotViewModel.connectionStatus.observe(this) { isConnected ->
            updateConnectionUI(isConnected)
        }
    }

    private fun setupBluetooth() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            return
        }

        bluetoothService = BluetoothService(bluetoothAdapter!!) { isConnected ->
            runOnUiThread {
                robotViewModel.setConnectionStatus(isConnected)
            }
        }

        checkBluetoothPermissions()
    }

    private fun checkBluetoothPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            initializeBluetooth()
        }
    }

    private fun initializeBluetooth() {
        // Bluetooth permissions granted
        robotViewModel.setBluetoothService(bluetoothService)
    }

    private fun connectToRobot() {
        val pairedDevices = bluetoothService.getPairedDevices().toList()
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show()
            return
        }
        val dialog = DeviceListDialog(pairedDevices) { device ->
            bluetoothService.connect(device)
        }
        dialog.show(supportFragmentManager, "devicePicker")
    }


    private fun updateConnectionUI(isConnected: Boolean) {
        binding.btnConnect.text = if (isConnected) "Disconnect" else "Connect"
        binding.connectionStatus.text = if (isConnected) "Connected" else "Disconnected"
        binding.connectionIndicator.setBackgroundColor(
            ContextCompat.getColor(this, if (isConnected) R.color.green_500 else R.color.red_500)
        )
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_manual -> {
                    loadFragment(ManualFragment())
                    true
                }
                R.id.nav_auto -> {
                    loadFragment(AutoFragment())
                    true
                }
                R.id.nav_sensors -> {
                    loadFragment(SensorFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::bluetoothService.isInitialized) {
            bluetoothService.disconnect()
        }
    }
}
