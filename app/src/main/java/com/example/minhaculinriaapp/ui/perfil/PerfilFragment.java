package com.example.minhaculinriaapp.ui.perfil;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

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

public class PerfilFragment extends Fragment {

    private static final String PREFS = "perfil_prefs";
    private static final String KEY_NOME = "nome";
    private static final String KEY_LOCALIDADE = "localidade";
    public static final String KEY_VOZ_ATIVADA = "voz_ativada";

    private PerfilViewModel viewModel;
    private SharedPreferences prefs;

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

        TextView tvNome = view.findViewById(R.id.tv_nome_chef);
        TextView tvAvatar = view.findViewById(R.id.tv_avatar_inicial);
        TextView tvLocalidade = view.findViewById(R.id.tv_localidade);
        TextView tvReceitas = view.findViewById(R.id.tv_total_receitas);
        TextView tvExecucoes = view.findViewById(R.id.tv_total_execucoes);
        TextView tvHoras = view.findViewById(R.id.tv_total_horas);
        SwitchCompat switchVoz = view.findViewById(R.id.switch_voz);

        // Carregar dados salvos
        String nome = prefs.getString(KEY_NOME, "Chef");
        String localidade = prefs.getString(KEY_LOCALIDADE, "");
        atualizarNomeUI(tvNome, tvAvatar, nome);
        tvLocalidade.setText(localidade.isEmpty() ? "Toque para definir" : localidade);

        // Switch de voz — persiste preferência
        switchVoz.setChecked(prefs.getBoolean(KEY_VOZ_ATIVADA, true));
        switchVoz.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean(KEY_VOZ_ATIVADA, checked).apply());

        // Editar nome ao clicar
        tvNome.setOnClickListener(v -> mostrarDialogoEdicao(
                "Seu nome", nome, KEY_NOME, novo -> {
                    atualizarNomeUI(tvNome, tvAvatar, novo);
                }));
        tvAvatar.setOnClickListener(v -> tvNome.performClick());

        // Editar localidade ao clicar
        tvLocalidade.setOnClickListener(v -> mostrarDialogoEdicao(
                "Cidade / País", prefs.getString(KEY_LOCALIDADE, ""), KEY_LOCALIDADE, novo -> {
                    tvLocalidade.setText(novo.isEmpty() ? "Toque para definir" : novo);
                }));

        // Stats do Room
        viewModel.totalReceitas.observe(getViewLifecycleOwner(), n ->
                tvReceitas.setText(n != null ? String.valueOf(n) : "0"));
        viewModel.totalExecucoes.observe(getViewLifecycleOwner(), n ->
                tvExecucoes.setText(n != null ? String.valueOf(n) : "0"));
        viewModel.totalHorasCozinhando.observe(getViewLifecycleOwner(), h ->
                tvHoras.setText((h != null ? String.valueOf(h) : "0") + "h"));

        // Rows de configuração (abrem configurações do sistema)
        view.findViewById(R.id.row_timer).setOnClickListener(v ->
                abrirConfiguracoesNotificacao());
        view.findViewById(R.id.row_notificacoes).setOnClickListener(v ->
                abrirConfiguracoesNotificacao());
        view.findViewById(R.id.row_privacidade).setOnClickListener(v ->
                mostrarInfo("Privacidade",
                        "Todos os seus dados ficam armazenados localmente neste dispositivo.\nNenhuma informação é enviada a servidores externos."));
    }

    private void atualizarNomeUI(TextView tvNome, TextView tvAvatar, String nome) {
        tvNome.setText(nome);
        tvAvatar.setText(nome.isEmpty() ? "?" : String.valueOf(nome.charAt(0)).toUpperCase());
    }

    private void mostrarDialogoEdicao(String titulo, String valorAtual, String chave, Callback callback) {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(valorAtual);
        input.setSelection(valorAtual.length());

        new AlertDialog.Builder(requireContext())
                .setTitle(titulo)
                .setView(input)
                .setPositiveButton("Salvar", (d, w) -> {
                    String novo = input.getText().toString().trim();
                    prefs.edit().putString(chave, novo).apply();
                    callback.onSaved(novo);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirConfiguracoesNotificacao() {
        android.content.Intent intent = new android.content.Intent();
        intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,
                requireContext().getPackageName());
        startActivity(intent);
    }

    private void mostrarInfo(String titulo, String mensagem) {
        new AlertDialog.Builder(requireContext())
                .setTitle(titulo)
                .setMessage(mensagem)
                .setPositiveButton("OK", null)
                .show();
    }

    interface Callback {
        void onSaved(String valor);
    }
}
