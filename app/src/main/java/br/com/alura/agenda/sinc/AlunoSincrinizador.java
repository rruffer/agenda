package br.com.alura.agenda.sinc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.dto.AlunoSync;
import br.com.alura.agenda.event.AtualizaListaAlunosEvent;
import br.com.alura.agenda.preferences.AlunoPreferences;
import br.com.alura.agenda.retrofit.RetrofitInicializador;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlunoSincrinizador {

    private final Context context;
    private EventBus eventBus = EventBus.getDefault();
    private AlunoPreferences preferences;

    public AlunoSincrinizador(Context context) {
        this.context = context;
        this.preferences = new AlunoPreferences(context);
    }

    public void buscaTodos(){
        if (preferences.temVersao()){
            buscaNovos();
        }else{
            buscaAlunosDoServidor();
        }
    }

    private void buscaNovos() {
        Call<AlunoSync> call = new RetrofitInicializador().getAlunoService().novos(preferences.getVersao());
        call.enqueue(buscaAlunosCallback());
    }

    private void buscaAlunosDoServidor() {
        Call<AlunoSync> call = new RetrofitInicializador().getAlunoService().lista();
        call.enqueue(buscaAlunosCallback());
    }

    @NonNull
    private Callback<AlunoSync> buscaAlunosCallback() {
        return new Callback<AlunoSync>() {
            @Override
            public void onResponse(Call<AlunoSync> call, Response<AlunoSync> response) {
                AlunoSync lista = response.body();
                String versao = lista.getMomentoDaUltimaModificacao();

                preferences.salvaVersao(versao);


                AlunoDAO dao = new AlunoDAO(context);
                dao.sincroniza(lista.getAlunos());
                dao.close();

                Log.i("versao", preferences.getVersao());

                eventBus.post(new AtualizaListaAlunosEvent());
//                context.carregaLista();
//                context.getSwipe().setRefreshing(false);
            }

            @Override
            public void onFailure(Call<AlunoSync> call, Throwable t) {
                Log.i("onFailure", "Erro ao buscar alunos do servidor: " + t);
                eventBus.post(new AtualizaListaAlunosEvent());
//                context.getSwipe().setRefreshing(false);
            }
        };
    }
}