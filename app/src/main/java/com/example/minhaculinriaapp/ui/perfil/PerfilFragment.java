package com.example.minhaculinriaapp.ui.perfil;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.minhaculinriaapp.R;
import com.example.minhaculinriaapp.viewmodel.PerfilViewModel;

import java.io.File;

public class PerfilFragment extends Fragment {

    private static final String PREFS = "perfil_prefs";
    private static final String KEY_NOME = "nome";
    private static final String KEY_FOTO_AVATAR = "foto_avatar";
    public static final String KEY_VOZ_ATIVADA = "voz_ativada";

    private PerfilViewModel viewModel;
    private SharedPreferences prefs;
    private ImageView ivAvatarFoto;
    private TextView tvNome;
    private TextView tvAvatar;

    private final ActivityResultLauncher<String> pickAvatarImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    prefs.edit().putString(KEY_FOTO_AVATAR, uri.toString()).apply();
                    mostrarFotoAvatar(uri.toString());
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.top_bar_perfil), (v, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), statusBar.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        prefs = requireContext().getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE);
        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        tvNome = view.findViewById(R.id.tv_nome_chef);
        tvAvatar = view.findViewById(R.id.tv_avatar_inicial);
        ivAvatarFoto = view.findViewById(R.id.iv_avatar_foto);
        TextView tvReceitas = view.findViewById(R.id.tv_total_receitas);
        TextView tvExecucoes = view.findViewById(R.id.tv_total_execucoes);
        TextView tvHoras = view.findViewById(R.id.tv_total_horas);
        SwitchCompat switchVoz = view.findViewById(R.id.switch_voz);

        String nome = prefs.getString(KEY_NOME, "Chef");
        atualizarNomeUI(nome);

        String fotoPath = prefs.getString(KEY_FOTO_AVATAR, null);
        if (fotoPath != null) mostrarFotoAvatar(fotoPath);

        switchVoz.setChecked(prefs.getBoolean(KEY_VOZ_ATIVADA, true));
        switchVoz.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean(KEY_VOZ_ATIVADA, checked).apply());

        view.findViewById(R.id.frame_avatar).setOnClickListener(v -> mostrarOpcoesEdicaoPerfil());

        viewModel.totalReceitas.observe(getViewLifecycleOwner(), n ->
                tvReceitas.setText(n != null ? String.valueOf(n) : "0"));
        viewModel.totalExecucoes.observe(getViewLifecycleOwner(), n ->
                tvExecucoes.setText(n != null ? String.valueOf(n) : "0"));
        viewModel.totalHorasCozinhando.observe(getViewLifecycleOwner(), h ->
                tvHoras.setText((h != null ? String.valueOf(h) : "0") + "h"));

        view.findViewById(R.id.row_timer).setOnClickListener(v -> abrirConfiguracoesNotificacao());
    }

    private void mostrarOpcoesEdicaoPerfil() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Editar Perfil")
                .setItems(new CharSequence[]{"Editar Nome", "Trocar Foto"}, (dialog, which) -> {
                    if (which == 0) {
                        mostrarDialogoEdicaoNome();
                    } else {
                        pickAvatarImageLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void mostrarDialogoEdicaoNome() {
        String nomeAtual = prefs.getString(KEY_NOME, "Chef");
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(nomeAtual);
        input.setSelection(nomeAtual.length());

        new AlertDialog.Builder(requireContext())
                .setTitle("Seu nome")
                .setView(input)
                .setPositiveButton("Salvar", (d, w) -> {
                    String novo = input.getText().toString().trim();
                    prefs.edit().putString(KEY_NOME, novo).apply();
                    atualizarNomeUI(novo);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarFotoAvatar(String path) {
        Uri uri = path.startsWith("content://")
                ? Uri.parse(path)
                : Uri.fromFile(new File(path));
        ivAvatarFoto.setImageURI(uri);
        ivAvatarFoto.setVisibility(View.VISIBLE);
    }

    private void atualizarNomeUI(String nome) {
        tvNome.setText(nome);
        tvAvatar.setText(nome.isEmpty() ? "?" : String.valueOf(nome.charAt(0)).toUpperCase());
    }

    private void abrirConfiguracoesNotificacao() {
        android.content.Intent intent = new android.content.Intent();
        intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,
                requireContext().getPackageName());
        startActivity(intent);
    }
}
