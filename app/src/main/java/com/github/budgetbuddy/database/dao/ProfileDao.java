package com.github.budgetbuddy.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.budgetbuddy.database.entity.Profile;

import java.util.List;

@Dao
public interface ProfileDao {

    @Insert
    long insertProfile(Profile profile);

    @Query("SELECT * FROM profile")
    List<Profile> getAllProfiles();

    @Query("SELECT * FROM profile WHERE id = :id")
    Profile getProfileById(int id);

    @Query("SELECT * FROM profile WHERE name = :name LIMIT 1")
    Profile getProfileByName(String name);

    @Delete
    void deleteProfile(Profile profile);
}
