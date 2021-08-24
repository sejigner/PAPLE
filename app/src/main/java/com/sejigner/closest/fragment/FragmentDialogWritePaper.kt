package com.sejigner.closest.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.FirebaseDatabase
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.R
import com.sejigner.closest.UI.FragmentChatViewModel
import com.sejigner.closest.UI.FragmentChatViewModelFactory
import com.sejigner.closest.models.PaperplaneMessage
import com.sejigner.closest.room.MyPaperPlaneRecord
import com.sejigner.closest.room.PaperPlaneDatabase
import com.sejigner.closest.room.PaperPlaneRepository
import kotlinx.android.synthetic.main.fragment_dialog_first.*
import kotlinx.android.synthetic.main.fragment_dialog_write.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ITEMS = "data"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentDialogFirst.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentDialogWritePaper : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var uid: String? = null
    private var userFoundId: String? = null
    private var currentAddress: String? = null
    private var flightDistance: Double = 0.0
    private var mCallbackMain : WritePaperListenerMain ?= null
    private var mCallbackHome : WritePaperListenerHome ?= null
    lateinit var ViewModel: FragmentChatViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString("uid")
            userFoundId = it.getString("userFoundId")
            currentAddress = it.getString("currentAddress")
            flightDistance = it.getDouble("flightDistance")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog_write, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = PaperPlaneRepository(PaperPlaneDatabase(requireActivity()))
        val factory = FragmentChatViewModelFactory(repository)

        ViewModel =
            ViewModelProvider(requireActivity(), factory).get(FragmentChatViewModel::class.java)

        val etPaper = view.findViewById<View>(R.id.et_write_paper) as? EditText
        val textCount = view.findViewById<View>(R.id.tv_count_letter_paper) as? TextView
        val btnClose = view.findViewById<View>(R.id.iv_close_paper) as? ImageView
        val btnFly = view.findViewById<View>(R.id.tv_paper_send) as? TextView
        val location = view.findViewById<View>(R.id.tv_update_location_paper) as? TextView

        location?.text = currentAddress
        btnFly?.setOnClickListener{
            val isSuccess = performSendAnonymousMessage()
            if(isSuccess) {
                mCallbackHome?.setUserFound()
                mCallbackMain?.showSuccessFragment(flightDistance)
                dismiss()
                mCallbackHome?.getClosestUser()
            }
        }

        btnClose?.setOnClickListener {
            dismiss()
        }

        etPaper?.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                textCount?.text = getString(R.string.limit_write)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var userinput = et_write_paper.text.toString()
                textCount?.text = userinput.length.toString() + " / 250"
            }

            override fun afterTextChanged(s: Editable?) {
                var userinput = et_write_paper.text.toString()
                textCount?.text = userinput.length.toString() + " / 250"
            }
        })

    }


    private fun performSendAnonymousMessage() : Boolean {

        if (userFoundId != "") {
            val toId = userFoundId!!
            val message = et_write_paper.text.toString()
            val fromId = UID
            val distance = flightDistance

            val paperPlaneReceiverReference =
                FirebaseDatabase.getInstance().getReference("/PaperPlanes/Receiver/$toId/$fromId")

            val acquaintanceRecordFromReference =
                FirebaseDatabase.getInstance().getReference("/Acquaintances/$fromId")
            val acquaintanceRecordToReference =
                FirebaseDatabase.getInstance().getReference("/Acquaintances/$toId")
            val paperplaneMessage = PaperplaneMessage(
                paperPlaneReceiverReference.key!!,
                message,
                fromId,
                toId,
                distance,
                System.currentTimeMillis() / 1000L,
                false
            )

            paperPlaneReceiverReference.setValue(paperplaneMessage).addOnFailureListener {
                Log.d(FragmentHome.TAG, "Receiver 실패")
            }.addOnSuccessListener {
                val sentPaper = MyPaperPlaneRecord(paperplaneMessage.toId,paperplaneMessage.text, paperplaneMessage.timestamp)
                ViewModel.insert(sentPaper)

                acquaintanceRecordFromReference.child(toId).child("haveMet").setValue(true)
                    .addOnSuccessListener {
                        Log.d(FragmentHome.TAG, "소통 기록 저장 - 발신자: $fromId")
                    }
                acquaintanceRecordToReference.child(fromId).child("haveMet").setValue(true)
                    .addOnSuccessListener {
                        Log.d(FragmentHome.TAG, "소통 기록 저장 - 수신자: $toId")
                    }
            }
        }
        else {
            return false
        }
        return true
    }


    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    interface WritePaperListenerMain {
        fun showSuccessFragment(flightDistance: Double)
    }

    interface WritePaperListenerHome {
        fun getClosestUser()
        fun setUserFound()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is WritePaperListenerMain) {
            mCallbackMain = context
        } else {
            throw RuntimeException(context.toString() + "must implement WritePaperListener")
        }
    }


    companion object{
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentDialog.
         */

        const val TAG = "WritePaperDialog"

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(uid: String, userFoundId : String, currentAddress: String, flightDistance: Double) =
            FragmentDialogWritePaper().apply {
                arguments = Bundle().apply {
                    putString("uid", uid)
                    putString("userFoundId", userFoundId)
                    putString("currentAddress", currentAddress)
                    putDouble("flightDistance",flightDistance)
                }
            }
    }
}