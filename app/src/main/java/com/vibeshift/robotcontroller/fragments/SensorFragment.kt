package com.vibeshift.robotcontroller.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.vibeshift.robotcontroller.RobotViewModel
import com.vibeshift.robotcontroller.databinding.FragmentSensorBinding

class SensorFragment : Fragment() {
    private var _binding: FragmentSensorBinding? = null
    private val binding get() = _binding!!

    private val robotViewModel: RobotViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupControls()
        observeSensorData()
    }

    private fun setupControls() {
        binding.btnRequestData.setOnClickListener {
            robotViewModel.requestStatus()
        }

        binding.btnClearHistory.setOnClickListener {
            // Clear command history would be implemented in ViewModel
        }
    }

    private fun observeSensorData() {
        robotViewModel.robotState.observe(viewLifecycleOwner) { state ->
            // Update ultrasonic sensors
            binding.tvUltrasonicLeft.text = "Left: ${state.leftUltrasonic} cm"
            binding.tvUltrasonicRight.text = "Right: ${state.rightUltrasonic} cm"

            // Update IR sensors
            binding.tvIr0.text = "IR0: ${state.irSensors.getOrNull(0) ?: 0}"
            binding.tvIr1.text = "IR1: ${state.irSensors.getOrNull(1) ?: 0}"
            binding.tvIr2.text = "IR2: ${state.irSensors.getOrNull(2) ?: 0}"
            binding.tvIr3.text = "IR3: ${state.irSensors.getOrNull(3) ?: 0}"
            binding.tvIr4.text = "IR4: ${state.irSensors.getOrNull(4) ?: 0}"
            binding.tvIr5.text = "IR5: ${state.irSensors.getOrNull(5) ?: 0}"
            binding.tvIr6.text = "IR6: ${state.irSensors.getOrNull(6) ?: 0}"
            binding.tvIr7.text = "IR7: ${state.irSensors.getOrNull(7) ?: 0}"

            // Update position and status
            binding.tvPosition.text = "Position: (${state.positionX}, ${state.positionY})"
            binding.tvOrientation.text = "Orientation: ${state.orientation}°"
            binding.tvBattery.text = "Battery: ${state.batteryLevel}V"
            binding.tvMode.text = "Mode: ${state.currentMode}"
        }

        robotViewModel.commandHistory.observe(viewLifecycleOwner) { commands ->
            binding.tvCommandHistory.text = commands.take(10).joinToString("\n")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
