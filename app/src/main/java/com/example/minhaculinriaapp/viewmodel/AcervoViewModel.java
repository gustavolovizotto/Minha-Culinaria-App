package com.example.minhaculinriaapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.minhaculinriaapp.data.entity.CategoriaComContagem;
import com.example.minhaculinriaapp.data.entity.ReceitaResumida;
import com.example.minhaculinriaapp.data.repository.CategoriaRepository;
import com.example.minhaculinriaapp.data.repository.ReceitaRepository;

import java.util.List;

public class AcervoViewModel extends AndroidViewModel {

    private final ReceitaRepository receitaRepo;

    public final LiveData<List<CategoriaComContagem>> categorias;
    public final LiveData<List<ReceitaResumida>> receitas;

    // [busca (String), categoriaId (Long)]
    private final MutableLiveData<Object[]> filtros = new MutableLiveData<>(new Object[]{"", null});

    // ID da categoria selecionada para toggle
    private Long categoriaFiltroAtual = null;

    public AcervoViewModel(@NonNull Application app) {
        super(app);
        receitaRepo = new ReceitaRepository(app);
        categorias = new CategoriaRepository(app).listarComContagem();
        receitas = Transformations.switchMap(filtros, f -> {
            String b = (String) f[0];
            Long c = (Long) f[1];
            return receitaRepo.listarComFiltro(b.isEmpty() ? null : b, c);
        });
    }

    public void setBusca(String busca) {
        Object[] atual = filtros.getValue();
        Long cat = atual != null ? (Long) atual[1] : null;
        filtros.setValue(new Object[]{busca != null ? busca : "", cat});
    }

    /** Seleciona ou deseleciona uma categoria (toggle). */
    public Long toggleCategoria(long categoriaId) {
        Object[] atual = filtros.getValue();
        String busca = atual != null ? (String) atual[0] : "";

        if (categoriaFiltroAtual != null && categoriaFiltroAtual == categoriaId) {
            categoriaFiltroAtual = null;
        } else {
            categoriaFiltroAtual = categoriaId;
        }
        filtros.setValue(new Object[]{busca, categoriaFiltroAtual});
        return categoriaFiltroAtual;
    }

    public Long getCategoriaFiltroAtual() {
        return categoriaFiltroAtual;
    }
}
