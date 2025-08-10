package com.vibeshift.robotcontroller.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.vibeshift.robotcontroller.RobotViewModel
import com.vibeshift.robotcontroller.databinding.FragmentAutoBinding

class AutoFragment : Fragment() {
    private var _binding: FragmentAutoBinding? = null
    private val binding get() = _binding!!

    private val robotViewModel: RobotViewModel by activityViewModels()
    private var isNavigating = false
    private var isFigure8Active = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAutoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTargetControls()
        setupFigure8Controls()
        setupPresetButtons()
        observeRobotState()
    }

    private fun setupTargetControls() {
        binding.btnSetTarget.setOnClickListener {
            val xText = binding.etTargetX.text.toString()
            val yText = binding.etTargetY.text.toString()

            if (xText.isNotEmpty() && yText.isNotEmpty()) {
                try {
                    val x = xText.toFloat()
                    val y = yText.toFloat()
                    robotViewModel.setTarget(x, y)
                    binding.tvTargetStatus.text = "Target set: ($x, $y)"
                } catch (e: NumberFormatException) {
                    Toast.makeText(context, "Invalid coordinates", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Enter both X and Y coordinates", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnStartNavigation.setOnClickListener {
            if (!isNavigating) {
                robotViewModel.startAutonomous()
                isNavigating = true
                updateNavigationButtons()
                binding.tvNavigationStatus.text = "Navigating to target..."
            }
        }

        binding.btnStopNavigation.setOnClickListener {
            if (isNavigating) {
                robotViewModel.stopAutonomous()
                isNavigating = false
                updateNavigationButtons()
                binding.tvNavigationStatus.text = "Navigation stopped"
            }
        }
    }

    private fun setupFigure8Controls() {
        binding.btnStartFigure8.setOnClickListener {
            if (!isFigure8Active) {
                robotViewModel.startFigure8()
                isFigure8Active = true
                updateFigure8Buttons()
                binding.tvFigure8Status.text = "Figure-8 pattern active"
            }
        }

        binding.btnStopFigure8.setOnClickListener {
            if (isFigure8Active) {
                robotViewModel.stopFigure8()
                isFigure8Active = false
                updateFigure8Buttons()
                binding.tvFigure8Status.text = "Figure-8 pattern stopped"
            }
        }
    }

    private fun setupPresetButtons() {
        binding.btnPresetHome.setOnClickListener {
            setPresetTarget(0f, 0f)
        }

        binding.btnPresetPoint1.setOnClickListener {
            setPresetTarget(50f, 100f)
        }

        binding.btnPresetPoint2.setOnClickListener {
            setPresetTarget(100f, 50f)
        }
    }

    private fun setPresetTarget(x: Float, y: Float) {
        binding.etTargetX.setText(x.toString())
        binding.etTargetY.setText(y.toString())
        robotViewModel.setTarget(x, y)
        binding.tvTargetStatus.text = "Preset target set: ($x, $y)"
    }

    private fun updateNavigationButtons() {
        binding.btnStartNavigation.isEnabled = !isNavigating
        binding.btnStopNavigation.isEnabled = isNavigating
    }

    private fun updateFigure8Buttons() {
        binding.btnStartFigure8.isEnabled = !isFigure8Active
        binding.btnStopFigure8.isEnabled = isFigure8Active
    }

    private fun observeRobotState() {
        robotViewModel.robotState.observe(viewLifecycleOwner) { state ->
            binding.tvCurrentPosition.text = "Position: (${state.positionX}, ${state.positionY})"
            binding.tvCurrentOrientation.text = "Orientation: ${state.orientation}°"
            binding.tvCurrentMode.text = "Mode: ${state.currentMode}"
            binding.tvBatteryLevel.text = "Battery: ${state.batteryLevel}V"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
