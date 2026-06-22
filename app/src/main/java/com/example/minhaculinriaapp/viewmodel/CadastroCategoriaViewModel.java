package com.example.minhaculinriaapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.minhaculinriaapp.data.entity.Categoria;
import com.example.minhaculinriaapp.data.repository.CategoriaRepository;

public class CadastroCategoriaViewModel extends AndroidViewModel {

    private final CategoriaRepository repo;

    public CadastroCategoriaViewModel(@NonNull Application app) {
        super(app);
        repo = new CategoriaRepository(app);
    }

    public void salvar(String nome, String descricao, String cor, String fotoPath) {
        Categoria c = new Categoria();
        c.nome = nome;
        c.descricao = descricao;
        c.cor = cor;
        c.fotoPath = fotoPath;
        repo.inserir(c);
    }
}
