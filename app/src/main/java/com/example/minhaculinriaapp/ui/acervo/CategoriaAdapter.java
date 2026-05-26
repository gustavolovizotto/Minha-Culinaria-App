package com.example.minhaculinriaapp.ui.acervo;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minhaculinriaapp.R;
import com.example.minhaculinriaapp.data.entity.CategoriaComContagem;

import java.util.ArrayList;
import java.util.List;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.ViewHolder> {

    private List<CategoriaComContagem> lista = new ArrayList<>();
    private Long categoriaFiltroId = null;
    private OnItemClickListener listener;
    private OnItemLongClickListener longListener;

    public interface OnItemClickListener {
        void onClick(CategoriaComContagem categoria);
    }

    public interface OnItemLongClickListener {
        void onLongClick(CategoriaComContagem categoria);
    }

    public void setOnItemClickListener(OnItemClickListener l) { this.listener = l; }
    public void setOnItemLongClickListener(OnItemLongClickListener l) { this.longListener = l; }

    public void submitList(List<CategoriaComContagem> novaLista) {
        lista = novaLista != null ? novaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setCategoriaFiltro(Long id) {
        categoriaFiltroId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoriaComContagem item = lista.get(position);
        holder.tvNome.setText(item.nome);
        holder.tvContagem.setText(String.valueOf(item.total));

        int cor;
        if (item.cor != null && !item.cor.isEmpty()) {
            try {
                cor = Color.parseColor(item.cor);
            } catch (Exception e) {
                cor = holder.itemView.getContext().getColor(R.color.primary_container);
            }
        } else {
            cor = holder.itemView.getContext().getColor(R.color.primary_container);
        }
        holder.viewCor.setBackgroundColor(cor);

        boolean selecionada = categoriaFiltroId != null && categoriaFiltroId == item.id;
        holder.itemView.setAlpha(selecionada ? 1f : (categoriaFiltroId != null ? 0.5f : 1f));
        holder.itemView.setScaleX(selecionada ? 1.03f : 1f);
        holder.itemView.setScaleY(selecionada ? 1.03f : 1f);

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onClick(item));
        }
        if (longListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longListener.onLongClick(item);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewCor;
        TextView tvNome, tvContagem;

        ViewHolder(View itemView) {
            super(itemView);
            viewCor = itemView.findViewById(R.id.view_cor);
            tvNome = itemView.findViewById(R.id.tv_nome_categoria);
            tvContagem = itemView.findViewById(R.id.tv_contagem);
        }
    }
}
