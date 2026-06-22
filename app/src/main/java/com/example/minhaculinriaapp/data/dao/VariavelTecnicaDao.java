package com.example.minhaculinriaapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.minhaculinriaapp.data.entity.VariavelTecnica;

import java.util.List;

@Dao
public interface VariavelTecnicaDao {

    @Insert
    void inserirLista(List<VariavelTecnica> variaveis);

    @Query("SELECT * FROM variaveis_tecnicas WHERE execucao_id = :execucaoId ORDER BY id ASC")
    LiveData<List<VariavelTecnica>> listarPorExecucao(long execucaoId);

    @Query("DELETE FROM variaveis_tecnicas WHERE execucao_id = :execucaoId")
    void deletarPorExecucao(long execucaoId);
}
