package com.example.minhaculinriaapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.minhaculinriaapp.data.entity.Categoria;
import com.example.minhaculinriaapp.data.entity.Ingrediente;
import com.example.minhaculinriaapp.data.entity.Passo;
import com.example.minhaculinriaapp.data.entity.Receita;
import com.example.minhaculinriaapp.data.repository.CategoriaRepository;
import com.example.minhaculinriaapp.data.repository.ReceitaRepository;

import java.util.ArrayList;
import java.util.List;

public class CadastroReceitaViewModel extends AndroidViewModel {

    private final ReceitaRepository receitaRepo;
    public final LiveData<List<Categoria>> categorias;

    // null = nova receita, non-null = edição
    public Long receitaEditandoId = null;

    // Dados acumulados entre os 3 passos
    public String nome;
    public String descricao;
    public Long categoriaId;
    public String fotoPath;

    public final List<Ingrediente> ingredientes = new ArrayList<>();
    public final List<Passo> passos = new ArrayList<>();

    public Integer rendimento;
    public Integer tempoMinutos;
    public String dificuldade = "Médio";
    public String tags;
    public String notas;

    public CadastroReceitaViewModel(@NonNull Application app) {
        super(app);
        receitaRepo = new ReceitaRepository(app);
        categorias = new CategoriaRepository(app).listarTodas();
    }

    public boolean modoEdicao() {
        return receitaEditandoId != null;
    }

    public void salvar() {
        Receita r = new Receita();
        r.nome = nome != null ? nome : "";
        r.descricao = descricao;
        r.categoriaId = categoriaId;
        r.fotoPath = fotoPath;
        r.rendimento = rendimento;
        r.tempoMinutos = tempoMinutos;
        r.dificuldade = dificuldade;
        r.tags = tags;
        r.notas = notas;

        if (modoEdicao()) {
            r.id = receitaEditandoId;
            receitaRepo.atualizarCompleta(r, new ArrayList<>(ingredientes), new ArrayList<>(passos));
        } else {
            r.criadoEm = System.currentTimeMillis();
            receitaRepo.inserirCompleta(r, new ArrayList<>(ingredientes), new ArrayList<>(passos));
        }
    }

    public void limpar() {
        receitaEditandoId = null;
        nome = null;
        descricao = null;
        categoriaId = null;
        fotoPath = null;
        ingredientes.clear();
        passos.clear();
        rendimento = null;
        tempoMinutos = null;
        dificuldade = "Médio";
        tags = null;
        notas = null;
    }
}
