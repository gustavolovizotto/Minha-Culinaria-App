package com.example.minhaculinriaapp.ui.acervo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minhaculinriaapp.R;
import com.example.minhaculinriaapp.data.entity.Categoria;
import com.example.minhaculinriaapp.data.repository.CategoriaRepository;
import com.example.minhaculinriaapp.viewmodel.AcervoViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class AcervoFragment extends Fragment {

    private AcervoViewModel viewModel;
    private CategoriaAdapter categoriaAdapter;
    private ReceitaAdapter receitaAdapter;
    private LinearLayout sectionCategorias;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_acervo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AcervoViewModel.class);

        sectionCategorias = view.findViewById(R.id.section_categorias);

        // RecyclerView categorias
        categoriaAdapter = new CategoriaAdapter();
        RecyclerView rvCategorias = view.findViewById(R.id.rv_categorias);
        rvCategorias.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvCategorias.setAdapter(categoriaAdapter);
        rvCategorias.setHasFixedSize(false);

        // RecyclerView receitas
        receitaAdapter = new ReceitaAdapter();
        RecyclerView rvReceitas = view.findViewById(R.id.rv_receitas);
        rvReceitas.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvReceitas.setAdapter(receitaAdapter);
        rvReceitas.setHasFixedSize(false);

        // Observar categorias — oculta seção quando vazia
        viewModel.categorias.observe(getViewLifecycleOwner(), lista -> {
            categoriaAdapter.submitList(lista);
            if (sectionCategorias != null) {
                sectionCategorias.setVisibility(
                        lista == null || lista.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        // Observar receitas filtradas
        viewModel.receitas.observe(getViewLifecycleOwner(), lista -> {
            receitaAdapter.submitList(lista);
            view.findViewById(R.id.layout_vazio)
                    .setVisibility(lista == null || lista.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Clique na receita → detalhes
        receitaAdapter.setOnItemClickListener(receita -> {
            Bundle args = new Bundle();
            args.putLong("receitaId", receita.id);
            Navigation.findNavController(view)
                    .navigate(R.id.action_acervo_to_detalhes_receita, args);
        });

        // Filtro por categoria (toggle)
        categoriaAdapter.setOnItemClickListener(cat -> {
            Long selecionada = viewModel.toggleCategoria(cat.id);
            categoriaAdapter.setCategoriaFiltro(selecionada);
        });

        // Long-press na categoria → excluir
        categoriaAdapter.setOnItemLongClickListener(cat -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Excluir categoria")
                    .setMessage("Excluir \"" + cat.nome + "\"? As receitas associadas não serão apagadas.")
                    .setPositiveButton("Excluir", (d, w) -> {
                        Categoria c = new Categoria();
                        c.id = cat.id;
                        c.nome = cat.nome;
                        c.cor = cat.cor;
                        new CategoriaRepository(requireActivity().getApplication()).deletar(c);
                        // Limpa filtro se a categoria excluída estava selecionada
                        if (cat.id == (viewModel.getCategoriaFiltroAtual() != null
                                ? viewModel.getCategoriaFiltroAtual() : -1L)) {
                            viewModel.toggleCategoria(cat.id);
                            categoriaAdapter.setCategoriaFiltro(null);
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Busca em tempo real
        TextInputEditText etBusca = view.findViewById(R.id.et_busca);
        if (etBusca != null) {
            etBusca.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    viewModel.setBusca(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // FAB nova receita
        view.findViewById(R.id.fab_nova_receita).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_acervo_to_cadastro_receita));

        // Nova categoria
        view.findViewById(R.id.card_nova_categoria).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_acervo_to_cadastro_categoria));
    }
}
