package app.mimo.browser.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "history", indices = [Index(value = ["url"], unique = true)])
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val visitedAt: Long,
    val visitCount: Int = 1,
)

@Entity(tableName = "bookmarks", indices = [Index(value = ["url"], unique = true)])
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val createdAt: Long,
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val downloadId: Long,
    val url: String,
    val fileName: String,
    val mimeType: String?,
    val totalBytes: Long,
    val createdAt: Long,
    val status: String,
    val localPath: String?,
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history ORDER BY visitedAt DESC")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Query(
        "SELECT * FROM history WHERE title LIKE '%' || :query || '%' " +
            "OR url LIKE '%' || :query || '%' ORDER BY visitedAt DESC LIMIT :limit"
    )
    fun search(query: String, limit: Int): Flow<List<HistoryEntity>>

    @Query("SELECT COUNT(*) FROM history")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM history WHERE url = :url LIMIT 1")
    suspend fun find(url: String): HistoryEntity?

    @Query("UPDATE history SET title = :title, visitedAt = :visitedAt, visitCount = visitCount + 1 WHERE url = :url")
    suspend fun touch(url: String, title: String, visitedAt: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM history")
    suspend fun clear()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BookmarkEntity>>

    @Query("SELECT COUNT(*) FROM bookmarks")
    fun observeCount(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url)")
    fun observe(url: String): Flow<Boolean>

    @Query(
        "SELECT * FROM bookmarks WHERE title LIKE '%' || :query || '%' " +
            "OR url LIKE '%' || :query || '%' ORDER BY createdAt DESC LIMIT :limit"
    )
    fun search(query: String, limit: Int): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    suspend fun find(url: String): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("DELETE FROM bookmarks")
    suspend fun clear()
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("SELECT COUNT(*) FROM downloads")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun find(id: Long): DownloadEntity?

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    suspend fun all(): List<DownloadEntity>

    @Insert
    suspend fun insert(entity: DownloadEntity): Long

    @Query("UPDATE downloads SET status = :status, localPath = :localPath WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, localPath: String?)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM downloads")
    suspend fun clear()
}

@Database(
    entities = [HistoryEntity::class, BookmarkEntity::class, DownloadEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var instance: BrowserDatabase? = null

        fun get(context: android.content.Context): BrowserDatabase = instance ?: synchronized(this) {
            instance ?: androidx.room.Room.databaseBuilder(
                context.applicationContext,
                BrowserDatabase::class.java,
                "mimo-browser.db",
            ).build().also { instance = it }
        }
    }
}
