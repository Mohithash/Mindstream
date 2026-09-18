package com.mohithash.mindstream

import android.app.Application
import androidx.room.Room
import com.mohithash.mindstream.ai.AiClient
import com.mohithash.mindstream.ai.JournalAi
import com.mohithash.mindstream.data.AppDb
import com.mohithash.mindstream.data.JsonStore

class App : Application() {
    lateinit var db: AppDb
    lateinit var store: JsonStore
    val client = AiClient()
    val journal by lazy { JournalAi(client) }
    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDb::class.java, "mindstream.db").build()
        store = JsonStore(this)
    }
}
