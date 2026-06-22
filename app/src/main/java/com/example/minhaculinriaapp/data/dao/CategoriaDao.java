package com.example.minhaculinriaapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.minhaculinriaapp.data.entity.Categoria;
import com.example.minhaculinriaapp.data.entity.CategoriaComContagem;

import java.util.List;

@Dao
public interface CategoriaDao {

    @Insert
    long inserir(Categoria categoria);

    @Update
    void atualizar(Categoria categoria);

    @Delete
    void deletar(Categoria categoria);

    @Query("SELECT * FROM categorias ORDER BY nome ASC")
    LiveData<List<Categoria>> listarTodas();

    @Query("SELECT * FROM categorias WHERE id = :id")
    LiveData<Categoria> buscarPorId(long id);

    @Query("SELECT c.id, c.nome, c.descricao, c.cor, c.foto_path as fotoPath, COUNT(r.id) as total " +
           "FROM categorias c LEFT JOIN receitas r ON r.categoria_id = c.id " +
           "GROUP BY c.id ORDER BY c.nome ASC")
    LiveData<List<CategoriaComContagem>> listarComContagem();
}
