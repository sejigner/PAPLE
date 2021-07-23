package com.sejigner.closest.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sejigner.closest.R
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_chat.*

class FragmentChat : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
    : View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = GroupieAdapter()
        setupDummyRows()

    }

    class LatestChatRow : Item<GroupieViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.latest_chat_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        }
    }

    private fun setupDummyRows() {
        val adapter = GroupieAdapter()

        adapter.add(LatestChatRow())
        adapter.add(LatestChatRow())
        adapter.add(LatestChatRow())

        rv_chat.adapter = adapter

    }
}