package com.medihelp.app.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.medihelp.app.model.Consultation;
import java.util.List;

@Dao
public interface ConsultationDao {
    @Insert
    void insert(Consultation consultation);

    @Query("SELECT * FROM consultations ORDER BY timestamp DESC")
    List<Consultation> getAll();

    @Query("DELETE FROM consultations")
    void deleteAll();
}
