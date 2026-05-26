package com.example.minhaculinriaapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.minhaculinriaapp.data.entity.Execucao;

import java.util.List;

@Dao
public interface ExecucaoDao {

    @Insert
    long inserir(Execucao execucao);

    @Update
    void atualizar(Execucao execucao);

    @Delete
    void deletar(Execucao execucao);

    @Query("SELECT * FROM execucoes WHERE receita_id = :receitaId ORDER BY data DESC")
    LiveData<List<Execucao>> listarPorReceita(long receitaId);

    @Query("SELECT COUNT(*) FROM execucoes")
    LiveData<Integer> contarExecucoes();
}
