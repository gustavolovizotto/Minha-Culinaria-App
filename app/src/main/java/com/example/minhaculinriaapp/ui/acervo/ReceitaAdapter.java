package com.example.minhaculinriaapp.ui.acervo;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minhaculinriaapp.R;
import com.example.minhaculinriaapp.data.entity.ReceitaResumida;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReceitaAdapter extends RecyclerView.Adapter<ReceitaAdapter.ViewHolder> {

    private List<ReceitaResumida> lista = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(ReceitaResumida receita);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    public void submitList(List<ReceitaResumida> novaLista) {
        lista = novaLista != null ? novaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receita, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReceitaResumida item = lista.get(position);

        holder.tvNome.setText(item.nome);
        holder.tvDescricao.setText(item.descricao != null ? item.descricao : "");
        if (item.categoriaNome != null && !item.categoriaNome.isEmpty()) {
            holder.tvCategoria.setText(item.categoriaNome);
            holder.tvCategoria.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategoria.setVisibility(View.GONE);
        }
        holder.tvDificuldade.setText(item.dificuldade != null ? item.dificuldade : "");

        if (item.tempoMinutos != null) {
            holder.tvTempo.setText("⏱ " + item.tempoMinutos + " min");
        } else {
            holder.tvTempo.setText("");
        }

        if (item.rendimento != null) {
            holder.tvPorcoes.setText("🍽 " + item.rendimento + " porções");
        } else {
            holder.tvPorcoes.setText("");
        }

        if (item.fotoPath != null && !item.fotoPath.isEmpty()) {
            Uri uri = item.fotoPath.startsWith("content://")
                    ? Uri.parse(item.fotoPath)
                    : Uri.fromFile(new File(item.fotoPath));
            holder.ivFoto.setImageURI(uri);
            holder.ivFoto.setVisibility(View.VISIBLE);
        } else {
            holder.ivFoto.setVisibility(View.GONE);
        }

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onClick(item));
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView tvCategoria, tvDificuldade, tvNome, tvDescricao, tvTempo, tvPorcoes;

        ViewHolder(View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.iv_foto);
            tvCategoria = itemView.findViewById(R.id.tv_categoria);
            tvDificuldade = itemView.findViewById(R.id.tv_dificuldade);
            tvNome = itemView.findViewById(R.id.tv_nome);
            tvDescricao = itemView.findViewById(R.id.tv_descricao);
            tvTempo = itemView.findViewById(R.id.tv_tempo);
            tvPorcoes = itemView.findViewById(R.id.tv_porcoes);
        }
    }
}
