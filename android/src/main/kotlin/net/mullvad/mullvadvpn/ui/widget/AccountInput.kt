package net.mullvad.mullvadvpn.ui.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.style.MetricAffectingSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.properties.Delegates.observable
import net.mullvad.mullvadvpn.R
import net.mullvad.mullvadvpn.ui.LoginState
import net.mullvad.talpid.util.EventNotifier

const val MIN_ACCOUNT_TOKEN_LENGTH = 10

class AccountInput : LinearLayout {
    private val disabledTextColor = context.getColor(R.color.white)
    private val enabledTextColor = context.getColor(R.color.blue)
    private val errorTextColor = context.getColor(R.color.red)

    private val container =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).let { service ->
            val inflater = service as LayoutInflater

            inflater.inflate(R.layout.account_input, this)
        }

    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(text: Editable) {
            removeFormattingSpans(text)
            setButtonEnabled(text.length >= MIN_ACCOUNT_TOKEN_LENGTH)
        }
    }

    private val input = container.findViewById<TextView>(R.id.login_input).apply {
        addTextChangedListener(inputWatcher)

        onFocusChangeListener = OnFocusChangeListener { view, inputHasFocus ->
            hasFocus = inputHasFocus && view.isEnabled
        }
    }

    private val button = container.findViewById<ImageButton>(R.id.login_button).apply {
        setOnClickListener {
            onLogin?.invoke(input.text.toString())
        }
    }

    val onFocusChanged = EventNotifier(false)
    private var hasFocus by onFocusChanged.notifiable()

    var loginState by observable(LoginState.Initial) { _, _, state ->
        when (state) {
            LoginState.Initial -> initialState()
            LoginState.InProgress -> loggingInState()
            LoginState.Success -> successState()
            LoginState.Failure -> failureState()
        }
    }

    var onLogin: ((String) -> Unit)? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {}

    constructor(context: Context, attributes: AttributeSet, defaultStyleAttribute: Int) :
        super(context, attributes, defaultStyleAttribute) {}

    constructor(
        context: Context,
        attributes: AttributeSet,
        defaultStyleAttribute: Int,
        defaultStyleResource: Int
    ) : super(context, attributes, defaultStyleAttribute, defaultStyleResource) {
    }

    init {
        orientation = HORIZONTAL

        setButtonEnabled(false)
    }

    fun loginWith(accountNumber: String) {
        input.text = accountNumber
        onLogin?.invoke(accountNumber)
    }

    private fun initialState() {
        input.apply {
            setTextColor(enabledTextColor)
            setEnabled(true)
            setFocusableInTouchMode(true)
            visibility = View.VISIBLE
        }

        button.visibility = View.VISIBLE
        setButtonEnabled(input.text.length >= MIN_ACCOUNT_TOKEN_LENGTH)
    }

    private fun loggingInState() {
        input.apply {
            setTextColor(disabledTextColor)
            setEnabled(false)
            setFocusable(false)
            visibility = View.VISIBLE
        }

        button.visibility = View.GONE
        setButtonEnabled(false)
    }

    private fun successState() {
        button.visibility = View.GONE
        setButtonEnabled(false)

        input.visibility = View.GONE
    }

    private fun failureState() {
        button.visibility = View.VISIBLE
        setButtonEnabled(false)

        input.apply {
            setTextColor(errorTextColor)
            setEnabled(true)
            setFocusableInTouchMode(true)
            visibility = View.VISIBLE
            requestFocus()
        }
    }

    private fun setButtonEnabled(enabled: Boolean) {
        button.apply {
            if (enabled != isEnabled()) {
                setEnabled(enabled)
                setClickable(enabled)
                setFocusable(enabled)
            }
        }
    }

    private fun removeFormattingSpans(text: Editable) {
        for (span in text.getSpans(0, text.length, MetricAffectingSpan::class.java)) {
            text.removeSpan(span)
        }
    }
}
