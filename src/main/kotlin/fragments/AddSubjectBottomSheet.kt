package fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import models.SubjectInput
import states.SubjectState
import viewmodels.SubjectViewModel
import vn.quizmaster.R
import vn.quizmaster.databinding.BottomSheetAddSubjectBinding

class AddSubjectBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetAddSubjectBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SubjectViewModel by activityViewModels()

    private var selectedIcon = "</>"
    private var selectedColor = "#7C35E8"
    private var coverUri: String? = null

    private val coverPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        runCatching {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        coverUri = uri.toString()
        binding.tvCover.text = uri.lastPathSegment ?: "Đã chọn ảnh bìa"
        binding.tvCover.setTextColor(Color.parseColor(selectedColor))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChoices()
        binding.btnClose.setOnClickListener { dismiss() }
        binding.coverCard.setOnClickListener { coverPicker.launch(arrayOf("image/*")) }
        binding.btnCreate.setOnClickListener { submit() }
        observeState()
    }

    private fun setupChoices() {
        val iconViews = linkedMapOf(
            binding.iconCode to "</>",
            binding.iconGlobe to "◎",
            binding.iconBook to "▤",
            binding.iconFlask to "⚗",
            binding.iconAtom to "⚛",
            binding.iconLanguage to "GB"
        )
        fun selectIcon(view: TextView) {
            iconViews.keys.forEach {
                it.isSelected = it == view
                it.setTextColor(if (it == view) Color.WHITE else requireContext().getColor(R.color.text_primary))
            }
            selectedIcon = iconViews.getValue(view)
        }
        iconViews.keys.forEach { choice -> choice.setOnClickListener { selectIcon(choice) } }
        selectIcon(binding.iconCode)

        val colors = linkedMapOf(
            binding.colorPurple to "#7C35E8",
            binding.colorGreen to "#16B98A",
            binding.colorBlue to "#16A9D7",
            binding.colorOrange to "#FF9800",
            binding.colorRed to "#F34264"
        )
        fun selectColor(view: TextView) {
            colors.keys.forEach {
                it.scaleX = if (it == view) 1.18f else 1f
                it.scaleY = if (it == view) 1.18f else 1f
                it.alpha = if (it == view) 1f else .62f
                it.contentDescription = if (it == view) "Màu đang chọn" else "Chọn màu"
            }
            selectedColor = colors.getValue(view)
        }
        colors.keys.forEach { choice -> choice.setOnClickListener { selectColor(choice) } }
        selectColor(binding.colorPurple)
    }

    private fun submit() {
        val name = binding.edtName.text?.toString()?.trim().orEmpty()
        val description = binding.edtDescription.text?.toString()?.trim().orEmpty()
        binding.nameLayout.error = null
        binding.descriptionLayout.error = null
        when {
            name.isBlank() -> binding.nameLayout.error = "Tên môn học không được để trống"
            name.length !in 3..50 -> binding.nameLayout.error = "Tên môn học phải từ 3 đến 50 ký tự"
            description.length > 300 -> binding.descriptionLayout.error = "Mô tả tối đa 300 ký tự"
            else -> {
                hideKeyboard()
                viewModel.createSubject(
                    SubjectInput(
                        name = name,
                        description = description,
                        symbol = selectedIcon,
                        color = selectedColor,
                        coverImage = coverUri,
                        isPublic = binding.switchPublic.isChecked
                    )
                )
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        SubjectState.Loading -> setLoading(true)
                        is SubjectState.Created -> {
                            setLoading(false)
                            dismiss()
                        }
                        is SubjectState.Error -> {
                            setLoading(false)
                            if (state.message.contains("tồn tại", ignoreCase = true)) {
                                binding.nameLayout.error = state.message
                            }
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                        else -> setLoading(false)
                    }
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        setChildrenEnabled(binding.formContainer, !loading)
        binding.btnClose.isEnabled = !loading
        binding.btnCreate.isEnabled = !loading
        binding.btnCreate.text = if (loading) "Đang tạo..." else "Tạo môn học"
        binding.progressCreate.visibility = if (loading) View.VISIBLE else View.GONE
        isCancelable = !loading
    }

    private fun setChildrenEnabled(group: ViewGroup, enabled: Boolean) {
        group.children.forEach { child ->
            child.isEnabled = enabled
            if (child is ViewGroup) setChildrenEnabled(child, enabled)
        }
    }

    private fun hideKeyboard() {
        val input = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        input.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "AddSubjectBottomSheet"
    }
}
