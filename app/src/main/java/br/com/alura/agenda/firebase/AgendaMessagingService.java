package br.com.alura.agenda.firebase;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Map;

import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.dto.AlunoSync;
import br.com.alura.agenda.event.AtualizaListaAlunosEvent;

/**
 * Created by rruffer on 03/02/2018.
 */

public class AgendaMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> mensagem = remoteMessage.getData();
        Log.d("Mensagem  recebida", String.valueOf(mensagem));
        
        converteParaAluno(mensagem);

    }

    private void converteParaAluno(Map<String, String> mensagem) {
        String chavedeAcesso = "alunoSync";
        if (mensagem.containsKey(chavedeAcesso)){
            String json = mensagem.get(chavedeAcesso);
            ObjectMapper mapper = new ObjectMapper();
            try {
                AlunoSync alunoSync = mapper.readValue(json, AlunoSync.class);
                AlunoDAO dao = new AlunoDAO(this);
                dao.sincroniza(alunoSync.getAlunos());
                dao.close();
                EventBus eventBus = EventBus.getDefault();
                eventBus.post(new AtualizaListaAlunosEvent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
