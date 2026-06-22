package com.example.minhaculinriaapp.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias")
public class Categoria {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String nome = "";

    public String descricao;

    public String cor;

    @androidx.room.ColumnInfo(name = "foto_path")
    public String fotoPath;
}
