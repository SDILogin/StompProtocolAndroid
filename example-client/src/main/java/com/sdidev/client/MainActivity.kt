package com.sdidev.client

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.EditText
import android.widget.TextView
import com.sdidev.stomp.LifecycleEvent
import com.sdidev.stomp.R
import com.sdidev.stomp.Stomp
import com.sdidev.stomp.client.StompClient
import org.java_websocket.WebSocket
import rx.Observer
import rx.Subscription
import rx.subscriptions.CompositeSubscription

class MainActivity() : AppCompatActivity() {

    lateinit var connectionLogTextView: TextView
    lateinit var serverUrlEditText: EditText
    var stompClient: StompClient? = null

    val compositeSubscription: CompositeSubscription = CompositeSubscription();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectionLogTextView = findViewById(R.id.connection_log_text_view) as TextView
        serverUrlEditText = findViewById(R.id.server_url_text_view) as EditText

        findViewById(R.id.connect_button).setOnClickListener {
            compositeSubscription.clear()

            stompClient = Stomp.over(WebSocket::class.java, serverUrlEditText.text.toString())

            val subscription: Subscription? = stompClient?.lifecycle()?.subscribe(LifecycleObserver())
            if (subscription != null) {
                compositeSubscription.add(subscription)
            }

            stompClient?.connect()
        }
    }

    override fun onPause() {
        super.onPause()

        compositeSubscription.clear()
    }

    inner class LifecycleObserver() : Observer<LifecycleEvent> {
        
        override fun onCompleted() {
            connectionLogTextView.text = "${connectionLogTextView.text}\nfinished"
        }

        override fun onNext(t: LifecycleEvent?) {
            when (t?.type) {
                LifecycleEvent.Type.OPENED -> connectionLogTextView.text = "${connectionLogTextView.text}\nopened"
                LifecycleEvent.Type.ERROR -> connectionLogTextView.text = "${connectionLogTextView.text}\nerror\n${t?.exception?.message}\n"
                LifecycleEvent.Type.CLOSED -> connectionLogTextView.text = "${connectionLogTextView.text}\nclosed"
                else -> connectionLogTextView.text = "${connectionLogTextView.text}\n${t?.message}"
            }
        }

        override fun onError(e: Throwable?) {
            connectionLogTextView.text = "${connectionLogTextView.text}\nerror: ${e?.message}"
        }
    }
}
