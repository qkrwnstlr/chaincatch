package com.dtd.chaincatch.config

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.dtd.chaincatch.R
import com.dtd.chaincatch.databinding.DialogBaseBinding
import com.dtd.chaincatch.util.BgmService
import com.dtd.chaincatch.util.LoadingDialog
import com.dtd.chaincatch.util.SettingsManager
import com.dtd.chaincatch.util.dp

// Fragment의 기본을 작성, 뷰 바인딩 활용
abstract class BaseFragment<B : ViewBinding>(
  private val bind: (View) -> B,
  @LayoutRes layoutResId: Int
) : Fragment(layoutResId) {
  private var _binding: B? = null
  protected val mLoadingDialog: LoadingDialog by lazy {
    LoadingDialog(requireContext())
  }
  private lateinit var settingsManager: SettingsManager

  protected val binding get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    settingsManager = SettingsManager(requireContext())
    if (settingsManager.getSoundSettingVal()) startBGM()
    else stopBGM()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = bind(super.onCreateView(inflater, container, savedInstanceState)!!)
    return binding.root
  }

  override fun onDestroyView() {
    _binding = null
    super.onDestroyView()
  }

  fun showCustomToast(message: String) {
    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
  }

  fun showLoadingDialog() {
    if (!mLoadingDialog.isShowing) {
      mLoadingDialog.show()
    }
  }

  fun dismissLoadingDialog() {
    if (mLoadingDialog.isShowing) {
      mLoadingDialog.dismiss()
    }
  }

  fun buildSystemDialog(message: String, cancelAble: Boolean = false): AlertDialog {
    val dialogBaseBinding = DialogBaseBinding.inflate(layoutInflater)
    dialogBaseBinding.tvMessage.text = message

    return AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
      .setView(dialogBaseBinding.root).setCancelable(cancelAble).show().apply {
        window?.setLayout(400.dp, 250.dp)
      }
  }

  fun stopBGM() {
    requireActivity().stopService(Intent(requireContext(), BgmService::class.java))
  }

  fun startBGM() {
    requireActivity().startService(Intent(requireContext(), BgmService::class.java))
  }

}