package com.example.minhaculinriaapp.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "variaveis_tecnicas",
    foreignKeys = @ForeignKey(
        entity = Execucao.class,
        parentColumns = "id",
        childColumns = "execucao_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("execucao_id")
)
public class VariavelTecnica {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "execucao_id")
    public long execucaoId;

    public String chave;

    public String valor;
}
