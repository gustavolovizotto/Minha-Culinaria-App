package com.example.minhaculinriaapp.ui.cadastro;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.minhaculinriaapp.R;
import com.example.minhaculinriaapp.viewmodel.CadastroCategoriaViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

public class CadastroCategoriaFragment extends Fragment {

    private static final String[] CORES = {
        "#8E3A1F",
        "#47664B",
        "#614400",
        "#55423D",
    };

    private CadastroCategoriaViewModel viewModel;
    private String corSelecionada = CORES[0];
    private String fotoPath = null;
    private ImageView ivIcone;
    private TextView tvPlaceholder;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    fotoPath = uri.toString();
                    mostrarFoto(fotoPath);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cadastro_categoria, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CadastroCategoriaViewModel.class);

        TextInputLayout tilNome = view.findViewById(R.id.til_nome_categoria);
        TextInputEditText etNome = view.findViewById(R.id.et_nome_categoria);
        TextInputEditText etDescricao = view.findViewById(R.id.et_descricao_categoria);
        ivIcone = view.findViewById(R.id.iv_icone_categoria);
        tvPlaceholder = view.findViewById(R.id.tv_icone_placeholder);

        int[] corViews = {R.id.color_0, R.id.color_1, R.id.color_2, R.id.color_tertiary};
        for (int i = 0; i < corViews.length; i++) {
            int idx = i;
            View corView = view.findViewById(corViews[i]);
            if (corView != null) {
                corView.setOnClickListener(v -> corSelecionada = CORES[idx]);
            }
        }

        view.findViewById(R.id.area_icone).setOnClickListener(v ->
                pickImageLauncher.launch("image/*"));

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        view.findViewById(R.id.btn_salvar_categoria).setOnClickListener(v -> {
            String nome = etNome.getText() != null ? etNome.getText().toString().trim() : "";
            if (TextUtils.isEmpty(nome)) {
                if (tilNome != null) tilNome.setError("Nome é obrigatório");
                return;
            }
            String descricao = etDescricao.getText() != null
                    ? etDescricao.getText().toString().trim() : null;
            viewModel.salvar(nome, descricao, corSelecionada, fotoPath);
            Navigation.findNavController(v).navigateUp();
        });
    }

    private void mostrarFoto(String path) {
        Uri uri = path.startsWith("content://")
                ? Uri.parse(path)
                : Uri.fromFile(new File(path));
        ivIcone.setImageURI(uri);
        ivIcone.setVisibility(View.VISIBLE);
        tvPlaceholder.setVisibility(View.GONE);
    }
}
