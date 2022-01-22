package com.gievenbeck.paple.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.gievenbeck.paple.adapter.FirstPaperPlaneAdapter
import com.gievenbeck.paple.adapter.LatestMessageAdapter
import com.gievenbeck.paple.adapter.RepliedPaperPlaneAdapter
import com.gievenbeck.paple.ChatLogActivity
import com.gievenbeck.paple.MainActivity.Companion.UID
import com.gievenbeck.paple.R
import com.gievenbeck.paple.interfaces.FirstPlaneListener
import com.gievenbeck.paple.ui.FragmentChatViewModel
import com.gievenbeck.paple.ui.FragmentChatViewModelFactory
import com.gievenbeck.paple.room.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.latest_chat_row.*

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


        val repository = PaperPlaneRepository(PaperPlaneDatabase(requireActivity()))
        val factory = FragmentChatViewModelFactory(repository)
        // initialized View Model
        ViewModel =
            ViewModelProvider(this, factory).get(FragmentChatViewModel::class.java)
        val firstPlaneAdapter = FirstPaperPlaneAdapter(listOf(), ViewModel) { FirstPaperPlanes ->

            val dialog = FirstDialogFragment.newInstance(
                FirstPaperPlanes
            )
            val fm = childFragmentManager
            dialog.show(fm, "first paper")
        }

        val repliedPlaneAdapter =
            RepliedPaperPlaneAdapter(listOf(), ViewModel) { RepliedPaperPlanes ->
                val dialog = RepliedDialogFragment.newInstance(
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

        ViewModel.allFirstPaperPlanes(UID).observe(viewLifecycleOwner, {
            firstPlaneAdapter.differ.submitList(it)
            rv_paperplane_first.scrollToPosition(firstPlaneAdapter.itemCount-1)
            tv_count_first.text = it.size.toString()
            if(it.isNotEmpty()) {
                tv_notice_first_paper.visibility = View.GONE
            } else {
                tv_notice_first_paper.visibility = View.VISIBLE
            }
        })

        ViewModel.allRepliedPaperPlanes(UID).observe(viewLifecycleOwner, {
            repliedPlaneAdapter.differ.submitList(it)
            rv_paperplane_replied.scrollToPosition(repliedPlaneAdapter.itemCount-1)
            tv_count_replied.text = it.size.toString()
            if(it.isNotEmpty()) {
                tv_notice_replied_paper.text = resources.getText(R.string.tip_replied_paper)
            } else {
                tv_notice_replied_paper.text = resources.getText(R.string.notice_no_paper)
            }
        })

        ViewModel.allChatRooms(UID).observe(viewLifecycleOwner, {
            latestMessageAdapter.differ.submitList(it)
            rv_paperplane_replied.scrollToPosition(latestMessageAdapter.itemCount-1)
        })
    }

    override fun onResume() {
        super.onResume()
        callback.removeBadge()
    }

    override fun onPaperClicked(item: FirstPaperPlanes) {
        Log.d("FirstPlane", "clicked")
        val dialog = FirstDialogFragment.newInstance(
            item
        )
        val fm = childFragmentManager
        dialog.show(fm, "first paper")
    }
}
