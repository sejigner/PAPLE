package com.sejigner.closest.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.google.firebase.database.*
import com.sejigner.closest.adapter.FirstPaperPlaneAdapter
import com.sejigner.closest.adapter.LatestMessageAdapter
import com.sejigner.closest.adapter.RepliedPaperPlaneAdapter
import com.sejigner.closest.ChatLogActivity
import com.sejigner.closest.MainActivity
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.R
import com.sejigner.closest.interfaces.FirstPlaneListener
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.ui.FragmentChatViewModelFactory
import com.sejigner.closest.models.Users
import com.sejigner.closest.models.ChatMessage
import com.sejigner.closest.models.LatestChatMessage
import com.sejigner.closest.models.PaperplaneMessage
import com.sejigner.closest.room.*
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.latest_chat_row.*
import kotlinx.android.synthetic.main.latest_chat_row.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.text.SimpleDateFormat
import java.util.*

// TODO : ChatLogActivity 내 chatRoomAndAllMessages 참고하여 일대다관계 데이터 활용
//  chatRoomAndAllMessages ( UID - ChatRoomId - Message 구조)
//  planes (UID - Planes)
//  ※ 수정하기 전, 각 리사이클러뷰 Adapter의 자료형 참고할 것
//  Ex) FirstPaperPlane -> Uid -> Uid를 필드 값으로 갖는 LiveData 형태의 FirstPlanesWithUid 리스트 반환 -> observe
class FragmentChat : Fragment(), FirstPlaneListener {

    companion object {
        const val TAG = "FragmentChat"
        const val USER_KEY = "USER_KEY"
    }

    lateinit var ViewModel: FragmentChatViewModel
    lateinit var list: List<FirstPaperPlanes>
    lateinit var callback : OnCommunicationUpdatedListener

    interface OnCommunicationUpdatedListener {
        fun removeBadge()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCommunicationUpdatedListener) {
            callback = context
        } else {
            throw RuntimeException(context.toString() + "must implement OnCommunicationUpdatedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )
            : View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = MainActivity.getUid()
        val repository = PaperPlaneRepository(PaperPlaneDatabase(requireActivity()))
        val factory = FragmentChatViewModelFactory(repository)
        // initialized View Model
        ViewModel =
            ViewModelProvider(this, factory).get(FragmentChatViewModel::class.java)
        val firstPlaneAdapter = FirstPaperPlaneAdapter(listOf(), ViewModel) { FirstPaperPlanes ->

            val dialog = FragmentDialogFirst.newInstance(
                FirstPaperPlanes
            )
            val fm = childFragmentManager
            dialog.show(fm, "first paper")
        }

        val repliedPlaneAdapter =
            RepliedPaperPlaneAdapter(listOf(), ViewModel) { RepliedPaperPlanes ->
                val dialog = FragmentDialogReplied.newInstance(
                    RepliedPaperPlanes
                )
                val fm = childFragmentManager
                dialog.show(fm, "replied paper")
            }

        val latestMessageAdapter =
            LatestMessageAdapter(listOf(), ViewModel) { LatestMessages ->
                cl_latest_chat_row.performClick()
                val messageItem = LatestMessages
                val intent = Intent(requireActivity(), ChatLogActivity::class.java)
                val chatPartnerId = messageItem.partnerId
                intent.putExtra(USER_KEY, chatPartnerId)
                startActivity(intent)
            }


        rv_chat.adapter = latestMessageAdapter
        rv_paperplane_first.adapter = firstPlaneAdapter
        rv_paperplane_replied.adapter = repliedPlaneAdapter

        // 역순 정렬
        val mLayoutManagerFirst = LinearLayoutManager(requireActivity())
        val mLayoutManagerReplied = LinearLayoutManager(requireActivity())
        val mLayoutManagerMessages = LinearLayoutManager(requireActivity())
        mLayoutManagerFirst.reverseLayout = true
        mLayoutManagerFirst.stackFromEnd = true
        mLayoutManagerFirst.orientation = HORIZONTAL
        mLayoutManagerReplied.reverseLayout = true
        mLayoutManagerReplied.stackFromEnd = true
        mLayoutManagerReplied.orientation = HORIZONTAL
        mLayoutManagerMessages.orientation = VERTICAL

        rv_chat.layoutManager = mLayoutManagerMessages
        rv_paperplane_first.layoutManager = mLayoutManagerFirst
        rv_paperplane_replied.layoutManager = mLayoutManagerReplied

        ViewModel.allFirstPaperPlanes(UID).observe(viewLifecycleOwner, Observer {
            firstPlaneAdapter.list = it
            firstPlaneAdapter.notifyDataSetChanged()
            tv_count_first.text = firstPlaneAdapter.list.size.toString()
        })
        ViewModel.allRepliedPaperPlanes(UID).observe(viewLifecycleOwner, Observer {
            repliedPlaneAdapter.list = it
            repliedPlaneAdapter.notifyDataSetChanged()
            tv_count_replied.text = repliedPlaneAdapter.list.size.toString()
        })

        ViewModel.allChatRooms(UID).observe(viewLifecycleOwner, {
            latestMessageAdapter.list = it
            latestMessageAdapter.notifyDataSetChanged()
        })
    }

    override fun onResume() {
        super.onResume()
        callback.removeBadge()
    }

    override fun onPaperClicked(item: FirstPaperPlanes) {
        Log.d("FirstPlane", "clicked")
        val dialog = FragmentDialogFirst.newInstance(
            item
        )
        val fm = childFragmentManager
        dialog.show(fm, "first paper")
    }
}
