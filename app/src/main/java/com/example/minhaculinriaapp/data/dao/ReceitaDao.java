package com.example.minhaculinriaapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.minhaculinriaapp.data.entity.Receita;
import com.example.minhaculinriaapp.data.entity.ReceitaResumida;

import java.util.List;

@Dao
public interface ReceitaDao {

    @Insert
    long inserir(Receita receita);

    @Update
    void atualizar(Receita receita);

    @Delete
    void deletar(Receita receita);

    @Query("SELECT * FROM receitas ORDER BY criado_em DESC")
    LiveData<List<Receita>> listarTodas();

    @Query("SELECT * FROM receitas WHERE id = :id")
    LiveData<Receita> buscarPorId(long id);

    @Query("SELECT r.id, r.nome, r.descricao, r.foto_path, r.rendimento, " +
           "r.tempo_minutos, r.dificuldade, r.tags, r.criado_em, " +
           "c.nome as categoria_nome " +
           "FROM receitas r LEFT JOIN categorias c ON c.id = r.categoria_id " +
           "ORDER BY r.criado_em DESC")
    LiveData<List<ReceitaResumida>> listarResumidas();

    @Query("SELECT r.id, r.nome, r.descricao, r.foto_path, r.rendimento, " +
           "r.tempo_minutos, r.dificuldade, r.tags, r.criado_em, " +
           "c.nome as categoria_nome " +
           "FROM receitas r LEFT JOIN categorias c ON c.id = r.categoria_id " +
           "WHERE r.id = :id")
    LiveData<ReceitaResumida> buscarResumidaPorId(long id);

    @Query("SELECT r.id, r.nome, r.descricao, r.foto_path, r.rendimento, " +
           "r.tempo_minutos, r.dificuldade, r.tags, r.criado_em, " +
           "c.nome as categoria_nome " +
           "FROM receitas r LEFT JOIN categorias c ON c.id = r.categoria_id " +
           "WHERE (:busca IS NULL OR r.nome LIKE '%' || :busca || '%') " +
           "AND (:categoriaId IS NULL OR r.categoria_id = :categoriaId) " +
           "ORDER BY r.criado_em DESC")
    LiveData<List<ReceitaResumida>> listarComFiltro(String busca, Long categoriaId);

    @Query("SELECT COUNT(*) FROM receitas")
    LiveData<Integer> contarReceitas();

    @Query("SELECT COALESCE(SUM(tempo_minutos), 0) FROM receitas")
    LiveData<Integer> somarTempoMinutos();
}
