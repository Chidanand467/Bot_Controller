package com.vibeshift.robotcontroller.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.vibeshift.robotcontroller.RobotViewModel
import com.vibeshift.robotcontroller.databinding.FragmentManualBinding

class ManualFragment : Fragment() {
    private var _binding: FragmentManualBinding? = null
    private val binding get() = _binding!!

    private val robotViewModel: RobotViewModel by activityViewModels()
    private var currentSpeed = 128

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManualBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpeedControl()
        setupDirectionalButtons()
        setupEmergencyControls()
    }

    private fun setupSpeedControl() {
        binding.speedSeekBar.progress = currentSpeed
        binding.speedValue.text = "Speed: $currentSpeed"

        binding.speedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentSpeed = progress
                binding.speedValue.text = "Speed: $currentSpeed"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupDirectionalButtons() {
        // Forward button
        binding.btnForward.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    robotViewModel.moveForward(currentSpeed)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    robotViewModel.stop()
                    true
                }
                else -> false
            }
        }

        // Backward button
        binding.btnBackward.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    robotViewModel.moveBackward(currentSpeed)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    robotViewModel.stop()
                    true
                }
                else -> false
            }
        }

        // Left button
        binding.btnLeft.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    robotViewModel.turnLeft(currentSpeed)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    robotViewModel.stop()
                    true
                }
                else -> false
            }
        }

        // Right button
        binding.btnRight.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    robotViewModel.turnRight(currentSpeed)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    robotViewModel.stop()
                    true
                }
                else -> false
            }
        }

        // Stop button
        binding.btnStop.setOnClickListener {
            robotViewModel.stop()
        }
    }

    private fun setupEmergencyControls() {
        binding.btnEmergencyStop.setOnClickListener {
            robotViewModel.emergencyStop()
        }

        binding.btnReset.setOnClickListener {
            robotViewModel.resetRobot()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
