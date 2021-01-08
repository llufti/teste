/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entidades.Hinos;
import entidades.Setores;
import entidades.Usuarios;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.primefaces.component.fileupload.FileUpload;
import upload.FileUploadView;
import util.ErroSistema;
import util.FabricaConexao;

/**
 *
 * @author Luciano
 */
public class HinosDao {

    @Inject
    FileUploadView fuv;

    public void salvarHino(Hinos hinos, FileUploadView fileUpload, Setores setores) throws ErroSistema {
        int totalDeHinos = buscarTotalDeHinos(setores);
        try {
            Connection conexao = FabricaConexao.getConexao();
            PreparedStatement ps;

            if (hinos.getIdHino() == null) {
                ps = conexao.prepareCall("INSERT INTO `hinos` (`nome`,`idSetor`,`nomeLouvor`,`nomeCantor`,`numeroHino`) VALUES (?,?,?,?,?)");
            } else {
                ps = conexao.prepareStatement("update hinos set nome=?,idSetor=?,nomeLouvor=?,nomeCantor=? ,numeroHino=? where idHinos=?");
                ps.setInt(6, hinos.getIdHino());
            }
            ps.setString(1, FileUploadView.getStaticArquivoReal());
            ps.setInt(2, setores.getIdSetor());
            ps.setString(3, hinos.getNomeLouvor());
            ps.setString(4, hinos.getNomeCantor());
            ps.setInt(5, totalDeHinos);
            ps.execute();

            FabricaConexao.fecharConexao();
        } catch (SQLException ex) {
            throw new ErroSistema("Erro ao inserir", ex);
        }
    }

    public List<Hinos> buscarLouvoresPeloIdSetor(Hinos hinos, Setores setores, Usuarios usuarios) throws ErroSistema {
        try {
            Connection conexao = FabricaConexao.getConexao();
            PreparedStatement ps = conexao.prepareStatement("SELECT hinos.idHinos, hinos.nomeLouvor, hinos.nomeCantor, hinos.nome, hinos.numeroHino  FROM hinos WHERE hinos.idSetor LIKE ?");
            ps.setInt(1, usuarios.getIdSetor());
            ResultSet resultSet = ps.executeQuery();

            List<Hinos> entidades = new ArrayList<>();

            while (resultSet.next()) {
                hinos = new Hinos();
                hinos.setIdHino(resultSet.getInt("idHinos"));
                hinos.setNomeLouvor(resultSet.getString("nomeLouvor"));
                hinos.setNomeCantor(resultSet.getString("nomeCantor"));
                hinos.setNome(resultSet.getString("nome"));
                hinos.setNumeroHino(resultSet.getInt("numeroHino"));
                entidades.add(hinos);
            }
            FabricaConexao.fecharConexao();
            return entidades;
        } catch (SQLException ex) {
            throw new ErroSistema("Erro ao inserir na lista", ex);
        }
    }

    public List<Hinos> buscarLouvoresPeloIdSetorSonoplasta(Hinos hinos, Setores setores, Usuarios usuarios) throws ErroSistema {
        try {
            Connection conexao = FabricaConexao.getConexao();
            PreparedStatement ps = conexao.prepareStatement("SELECT hinos.idHinos, hinos.nomeLouvor, hinos.nomeCantor, hinos.nome, hinos.numeroHino, setor.login FROM hinos JOIN setor ON setor.idSetor = hinos.idSetor WHERE hinos.idSetor LIKE ?");
            ps.setInt(1, usuarios.getIdSetor());
            ResultSet resultSet = ps.executeQuery();

            List<Hinos> entidades = new ArrayList<>();

            while (resultSet.next()) {
                hinos = new Hinos();
                hinos.setIdHino(resultSet.getInt("idHinos"));
                hinos.setNomeLouvor(resultSet.getString("nomeLouvor"));
                hinos.setNomeCantor(resultSet.getString("nomeCantor"));
                hinos.setNome(resultSet.getString("nome"));
                hinos.setNumeroHino(resultSet.getInt("numeroHino"));
                hinos.setLoginSonoplasta(resultSet.getString("login"));
                entidades.add(hinos);
            }
            FabricaConexao.fecharConexao();
            return entidades;
        } catch (SQLException ex) {
            throw new ErroSistema("Erro ao inserir na lista", ex);
        }
    }

    public List<Hinos> buscarLouvoresPeloIdDoHino(Hinos hinos, Setores setores) throws ErroSistema {
        try {
            Connection conexao = FabricaConexao.getConexao();
            PreparedStatement ps = conexao.prepareStatement("SELECT hinos.idHinos, hinos.nomeLouvor, hinos.nomeCantor, hinos.nome, hinos.numeroHino, setor.login FROM hinos JOIN setor ON setor.idSetor = hinos.idSetor WHERE hinos.idHinos LIKE ?");
            ps.setInt(1, hinos.getIdHino());
            ResultSet resultSet = ps.executeQuery();

            List<Hinos> entidades = new ArrayList<>();

            while (resultSet.next()) {
                hinos = new Hinos();
                hinos.setIdHino(resultSet.getInt("idHinos"));
                hinos.setNomeLouvor(resultSet.getString("nomeLouvor"));
                hinos.setNomeCantor(resultSet.getString("nomeCantor"));
                hinos.setNome(resultSet.getString("nome"));
                hinos.setNumeroHino(resultSet.getInt("numeroHino"));
                hinos.setLoginSonoplasta(resultSet.getString("login"));
                entidades.add(hinos);
            }
            FabricaConexao.fecharConexao();
            return entidades;
        } catch (SQLException ex) {
            throw new ErroSistema("Erro ao inserir na lista", ex);
        }
    }

    public int buscarTotalDeHinos(Setores setores) throws ErroSistema {
        try {
            int totalHino = 0;
            Connection conexao = FabricaConexao.getConexao();
            PreparedStatement ps = conexao.prepareStatement("SELECT COUNT(*) as total FROM hinos WHERE idSetor LIKE ? ");
            ps.setInt(1, setores.getIdSetor());
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                totalHino = Integer.parseInt(resultSet.getString("total"));
                if (totalHino == 0) {
                    totalHino = 1;
                } else {
                    totalHino = totalHino + 1;
                }

            }
            FabricaConexao.fecharConexao();
            return totalHino;
        } catch (SQLException ex) {
            throw new ErroSistema("Erro ao inserir na lista", ex);
        }
    }

}
