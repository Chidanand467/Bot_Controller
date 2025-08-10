package com.vibeshift.robotcontroller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class RobotState(
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val orientation: Float = 0f,
    val batteryLevel: Float = 12.0f,
    val leftUltrasonic: Int = 0,
    val rightUltrasonic: Int = 0,
    val irSensors: List<Int> = List(8) { 1024 },
    val currentMode: String = "IDLE"
)

class RobotViewModel : ViewModel() {
    private var bluetoothService: BluetoothService? = null

    private val _connectionStatus = MutableLiveData<Boolean>()
    val connectionStatus: LiveData<Boolean> = _connectionStatus

    private val _robotState = MutableLiveData<RobotState>()
    val robotState: LiveData<RobotState> = _robotState

    private val _commandHistory = MutableLiveData<List<String>>()
    val commandHistory: LiveData<List<String>> = _commandHistory

    private val commandList = mutableListOf<String>()

    init {
        _robotState.value = RobotState()
        _connectionStatus.value = false
        _commandHistory.value = emptyList()
    }

    fun setBluetoothService(service: BluetoothService) {
        bluetoothService = service
    }

    fun setConnectionStatus(isConnected: Boolean) {
        _connectionStatus.value = isConnected
    }

    fun sendCommand(command: String): Boolean {
        val success = bluetoothService?.sendCommand(command) ?: false
        if (success) {
            commandList.add(0, "${System.currentTimeMillis()}: $command")
            if (commandList.size > 20) {
                commandList.removeAt(commandList.size - 1)
            }
            _commandHistory.value = commandList.toList()
        }
        return success
    }

    // Manual Control Commands
    fun moveForward(speed: Int) = sendCommand("FORWARD:$speed")
    fun moveBackward(speed: Int) = sendCommand("BACKWARD:$speed")
    fun turnLeft(speed: Int) = sendCommand("LEFT:$speed")
    fun turnRight(speed: Int) = sendCommand("RIGHT:$speed")
    fun stop() = sendCommand("STOP")
    fun emergencyStop() = sendCommand("EMERGENCY_STOP")

    // Auto Mode Commands
    fun setTarget(x: Float, y: Float) = sendCommand("TARGET:$x,$y")
    fun startAutonomous() = sendCommand("START_AUTO")
    fun stopAutonomous() = sendCommand("STOP_AUTO")
    fun startFigure8() = sendCommand("START_FIGURE8")
    fun stopFigure8() = sendCommand("STOP_FIGURE8")

    // System Commands
    fun requestStatus() = sendCommand("REQUEST_STATUS")
    fun resetRobot() = sendCommand("RESET")

    fun updateRobotState(newState: RobotState) {
        _robotState.value = newState
    }
}
