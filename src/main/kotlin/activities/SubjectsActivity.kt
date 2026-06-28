package activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import adapters.SubjectAdapter
import com.google.android.material.snackbar.Snackbar
import database.QuizDatabase
import fragments.AddSubjectBottomSheet
import kotlinx.coroutines.launch
import models.Subject
import repositories.SubjectRepository
import states.SubjectState
import viewmodels.SubjectViewModel
import viewmodels.SubjectViewModelFactory
import vn.quizmaster.R
import vn.quizmaster.databinding.ActivitySubjectsBinding

class SubjectsActivity : BaseActivity(), SubjectAdapter.Listener {
    private lateinit var binding: ActivitySubjectsBinding
    private lateinit var adapter: SubjectAdapter
    private lateinit var touchHelper: ItemTouchHelper
    private var allSubjects: List<Subject> = emptyList()

    private val database by lazy { QuizDatabase(applicationContext) }
    private val viewModel: SubjectViewModel by viewModels {
        SubjectViewModelFactory(SubjectRepository(database))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation(R.id.navSubjects)
        setupRecyclerView()
        setupActions()
        observeState()
        if (viewModel.state.value is SubjectState.Idle) viewModel.loadSubjects()
    }

    private fun setupRecyclerView() {
        adapter = SubjectAdapter(mutableListOf(), this)
        binding.rvSubjects.layoutManager = LinearLayoutManager(this)
        binding.rvSubjects.adapter = adapter
        touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = adapter.move(source.bindingAdapterPosition, target.bindingAdapterPosition)

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewModel.saveOrder(adapter.currentItems())
                Snackbar.make(binding.root, "Đã lưu thứ tự môn học", Snackbar.LENGTH_SHORT).show()
            }

            override fun isLongPressDragEnabled(): Boolean = false
        })
        touchHelper.attachToRecyclerView(binding.rvSubjects)
    }

    private fun setupActions() {
        binding.fabAddSubject.setOnClickListener {
            if (supportFragmentManager.findFragmentByTag(AddSubjectBottomSheet.TAG) == null) {
                AddSubjectBottomSheet().show(supportFragmentManager, AddSubjectBottomSheet.TAG)
            }
        }
        binding.btnSearch.setOnClickListener {
            val showing = binding.searchContainer.visibility == View.VISIBLE
            binding.searchContainer.visibility = if (showing) View.GONE else View.VISIBLE
            if (!showing) binding.edtSearch.requestFocus() else binding.edtSearch.setText("")
        }
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                renderList(
                    allSubjects.filter { it.name.contains(s?.toString().orEmpty(), ignoreCase = true) }
                )
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is SubjectState.Ready -> {
                            allSubjects = state.subjects
                            renderList(filterCurrentQuery(state.subjects))
                        }
                        is SubjectState.Created -> {
                            allSubjects = state.subjects
                            binding.edtSearch.setText("")
                            renderList(state.subjects, state.subject.id)
                            val position = state.subjects.indexOfFirst { it.id == state.subject.id }
                            if (position >= 0) {
                                binding.rvSubjects.scrollToPosition(position)
                                binding.rvSubjects.post {
                                    binding.rvSubjects.findViewHolderForAdapterPosition(position)
                                        ?.itemView?.apply {
                                            alpha = .25f
                                            animate().alpha(1f).setDuration(1000).start()
                                        }
                                }
                            }
                            Snackbar.make(
                                binding.root,
                                "Môn học đã được tạo thành công.",
                                Snackbar.LENGTH_LONG
                            ).show()
                            binding.root.postDelayed({ viewModel.consumeEvent() }, 500)
                        }
                        is SubjectState.Error ->
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun filterCurrentQuery(subjects: List<Subject>): List<Subject> {
        val query = binding.edtSearch.text?.toString().orEmpty()
        return subjects.filter { it.name.contains(query, ignoreCase = true) }
    }

    private fun renderList(subjects: List<Subject>, highlightId: Long? = null) {
        adapter.submitItems(subjects, highlightId)
        binding.tvSubjectCount.text = "${subjects.size} môn học"
        binding.tvEmpty.visibility = if (subjects.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onSubjectClick(subject: Subject) {
        startActivity(Intent(this, SubjectDetailActivity::class.java).apply {
            putExtra(SubjectDetailActivity.EXTRA_SUBJECT_ID, subject.id)
        })
    }

    override fun onSubjectLongClick(subject: Subject) {
        AlertDialog.Builder(this)
            .setTitle(subject.name)
            .setMessage("Xóa môn học này? Các câu hỏi và tiến độ liên quan cũng sẽ bị xóa.")
            .setNegativeButton("Giữ lại", null)
            .setPositiveButton("Xóa") { _, _ ->
                adapter.remove(subject)
                viewModel.deleteSubject(subject)
                Snackbar.make(binding.root, "Đã xóa ${subject.name}", Snackbar.LENGTH_LONG).show()
            }
            .show()
    }

    override fun onDragRequested(holder: RecyclerView.ViewHolder) {
        if (binding.edtSearch.text.isNullOrBlank()) {
            touchHelper.startDrag(holder)
        } else {
            Snackbar.make(binding.root, "Xóa từ khóa tìm kiếm trước khi sắp xếp", Snackbar.LENGTH_SHORT).show()
        }
    }
}
