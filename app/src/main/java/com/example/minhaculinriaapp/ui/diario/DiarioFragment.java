package com.example.minhaculinriaapp.ui.diario;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.minhaculinriaapp.R;
import com.example.minhaculinriaapp.data.entity.Execucao;
import com.example.minhaculinriaapp.data.entity.ReceitaResumida;
import com.example.minhaculinriaapp.data.entity.VariavelTecnica;
import com.example.minhaculinriaapp.ui.camera.CameraFragment;
import com.example.minhaculinriaapp.ui.camera.FotoPickerBottomSheet;
import com.example.minhaculinriaapp.viewmodel.DiarioViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiarioFragment extends Fragment implements FotoPickerBottomSheet.Listener {

    private DiarioViewModel viewModel;

    private TextView tvData, tvTentativa, tvVaultScore;
    private AutoCompleteTextView actReceita;
    private LinearLayout containerVariaveis, containerEvolucao;
    private TextView tvSemHistorico;
    private EditText etFuncionou, etMelhoria;
    private ImageView ivFoto;
    private View layoutSemFoto;
    private View btnTrocarFoto;

    private final TextView[] stars = new TextView[5];
    private int notaAtual = 0;

    private long receitaIdSelecionada = -1;
    private String fotoPathAtual = null;
    private List<ReceitaResumida> listaReceitas = new ArrayList<>();

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    fotoPathAtual = uri.toString();
                    mostrarFoto(fotoPathAtual);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DiarioViewModel.class);

        tvData = view.findViewById(R.id.tv_data_execucao);
        tvTentativa = view.findViewById(R.id.tv_tentativa);
        tvVaultScore = view.findViewById(R.id.tv_vault_score);
        actReceita = view.findViewById(R.id.act_receita);
        containerVariaveis = view.findViewById(R.id.container_variaveis);
        containerEvolucao = view.findViewById(R.id.container_evolucao);
        tvSemHistorico = view.findViewById(R.id.tv_sem_historico);
        etFuncionou = view.findViewById(R.id.et_funcionou);
        etMelhoria = view.findViewById(R.id.et_melhoria);
        ivFoto = view.findViewById(R.id.iv_foto_execucao);
        layoutSemFoto = view.findViewById(R.id.layout_sem_foto);
        btnTrocarFoto = view.findViewById(R.id.btn_trocar_foto);

        stars[0] = view.findViewById(R.id.star1);
        stars[1] = view.findViewById(R.id.star2);
        stars[2] = view.findViewById(R.id.star3);
        stars[3] = view.findViewById(R.id.star4);
        stars[4] = view.findViewById(R.id.star5);

        tvData.setText(new SimpleDateFormat("dd MMM yyyy", new Locale("pt", "BR"))
                .format(new Date()).toUpperCase());

        setupStarRating();
        setupPhotoCard(view);
        setupAddVariavel(view);

        viewModel.receitas.observe(getViewLifecycleOwner(), receitas -> {
            listaReceitas = receitas != null ? receitas : new ArrayList<>();
            List<String> nomes = new ArrayList<>();
            for (ReceitaResumida r : listaReceitas) nomes.add(r.nome);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, nomes);
            actReceita.setAdapter(adapter);
        });

        actReceita.setOnItemClickListener((parent, v, position, id) -> {
            if (position < listaReceitas.size()) {
                ReceitaResumida r = listaReceitas.get(position);
                receitaIdSelecionada = r.id;
                viewModel.selecionarReceita(r.id);
                View btnSalvar = requireView().findViewById(R.id.btn_salvar_registro);
                btnSalvar.setEnabled(true);
                btnSalvar.setAlpha(1f);
            }
        });

        viewModel.execucoes.observe(getViewLifecycleOwner(), this::renderEvolucao);

        View btnSalvar = view.findViewById(R.id.btn_salvar_registro);
        btnSalvar.setEnabled(false);
        btnSalvar.setAlpha(0.5f);
        btnSalvar.setOnClickListener(v -> salvar());

        getParentFragmentManager().setFragmentResultListener(
                CameraFragment.RESULT_KEY, getViewLifecycleOwner(), (key, bundle) -> {
                    String path = bundle.getString(CameraFragment.BUNDLE_PATH);
                    if (path != null) {
                        fotoPathAtual = path;
                        mostrarFoto(path);
                    }
                });
    }

    private void setupStarRating() {
        for (int i = 0; i < 5; i++) {
            final int index = i + 1;
            stars[i].setOnClickListener(v -> setNota(index));
        }
    }

    private void setNota(int valor) {
        notaAtual = valor;
        int primaryColor = ContextCompat.getColor(requireContext(), R.color.primary);
        int outlineColor = ContextCompat.getColor(requireContext(), R.color.outline_variant);
        for (int i = 0; i < 5; i++) {
            stars[i].setTextColor(i < valor ? primaryColor : outlineColor);
        }
        tvVaultScore.setText(valor == 0 ? "—" : String.valueOf(valor * 2));
    }

    private void setupPhotoCard(View view) {
        View cardFoto = view.findViewById(R.id.card_foto);
        cardFoto.setOnClickListener(v -> mostrarPicker());
        btnTrocarFoto.setOnClickListener(v -> mostrarPicker());
    }

    private void mostrarPicker() {
        FotoPickerBottomSheet sheet = FotoPickerBottomSheet.newInstance();
        sheet.setListener(this);
        sheet.show(getParentFragmentManager(), "foto_picker");
    }

    @Override
    public void onEscolherCamera() {
        Navigation.findNavController(requireView()).navigate(R.id.action_diario_to_camera);
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
        btnTrocarFoto.setVisibility(View.VISIBLE);
    }

    private void setupAddVariavel(View view) {
        view.findViewById(R.id.btn_add_variavel).setOnClickListener(v -> {
            View row = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_variavel, containerVariaveis, false);
            containerVariaveis.addView(row);
        });
    }

    private void renderEvolucao(List<Execucao> execucoes) {
        containerEvolucao.removeAllViews();

        if (execucoes == null || execucoes.isEmpty()) {
            tvSemHistorico.setText(receitaIdSelecionada == -1
                    ? "Selecione uma receita para ver o histórico"
                    : "Nenhuma tentativa registrada ainda");
            tvSemHistorico.setVisibility(View.VISIBLE);

            if (receitaIdSelecionada != -1) {
                tvTentativa.setText("Tentativa #01 — Primeira vez");
            }
            return;
        }

        tvSemHistorico.setVisibility(View.GONE);
        tvTentativa.setText("Tentativa #" + String.format(Locale.getDefault(), "%02d", execucoes.size() + 1));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("pt", "BR"));

        for (int i = 0; i < execucoes.size(); i++) {
            Execucao e = execucoes.get(i);
            boolean isMaisRecente = (i == 0);
            int numero = execucoes.size() - i;

            View row = buildTimelineRow(e, numero, isMaisRecente, sdf);
            containerEvolucao.addView(row);
        }
    }

    private View buildTimelineRow(Execucao e, int numero, boolean atual, SimpleDateFormat sdf) {
        LinearLayout outer = new LinearLayout(requireContext());
        outer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams outerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        outerParams.bottomMargin = dpToPx(20);
        outer.setLayoutParams(outerParams);

        LinearLayout leftCol = new LinearLayout(requireContext());
        leftCol.setOrientation(LinearLayout.VERTICAL);
        leftCol.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(dpToPx(20), LinearLayout.LayoutParams.WRAP_CONTENT);
        leftParams.rightMargin = dpToPx(12);
        leftCol.setLayoutParams(leftParams);

        View dot = new View(requireContext());
        dot.setBackground(ContextCompat.getDrawable(requireContext(),
                atual ? R.drawable.bg_circle_primary : R.drawable.bg_circle_outline));
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dpToPx(14), dpToPx(14));
        dotParams.topMargin = dpToPx(4);
        dot.setLayoutParams(dotParams);
        leftCol.addView(dot);

        View line = new View(requireContext());
        line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.outline_variant));
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(dpToPx(2),
                LinearLayout.LayoutParams.MATCH_PARENT);
        lineParams.topMargin = dpToPx(4);
        lineParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        line.setLayoutParams(lineParams);
        leftCol.addView(line);

        outer.addView(leftCol);

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvLabel = new TextView(requireContext());
        tvLabel.setText("TENTATIVA #" + String.format(Locale.getDefault(), "%02d", numero));
        tvLabel.setTextSize(10f);
        setTextStyle(tvLabel, atual);
        tvLabel.setTextColor(ContextCompat.getColor(requireContext(),
                atual ? R.color.primary : R.color.outline));
        if (atual) {
            tvLabel.setBackground(ContextCompat.getDrawable(requireContext(),
                    R.drawable.bg_diario_chip));
            tvLabel.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(4));
        } else {
            tvLabel.setPadding(0, dpToPx(2), 0, dpToPx(4));
        }
        content.addView(tvLabel);

        TextView tvData = new TextView(requireContext());
        tvData.setText(sdf.format(new Date(e.data)).toUpperCase(Locale.getDefault()));
        tvData.setTextSize(11f);
        tvData.setTextColor(ContextCompat.getColor(requireContext(), R.color.outline));
        tvData.setPadding(0, 0, 0, dpToPx(4));
        content.addView(tvData);

        if (!TextUtils.isEmpty(e.nota)) {
            try {
                float nota = Float.parseFloat(e.nota);
                int starCount = Math.round(nota);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 5; i++) sb.append(i < starCount ? "★" : "☆");
                sb.append("  ").append(String.format(Locale.getDefault(), "%.0f", nota * 2));

                TextView tvNota = new TextView(requireContext());
                tvNota.setText(sb.toString());
                tvNota.setTextSize(13f);
                tvNota.setTextColor(ContextCompat.getColor(requireContext(),
                        atual ? R.color.primary : R.color.outline));
                content.addView(tvNota);
            } catch (NumberFormatException ignored) {}
        }

        outer.addView(content);
        return outer;
    }

    private void setTextStyle(TextView tv, boolean bold) {
        tv.setTypeface(null, bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void salvar() {
        if (receitaIdSelecionada == -1) {
            Toast.makeText(requireContext(), "Selecione uma receita", Toast.LENGTH_SHORT).show();
            return;
        }

        String funcionou = etFuncionou.getText().toString().trim();
        String melhoria = etMelhoria.getText().toString().trim();

        String observacoes;
        if (!funcionou.isEmpty() && !melhoria.isEmpty()) {
            observacoes = "O QUE FUNCIONOU:\n" + funcionou + "\n\nPONTOS DE MELHORIA:\n" + melhoria;
        } else if (!funcionou.isEmpty()) {
            observacoes = funcionou;
        } else {
            observacoes = melhoria;
        }

        String nota = notaAtual > 0 ? String.valueOf(notaAtual) : null;

        // Coletar variáveis técnicas preenchidas
        List<VariavelTecnica> variaveis = new ArrayList<>();
        for (int i = 0; i < containerVariaveis.getChildCount(); i++) {
            View row = containerVariaveis.getChildAt(i);
            android.widget.EditText etLabel = row.findViewById(R.id.et_label_variavel);
            android.widget.EditText etValor = row.findViewById(R.id.et_valor_variavel);
            String chave = etLabel != null && etLabel.getText() != null
                    ? etLabel.getText().toString().trim() : "";
            String valor = etValor != null && etValor.getText() != null
                    ? etValor.getText().toString().trim() : "";
            if (!chave.isEmpty() || !valor.isEmpty()) {
                VariavelTecnica v = new VariavelTecnica();
                v.chave = chave;
                v.valor = valor;
                variaveis.add(v);
            }
        }

        viewModel.salvar(receitaIdSelecionada, nota, observacoes, fotoPathAtual, variaveis);

        etFuncionou.setText("");
        etMelhoria.setText("");
        containerVariaveis.removeAllViews();
        fotoPathAtual = null;
        ivFoto.setVisibility(View.GONE);
        layoutSemFoto.setVisibility(View.VISIBLE);
        btnTrocarFoto.setVisibility(View.GONE);
        setNota(0);

        com.google.android.material.snackbar.Snackbar.make(
                requireView(), "Registro salvo!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
