package com.example.minhaculinriaapp.ui.detalhes;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.minhaculinriaapp.R;
import com.example.minhaculinriaapp.data.entity.Ingrediente;
import com.example.minhaculinriaapp.data.entity.Passo;
import com.example.minhaculinriaapp.data.entity.Receita;
import com.example.minhaculinriaapp.data.entity.ReceitaResumida;
import com.example.minhaculinriaapp.data.repository.ReceitaRepository;
import com.example.minhaculinriaapp.viewmodel.CadastroReceitaViewModel;
import com.example.minhaculinriaapp.viewmodel.DetalhesReceitaViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DetalhesReceitaFragment extends Fragment {

    private DetalhesReceitaViewModel viewModel;

    private ImageView ivFoto;
    private TextView tvNome, tvCategoria, tvDificuldade, tvDescricao, tvTempo, tvPorcoes;
    private LinearLayout containerIngredientes, containerPassos, containerTags;
    private View scrollTags;

    private ReceitaResumida receitaAtual;
    private List<Ingrediente> ingredientesAtuais = new ArrayList<>();
    private List<Passo> passosAtuais = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalhes_receita, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DetalhesReceitaViewModel.class);

        ivFoto = view.findViewById(R.id.iv_foto);
        tvNome = view.findViewById(R.id.tv_nome);
        tvCategoria = view.findViewById(R.id.tv_categoria);
        tvDificuldade = view.findViewById(R.id.tv_dificuldade);
        tvDescricao = view.findViewById(R.id.tv_descricao);
        tvTempo = view.findViewById(R.id.tv_tempo);
        tvPorcoes = view.findViewById(R.id.tv_porcoes);
        containerIngredientes = view.findViewById(R.id.container_ingredientes);
        containerPassos = view.findViewById(R.id.container_passos);
        containerTags = view.findViewById(R.id.container_tags);
        scrollTags = view.findViewById(R.id.scroll_tags);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        // Botão de menu (editar / excluir)
        ImageButton btnMenu = view.findViewById(R.id.btn_menu_receita);
        btnMenu.setOnClickListener(v -> mostrarMenuOpcoes(v));

        view.findViewById(R.id.btn_iniciar_execucao).setOnClickListener(v -> {
            long receitaId = getArguments() != null ? getArguments().getLong("receitaId", -1) : -1;
            if (receitaId != -1) {
                Bundle args = new Bundle();
                args.putLong("receitaId", receitaId);
                Navigation.findNavController(v).navigate(R.id.action_detalhes_to_maos_massa, args);
            }
        });

        long receitaId = getArguments() != null ? getArguments().getLong("receitaId", -1) : -1;
        if (receitaId != -1) {
            viewModel.carregarReceita(receitaId);
        }

        viewModel.receita.observe(getViewLifecycleOwner(), r -> {
            receitaAtual = r;
            preencherCabecalho(r);
        });
        viewModel.ingredientes.observe(getViewLifecycleOwner(), lista -> {
            ingredientesAtuais = lista != null ? lista : new ArrayList<>();
            preencherIngredientes(ingredientesAtuais);
        });
        viewModel.passos.observe(getViewLifecycleOwner(), lista -> {
            passosAtuais = lista != null ? lista : new ArrayList<>();
            preencherPassos(passosAtuais);
        });
    }

    private void mostrarMenuOpcoes(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "Editar receita");
        popup.getMenu().add(0, 2, 1, "Excluir receita");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                editarReceita();
                return true;
            } else if (item.getItemId() == 2) {
                confirmarExclusao();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void editarReceita() {
        if (receitaAtual == null) return;

        // Popula o ViewModel de cadastro com os dados atuais para edição
        CadastroReceitaViewModel cadastroVM =
                new ViewModelProvider(requireActivity()).get(CadastroReceitaViewModel.class);
        cadastroVM.limpar();
        cadastroVM.receitaEditandoId = receitaAtual.id;
        cadastroVM.nome = receitaAtual.nome;
        cadastroVM.descricao = receitaAtual.descricao;
        cadastroVM.fotoPath = receitaAtual.fotoPath;
        cadastroVM.tags = receitaAtual.tags;
        cadastroVM.tempoMinutos = receitaAtual.tempoMinutos;
        cadastroVM.rendimento = receitaAtual.rendimento;
        cadastroVM.dificuldade = receitaAtual.dificuldade;
        cadastroVM.ingredientes.addAll(ingredientesAtuais);
        cadastroVM.passos.addAll(passosAtuais);

        Navigation.findNavController(requireView())
                .navigate(R.id.action_detalhes_to_editar_receita);
    }

    private void confirmarExclusao() {
        if (receitaAtual == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Excluir receita")
                .setMessage("Excluir \"" + receitaAtual.nome + "\"? Esta ação não pode ser desfeita.")
                .setPositiveButton("Excluir", (d, w) -> {
                    Receita r = new Receita();
                    r.id = receitaAtual.id;
                    r.nome = receitaAtual.nome;
                    new ReceitaRepository(requireActivity().getApplication()).deletar(r);
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void preencherCabecalho(ReceitaResumida r) {
        if (r == null) return;

        tvNome.setText(r.nome);
        tvDescricao.setText(r.descricao != null ? r.descricao : "");
        tvCategoria.setText(r.categoriaNome != null ? r.categoriaNome : "Geral");
        tvDificuldade.setText(r.dificuldade != null ? r.dificuldade : "");
        tvTempo.setText(r.tempoMinutos != null ? r.tempoMinutos + " min" : "—");
        tvPorcoes.setText(r.rendimento != null ? String.valueOf(r.rendimento) : "—");

        if (r.fotoPath != null && !r.fotoPath.isEmpty()) {
            Uri uri = r.fotoPath.startsWith("content://")
                    ? Uri.parse(r.fotoPath)
                    : Uri.fromFile(new File(r.fotoPath));
            ivFoto.setImageURI(uri);
        }

        // Tags
        containerTags.removeAllViews();
        if (!TextUtils.isEmpty(r.tags)) {
            scrollTags.setVisibility(View.VISIBLE);
            for (String tag : r.tags.split(",")) {
                String t = tag.trim();
                if (!t.isEmpty()) {
                    TextView chip = (TextView) LayoutInflater.from(requireContext())
                            .inflate(R.layout.item_tag_chip, containerTags, false);
                    chip.setText(t);
                    chip.setBackground(ContextCompat.getDrawable(requireContext(),
                            R.drawable.chip_category_selected));
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_primary));
                    chip.setClickable(false);
                    containerTags.addView(chip);
                }
            }
        } else {
            scrollTags.setVisibility(View.GONE);
        }
    }

    private void preencherIngredientes(List<Ingrediente> lista) {
        containerIngredientes.removeAllViews();
        if (lista == null || lista.isEmpty()) {
            adicionarTextoVazio(containerIngredientes, "Nenhum ingrediente cadastrado");
            return;
        }
        for (Ingrediente ing : lista) {
            TextView tv = new TextView(requireContext());
            String texto = "• ";
            if (!TextUtils.isEmpty(ing.quantidade)) texto += ing.quantidade + "  ";
            texto += ing.nome != null ? ing.nome : "";
            tv.setText(texto);
            tv.setTextSize(15f);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface));
            tv.setPadding(0, 0, 0, dpToPx(8));
            containerIngredientes.addView(tv);
        }
    }

    private void preencherPassos(List<Passo> lista) {
        containerPassos.removeAllViews();
        if (lista == null || lista.isEmpty()) {
            adicionarTextoVazio(containerPassos, "Nenhum passo cadastrado");
            return;
        }
        for (Passo passo : lista) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.bottomMargin = dpToPx(16);
            row.setLayoutParams(rowParams);

            TextView badge = new TextView(requireContext());
            badge.setText(String.valueOf(passo.numero));
            badge.setTextSize(13f);
            badge.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_primary));
            badge.setBackground(ContextCompat.getDrawable(requireContext(),
                    R.drawable.chip_category_selected));
            badge.setGravity(android.view.Gravity.CENTER);
            int badgeSize = dpToPx(28);
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(badgeSize, badgeSize);
            badgeParams.rightMargin = dpToPx(12);
            badgeParams.topMargin = dpToPx(2);
            badge.setLayoutParams(badgeParams);
            row.addView(badge);

            TextView tvDesc = new TextView(requireContext());
            tvDesc.setText(passo.descricao != null ? passo.descricao : "");
            tvDesc.setTextSize(15f);
            tvDesc.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface));
            LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvDesc.setLayoutParams(descParams);
            row.addView(tvDesc);

            containerPassos.addView(row);
        }
    }

    private void adicionarTextoVazio(LinearLayout container, String mensagem) {
        TextView tv = new TextView(requireContext());
        tv.setText(mensagem);
        tv.setTextSize(14f);
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.outline));
        container.addView(tv);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
