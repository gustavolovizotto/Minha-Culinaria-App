package com.example.minhaculinriaapp.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.minhaculinriaapp.data.dao.CategoriaDao;
import com.example.minhaculinriaapp.data.dao.ExecucaoDao;
import com.example.minhaculinriaapp.data.dao.IngredienteDao;
import com.example.minhaculinriaapp.data.dao.PassoDao;
import com.example.minhaculinriaapp.data.dao.ReceitaDao;
import com.example.minhaculinriaapp.data.dao.VariavelTecnicaDao;
import com.example.minhaculinriaapp.data.entity.Categoria;
import com.example.minhaculinriaapp.data.entity.Execucao;
import com.example.minhaculinriaapp.data.entity.Ingrediente;
import com.example.minhaculinriaapp.data.entity.Passo;
import com.example.minhaculinriaapp.data.entity.Receita;
import com.example.minhaculinriaapp.data.entity.VariavelTecnica;

@Database(
    entities = {Categoria.class, Receita.class, Ingrediente.class, Passo.class, Execucao.class, VariavelTecnica.class},
    version = 4,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract CategoriaDao categoriaDao();
    public abstract ReceitaDao receitaDao();
    public abstract IngredienteDao ingredienteDao();
    public abstract PassoDao passoDao();
    public abstract ExecucaoDao execucaoDao();
    public abstract VariavelTecnicaDao variavelTecnicaDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "cofre_db"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
