package com.example.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class LeaderboardEntry(
    val userId: String = "",
    val playerName: String = "",
    val score: Int = 0,
    val avatar: String = "🐶",
    val timestamp: Long = System.currentTimeMillis()
)

class LeaderboardRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val scoresCollection = firestore.collection("leaderboard")

    // Sửa lại hàm submitScore để kiểm tra điểm trên cloud trước khi ghi đè
    suspend fun submitScore(userId: String, playerName: String, score: Int, avatar: String = "🐶") {
        val docRef = scoresCollection.document(userId)

        try {
            // Lấy dữ liệu hiện tại của user này trên Firestore
            val snapshot = docRef.get().await()
            val existingScore = snapshot.getLong("score")?.toInt() ?: 0

            // Chỉ cập nhật nếu: Điểm mới >= Điểm cũ HOẶC tên người chơi thay đổi (để update tên)
            // Nếu điểm mới thấp hơn điểm cũ, ta chỉ giữ lại điểm cũ nhưng vẫn cho phép cập nhật tên mới nếu cần.
            val finalScore = maxOf(score, existingScore)

            val data = mapOf(
                "userId" to userId,
                "playerName" to playerName,
                "score" to finalScore,
                "avatar" to avatar,
                "timestamp" to System.currentTimeMillis()
            )
            docRef.set(data).await()

        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback nếu có lỗi mạng thì cứ gọi set bình thường
            val data = mapOf(
                "userId" to userId,
                "playerName" to playerName,
                "score" to score,
                "avatar" to avatar,
                "timestamp" to System.currentTimeMillis()
            )
            docRef.set(data).await()
        }
    }

    fun getTopScores(): Flow<List<LeaderboardEntry>> = callbackFlow {
        val listener = scoresCollection
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val entries = snapshot.toObjects(LeaderboardEntry::class.java)
                    trySend(entries)
                }
            }
        awaitClose { listener.remove() }
    }
}