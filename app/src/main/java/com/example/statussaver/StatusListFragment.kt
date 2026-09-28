package com.example.statussaver

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class StatusListFragment : Fragment() {

    private var isVideo = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyView: TextView
    private lateinit var adapter: StatusAdapter

    companion object {
        private const val ARG_IS_VIDEO = "is_video"
        fun newInstance(isVideo: Boolean): StatusListFragment {
            val f = StatusListFragment()
            f.arguments = Bundle().apply { putBoolean(ARG_IS_VIDEO, isVideo) }
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        isVideo = arguments?.getBoolean(ARG_IS_VIDEO) ?: false
        return inflater.inflate(R.layout.fragment_status_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        emptyView = view.findViewById(R.id.emptyView)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = StatusAdapter(
            items = emptyList(),
            onSaveClick = { item -> saveItem(item) },
            onItemClick = { item -> openPreview(item) }
        )
        recyclerView.adapter = adapter

        swipeRefresh.setOnRefreshListener { loadStatuses() }

        loadStatuses()
    }

    override fun onResume() {
        super.onResume()
        loadStatuses()
    }

    private fun loadStatuses() {
        val sourceUri = Prefs.getSourceUri(requireContext())
        if (sourceUri == null) {
            emptyView.visibility = View.VISIBLE
            emptyView.text = getString(R.string.no_source_configured)
            swipeRefresh.isRefreshing = false
            adapter.updateItems(emptyList())
            return
        }

        swipeRefresh.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            val (images, videos) = StatusScanner.scan(requireContext(), sourceUri)
            val list = if (isVideo) videos else images
            adapter.updateItems(list)
            emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            emptyView.text = getString(R.string.no_statuses_found)
            swipeRefresh.isRefreshing = false
        }
    }

    private fun saveItem(item: StatusItem) {
        val destUri = Prefs.getDestUri(requireContext())
        if (destUri == null) {
            Snackbar.make(recyclerView, R.string.no_dest_configured, Snackbar.LENGTH_LONG).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val result = SaveHelper.save(requireContext(), item, destUri)
            val msg = when (result) {
                is SaveHelper.SaveResult.Success -> getString(R.string.saved_successfully)
                is SaveHelper.SaveResult.AlreadyExists -> getString(R.string.already_saved)
                is SaveHelper.SaveResult.Error -> getString(R.string.save_failed, result.message)
            }
            Snackbar.make(recyclerView, msg, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun openPreview(item: StatusItem) {
        val intent = Intent(requireContext(), PreviewActivity::class.java)
        intent.putExtra("uri", item.uri)
        intent.putExtra("isVideo", item.isVideo)
        intent.putExtra("name", item.name)
        startActivity(intent)
    }
}
