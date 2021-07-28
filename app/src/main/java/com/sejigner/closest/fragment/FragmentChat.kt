package com.sejigner.closest.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sejigner.closest.R
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.arrived_paperplane.view.*
import kotlinx.android.synthetic.main.fragment_chat.*

class FragmentChat : Fragment() {
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

        fetchPapers()

    }

    override fun onStart() {
        super.onStart()
        fetchPapers()
    }


    private fun fetchPapers() {

        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/PaperPlanes/Receiver/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapterHorizontal = GroupAdapter<GroupieViewHolder>()
                val adapterVertical = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach{
                    val paperplane = it.getValue(FragmentHome.PaperplaneMessage::class.java)
                    if(paperplane!=null) {
                        adapterHorizontal.add(PaperPlanes(paperplane))
                    }

                    if (!paperplane!!.isReplied) {
                        adapterHorizontal.setOnItemClickListener { item, view ->

                            val paperPlanes = item as PaperPlanes
                            val message = paperPlanes.paperplaneMessage.text
                            val distance = paperPlanes.paperplaneMessage.flightDistance.toString()
                            val time = paperPlanes.paperplaneMessage.timestamp.toString()
                            val toId = paperPlanes.paperplaneMessage.toId
                            val fromId = paperPlanes.paperplaneMessage.fromId
                            var isReplied = paperPlanes.paperplaneMessage.isReplied


                            val dialog = FragmentDialogFirst.newInstance(message,distance,time,toId, fromId, isReplied)
                            val fm = childFragmentManager
                            dialog.show(fm,"papaerplane mesage")
                        }
                    } else {
                        adapterHorizontal.setOnItemClickListener { item, view ->

                            val paperPlanes = item as PaperPlanes
                            val message = paperPlanes.paperplaneMessage.text
                            val distance = paperPlanes.paperplaneMessage.flightDistance.toString()
                            val time = paperPlanes.paperplaneMessage.timestamp.toString()
                            val toId = paperPlanes.paperplaneMessage.toId
                            val fromId = paperPlanes.paperplaneMessage.fromId
                            var isReplied = paperPlanes.paperplaneMessage.isReplied


                            val dialog = FragmentDialogSecond.newInstance(message,distance,time,toId, fromId, isReplied)
                            val fm = childFragmentManager
                            dialog.show(fm,"papaerplane mesage")
                        }
                    }
                }
                rv_paperplane.adapter = adapterHorizontal
                rv_chat.adapter = adapterVertical
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}

class PaperPlanes(val paperplaneMessage : FragmentHome.PaperplaneMessage) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.arrived_paperplane
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tv_paperplane_message.text = paperplaneMessage.text
        viewHolder.itemView.tv_paperplane_distance.text = paperplaneMessage.flightDistance.toString()
        viewHolder.itemView.tv_paperplane_time.text = paperplaneMessage.timestamp.toString()
    }
}
