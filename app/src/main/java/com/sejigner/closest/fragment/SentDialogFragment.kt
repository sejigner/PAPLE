package com.sejigner.closest.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sejigner.closest.R
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.ui.FragmentChatViewModelFactory
import com.sejigner.closest.room.*
import kotlinx.android.synthetic.main.fragment_dialog_first.*
import kotlinx.android.synthetic.main.fragment_dialog_sent.*
import kotlinx.android.synthetic.main.fragment_dialog_write.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ITEMS = "data"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentDialogFirst.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentDialogSent : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var message: String? = null
    private var distance: Double? = null
    private var time: Long? = null
    private var paper: MyPaperPlaneRecord? = null
    lateinit var repository : PaperPlaneRepository
    lateinit var factory : FragmentChatViewModelFactory
    lateinit var viewModel : FragmentChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            message = it.getString("message")
            time = it.getLong("time")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog_sent, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = PaperPlaneRepository(PaperPlaneDatabase.invoke(requireActivity()))
        factory = FragmentChatViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory).get(FragmentChatViewModel::class.java)

        tv_dialog_time_sent.text = setDateToTextView(time!!)
        tv_dialog_message_sent.text = message

        tv_discard_my_paper.setOnClickListener {
            viewModel.delete(paper!!)
            dismiss()
        }
    }

    private fun convertDistanceToString(distance : Double) : String {
        return if(distance >= 1000) {
            (round((distance/1000)*100) /100).toString() + "km"
        } else distance.toString() + "m"
    }

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog!!.window!!.setLayout(width, height)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setDateToTextView(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd a hh:mm")
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return sdf.format(timestamp * 1000L)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentDialog.
         */

        const val TAG = "DialogFlightSuccess"

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(paperPlane: MyPaperPlaneRecord) =
            FragmentDialogSent().apply {
                arguments = Bundle().apply {
                    putString("message", paperPlane.userMessage)
                    putLong("time", paperPlane.firstTimestamp)
                }
                paper = paperPlane
            }
    }


}