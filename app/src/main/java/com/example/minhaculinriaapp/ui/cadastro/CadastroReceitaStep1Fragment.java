package com.example.minhaculinriaapp.ui.cadastro;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.minhaculinriaapp.R;
import com.example.minhaculinriaapp.data.entity.Categoria;
import com.example.minhaculinriaapp.ui.camera.CameraFragment;
import com.example.minhaculinriaapp.ui.camera.FotoPickerBottomSheet;
import com.example.minhaculinriaapp.viewmodel.CadastroReceitaViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.List;

public class CadastroReceitaStep1Fragment extends Fragment
        implements FotoPickerBottomSheet.Listener {

    private CadastroReceitaViewModel viewModel;
    private TextInputLayout tilNome;
    private TextInputEditText etNome, etDescricao;
    private LinearLayout chipsContainer;
    private Long categoriaIdSelecionada = null;
    private ImageView ivFoto;
    private View layoutSemFoto;
    private View chipNovaRef;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    viewModel.fotoPath = uri.toString();
                    mostrarFoto(uri.toString());
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cadastro_receita_1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CadastroReceitaViewModel.class);

        tilNome = view.findViewById(R.id.til_nome);
        etNome = view.findViewById(R.id.et_nome);
        etDescricao = view.findViewById(R.id.et_descricao);
        chipsContainer = view.findViewById(R.id.chips_categorias);
        chipNovaRef = view.findViewById(R.id.chip_nova_categoria);
        ivFoto = view.findViewById(R.id.iv_foto_receita);
        layoutSemFoto = view.findViewById(R.id.layout_sem_foto);

        if (viewModel.nome != null) etNome.setText(viewModel.nome);
        if (viewModel.descricao != null) etDescricao.setText(viewModel.descricao);
        categoriaIdSelecionada = viewModel.categoriaId;

        if (viewModel.fotoPath != null) mostrarFoto(viewModel.fotoPath);

        // Atualiza título da toolbar quando em modo edição
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            View parent = (View) btnBack.getParent();
            if (parent instanceof android.view.ViewGroup) {
                android.view.ViewGroup toolbar = (android.view.ViewGroup) parent;
                for (int i = 0; i < toolbar.getChildCount(); i++) {
                    android.view.View child = toolbar.getChildAt(i);
                    if (child instanceof android.widget.TextView) {
                        android.widget.TextView tv = (android.widget.TextView) child;
                        if (tv.getText() != null && tv.getText().toString().contains("Receita")) {
                            tv.setText(viewModel.modoEdicao() ? "Editar Receita" : "Nova Receita");
                        }
                    }
                }
            }
        }

        viewModel.categorias.observe(getViewLifecycleOwner(), this::popularChipsCategorias);

        view.findViewById(R.id.area_foto).setOnClickListener(v -> mostrarPicker());

        view.findViewById(R.id.chip_nova_categoria).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_step1_to_cadastro_categoria));

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        view.findViewById(R.id.btn_proximo).setOnClickListener(v -> {
            String nome = etNome.getText() != null ? etNome.getText().toString().trim() : "";
            if (TextUtils.isEmpty(nome)) {
                tilNome.setError("Nome é obrigatório");
                return;
            }
            tilNome.setError(null);
            viewModel.nome = nome;
            viewModel.descricao = etDescricao.getText() != null
                    ? etDescricao.getText().toString().trim() : null;
            viewModel.categoriaId = categoriaIdSelecionada;
            Navigation.findNavController(v).navigate(R.id.action_step1_to_step2);
        });

        getParentFragmentManager().setFragmentResultListener(
                CameraFragment.RESULT_KEY, getViewLifecycleOwner(), (key, bundle) -> {
                    String path = bundle.getString(CameraFragment.BUNDLE_PATH);
                    if (path != null) {
                        viewModel.fotoPath = path;
                        mostrarFoto(path);
                    }
                });
    }

    private void mostrarPicker() {
        FotoPickerBottomSheet sheet = FotoPickerBottomSheet.newInstance();
        sheet.setListener(this);
        sheet.show(getParentFragmentManager(), "foto_picker");
    }

    @Override
    public void onEscolherCamera() {
        Navigation.findNavController(requireView()).navigate(R.id.action_step1_to_camera);
    }

    @Override
    public void onEscolherGaleria() {
        pickImageLauncher.launch("image/*");
    }

    private void mostrarFoto(String path) {
        Uri uri = path.startsWith("content://")
                ? Uri.parse(path)
                : Uri.fromFile(new File(path));
        ivFoto.setImageURI(uri);
        ivFoto.setVisibility(View.VISIBLE);
        layoutSemFoto.setVisibility(View.GONE);
    }

    private void popularChipsCategorias(List<Categoria> categorias) {
        // Remover o chip "Nova" temporariamente antes de limpar o container
        if (chipNovaRef.getParent() != null) {
            ((android.view.ViewGroup) chipNovaRef.getParent()).removeView(chipNovaRef);
        }
        chipsContainer.removeAllViews();

        if (categorias != null) {
            for (Categoria cat : categorias) {
                TextView chip = (TextView) LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_tag_chip, chipsContainer, false);
                chip.setText(cat.nome);
                boolean selecionada = cat.id == (categoriaIdSelecionada != null ? categoriaIdSelecionada : -1);
                applyChipStyle(chip, selecionada);
                chip.setOnClickListener(v -> selecionarCategoria(chip, cat.id, categorias.size()));
                chipsContainer.addView(chip);
            }
        }

        // Readicionar o chip "Nova" sempre no final
        chipsContainer.addView(chipNovaRef);
    }

    private void selecionarCategoria(TextView chipClicado, long catId, int total) {
        boolean jaEstavaSelecionada = categoriaIdSelecionada != null && categoriaIdSelecionada == catId;
        if (jaEstavaSelecionada) {
            categoriaIdSelecionada = null;
            applyChipStyle(chipClicado, false);
            chipClicado.setTag(false);
        } else {
            categoriaIdSelecionada = catId;
            for (int i = 0; i < chipsContainer.getChildCount() - 1; i++) {
                View child = chipsContainer.getChildAt(i);
                if (child instanceof TextView) {
                    applyChipStyle((TextView) child, false);
                    child.setTag(false);
                }
            }
            applyChipStyle(chipClicado, true);
            chipClicado.setTag(true);
        }
    }

    private void applyChipStyle(TextView chip, boolean selecionada) {
        if (selecionada) {
            chip.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.chip_category_selected));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_primary));
        } else {
            chip.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.chip_category));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface));
        }
    }
}
