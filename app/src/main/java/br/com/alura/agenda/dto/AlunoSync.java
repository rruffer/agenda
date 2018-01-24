package br.com.alura.agenda.dto;

import java.util.List;

import br.com.alura.agenda.modelo.Aluno;

/**
 * Created by rruffer on 23/01/2018.
 */

public class AlunoSync {

    private List<Aluno> alunos;
    private String momentoDaUltimaModificacao;

    public String getMomentoDaUltimaModificacao() {
        return momentoDaUltimaModificacao;
    }

    public List<Aluno> getAlunos() {
        return alunos;
    }
}
