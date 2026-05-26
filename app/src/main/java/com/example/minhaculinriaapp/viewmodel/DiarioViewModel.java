package com.example.minhaculinriaapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.minhaculinriaapp.data.database.AppDatabase;
import com.example.minhaculinriaapp.data.entity.Execucao;
import com.example.minhaculinriaapp.data.entity.ReceitaResumida;
import com.example.minhaculinriaapp.data.entity.VariavelTecnica;

import java.util.List;

public class DiarioViewModel extends AndroidViewModel {

    private final AppDatabase db;

    public final LiveData<List<ReceitaResumida>> receitas;
    public final LiveData<List<Execucao>> execucoes;

    private final MutableLiveData<Long> receitaIdLiveData = new MutableLiveData<>();

    public DiarioViewModel(@NonNull Application app) {
        super(app);
        db = AppDatabase.getInstance(app);
        receitas = db.receitaDao().listarResumidas();
        execucoes = Transformations.switchMap(receitaIdLiveData, id ->
                db.execucaoDao().listarPorReceita(id));
    }

    public void selecionarReceita(long id) {
        receitaIdLiveData.setValue(id);
    }

    public void salvar(long receitaId, String nota, String observacoes,
                       String fotoPath, List<VariavelTecnica> variaveis) {
        new Thread(() -> {
            Execucao execucao = new Execucao();
            execucao.receitaId = receitaId;
            execucao.data = System.currentTimeMillis();
            execucao.nota = nota;
            execucao.observacoes = observacoes;
            execucao.fotoPath = fotoPath;
            long execucaoId = db.execucaoDao().inserir(execucao);

            if (variaveis != null && !variaveis.isEmpty()) {
                for (VariavelTecnica v : variaveis) {
                    v.execucaoId = execucaoId;
                }
                db.variavelTecnicaDao().inserirLista(variaveis);
            }
        }).start();
    }
}
