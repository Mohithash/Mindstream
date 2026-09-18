package com.mohithash.mindstream.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val text: String,
    /** 1..5 */
    val mood: Int,
    val tags: String = "",
    /** domain.Reflection JSON, empty until reflected. */
    val reflection: String = "",
)

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries ORDER BY timestamp DESC") fun all(): Flow<List<Entry>>
    @Query("SELECT * FROM entries ORDER BY timestamp DESC LIMIT :n") suspend fun recent(n: Int): List<Entry>
    @Insert suspend fun insert(e: Entry): Long
    @Update suspend fun update(e: Entry)
    @Query("DELETE FROM entries WHERE id = :id") suspend fun delete(id: Long)
    @Query("SELECT * FROM entries WHERE id = :id") suspend fun get(id: Long): Entry?
}

@Database(entities = [Entry::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() { abstract fun entries(): EntryDao }
