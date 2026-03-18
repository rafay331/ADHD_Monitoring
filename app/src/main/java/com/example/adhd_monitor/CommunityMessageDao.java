package com.example.adhd_monitor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CommunityMessageDao {

    @Insert
    long insert(CommunityMessageEntity msg);

    @Query("SELECT * FROM community_messages ORDER BY createdAt ASC")
    List<CommunityMessageEntity> getAllMessages();

    @Query("DELETE FROM community_messages")
    void clearAll();

    @Query("SELECT * FROM community_messages ORDER BY createdAt ASC")
    LiveData<List<CommunityMessageEntity>> getAllMessagesLive();

    @Query("SELECT * FROM community_messages WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    CommunityMessageEntity getLastMessageForUser(int userId);

    @Query("UPDATE community_messages SET encouragementCount = encouragementCount + 1 WHERE id = :messageId")
    void incrementEncouragement(int messageId);

    @Query("UPDATE community_messages SET participationPoints = participationPoints + :bonus WHERE id = :messageId")
    void addBonusPointsToMessage(int messageId, int bonus);


}
