package br.com.alura.agenda.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.alura.agenda.modelo.Aluno;

/**
 * rruffer
 */
public class AlunoDAO extends SQLiteOpenHelper {
    public AlunoDAO(Context context) {
        super(context, "Agenda", null, 5);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE Alunos (id CHAR(36) PRIMARY KEY, " +
                "nome TEXT NOT NULL, " +
                "endereco TEXT, " +
                "telefone TEXT, " +
                "site TEXT, " +
                "nota REAL, " +
                "caminhoFoto TEXT);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "";
        switch (oldVersion) {
            case 1:
                sql = "ALTER TABLE Alunos ADD COLUMN caminhoFoto TEXT";
                db.execSQL(sql); // indo para versao 2
            case 2:
                String criandoTabelaNova = "CREATE TABLE Alunos_novo " +
                        "(id CHAR(36) PRIMARY KEY," +
                        "nome TEXT NOT NULL, " +
                        "endereco TEXT, " +
                        "telefone TEXT, " +
                        "site TEXT, " +
                        "nota REAL, " +
                        "caminhoFoto TEXT," +
                        "SINNCRONIZADO INT DEFAULT 0);";

                db.execSQL(criandoTabelaNova);

                String inserindoAlumosNaTabelaNova = "INSERT INTO Alunos_novo " +
                        "(id, nome, endereco, telefone, site, nota, caminhoFoto) " +
                        "SELECT id, nome, endereco, telefone, site, nota, caminhoFoto " +
                        "FROM Alunos";

                db.execSQL(inserindoAlumosNaTabelaNova);

                String removendoTabelaAntiga = "DROP TABLE Alunos";
                db.execSQL(removendoTabelaAntiga);

                String alterandoNomeTabelaNova = "ALTER TABLE Alunos_novo " +
                        "RENAME TO Alunos";
                db.execSQL(alterandoNomeTabelaNova);

/*            case 3:
                String buscaAlunos = "SELECT * FROM Alunos";
                Cursor c = db.rawQuery(buscaAlunos, null);
                List<Aluno> alunos = populaAlunos(c);

                String atualizaIdDoAluno = "UPDATE Alunos SET id=? WHERE id=?";

                for (Aluno aluno : alunos) {
                    ContentValues dados = new ContentValues();
                    dados.put("id", geraUUID());

                    String[] params = {aluno.getId()};
                    db.update("Alunos", dados, "id = ?", params);
                }
                */

            case 4: String adicionaCampoSincronizado = "ALTER TABLE ALUNOS ADD COLUMN SINNCRONIZADO INT DEFAULT 0";
            db.execSQL(adicionaCampoSincronizado);
        }
    }

    private String geraUUID() {
        return UUID.randomUUID().toString();
    }


    public void insere(Aluno aluno) {

        if(aluno.getId() == null){
            aluno.setId(geraUUID());
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = pegaDadosDoAluno(aluno);

        /*long id =*/ db.insert("Alunos", null, dados);
//        aluno.setId(id);
    }

    public void sincroniza(List<Aluno> alunos) {

        for(Aluno aluno: alunos){
            if (existe(aluno)){
                if (aluno.estaDesativado()){
                    deleta(aluno);
                }else{
                    altera(aluno);
                }
            }else if (!aluno.estaDesativado()){
                insere(aluno);
            }
        }
    }

    public List<Aluno> buscaAlunos() {
        String sql = "SELECT * FROM Alunos;";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);

        List<Aluno> alunos = populaAlunos(c);
        c.close();

        return alunos;
    }

    @NonNull
    private ContentValues pegaDadosDoAluno(Aluno aluno) {
        ContentValues dados = new ContentValues();
        dados.put("id", aluno.getId());
        dados.put("nome", aluno.getNome());
        dados.put("endereco", aluno.getEndereco());
        dados.put("telefone", aluno.getTelefone());
        dados.put("site", aluno.getSite());
        dados.put("nota", aluno.getNota());
        dados.put("caminhoFoto", aluno.getCaminhoFoto());
        dados.put("sincronizado", aluno.getSincronizado());
        return dados;
    }

    @NonNull
    private List<Aluno> populaAlunos(Cursor c) {
        List<Aluno> alunos = new ArrayList<Aluno>();
        while (c.moveToNext()) {
            Aluno aluno = new Aluno();
            aluno.setId(c.getString(c.getColumnIndex("id")));
            aluno.setNome(c.getString(c.getColumnIndex("nome")));
            aluno.setEndereco(c.getString(c.getColumnIndex("endereco")));
            aluno.setTelefone(c.getString(c.getColumnIndex("telefone")));
            aluno.setSite(c.getString(c.getColumnIndex("site")));
            aluno.setNota(c.getDouble(c.getColumnIndex("nota")));
            aluno.setCaminhoFoto(c.getString(c.getColumnIndex("caminhoFoto")));
            aluno.setSincronizado(c.getInt(c.getColumnIndex("caminhoFoto")));

            alunos.add(aluno);
        }
        return alunos;
    }

    public void deleta(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();

        String[] params = {aluno.getId().toString()};
        db.delete("Alunos", "id = ?", params);
    }

    public void altera(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues dados = pegaDadosDoAluno(aluno);

        String[] params = {aluno.getId().toString()};
        db.update("Alunos", dados, "id = ?", params);
    }

    public boolean ehAluno(String telefone) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM Alunos WHERE telefone = ?";
        Cursor c = db.rawQuery(sql, new String[]{telefone});
        int resultados = c.getCount();
        c.close();
        return resultados > 0;

    }

    private boolean existe(Aluno aluno){
        SQLiteDatabase db = getReadableDatabase();
        String existe = "SELECT ID FROM ALUNOS WHERE ID=?";
        Cursor cursor = db.rawQuery(existe, new String[]{aluno.getId()});

        return cursor.getCount() > 0;

    }


}
